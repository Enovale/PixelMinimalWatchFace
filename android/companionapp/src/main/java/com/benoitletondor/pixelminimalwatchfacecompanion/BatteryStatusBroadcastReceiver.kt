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
package com.benoitletondor.pixelminimalwatchfacecompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import javax.inject.Inject

@AndroidEntryPoint
class BatteryStatusBroadcastReceiver : BroadcastReceiver() {
    @Inject lateinit var sync: Sync
    @Inject lateinit var storage: Storage

    private var lastBatteryLevelPercentSent: Int? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (!storage.isBatterySyncActivated()) {
            return
        }

        try {
            val maybeBatteryPercentage = when(intent.action) {
                ACTION_BATTERY_CHANGED -> intent.getBatteryLevelPercent()
                ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED, ACTION_BATTERY_LOW, ACTION_BATTERY_OKAY -> context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))?.getBatteryLevelPercent()
                else -> null
            }

            if (maybeBatteryPercentage == null) {
                Log.w("BatteryStatusBroadcastReceiver", "Unable to extract battery level")
                return
            }

            GlobalScope.launch {
                try {
                    if (maybeBatteryPercentage != lastBatteryLevelPercentSent) {
                        sync.sendBatteryStatus(maybeBatteryPercentage)
                        lastBatteryLevelPercentSent = maybeBatteryPercentage
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) {
                        throw t
                    }

                    Log.e("BatteryStatusBroadcastReceiver", "Error sending battery level", t)
                }
            }
        } catch (t: Throwable) {
            Log.e("BatteryStatusBroadcastReceiver", "Error computing battery level", t)
        }
    }

    companion object {
        private var isSubscribed = false
        private val receiver = BatteryStatusBroadcastReceiver()

        fun subscribeToUpdates(context: Context) {
            if (!isSubscribed) {
                unsubscribeFromUpdates(context)

                val intentFilter = IntentFilter().apply {
                    addAction(ACTION_BATTERY_CHANGED)
                    addAction(ACTION_POWER_CONNECTED)
                    addAction(ACTION_POWER_DISCONNECTED)
                    addAction(ACTION_BATTERY_LOW)
                    addAction(ACTION_BATTERY_OKAY)
                }
                context.applicationContext.registerReceiver(receiver, intentFilter)
            }

            isSubscribed = true
        }

        fun unsubscribeFromUpdates(context: Context) {
            try {
                context.applicationContext.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered, ignoring
            }

            isSubscribed = false
        }

        fun getCurrentBatteryLevel(context: Context): Int {
            val batteryStatus: Intent = context.registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))
                ?: throw RuntimeException("Unable to get battery status, null intent")

            return batteryStatus.getBatteryLevelPercent()
        }
    }
}

private fun Intent.getBatteryLevelPercent(): Int {
    val level: Int = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale: Int = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level == -1 || scale == -1) {
        throw RuntimeException("Unable to get battery percent (level: $level, scale: $scale)")
    }

    return (level * 100 / scale.toFloat()).toInt()
}