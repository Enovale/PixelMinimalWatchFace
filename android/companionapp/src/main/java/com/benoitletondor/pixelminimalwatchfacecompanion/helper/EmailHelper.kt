package com.benoitletondor.pixelminimalwatchfacecompanion.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.benoitletondor.pixelminimalwatchfacecompanion.R

fun Context.startSupportEmailActivity(): Boolean {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SENDTO
    sendIntent.data = Uri.parse("mailto:") // only email apps should handle this
    sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.feedback_email)))
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.feedback_send_subject))

    if ( sendIntent.resolveActivity(packageManager) != null) {
        startActivity(sendIntent)
        return true
    }

    return false
}