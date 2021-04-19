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
import com.benoitletondor.pixelminimalwatchface.settings.ComplicationConfigActivity
import kotlinx.android.synthetic.main.activity_feature_drop_2021.*

class FeatureDrop2021Activity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_drop_2021)

        feature_drop_recycler_view.isEdgeItemsCenteringEnabled = true
        feature_drop_recycler_view.layoutManager = LinearLayoutManager(this)
        feature_drop_recycler_view.setHasFixedSize(true)
        feature_drop_recycler_view.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return when(viewType) {
                    0 -> object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_title_layout, parent, false)) {}
                    1 -> object: RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.feature_drop_2021_layout, parent, false)) {}
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
                        applicationContext.startActivity(Intent(this@FeatureDrop2021Activity, ComplicationConfigActivity::class.java).apply {
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