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
package com.benoitletondor.pixelminimalwatchface

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationText
import android.support.wearable.complications.SystemProviders
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.benoitletondor.pixelminimalwatchface.drawer.digital.android12.Android12DigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.drawer.WatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.drawer.digital.regular.RegularDigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.helper.*
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.DEFAULT_APP_VERSION
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.rating.FeedbackActivity
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.settings.phonebattery.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max

const val MISC_NOTIFICATION_CHANNEL_ID = "rating"
private const val DATA_KEY_PREMIUM = "premium"
private const val DATA_KEY_BATTERY_STATUS_PERCENT = "/batterySync/batteryStatus"
private const val THREE_DAYS_MS: Long = 1000 * 60 * 60 * 24 * 3
private const val THIRTY_MINS_MS: Long = 1000 * 60 * 30
private const val MINIMUM_COMPLICATION_UPDATE_INTERVAL_MS = 1000L
val DEBUG_LOGS = BuildConfig.DEBUG
private const val TAG = "PixelMinimalWatchFace"

class PixelMinimalWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        val storage = Injection.storage(this)

        // Set app version to the current one if not set yet (first launch)
        if (storage.getAppVersion() == DEFAULT_APP_VERSION) {
            storage.setAppVersion(BuildConfig.VERSION_CODE)
        }

        if (DEBUG_LOGS) Log.d(TAG, "onCreateEngine")

        return Engine(this, storage)
    }

    inner class Engine(
        private val service: WatchFaceService,
        private val storage: Storage,
    ) : CanvasWatchFaceService.Engine(), DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener,
        Drawable.Callback, CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
        private lateinit var calendar: Calendar
        private var registeredTimeZoneReceiver = false

        private lateinit var watchFaceDrawer: WatchFaceDrawer

        private var complicationsColors: ComplicationColors = storage.getComplicationColors()
        private val complicationDataSparseArray: SparseArray<ComplicationData> = SparseArray(COMPLICATION_IDS.size)

        private var muteMode = false
        private var ambient = false
        private var lowBitAmbient = false
        private var burnInProtection = false

        private val timeDependentUpdateHandler = ComplicationTimeDependentUpdateHandler(WeakReference(this))
        private val timeDependentTexts = SparseArray<ComplicationText>()

        private var useAndroid12Style = storage.useAndroid12Style()

        private var shouldShowWeather = false
        private var shouldShowBattery = false
        private var weatherComplicationData: ComplicationData? = null
        private var batteryComplicationData: ComplicationData? = null

        private var lastTapEventTimestamp: Long = 0

        private var lastPhoneSyncRequestTimestamp: Long? = null
        private var phoneBatteryStatus: PhoneBatteryStatus = PhoneBatteryStatus.Unknown

        private var screenWidth = -1
        private var screenHeight = -1
        private var windowInsets: WindowInsets? = null

        private val timeZoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            if (DEBUG_LOGS) Log.d(TAG, "onCreate")

            setWatchFaceStyle(
                WatchFaceStyle.Builder(service)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            calendar = Calendar.getInstance()

            initWatchFaceDrawer()

            Wearable.getDataClient(service).addListener(this)
            Wearable.getMessageClient(service).addListener(this)
            syncPhoneBatteryStatus()
        }

        private fun initWatchFaceDrawer() {
            if (DEBUG_LOGS) Log.d(TAG, "initWatchFaceDrawer, a12? ${storage.useAndroid12Style()}")

            watchFaceDrawer = if (storage.useAndroid12Style()) {
                Android12DigitalWatchFaceDrawer(service, storage)
            } else {
                RegularDigitalWatchFaceDrawer(service, storage)
            }

            initializeComplications()

            val currentWindowInsets = windowInsets
            if (currentWindowInsets != null) {
                watchFaceDrawer.onApplyWindowInsets(currentWindowInsets)
            }

            if (screenWidth > 0 && screenHeight > 0) {
                watchFaceDrawer.onSurfaceChanged(screenWidth, screenHeight)
            }
        }

        private fun initializeComplications() {
            val activeComplicationIds = watchFaceDrawer.initializeComplicationDrawables(this)

            if (DEBUG_LOGS) Log.d(TAG, "initializeComplications, activeComplicationIds: $activeComplicationIds")

            shouldShowWeather = false
            shouldShowBattery = false

            setActiveComplications(*activeComplicationIds.plus(WEATHER_COMPLICATION_ID).plus(BATTERY_COMPLICATION_ID))

            watchFaceDrawer.onComplicationColorsUpdate(complicationsColors, complicationDataSparseArray)
        }

        private fun subscribeToWeatherComplicationData() {
            if (DEBUG_LOGS) Log.d(TAG, "subscribeToWeatherComplicationData")

            val weatherProviderInfo = getWeatherProviderInfo() ?: return
            setDefaultComplicationProvider(
                WEATHER_COMPLICATION_ID,
                ComponentName(weatherProviderInfo.appPackage, weatherProviderInfo.weatherProviderService),
                ComplicationData.TYPE_SHORT_TEXT
            )
        }

        private fun unsubscribeToWeatherComplicationData() {
            if (DEBUG_LOGS) Log.d(TAG, "unsubscribeToWeatherComplicationData")

            setDefaultComplicationProvider(
                WEATHER_COMPLICATION_ID,
                null,
                ComplicationData.TYPE_EMPTY
            )
        }

        private fun subscribeToBatteryComplicationData() {
            if (DEBUG_LOGS) Log.d(TAG, "subscribeToBatteryComplicationData")

            setDefaultSystemComplicationProvider(
                BATTERY_COMPLICATION_ID,
                SystemProviders.WATCH_BATTERY,
                ComplicationData.TYPE_SHORT_TEXT
            )
        }

        private fun unsubscribeToBatteryComplicationData() {
            if (DEBUG_LOGS) Log.d(TAG, "unsubscribeToBatteryComplicationData")

            setDefaultComplicationProvider(
                BATTERY_COMPLICATION_ID,
                null,
                ComplicationData.TYPE_EMPTY
            )
        }

        override fun onDestroy() {
            if (DEBUG_LOGS) Log.d(TAG, "onDestroy")

            unregisterReceiver()
            Wearable.getDataClient(service).removeListener(this)
            Wearable.getMessageClient(service).removeListener(this)
            timeDependentUpdateHandler.cancelUpdate()
            cancel()

            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)

            if (DEBUG_LOGS) Log.d(TAG, "onPropertiesChanged")

            lowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            burnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )

            invalidate()
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            if (DEBUG_LOGS) Log.d(TAG, "onApplyWindowInsets")

            windowInsets = insets
            watchFaceDrawer.onApplyWindowInsets(insets)
        }

        override fun onTimeTick() {
            super.onTimeTick()

            if (DEBUG_LOGS) Log.d(TAG, "onTimeTick")

            if( !storage.hasRatingBeenDisplayed() &&
                System.currentTimeMillis() - storage.getInstallTimestamp() > THREE_DAYS_MS ) {
                storage.setRatingDisplayed(true)
                sendRatingNotification()
            }

            val lastPhoneSyncRequestTimestamp = lastPhoneSyncRequestTimestamp
            if( storage.shouldShowPhoneBattery() &&
                phoneBatteryStatus.isStale(System.currentTimeMillis()) &&
                (lastPhoneSyncRequestTimestamp == null || System.currentTimeMillis() - lastPhoneSyncRequestTimestamp > THIRTY_MINS_MS) ) {
                this.lastPhoneSyncRequestTimestamp = System.currentTimeMillis()
                syncPhoneBatteryStatus()
            }

            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)

            if (DEBUG_LOGS) Log.d(TAG, "onAmbientModeChanged, ambient: ${isAmbientMode()}")

            ambient = inAmbientMode

            invalidate()
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)

            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE

            if (DEBUG_LOGS) Log.d(TAG, "onInterruptionFilterChanged, inMuteMode: $inMuteMode")

            if (muteMode != inMuteMode) {
                muteMode = inMuteMode

                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            if (DEBUG_LOGS) Log.d(TAG, "onSurfaceChanged, width: $width, height: $height")

            screenWidth = width
            screenHeight = height

            watchFaceDrawer.onSurfaceChanged(width, height)
        }

        override fun onComplicationDataUpdate(watchFaceComplicationId: Int, complicationData: ComplicationData) {
            super.onComplicationDataUpdate(watchFaceComplicationId, complicationData)

            if (DEBUG_LOGS) Log.d(TAG, "onComplicationDataUpdate, watchFaceComplicationId: $watchFaceComplicationId, complicationData: $complicationData")

            if( watchFaceComplicationId == WEATHER_COMPLICATION_ID ) {
                if (DEBUG_LOGS) Log.d(TAG, "onComplicationDataUpdate, weatherComplicationData")

                weatherComplicationData = if( complicationData.type == ComplicationData.TYPE_SHORT_TEXT ) {
                    complicationData
                } else {
                    null
                }

                invalidate()
                return
            }

            if( watchFaceComplicationId == BATTERY_COMPLICATION_ID ) {
                if (DEBUG_LOGS) Log.d(TAG, "onComplicationDataUpdate, batteryComplicationData")

                batteryComplicationData = if( complicationData.type == ComplicationData.TYPE_SHORT_TEXT ) {
                    complicationData
                } else {
                    null
                }

                invalidate()
                return
            }

            val data = complicationData.sanitize(this@PixelMinimalWatchFace)

            complicationDataSparseArray.put(watchFaceComplicationId, data)
            watchFaceDrawer.onComplicationDataUpdate(watchFaceComplicationId, data, complicationsColors)

            // Update time dependent complication
            val nextShortTextChangeTime = data.shortText?.getNextChangeTime(System.currentTimeMillis())
            if( nextShortTextChangeTime != null && nextShortTextChangeTime < Long.MAX_VALUE ) {
                timeDependentTexts.put(watchFaceComplicationId, data.shortText)
            } else {
                timeDependentTexts.remove(watchFaceComplicationId)
            }

            timeDependentUpdateHandler.cancelUpdate()

            if( !ambient || storage.shouldShowComplicationsInAmbientMode() ) {
                invalidate()
            }
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TAP -> {
                    if ( watchFaceDrawer.tapIsOnComplication(x, y) ) {
                        lastTapEventTimestamp = 0
                        return
                    }
                    if( watchFaceDrawer.tapIsOnWeather(x, y) ) {
                        val weatherProviderInfo = getWeatherProviderInfo() ?: return
                        openActivity(weatherProviderInfo.appPackage, weatherProviderInfo.weatherActivityName)
                        lastTapEventTimestamp = 0
                        return
                    }
                    if( watchFaceDrawer.tapIsInCenterOfScreen(x, y) ) {
                        if( lastTapEventTimestamp == 0L || eventTime - lastTapEventTimestamp > 400 ) {
                            lastTapEventTimestamp = eventTime
                            return
                        } else {
                            lastTapEventTimestamp = 0
                            startActivity(Intent(this@PixelMinimalWatchFace, FullBrightnessActivity::class.java).apply {
                                flags = FLAG_ACTIVITY_NEW_TASK
                            })
                            return
                        }
                    }
                    if ( storage.shouldShowPhoneBattery() && phoneBatteryStatus.isStale(System.currentTimeMillis()) && watchFaceDrawer.tapIsOnBattery(x, y)) {
                        startActivity(Intent(this@PixelMinimalWatchFace, PhoneBatteryConfigurationActivity::class.java).apply {
                            flags = FLAG_ACTIVITY_NEW_TASK
                        })
                        return
                    }
                }
            }
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            // Update drawer if needed
            if ( useAndroid12Style != storage.useAndroid12Style() ) {
                useAndroid12Style = storage.useAndroid12Style()
                initWatchFaceDrawer()
            }

            // Update weather subscription if needed
            if( storage.shouldShowWeather() != shouldShowWeather && storage.isUserPremium() ) {
                shouldShowWeather = storage.shouldShowWeather()

                if( shouldShowWeather ) {
                    subscribeToWeatherComplicationData()
                } else {
                    unsubscribeToWeatherComplicationData()
                    weatherComplicationData = null
                }
            }

            // Update battery subscription if needed
            if( storage.shouldShowBattery() != shouldShowBattery && storage.isUserPremium() ) {
                shouldShowBattery = storage.shouldShowBattery()

                if( shouldShowBattery ) {
                    subscribeToBatteryComplicationData()
                } else {
                    unsubscribeToBatteryComplicationData()
                    batteryComplicationData = null
                }
            }

            calendar.timeInMillis = System.currentTimeMillis()

            watchFaceDrawer.draw(
                canvas,
                calendar,
                muteMode,
                ambient,
                lowBitAmbient,
                burnInProtection,
                if( shouldShowWeather ) { weatherComplicationData } else { null },
                if( shouldShowBattery ) { batteryComplicationData } else { null },
                if (storage.shouldShowPhoneBattery()) { phoneBatteryStatus } else { null },
            )

            if( !ambient && isVisible && !timeDependentUpdateHandler.hasUpdateScheduled() ) {
                val nextUpdateDelay = getNextComplicationUpdateDelay()
                if( nextUpdateDelay != null ) {
                    timeDependentUpdateHandler.scheduleUpdate(nextUpdateDelay)
                }
            }
        }

        @Suppress("SameParameterValue")
        private fun getNextComplicationUpdateDelay(): Long? {
            if( storage.shouldShowSecondsRing() ) {
                return 1000
            }

            var minValue = Long.MAX_VALUE

            COMPLICATION_IDS.forEach { complicationId ->
                val timeDependentText = timeDependentTexts.get(complicationId)
                if( timeDependentText != null ) {
                    val nextTime = timeDependentText.getNextChangeTime(calendar.timeInMillis)
                    if( nextTime < Long.MAX_VALUE ) {
                        val updateDelay = max(MINIMUM_COMPLICATION_UPDATE_INTERVAL_MS, calendar.timeInMillis - nextTime)
                        if( updateDelay < minValue ) {
                            minValue = updateDelay
                        }
                    }
                }
            }

            if( minValue == Long.MAX_VALUE ) {
                return null
            }

            return minValue
        }

        fun isAmbientMode(): Boolean = ambient

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (DEBUG_LOGS) Log.d(TAG, "onVisibilityChanged: $visible")

            if (visible) {
                registerReceiver()

                /* Update time zone in case it changed while we weren't visible. */
                calendar.timeZone = TimeZone.getDefault()

                val newComplicationColors = storage.getComplicationColors()
                if( newComplicationColors != complicationsColors ) {
                    complicationsColors = newComplicationColors
                    setComplicationsActiveAndAmbientColors(complicationsColors)
                }

                invalidate()
            } else {
                unregisterReceiver()
            }
        }

        private fun registerReceiver() {
            if (registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            service.registerReceiver(timeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = false
            service.unregisterReceiver(timeZoneReceiver)
        }

        private fun setComplicationsActiveAndAmbientColors(complicationColors: ComplicationColors) {
            watchFaceDrawer.onComplicationColorsUpdate(complicationColors, complicationDataSparseArray)
        }

        override fun onDataChanged(dataEvents: DataEventBuffer) {
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                    if (dataMap.containsKey(DATA_KEY_PREMIUM)) {
                        handleIsPremiumCallback(dataMap.getBoolean(DATA_KEY_PREMIUM))
                    }
                }
            }
        }

        override fun onMessageReceived(messageEvent: MessageEvent) {
            if (messageEvent.path == DATA_KEY_BATTERY_STATUS_PERCENT) {
                try {
                    val phoneBatteryPercentage: Int = messageEvent.data[0].toInt()
                    if (phoneBatteryPercentage in 0..100) {
                        val previousPhoneBatteryStatus = phoneBatteryStatus as? PhoneBatteryStatus.DataReceived
                        phoneBatteryStatus = PhoneBatteryStatus.DataReceived(phoneBatteryPercentage, System.currentTimeMillis())

                        if (storage.shouldShowPhoneBattery() &&
                            (phoneBatteryPercentage != previousPhoneBatteryStatus?.batteryPercentage || previousPhoneBatteryStatus.isStale(System.currentTimeMillis()))) {
                            invalidate()
                        }
                    }
                } catch (t: Throwable) {
                    Log.e("PixelWatchFace", "Error while parsing phone battery percentage from phone", t)
                }
            } else if (messageEvent.path == DATA_KEY_PREMIUM) {
                try {
                    handleIsPremiumCallback(messageEvent.data[0].toInt() == 1)
                } catch (t: Throwable) {
                    Log.e("PixelWatchFace", "Error while parsing premium status from phone", t)
                    Toast.makeText(service, R.string.premium_error, Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun handleIsPremiumCallback(isPremium: Boolean) {
            val wasPremium = storage.isUserPremium()
            storage.setUserPremium(isPremium)

            if( !wasPremium && isPremium ) {
                Toast.makeText(service, R.string.premium_confirmation, Toast.LENGTH_LONG).show()
            }

            invalidate()
        }

        override fun unscheduleDrawable(who: Drawable, what: Runnable) {
            // No-op
        }

        override fun invalidateDrawable(who: Drawable) {
            if( !ambient || storage.shouldShowComplicationsInAmbientMode() ) {
                invalidate()
            }
        }

        override fun scheduleDrawable(who: Drawable, what: Runnable, time: Long) {
            // No-op
        }

        private fun sendRatingNotification() {
            // Create notification channel if needed
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(MISC_NOTIFICATION_CHANNEL_ID, getString(R.string.misc_notification_channel_name), importance)
                mChannel.description = getString(R.string.misc_notification_channel_description)

                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)
            }

            val activityIntent = Intent(service, FeedbackActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                service,
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
            )

            val notification = NotificationCompat.Builder(service, MISC_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.rating_notification_title))
                .setContentText(getString(R.string.rating_notification_message))
                .setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.rating_notification_message)))
                .addAction(NotificationCompat.Action(R.drawable.ic_feedback, getString(R.string.rating_notification_cta), pendingIntent))
                .setAutoCancel(true)
                .build()


            NotificationManagerCompat.from(service).notify(193828, notification)
        }

        private fun syncPhoneBatteryStatus() {
            launch {
                try {
                    val capabilityInfo = withTimeout(5000) {
                        Wearable.getCapabilityClient(service).getCapability(BuildConfig.COMPANION_APP_CAPABILITY, CapabilityClient.FILTER_REACHABLE).await()
                    }

                    if (storage.shouldShowPhoneBattery()) {
                        capabilityInfo.nodes.findBestNode()?.startPhoneBatterySync(this@PixelMinimalWatchFace)
                    } else {
                        capabilityInfo.nodes.findBestNode()?.stopPhoneBatterySync(this@PixelMinimalWatchFace)
                    }

                } catch (t: Throwable) {
                    Log.e("PixelWatchFace", "Error while sending phone battery sync signal", t)
                }
            }
        }
    }

    companion object {
        const val LEFT_COMPLICATION_ID = 100
        const val RIGHT_COMPLICATION_ID = 101
        const val MIDDLE_COMPLICATION_ID = 102
        const val BOTTOM_COMPLICATION_ID = 103
        const val WEATHER_COMPLICATION_ID = 104
        const val BATTERY_COMPLICATION_ID = 105
        const val ANDROID_12_TOP_LEFT_COMPLICATION_ID = 106
        const val ANDROID_12_TOP_RIGHT_COMPLICATION_ID = 107
        const val ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID = 108
        const val ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID = 109

        private val COMPLICATION_IDS = intArrayOf(
            LEFT_COMPLICATION_ID,
            MIDDLE_COMPLICATION_ID,
            RIGHT_COMPLICATION_ID,
            BOTTOM_COMPLICATION_ID,
            ANDROID_12_TOP_LEFT_COMPLICATION_ID,
            ANDROID_12_TOP_RIGHT_COMPLICATION_ID,
            ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID,
            ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID,
        )

        private val normalComplicationDataTypes = intArrayOf(
            ComplicationData.TYPE_SHORT_TEXT,
            ComplicationData.TYPE_ICON,
            ComplicationData.TYPE_RANGED_VALUE,
            ComplicationData.TYPE_SMALL_IMAGE
        )

        private val largeComplicationDataTypes = intArrayOf(
            ComplicationData.TYPE_LONG_TEXT,
            ComplicationData.TYPE_SHORT_TEXT,
            ComplicationData.TYPE_ICON,
            ComplicationData.TYPE_SMALL_IMAGE
        )

        fun getComplicationId(complicationLocation: ComplicationLocation): Int {
            return when (complicationLocation) {
                ComplicationLocation.LEFT -> LEFT_COMPLICATION_ID
                ComplicationLocation.MIDDLE -> MIDDLE_COMPLICATION_ID
                ComplicationLocation.RIGHT -> RIGHT_COMPLICATION_ID
                ComplicationLocation.BOTTOM -> BOTTOM_COMPLICATION_ID
                ComplicationLocation.ANDROID_12_TOP_LEFT -> ANDROID_12_TOP_LEFT_COMPLICATION_ID
                ComplicationLocation.ANDROID_12_TOP_RIGHT -> ANDROID_12_TOP_RIGHT_COMPLICATION_ID
                ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> ANDROID_12_BOTTOM_LEFT_COMPLICATION_ID
                ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> ANDROID_12_BOTTOM_RIGHT_COMPLICATION_ID
            }
        }

        fun getSupportedComplicationTypes(complicationLocation: ComplicationLocation): IntArray {
            return when (complicationLocation) {
                ComplicationLocation.LEFT -> normalComplicationDataTypes
                ComplicationLocation.MIDDLE -> normalComplicationDataTypes
                ComplicationLocation.RIGHT -> normalComplicationDataTypes
                ComplicationLocation.BOTTOM -> largeComplicationDataTypes
                ComplicationLocation.ANDROID_12_TOP_LEFT -> normalComplicationDataTypes
                ComplicationLocation.ANDROID_12_TOP_RIGHT -> normalComplicationDataTypes
                ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> normalComplicationDataTypes
                ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> normalComplicationDataTypes
            }
        }

        fun isActive(context: Context): Boolean {
            val wallpaperManager = WallpaperManager.getInstance(context)
            return wallpaperManager.wallpaperInfo?.packageName == context.packageName
        }
    }
}
