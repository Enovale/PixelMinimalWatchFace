/*
 *   Copyright 2021 Benoit LETONDOR
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
package com.benoitletondor.pixelminimalwatchface.helper

import android.os.Handler
import android.os.Message
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import java.lang.ref.WeakReference

class ComplicationTimeDependentUpdateHandler(
    private val engine: WeakReference<PixelMinimalWatchFace.Engine>,
    private var hasUpdateScheduled: Boolean = false
) : Handler() {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        val engine = engine.get() ?: return

        hasUpdateScheduled = false

        if( !engine.isAmbientMode() && engine.isVisible ) {
            engine.invalidate()
        }
    }

    fun cancelUpdate() {
        if( hasUpdateScheduled ) {
            hasUpdateScheduled = false
            removeMessages(MSG_UPDATE_TIME)
        }
    }

    fun scheduleUpdate(delay: Long) {
        if( hasUpdateScheduled ) {
            cancelUpdate()
        }

        hasUpdateScheduled = true
        sendEmptyMessageDelayed(MSG_UPDATE_TIME, delay)
    }

    fun hasUpdateScheduled(): Boolean = hasUpdateScheduled

    companion object {
        private const val MSG_UPDATE_TIME = 0
    }
}