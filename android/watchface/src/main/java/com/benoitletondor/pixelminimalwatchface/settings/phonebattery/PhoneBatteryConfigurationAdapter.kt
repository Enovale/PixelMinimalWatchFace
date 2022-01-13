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
package com.benoitletondor.pixelminimalwatchface.settings.phonebattery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.R

class PhoneBatteryConfigurationAdapter(
    private val onSyncActivatedChanged: (Boolean) -> Unit,
    private val forceDeactivateSync: () -> Unit,
    private val troubleShootConnectionClicked: () -> Unit,
    private val retryConnectionClicked: () -> Unit,
) : RecyclerView.Adapter<PhoneBatteryConfigurationViewHolder>() {

    private var state: PhoneBatteryConfigurationViewModel.State? = null

    fun setState(state: PhoneBatteryConfigurationViewModel.State) {
        this.state = state
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhoneBatteryConfigurationViewHolder = when(viewType) {
        VIEW_TYPE_TITLE -> PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationTitleViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_battery_configuration_title, parent, false)
        )
        VIEW_TYPE_CONNECTING -> PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationConnectingViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_battery_configuration_connecting, parent, false)
        )
        VIEW_TYPE_CONNECTED -> PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationConnectedViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_battery_configuration_connected, parent, false)
        )
        VIEW_TYPE_ERROR -> PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationErrorViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_battery_configuration_error, parent, false)
        )
        VIEW_TYPE_SYNCING -> PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationSyncingViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_battery_configuration_syncing, parent, false)
        )
        else -> throw IllegalArgumentException("Unknown view type: $viewType")
    }


    override fun onBindViewHolder(holder: PhoneBatteryConfigurationViewHolder, position: Int) {
        when(holder) {
            is PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationTitleViewHolder -> Unit // No-op
            is PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationConnectingViewHolder -> Unit // No-op
            is PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationSyncingViewHolder -> Unit // No-op
            is PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationConnectedViewHolder -> {
                val state = state as? PhoneBatteryConfigurationViewModel.State.PhoneStatusResponse ?: return
                holder.setSyncActivated(state.syncActivated)
                holder.setListener(onSyncActivatedChanged)
            }
            is PhoneBatteryConfigurationViewHolder.PhoneBatteryConfigurationErrorViewHolder -> {
                val canDeactivateSync = when(val state = state) {
                    is PhoneBatteryConfigurationViewModel.State.Error -> state.syncActivated
                    is PhoneBatteryConfigurationViewModel.State.PhoneNotFound -> state.syncActivated
                    else -> false
                }

                holder.setDeactivateButtonVisible(canDeactivateSync)
                holder.setDeactivateSyncListener(forceDeactivateSync)
                holder.setTroubleShootListener(troubleShootConnectionClicked)
                holder.setRetryListener(retryConnectionClicked)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_TITLE
        }

        return when(state) {
            is PhoneBatteryConfigurationViewModel.State.Error -> VIEW_TYPE_ERROR
            PhoneBatteryConfigurationViewModel.State.Loading -> VIEW_TYPE_CONNECTING
            is PhoneBatteryConfigurationViewModel.State.PhoneFound -> VIEW_TYPE_SYNCING
            is PhoneBatteryConfigurationViewModel.State.PhoneNotFound -> VIEW_TYPE_ERROR
            is PhoneBatteryConfigurationViewModel.State.PhoneStatusResponse -> VIEW_TYPE_CONNECTED
            is PhoneBatteryConfigurationViewModel.State.SendingStatusSyncToPhone -> VIEW_TYPE_SYNCING
            is PhoneBatteryConfigurationViewModel.State.WaitingForPhoneStatusResponse -> VIEW_TYPE_SYNCING
            null -> VIEW_TYPE_CONNECTING
        }
    }

    override fun getItemCount(): Int = if (state != null) { 2 } else { 1 }

    companion object {
        private const val VIEW_TYPE_TITLE = 0
        private const val VIEW_TYPE_CONNECTING = 1
        private const val VIEW_TYPE_CONNECTED = 2
        private const val VIEW_TYPE_ERROR = 3
        private const val VIEW_TYPE_SYNCING = 4
    }
}

sealed class PhoneBatteryConfigurationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class PhoneBatteryConfigurationTitleViewHolder(view: View) : PhoneBatteryConfigurationViewHolder(view)
    class PhoneBatteryConfigurationConnectingViewHolder(view: View) : PhoneBatteryConfigurationViewHolder(view)
    class PhoneBatteryConfigurationSyncingViewHolder(view: View) : PhoneBatteryConfigurationViewHolder(view)
    class PhoneBatteryConfigurationConnectedViewHolder(view: View) : PhoneBatteryConfigurationViewHolder(view) {
        private val syncActivatedSwitch: SwitchCompat = view.findViewById(R.id.phone_battery_sync_activate_switch)

        fun setSyncActivated(activated: Boolean) {
            syncActivatedSwitch.isChecked = activated
        }

        fun setListener(listener: (Boolean) -> Unit) {
            syncActivatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                listener(isChecked)
            }
        }
    }
    class PhoneBatteryConfigurationErrorViewHolder(view: View) : PhoneBatteryConfigurationViewHolder(view) {
        private val deactivateSyncButton: Button = view.findViewById(R.id.phone_battery_sync_deactivate_button)
        private val troubleShootSyncButton: Button = view.findViewById(R.id.phone_battery_sync_troubleshoot_button)
        private val retryButton: Button = view.findViewById(R.id.phone_battery_sync_retry_button)

        fun setDeactivateSyncListener(listener: () -> Unit) {
            deactivateSyncButton.setOnClickListener {
                listener()
            }
        }

        fun setTroubleShootListener(listener: () -> Unit) {
            troubleShootSyncButton.setOnClickListener {
                listener()
            }
        }

        fun setDeactivateButtonVisible(visible: Boolean) {
            deactivateSyncButton.visibility = if (visible) { View.VISIBLE } else { View.GONE }
        }

        fun setRetryListener(listener: () -> Unit) {
            retryButton.setOnClickListener {
                listener()
            }
        }
    }
}