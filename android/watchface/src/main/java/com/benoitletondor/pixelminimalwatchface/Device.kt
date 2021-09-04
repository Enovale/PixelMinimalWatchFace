package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.os.Build

object Device {
    val isSamsungGalaxy get(): Boolean = Build.BRAND.equals("samsung", ignoreCase = true)
    fun isSamsungGalaxyWatch4BigScreen(context: Context) = isSamsungGalaxy && context.resources.displayMetrics.heightPixels >= 450
    val isOppoWatch get(): Boolean = Build.BRAND == "OPPO" && Build.MODEL == "OPPO Watch"    
}
