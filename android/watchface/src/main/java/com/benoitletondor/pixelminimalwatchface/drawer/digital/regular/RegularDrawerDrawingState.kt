package com.benoitletondor.pixelminimalwatchface.drawer.digital.regular

import android.content.Context
import com.benoitletondor.pixelminimalwatchface.drawer.digital.*

sealed class RegularDrawerDrawingState {
    object NoScreenData : RegularDrawerDrawingState()
    data class NoCacheAvailable(
        val screenWidth: Int,
        val screenHeight: Int,
        val centerX: Float,
        val centerY: Float,
    ) : RegularDrawerDrawingState()
    data class CacheAvailable(
        private val context: Context,
        private val batteryIconSize: Int,
        private val batteryLevelBottomY: Int,
        private val batteryIconBottomY: Int,
        private val dateHeight: Int,
        private val dateYOffset: Float,
        val screenWidth: Int,
        val screenHeight: Int,
        val centerX: Float,
        val centerY: Float,
        val timeYOffset: Float,
        val complicationsDrawingCache: ComplicationsDrawingCache,
    ) : RegularDrawerDrawingState(),
        BatteryDrawer by BatteryDrawerImpl(context, centerX, screenWidth, batteryIconSize, batteryLevelBottomY, batteryIconBottomY),
        SecondsRingDrawer by SecondRingDrawerImpl(screenWidth, screenHeight),
        DateAndWeatherDrawer by DateAndWeatherDrawerImpl(context, dateHeight, dateYOffset, centerX)
}

data class ComplicationsDrawingCache(
    val iconXOffset: Float,
    val iconYOffset: Float
)
