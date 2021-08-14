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
package com.benoitletondor.pixelminimalwatchfacecompanion

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.config.Config
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), LifecycleObserver, CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
    @Inject lateinit var billing: Billing
    @Inject lateinit var config: Config
    @Inject lateinit var storage: Storage

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this)

        // Register battery receiver if needed
        if (storage.isBatterySyncActivated()) {
            BatteryStatusBroadcastReceiver.subscribeToUpdates(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @Suppress("UNUSED")
    private fun onAppForeground() {
        billing.updatePremiumStatusIfNeeded()

        launch {
            try {
                config.fetch()
            } catch (t: Throwable) {
                Log.e("App", "Error syncing config", t)
            }
        }
    }
}