package com.benoitletondor.pixelminimalwatchfacecompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import org.koin.java.KoinJavaComponent.get

class AppUpdateBroadcastReceiver : BroadcastReceiver() {
    private val storage: Storage = get(Storage::class.java)

    override fun onReceive(context: Context, intent: Intent?) {
        if( intent?.action != "android.intent.action.MY_PACKAGE_REPLACED" ) {
            return
        }

        if (storage.isBatterySyncActivated()) {
            BatteryStatusBroadcastReceiver.subscribeToUpdates(context)
        }
    }

}