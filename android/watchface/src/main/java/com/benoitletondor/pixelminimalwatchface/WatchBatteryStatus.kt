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

import kotlin.math.abs

sealed class WatchBatteryStatus {
    object Unknown : WatchBatteryStatus()
    data class DataReceived(
        val batteryPercentage: Int,
    ) : WatchBatteryStatus() {
        private var staleSinceTs: Long? = null

        fun markAsStale() {
            if (staleSinceTs == null) {
                staleSinceTs = System.currentTimeMillis()
            }
        }

        fun shouldRefresh(currentBatteryPercentage: Int): Boolean {
            val diff = abs(batteryPercentage - currentBatteryPercentage)
            val staleSinceTs = this.staleSinceTs
            return diff > 1 || (staleSinceTs != null && System.currentTimeMillis() - staleSinceTs > ONE_MIN_MS)
        }

        companion object {
            private const val ONE_MIN_MS: Long = 1000* 60L
        }
    }
}