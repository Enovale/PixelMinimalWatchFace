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
package com.benoitletondor.pixelminimalwatchface.settings.phonebattery

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.databinding.ActivityPhoneBatteryConfigurationBinding
import com.benoitletondor.pixelminimalwatchface.helper.await
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.troubleshoot.PhoneBatterySyncTroubleshootActivity
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.CapabilityClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PhoneBatteryConfigurationActivity : AppCompatActivity(), CapabilityClient.OnCapabilityChangedListener {
    private val viewModel: PhoneBatteryConfigurationViewModel by viewModels()

    private lateinit var binding: ActivityPhoneBatteryConfigurationBinding

    private val adapter = PhoneBatteryConfigurationAdapter(
        onSyncActivatedChanged = { syncActivated ->
            if (syncActivated) {
                viewModel.onSyncWithPhoneActivated()
            } else {
                viewModel.onSyncWithPhoneDeactivated()
            }
        },
        forceDeactivateSync = {
            viewModel.onForceDeactivateSyncClicked()
        },
        troubleShootConnectionClicked = {
            startActivity(Intent(this, PhoneBatterySyncTroubleshootActivity::class.java))
        },
        retryConnectionClicked = {
            viewModel.onRetryConnectionClicked()
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBatteryConfigurationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindToViewModel()

        binding.wearableRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            LinearLayoutManager(this@PhoneBatteryConfigurationActivity)
            setHasFixedSize(true)
            adapter = this@PhoneBatteryConfigurationActivity.adapter
        }

        Wearable.getCapabilityClient(this).addListener(this, BuildConfig.COMPANION_APP_CAPABILITY)
        checkIfPhoneHasApp()
    }

    override fun onDestroy() {
        Wearable.getCapabilityClient(this).removeListener(this, BuildConfig.COMPANION_APP_CAPABILITY)
        super.onDestroy()
    }

    private fun bindToViewModel() {
        lifecycleScope.launch {
            viewModel.stateFlow.collect { state ->
                adapter.setState(state)
            }
        }

        lifecycleScope.launch {
            viewModel.errorEventFlow.collect { error ->
                when(error) {
                    PhoneBatteryConfigurationViewModel.ErrorEventType.PHONE_CHANGED -> TODO()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.retryEventFlow.collect {
                checkIfPhoneHasApp()
            }
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        viewModel.onCapabilityChanged(capabilityInfo)
    }

    private fun checkIfPhoneHasApp() {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val result = Wearable.getCapabilityClient(this@PhoneBatteryConfigurationActivity)
                    .getCapability(BuildConfig.COMPANION_APP_CAPABILITY, CapabilityClient.FILTER_ALL)
                    .await()
                viewModel.onPhoneAppDetectionResult(result.nodes)
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                viewModel.onPhoneAppDetectionFailed(t)
            }
        }
    }
}
