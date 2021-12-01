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