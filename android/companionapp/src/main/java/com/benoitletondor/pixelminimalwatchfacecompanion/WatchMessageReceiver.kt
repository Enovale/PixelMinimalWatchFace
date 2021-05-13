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
package com.benoitletondor.pixelminimalwatchfacecompanion

import android.util.Log
import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig.WATCH_CAPABILITY
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class WatchMessageReceiver : WearableListenerService(), CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
    @Inject lateinit var sync: Sync
    @Inject lateinit var storage: Storage

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when(messageEvent.path) {
            QUERY_SYNC_STATUS_PATH -> sendSyncStatus(messageEvent.data)
            QUERY_ACTIVATED_SYNC_PATH -> activateSync()
            QUERY_DEACTIVATED_SYNC_PATH -> deactivateSync()
            else -> Log.e(TAG, "Received message with unknown path: ${messageEvent.path}")
        }
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo?) {
        super.onCapabilityChanged(capabilityInfo)

        if (capabilityInfo == null) {
            return
        }

        if (capabilityInfo.name != WATCH_CAPABILITY) {
            return
        }

        val watchNode = capabilityInfo.nodes?.firstOrNull { it.isNearby } ?: capabilityInfo.nodes?.firstOrNull()
        if (watchNode != null) {
            if (storage.isBatterySyncActivated()) {
                activateSync()
            }
        }
    }

    override fun onConnectedNodes(p0: MutableList<Node>?) {
        super.onConnectedNodes(p0)
    }

    private fun sendSyncStatus(watchData: ByteArray) {
        launch {
            try {
                val syncActivatedOnWatch = watchData.first().toInt() == 1
                if (syncActivatedOnWatch) {
                    activateSync()
                } else {
                    deactivateSync()
                }
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                Log.e(TAG, "Error while sending sync status", t)
            }
        }
    }

    private fun deactivateSync() {
        storage.setBatterySyncActivated(false)
        BatteryStatusBroadcastReceiver.unsubscribeFromUpdates(this)

        launch {
            try {
                sync.sendBatterySyncStatus(false)
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                Log.e(TAG, "Error while deactivating sync", t)
            }
        }
    }

    private fun activateSync() {
        storage.setBatterySyncActivated(true)
        BatteryStatusBroadcastReceiver.subscribeToUpdates(this)

        launch {
            try {
                sync.sendBatterySyncStatus(true)
                sync.sendBatteryStatus(BatteryStatusBroadcastReceiver.getCurrentBatteryLevel(this@WatchMessageReceiver))
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                Log.e(TAG, "Error while activating sync", t)
            }
        }
    }

    companion object {
        private const val TAG = "WatchMessageReceiver"

        private const val QUERY_SYNC_STATUS_PATH = "/batterySync/queryStatus"
        private const val QUERY_ACTIVATED_SYNC_PATH = "/batterySync/activate"
        private const val QUERY_DEACTIVATED_SYNC_PATH = "/batterySync/deactivate"
    }
}