/*
 *   Copyright 2020 Benoit LETONDOR
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

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import androidx.wear.widget.WearableLinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getSupportedComplicationTypes
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider
import kotlinx.android.synthetic.main.activity_widget_config.*

class WidgetConfigurationActivity : Activity() {
    private lateinit var adapter: WidgetConfigRecyclerViewAdapter
    private lateinit var complicationLocation: ComplicationLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)

        complicationLocation = intent.getParcelableExtra(EXTRA_COMPLICATION_LOCATION)!!
        title = when(complicationLocation) {
            ComplicationLocation.LEFT -> getString(R.string.config_left_complication)
            ComplicationLocation.MIDDLE -> getString(R.string.config_middle_complication)
            ComplicationLocation.RIGHT -> getString(R.string.config_right_complication)
            ComplicationLocation.BOTTOM -> getString(R.string.config_bottom_complication)
        }

        adapter = WidgetConfigRecyclerViewAdapter(
            complicationLocation = complicationLocation,
            context = this,
            title = title as String,
            onSelectColorClicked = {
                val defaultColor = when(complicationLocation) {
                    ComplicationLocation.LEFT -> ComplicationColorsProvider.getDefaultComplicationColors(this).leftColor
                    ComplicationLocation.MIDDLE -> ComplicationColorsProvider.getDefaultComplicationColors(this).middleColor
                    ComplicationLocation.RIGHT -> ComplicationColorsProvider.getDefaultComplicationColors(this).rightColor
                    ComplicationLocation.BOTTOM -> ComplicationColorsProvider.getDefaultComplicationColors(this).bottomColor
                }

                startActivityForResult(ColorSelectionActivity.createIntent(this, defaultColor), UPDATE_COLORS_CONFIG_REQUEST_CODE)
            },
            onSelectComplicationClicked = {
                startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                        this,
                        ComponentName(this, PixelMinimalWatchFace::class.java),
                        getComplicationId(complicationLocation),
                        *getSupportedComplicationTypes(complicationLocation)
                    ),
                    COMPLICATION_CONFIG_REQUEST_CODE
                )
            }
        )

        widget_config_recycler_view.isEdgeItemsCenteringEnabled = true
        widget_config_recycler_view.layoutManager = WearableLinearLayoutManager(this)
        widget_config_recycler_view.setHasFixedSize(true)
        widget_config_recycler_view.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            val complicationProviderInfo: ComplicationProviderInfo? = data?.getParcelableExtra(
                ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            adapter.updateComplication(complicationProviderInfo)

            setResult(RESULT_OK)
        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedColor = data?.getParcelableExtra<ComplicationColor>(ColorSelectionActivity.RESULT_SELECTED_COLOR)
                ?: return

            val storage = Injection.storage(this)
            val colors = storage.getComplicationColors()
            storage.setComplicationColors(when(complicationLocation) {
                ComplicationLocation.LEFT -> colors.copy(leftColor = selectedColor)
                ComplicationLocation.MIDDLE -> colors.copy(middleColor = selectedColor)
                ComplicationLocation.RIGHT -> colors.copy(rightColor = selectedColor)
                ComplicationLocation.BOTTOM -> colors.copy(bottomColor = selectedColor)
            })

            adapter.updatePreviewColors(selectedColor)
            setResult(RESULT_OK)
        }
    }

    override fun onDestroy() {
        adapter.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        private const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
        private const val EXTRA_COMPLICATION_LOCATION = "extra:complicationLocation"

        fun createIntent(
            context: Context,
            complicationLocation: ComplicationLocation
        ): Intent {
            return Intent(context, WidgetConfigurationActivity::class.java).apply {
                putExtra(EXTRA_COMPLICATION_LOCATION, complicationLocation as Parcelable)
            }
        }
    }
}