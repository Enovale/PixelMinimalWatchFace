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