package com.benoitletondor.pixelminimalwatchfacecompanion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import org.koin.java.KoinJavaComponent.get

class BootCompleteBroadcastReceiver : BroadcastReceiver() {
    private val storage: Storage = get(Storage:: class.java)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            if (storage.isBatterySyncActivated()) {
                BatteryStatusBroadcastReceiver.subscribeToUpdates(context)
            }
        }
    }
}