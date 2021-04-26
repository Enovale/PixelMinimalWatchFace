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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.IllegalArgumentException

class BatteryStatusBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val sync: Sync by inject()

    private var lastBatteryLevelPercentSent: Int? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            GlobalScope.launch {
                try {
                    val batteryLevelPercent = intent.getBatteryLevelPercent()
                    if (batteryLevelPercent != lastBatteryLevelPercentSent) {
                        sync.sendBatteryStatus(batteryLevelPercent)
                        lastBatteryLevelPercentSent = batteryLevelPercent
                    }
                } catch (t: Throwable) {
                    Log.e("BatteryStatusBroadcastReceiver", "Error computing battery level", t)
                }
            }
        }
    }

    companion object {
        private var isSubscribed = false
        private val receiver = BatteryStatusBroadcastReceiver()

        fun subscribeToUpdates(context: Context) {
            if (!isSubscribed) {
                unsubscribeFromUpdates(context)
                context.applicationContext.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
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
            val batteryStatus: Intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            } ?: throw RuntimeException("Unable to get battery status, null intent")

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