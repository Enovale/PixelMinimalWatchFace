package com.benoitletondor.pixelminimalwatchfacecompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import com.benoitletondor.pixelminimalwatchfacecompanion.device.Device
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService : Service() {
    @Inject lateinit var device: Device

    override fun onBind(intent: Intent): IBinder = LocalBinder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_STOP -> {
                stop()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                start()
                return START_STICKY
            }
            ACTION_FORCE_STOP -> {
                device.deactivateForegroundService(killServiceIfOn = false)
                stop()
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private fun start() {
        activated = true

        // Create notification channel if needed
        val channelId = "foreground"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(
                channelId,
                "Optional always-on mode notification",
                importance
            )
            mChannel.description = "Used to display a persistent notification in case of issues with phone synchronization"

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val stopServiceIntent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_FORCE_STOP
        }
        val stopServicePendingIntent = PendingIntent.getService(
            this,
            0,
            stopServiceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val startCompanionIntent = Intent(this, MainActivity::class.java)
        val startCompanionPendingIntent = PendingIntent.getActivity(
            this,
            0,
            startCompanionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setPriority(PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Pixel Minimal Watch Face")
            .setContentText("Always-on mode activated")
            .setContentIntent(startCompanionPendingIntent)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_stop,
                    "Stop",
                    stopServicePendingIntent,
                )
            )
            .build()

        startForeground(183729, notification)
    }

    private fun stop() {
        activated = false

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        activated = false

        super.onDestroy()
    }

    private object LocalBinder : Binder()

    companion object {
        private const val ACTION_STOP = "stop"
        private const val ACTION_START = "start"
        private const val ACTION_FORCE_STOP = "forceStop"

        private var activated = false

        fun isActivated(): Boolean = activated

        fun start(context: Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            if (!activated) {
                return
            }

            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_STOP
            }

            context.startService(intent)
        }
    }
}