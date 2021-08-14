package com.benoitletondor.pixelminimalwatchface.helper

import android.content.Context
import android.os.Build
import com.benoitletondor.pixelminimalwatchface.R

fun Context.getTopAndBottomMargins(): Float {
    if (Build.BRAND == "OPPO" && Build.MODEL == "OPPO Watch") {
        return dpToPx(5).toFloat()
    }

    return resources.getDimension(R.dimen.screen_top_and_bottom_margin)
}