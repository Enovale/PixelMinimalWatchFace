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
package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.databinding.ActivityColorSelectionConfigBinding
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColor
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider

class ColorSelectionActivity : Activity() {
    private lateinit var binding: ActivityColorSelectionConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorSelectionConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val defaultColor = intent.getParcelableExtra<ComplicationColor>(EXTRA_DEFAULT_COLOR)!!

        val availableColors = ComplicationColorsProvider.getAllComplicationColors(this)

        binding.colorsRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@ColorSelectionActivity)
            setHasFixedSize(true)
            adapter = ColorSelectionRecyclerViewAdapter(listOf(defaultColor).plus(availableColors)) { selectedColor ->
                setResult(RESULT_OK, Intent().apply {
                    putExtra(RESULT_SELECTED_COLOR, selectedColor)
                })

                finish()
            }
        }
    }

    companion object {
        const val RESULT_SELECTED_COLOR = "resultSelectedColor"

        private const val EXTRA_DEFAULT_COLOR = "extra:defaultColor"

        fun createIntent(context: Context, defaultColor: ComplicationColor) = Intent(context, ColorSelectionActivity::class.java).apply {
            putExtra(EXTRA_DEFAULT_COLOR, defaultColor)
        }
    }
}