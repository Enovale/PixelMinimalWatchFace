package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.os.Build

object Device {
    val isSamsungGalaxy get(): Boolean = Build.VERSION.SDK_INT >= 30 && Build.BRAND.equals("samsung", ignoreCase = true)
    fun isSamsungGalaxyWatch4BigScreen(context: Context) = isSamsungGalaxy && context.resources.displayMetrics.heightPixels >= 450
}