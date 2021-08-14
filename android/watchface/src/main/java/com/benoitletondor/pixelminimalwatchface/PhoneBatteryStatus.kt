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
package com.benoitletondor.pixelminimalwatchface

sealed class PhoneBatteryStatus {
    abstract fun isStale(currentTimestamp: Long): Boolean

    object Unknown : PhoneBatteryStatus() {
        override fun isStale(currentTimestamp: Long): Boolean = true
    }
    class DataReceived(val batteryPercentage: Int, private val timestamp: Long) : PhoneBatteryStatus() {
        override fun isStale(currentTimestamp: Long): Boolean {
            return currentTimestamp - timestamp > STALE_PHONE_BATTERY_LIMIT_MS
        }

        companion object {
            private const val STALE_PHONE_BATTERY_LIMIT_MS = 1000*60*60 // 1h
        }
    }
}

fun PhoneBatteryStatus.getBatteryText(currentTimestamp: Long): String {
    return when(this) {
        is PhoneBatteryStatus.DataReceived -> if (isStale(currentTimestamp)) { "?" } else { "${batteryPercentage}%" }
        PhoneBatteryStatus.Unknown -> "?"
    }
}