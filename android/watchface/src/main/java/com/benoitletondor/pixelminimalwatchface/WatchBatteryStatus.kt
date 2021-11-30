package com.benoitletondor.pixelminimalwatchface

import kotlin.math.abs

sealed class WatchBatteryStatus {
    object Unknown : WatchBatteryStatus()
    data class DataReceived(
        val batteryPercentage: Int,
        val timestamp: Long,
    ) : WatchBatteryStatus() {
        fun isStale(currentBatteryPercentage: Int): Boolean {
            val diff = abs(batteryPercentage - currentBatteryPercentage)
            return diff > 1 || System.currentTimeMillis() - timestamp > ONE_MIN_MS
        }

        companion object {
            private const val ONE_MIN_MS: Long = 1000* 60L
        }
    }
}