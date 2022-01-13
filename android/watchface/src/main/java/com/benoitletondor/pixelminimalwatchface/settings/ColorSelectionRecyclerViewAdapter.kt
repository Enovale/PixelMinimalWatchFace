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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor

class ColorSelectionRecyclerViewAdapter(
    private val colors: List<ComplicationColor>,
    private val onColorSelectedListener: (color: ComplicationColor) -> Unit
) : RecyclerView.Adapter<ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.color_config_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ColorViewHolder, position: Int) {
        val color = colors[position]

        viewHolder.setItem(color) {
            onColorSelectedListener(color)
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }
}

class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val colorView: CardView = view.findViewById(R.id.colorView)
    private val colorLabelTextView: TextView = view.findViewById(R.id.color_config_list_item_color_label)

    fun setItem(item: ComplicationColor,
                onClickListener: () -> Unit) {
        colorView.setCardBackgroundColor(item.color)
        colorLabelTextView.text = item.label

        itemView.setOnClickListener {
            onClickListener()
        }
    }
}