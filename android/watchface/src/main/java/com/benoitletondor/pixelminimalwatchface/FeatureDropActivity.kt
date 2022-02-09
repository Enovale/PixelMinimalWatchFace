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
package com.benoitletondor.pixelminimalwatchface

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.databinding.ActivityFeatureDropBinding
import com.benoitletondor.pixelminimalwatchface.settings.SettingsActivity

class FeatureDropActivity : Activity() {
    private lateinit var binding: ActivityFeatureDropBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeatureDropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.featureDropRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@FeatureDropActivity)
            setHasFixedSize(true)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RecyclerView.ViewHolder {
                    return when(viewType) {
                        0 -> object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_title_layout, parent, false)) {}
                        1 -> object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_layout, parent, false)) {}
                        2 -> if ( PixelMinimalWatchFace.isActive(parent.context) ) {
                            object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_button_layout, parent, false)) {}
                        } else {
                            object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_not_button_layout, parent, false)) {}
                        }
                        else -> object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_bottom_layout, parent, false)) {}
                    }
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    if( position == 2) {
                        holder.itemView.findViewById<View>(R.id.feature_drop_go_to_settings_button)?.setOnClickListener {
                            finish()
                            applicationContext.startActivity(Intent(this@FeatureDropActivity, SettingsActivity::class.java).apply {
                                flags = FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    }
                }

                override fun getItemViewType(position: Int): Int {
                    return position
                }

                override fun getItemCount(): Int = 4
            }
        }
    }
}