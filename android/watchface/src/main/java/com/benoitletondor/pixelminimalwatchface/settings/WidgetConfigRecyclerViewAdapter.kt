/*
 *   Copyright 2022 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.settings

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import java.util.concurrent.Executors

class WidgetConfigRecyclerViewAdapter(
    private val complicationLocation: ComplicationLocation,
    private val context: Context,
    private val title: String,
    private val onSelectComplicationClicked: () -> Unit,
    private val onSelectColorClicked: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())
    private var widgetViewHolder: WidgetViewHolder? = null
    private var colorViewHolder: ColorViewHolder? = null
    private var showColor = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TITLE_VIEW_TYPE -> TitleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_config_title, parent, false))
            WIDGET_VIEW_TYPE -> {
                val widgetViewHolder = WidgetViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.widget_config_complication, parent, false),
                    onSelectComplicationClicked
                )
                this.widgetViewHolder = widgetViewHolder
                return widgetViewHolder
            }
            COLOR_VIEW_TYPE -> {
                val colorViewHolder = ColorViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.widget_config_color, parent, false),
                    onSelectColorClicked
                )
                this.colorViewHolder = colorViewHolder
                return colorViewHolder
            }
            else -> throw IllegalArgumentException("onCreateViewHolder called with unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when(viewHolder) {
            is TitleViewHolder -> viewHolder.setTitle(title)
            is WidgetViewHolder -> {
                initializesColorsAndComplications()
            }
            is ColorViewHolder -> {
                val storage = Injection.storage(context)
                viewHolder.updateComplicationColor(when(complicationLocation) {
                    ComplicationLocation.LEFT -> storage.getComplicationColors().leftColor
                    ComplicationLocation.MIDDLE -> storage.getComplicationColors().middleColor
                    ComplicationLocation.RIGHT -> storage.getComplicationColors().rightColor
                    ComplicationLocation.BOTTOM -> storage.getComplicationColors().bottomColor
                    ComplicationLocation.ANDROID_12_TOP_LEFT -> storage.getComplicationColors().android12TopLeftColor
                    ComplicationLocation.ANDROID_12_TOP_RIGHT -> storage.getComplicationColors().android12TopRightColor
                    ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> storage.getComplicationColors().android12BottomLeftColor
                    ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> storage.getComplicationColors().android12BottomRightColor
                })
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> TITLE_VIEW_TYPE
            1 -> WIDGET_VIEW_TYPE
            2 -> COLOR_VIEW_TYPE
            else -> throw IllegalArgumentException("getItemViewType called with unknown position: $position")
        }
    }

    override fun getItemCount(): Int {
        return if( showColor ) { 3 } else { 2 }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        providerInfoRetriever.init()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        onDestroy()
    }

    fun onDestroy() {
        providerInfoRetriever.release()
    }

    fun updateComplication(complicationProviderInfo: ComplicationProviderInfo?) {
        widgetViewHolder?.updateComplicationView(complicationProviderInfo)
        showColor = complicationProviderInfo != null
    }

    fun updatePreviewColors(color: ComplicationColor) {
        colorViewHolder?.updateComplicationColor(color)
    }

    private fun initializesColorsAndComplications() {
        providerInfoRetriever.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    updateComplication(complicationProviderInfo)
                }
            },
            ComponentName(context, PixelMinimalWatchFace::class.java),
            getComplicationId(complicationLocation)
        )
    }

    private class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view as TextView

        fun setTitle(title: String) {
            titleTextView.text = title
        }
    }

    private class WidgetViewHolder(view: View, onComplicationSelectionClicked: () -> Unit) : RecyclerView.ViewHolder(view) {
        private val button: ImageButton = view.findViewById(R.id.complication)
        private val background: ImageView = view.findViewById(R.id.complication_background)
        private val subtitle: TextView = view.findViewById(R.id.complication_selection_subtitle)

        private val addComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.add_complication)!!
        private val addedComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.added_complication)!!

        init {
            view.setOnClickListener {
                onComplicationSelectionClicked()
            }
        }

        fun updateComplicationView(
            complicationProviderInfo: ComplicationProviderInfo?
        ) {
            if (complicationProviderInfo != null) {
                button.setImageIcon(complicationProviderInfo.providerIcon)
                background.setImageDrawable(addedComplicationDrawable)
                subtitle.text = itemView.context.getString(R.string.config_complication_tap_to_change)
            } else {
                button.setImageIcon(null)
                background.setImageDrawable(addComplicationDrawable)
                subtitle.text = itemView.context.getString(R.string.config_complication_tap_to_setup)
            }
        }
    }

    private class ColorViewHolder(view: View, onColorClicked: () -> Unit) : RecyclerView.ViewHolder(view) {
        private val colorCardView: CardView = view.findViewById(R.id.widget_config_color_color)

        init {
            view.setOnClickListener {
                onColorClicked()
            }
        }

        fun updateComplicationColor(complicationColor: ComplicationColor) {
            colorCardView.setCardBackgroundColor(complicationColor.color)
        }
    }

    companion object {
        private const val TITLE_VIEW_TYPE = 0
        private const val WIDGET_VIEW_TYPE = 1
        private const val COLOR_VIEW_TYPE = 2
    }
}

