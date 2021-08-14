/*
 *   Copyright 2021 Benoit LETONDOR
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
package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.benoitletondor.pixelminimalwatchface.R

object ComplicationColorsProvider {

    fun getDefaultComplicationColors(context: Context): ComplicationColors {
        val leftColor = ContextCompat.getColor(context, R.color.complication_default_left_color)
        val middleColor = ContextCompat.getColor(context, R.color.complication_default_middle_color)
        val rightColor = ContextCompat.getColor(context, R.color.complication_default_right_color)
        val bottomColor = ContextCompat.getColor(context, R.color.complication_default_bottom_color)

        return ComplicationColors(
            ComplicationColor(leftColor, context.getString(R.string.color_default), true),
            ComplicationColor(middleColor, context.getString(R.string.color_default), true),
            ComplicationColor(rightColor, context.getString(R.string.color_default), true),
            ComplicationColor(bottomColor, context.getString(R.string.color_default), true),
            ComplicationColor(leftColor, context.getString(R.string.color_default), true),
            ComplicationColor(rightColor, context.getString(R.string.color_default), true),
            ComplicationColor(middleColor, context.getString(R.string.color_default), true),
            ComplicationColor(bottomColor, context.getString(R.string.color_default), true),
        )
    }

    fun getLabelForColor(context: Context, @ColorInt color: Int): String {
        return when(color) {
            Color.parseColor("#FFFFFF") -> context.getString(R.string.color_white)
            Color.parseColor("#FFEB3B") -> context.getString(R.string.color_yellow)
            Color.parseColor("#FFC107") -> context.getString(R.string.color_amber)
            Color.parseColor("#FF9800") -> context.getString(R.string.color_orange)
            Color.parseColor("#FF5722") -> context.getString(R.string.color_deep_orange)
            Color.parseColor("#F44336") -> context.getString(R.string.color_red)
            Color.parseColor("#E91E63") -> context.getString(R.string.color_pink)
            Color.parseColor("#9C27B0") -> context.getString(R.string.color_purple)
            Color.parseColor("#673AB7") -> context.getString(R.string.color_deep_purple)
            Color.parseColor("#3F51B5") -> context.getString(R.string.color_indigo)
            Color.parseColor("#2196F3") -> context.getString(R.string.color_blue)
            Color.parseColor("#03A9F4") -> context.getString(R.string.color_light_blue)
            Color.parseColor("#00BCD4") -> context.getString(R.string.color_cyan)
            Color.parseColor("#009688") -> context.getString(R.string.color_teal)
            Color.parseColor("#4CAF50") -> context.getString(R.string.color_green)
            Color.parseColor("#8BC34A") -> context.getString(R.string.color_lime_green)
            Color.parseColor("#CDDC39") -> context.getString(R.string.color_lime)
            Color.parseColor("#607D8B") -> context.getString(R.string.color_blue_grey)
            Color.parseColor("#9E9E9E") -> context.getString(R.string.color_grey)
            Color.parseColor("#795548") -> context.getString(R.string.color_brown)
            else -> context.getString(R.string.color_default)
        }
    }

    fun getAllComplicationColors(context: Context): List<ComplicationColor> {
        return listOf(
            ComplicationColor(
                Color.parseColor("#FFFFFF"),
                context.getString(R.string.color_white),
                false
            ), // White
            ComplicationColor(
                Color.parseColor("#FFEB3B"),
                context.getString(R.string.color_yellow),
                false
            ), // Yellow
            ComplicationColor(
                Color.parseColor("#FFC107"),
                context.getString(R.string.color_amber),
                false
            ), // Amber
            ComplicationColor(
                Color.parseColor("#FF9800"),
                context.getString(R.string.color_orange),
                false
            ), // Orange
            ComplicationColor(
                Color.parseColor("#FF5722"),
                context.getString(R.string.color_deep_orange),
                false
            ), // Deep Orange
            ComplicationColor(
                Color.parseColor("#F44336"),
                context.getString(R.string.color_red),
                false
            ), // Red
            ComplicationColor(
                Color.parseColor("#E91E63"),
                context.getString(R.string.color_pink),
                false
            ), // Pink
            ComplicationColor(
                Color.parseColor("#9C27B0"),
                context.getString(R.string.color_purple),
                false
            ), // Purple
            ComplicationColor(
                Color.parseColor("#673AB7"),
                context.getString(R.string.color_deep_purple),
                false
            ), // Deep Purple
            ComplicationColor(
                Color.parseColor("#3F51B5"),
                context.getString(R.string.color_indigo),
                false
            ), // Indigo
            ComplicationColor(
                Color.parseColor("#2196F3"),
                context.getString(R.string.color_blue),
                false
            ), // Blue
            ComplicationColor(
                Color.parseColor("#03A9F4"),
                context.getString(R.string.color_light_blue),
                false
            ), // Light Blue
            ComplicationColor(
                Color.parseColor("#00BCD4"),
                context.getString(R.string.color_cyan),
                false
            ), // Cyan
            ComplicationColor(
                Color.parseColor("#009688"),
                context.getString(R.string.color_teal),
                false
            ), // Teal
            ComplicationColor(
                Color.parseColor("#4CAF50"),
                context.getString(R.string.color_green),
                false
            ), // Green
            ComplicationColor(
                Color.parseColor("#8BC34A"),
                context.getString(R.string.color_lime_green),
                false
            ), // Lime Green
            ComplicationColor(
                Color.parseColor("#CDDC39"),
                context.getString(R.string.color_lime),
                false
            ), // Lime
            ComplicationColor(
                Color.parseColor("#607D8B"),
                context.getString(R.string.color_blue_grey),
                false
            ), // Blue Grey
            ComplicationColor(
                Color.parseColor("#9E9E9E"),
                context.getString(R.string.color_grey),
                false
            ), // Grey
            ComplicationColor(
                Color.parseColor("#795548"),
                context.getString(R.string.color_brown),
                false
            ) // Brown
        )
    }
}