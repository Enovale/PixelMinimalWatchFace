package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.os.Build

object Device {
    val isSamsungGalaxyWatch get(): Boolean = Build.VERSION.SDK_INT >= 30 && Build.BRAND.equals("samsung", ignoreCase = true)
    fun isSamsungGalaxyWatchBigScreen(context: Context) = isSamsungGalaxyWatch && context.resources.displayMetrics.heightPixels >= 450
    val isSamsungGalaxyWatch4 get(): Boolean = isSamsungGalaxyWatch && galaxyWatch4ModelNumbers.contains(Build.MODEL)
    val isOppoWatch get(): Boolean = Build.BRAND == "OPPO" && Build.MODEL == "OPPO Watch"

    // https://en.wikipedia.org/wiki/Samsung_Galaxy_Watch_4
    private val galaxyWatch4ModelNumbers = setOf(
        "SM-R860",
        "SM-R865",
        "SM-R870",
        "SM-R875",
        "SM-R880",
        "SM-R885",
        "SM-R890",
        "SM-R895",
    )
}
