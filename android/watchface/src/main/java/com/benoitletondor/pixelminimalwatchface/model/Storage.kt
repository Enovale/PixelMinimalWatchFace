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
package com.benoitletondor.pixelminimalwatchface.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.ColorInt
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.helper.DEFAULT_TIME_SIZE

const val DEFAULT_APP_VERSION = -1

private const val SHARED_PREFERENCES_NAME = "pixelMinimalSharedPref"

private const val DEFAULT_COMPLICATION_COLOR = -147282
private const val KEY_COMPLICATION_COLORS = "complicationColors"
private const val KEY_LEFT_COMPLICATION_COLOR = "leftComplicationColor"
private const val KEY_MIDDLE_COMPLICATION_COLOR = "middleComplicationColor"
private const val KEY_RIGHT_COMPLICATION_COLOR = "rightComplicationColor"
private const val KEY_BOTTOM_COMPLICATION_COLOR = "bottomComplicationColor"
private const val KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR = "android12TopLeftComplicationColor"
private const val KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR = "android12TopRightComplicationColor"
private const val KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR = "android12BottomLeftComplicationColor"
private const val KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR = "android12BottomRightComplicationColor"
private const val KEY_USER_PREMIUM = "user_premium"
private const val KEY_USE_24H_TIME_FORMAT = "use24hTimeFormat"
private const val KEY_INSTALL_TIMESTAMP = "installTS"
private const val KEY_RATING_NOTIFICATION_SENT = "ratingNotificationSent"
private const val KEY_APP_VERSION = "appVersion"
private const val KEY_SHOW_WEAR_OS_LOGO = "showWearOSLogo"
private const val KEY_SHOW_COMPLICATIONS_AMBIENT = "showComplicationsAmbient"
private const val KEY_FILLED_TIME_AMBIENT = "filledTimeAmbient"
private const val KEY_TIME_SIZE = "timeSize"
private const val KEY_DATE_AND_BATTERY_SIZE = "dateSize"
private const val KEY_SECONDS_RING = "secondsRing"
private const val KEY_SHOW_WEATHER = "showWeather"
private const val KEY_SHOW_BATTERY = "showBattery"
private const val KEY_SHOW_PHONE_BATTERY = "showPhoneBattery"
private const val KEY_FEATURE_DROP_2021_NOTIFICATION = "featureDrop2021Notification_5"
private const val KEY_USE_SHORT_DATE_FORMAT = "useShortDateFormat"
private const val KEY_SHOW_DATE_AMBIENT = "showDateAmbient"
private const val KEY_TIME_AND_DATE_COLOR = "timeAndDateColor"
private const val KEY_BATTERY_COLOR = "batteryColor"
private const val KEY_USE_ANDROID_12_STYLE = "useAndroid12Style"
private const val KEY_HIDE_BATTERY_IN_AMBIENT = "hideBatteryInAmbient"
private const val KEY_SECONDS_RING_COLOR = "secondsRingColor"
private const val KEY_WIDGETS_SIZE = "widgetSize"

interface Storage {
    fun getComplicationColors(): ComplicationColors
    fun setComplicationColors(complicationColors: ComplicationColors)
    fun isUserPremium(): Boolean
    fun setUserPremium(premium: Boolean)
    fun setUse24hTimeFormat(use: Boolean)
    fun getUse24hTimeFormat(): Boolean
    fun getInstallTimestamp(): Long
    fun hasRatingBeenDisplayed(): Boolean
    fun setRatingDisplayed(sent: Boolean)
    fun getAppVersion(): Int
    fun setAppVersion(version: Int)
    fun shouldShowWearOSLogo(): Boolean
    fun setShouldShowWearOSLogo(shouldShowWearOSLogo: Boolean)
    fun shouldShowComplicationsInAmbientMode(): Boolean
    fun setShouldShowComplicationsInAmbientMode(show: Boolean)
    fun shouldShowFilledTimeInAmbientMode(): Boolean
    fun setShouldShowFilledTimeInAmbientMode(showFilledTime: Boolean)
    fun getTimeSize(): Int
    fun setTimeSize(timeSize: Int)
    fun getDateAndBatterySize(): Int
    fun setDateAndBatterySize(size: Int)
    fun shouldShowSecondsRing(): Boolean
    fun setShouldShowSecondsRing(showSecondsRing: Boolean)
    fun shouldShowWeather(): Boolean
    fun setShouldShowWeather(show: Boolean)
    fun shouldShowBattery(): Boolean
    fun setShouldShowBattery(show: Boolean)
    fun hasFeatureDropSummer2021NotificationBeenShown(): Boolean
    fun setFeatureDropSummer2021NotificationShown()
    fun getUseShortDateFormat(): Boolean
    fun setUseShortDateFormat(useShortDateFormat: Boolean)
    fun setShowDateInAmbient(showDateInAmbient: Boolean)
    fun getShowDateInAmbient(): Boolean
    fun shouldShowPhoneBattery(): Boolean
    fun setShouldShowPhoneBattery(show: Boolean)
    @ColorInt fun getTimeAndDateColor(): Int
    fun getTimeAndDateColorFilter(): PorterDuffColorFilter
    fun setTimeAndDateColor(@ColorInt color: Int)
    @ColorInt fun getBatteryIndicatorColor(): Int
    fun getBatteryIndicatorColorFilter(): PorterDuffColorFilter
    fun setBatteryIndicatorColor(@ColorInt color: Int)
    fun useAndroid12Style(): Boolean
    fun setUseAndroid12Style(useAndroid12Style: Boolean)
    fun shouldHideBatteryInAmbient(): Boolean
    fun setShouldHideBatteryInAmbient(hide: Boolean)
    fun getSecondRingColor(): PorterDuffColorFilter
    fun setSecondRingColor(@ColorInt color: Int)
    fun getWidgetsSize(): Int
    fun setWidgetsSize(widgetsSize: Int)
}

class StorageImpl : Storage {
    private var initialized: Boolean = false

    private lateinit var appContext: Context
    private lateinit var sharedPreferences: SharedPreferences

    // Those values will be called up to 60 times a minute when not in ambient mode
    // SharedPreferences uses a map so we cache the values to avoid map lookups
    private var timeSizeCached = false
    private var cacheTimeSize = 0
    private var dateAndBatterySizeCached = false
    private var cacheDateAndBatterySize = 0
    private var isUserPremiumCached = false
    private var cacheIsUserPremium = false
    private var isUse24hFormatCached = false
    private var cacheUse24hFormat = false
    private var shouldShowWearOSLogoCached = false
    private var cacheShouldShowWearOSLogo = false
    private var shouldShowComplicationsInAmbientModeCached = false
    private var cacheShouldShowComplicationsInAmbientMode = false
    private var shouldShowSecondsSettingCached = false
    private var cacheShouldShowSecondsSetting = false
    private var shouldShowWeatherCached = false
    private var cacheShouldShowWeather = false
    private var shouldShowBatteryCached = false
    private var cacheShouldShowBattery = false
    private var useShortDateFormatCached = false
    private var cacheUseShortDateFormat = false
    private var showDateAmbientCached = false
    private var cacheShowDateAmbient = false
    private var cacheComplicationsColor: ComplicationColors? = null
    private var shouldShowPhoneBatteryCached = false
    private var cacheShouldShowPhoneBattery = false
    private var timeAndDateColorCached = false
    private var cacheTimeAndDatePorterDuffColorFilter = PorterDuffColorFilter(0, PorterDuff.Mode.SRC_IN)
    private var cacheTimeAndDateColor = 0
    private var batteryIndicatorColorCached = false
    private var cacheBatteryPorterDuffColorFilter = PorterDuffColorFilter(0, PorterDuff.Mode.SRC_IN)
    private var cacheBatteryIndicatorColor = 0
    private var cacheUseAndroid12Style = false
    private var useAndroid12StyleCached = false
    private var cacheHideBatteryInAmbient = false
    private var hideBatteryInAmbientCached = false
    private var secondRingColorCached = false
    private var cacheSecondRingColor = 0
    private var cacheSecondRingPorterDuffColorFilter = PorterDuffColorFilter(0, PorterDuff.Mode.SRC_IN)
    private var widgetsSizeCached = false
    private var cacheWidgetsSize = 0

    fun init(context: Context): Storage {
        if( !initialized ) {
            appContext = context.applicationContext
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

            if( getInstallTimestamp() < 0 ) {
                sharedPreferences.edit().putLong(KEY_INSTALL_TIMESTAMP, System.currentTimeMillis()).apply()
            }

            initialized = true
        }

        return this
    }

    override fun getComplicationColors(): ComplicationColors {
        val cacheComplicationsColor = cacheComplicationsColor
        if( cacheComplicationsColor != null ) {
            return cacheComplicationsColor
        }

        val baseColor = sharedPreferences.getInt(
            KEY_COMPLICATION_COLORS,
            DEFAULT_COMPLICATION_COLOR
        )

        val leftColor = sharedPreferences.getInt(
            KEY_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val middleColor = sharedPreferences.getInt(
            KEY_MIDDLE_COMPLICATION_COLOR,
            baseColor
        )

        val rightColor = sharedPreferences.getInt(
            KEY_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val bottomColor = sharedPreferences.getInt(
            KEY_BOTTOM_COMPLICATION_COLOR,
            baseColor
        )

        val android12TopLeftColor = sharedPreferences.getInt(
            KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val android12TopRightColor = sharedPreferences.getInt(
            KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val android12BottomLeftColor = sharedPreferences.getInt(
            KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR,
            baseColor
        )

        val android12BottomRightColor = sharedPreferences.getInt(
            KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR,
            baseColor
        )

        val defaultColors = ComplicationColorsProvider.getDefaultComplicationColors(appContext)

        val colors = ComplicationColors(
            if( leftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.leftColor } else { ComplicationColor(leftColor, ComplicationColorsProvider.getLabelForColor(appContext, leftColor),false) },
            if( middleColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.middleColor } else { ComplicationColor(middleColor, ComplicationColorsProvider.getLabelForColor(appContext, middleColor),false) },
            if( rightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.rightColor } else { ComplicationColor(rightColor, ComplicationColorsProvider.getLabelForColor(appContext, rightColor),false) },
            if( bottomColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.bottomColor } else { ComplicationColor(bottomColor, ComplicationColorsProvider.getLabelForColor(appContext, bottomColor),false) },
            if( android12TopLeftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12TopLeftColor } else { ComplicationColor(android12TopLeftColor, ComplicationColorsProvider.getLabelForColor(appContext, android12TopLeftColor),false) },
            if( android12TopRightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12TopRightColor } else { ComplicationColor(android12TopRightColor, ComplicationColorsProvider.getLabelForColor(appContext, android12TopRightColor),false) },
            if( android12BottomLeftColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12BottomLeftColor } else { ComplicationColor(android12BottomLeftColor, ComplicationColorsProvider.getLabelForColor(appContext, android12BottomLeftColor),false) },
            if( android12BottomRightColor == DEFAULT_COMPLICATION_COLOR ) { defaultColors.android12BottomRightColor } else { ComplicationColor(android12BottomRightColor, ComplicationColorsProvider.getLabelForColor(appContext, android12BottomRightColor),false) },
        )

        this.cacheComplicationsColor = colors
        return colors
    }

    override fun setComplicationColors(complicationColors: ComplicationColors) {
        cacheComplicationsColor = complicationColors
        sharedPreferences.edit()
            .putInt(
                KEY_LEFT_COMPLICATION_COLOR,
                if( complicationColors.leftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.leftColor.color }
            )
            .putInt(
                KEY_MIDDLE_COMPLICATION_COLOR,
                if( complicationColors.middleColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.middleColor.color }
            )
            .putInt(
                KEY_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.rightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.rightColor.color }
            )
            .putInt(
                KEY_BOTTOM_COMPLICATION_COLOR,
                if( complicationColors.bottomColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.bottomColor.color }
            )
            .putInt(
                KEY_ANDROID_12_BOTTOM_LEFT_COMPLICATION_COLOR,
                if( complicationColors.android12BottomLeftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12BottomLeftColor.color }
            )
            .putInt(
                KEY_ANDROID_12_TOP_LEFT_COMPLICATION_COLOR,
                if( complicationColors.android12TopLeftColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12TopLeftColor.color }
            )
            .putInt(
                KEY_ANDROID_12_TOP_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.android12TopRightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12TopRightColor.color }
            )
            .putInt(
                KEY_ANDROID_12_BOTTOM_RIGHT_COMPLICATION_COLOR,
                if( complicationColors.android12BottomRightColor.isDefault ) {
                    DEFAULT_COMPLICATION_COLOR
                } else { complicationColors.android12BottomRightColor.color }
            )
            .apply()
    }

    override fun isUserPremium(): Boolean {
        if( !isUserPremiumCached ) {
            cacheIsUserPremium = sharedPreferences.getBoolean(KEY_USER_PREMIUM, false)
            isUserPremiumCached = true
        }

        return cacheIsUserPremium
    }

    override fun setUserPremium(premium: Boolean) {
        cacheIsUserPremium = premium
        isUserPremiumCached = true

        sharedPreferences.edit().putBoolean(KEY_USER_PREMIUM, premium).apply()
    }

    override fun setUse24hTimeFormat(use: Boolean) {
        cacheUse24hFormat = use
        isUse24hFormatCached = true

        sharedPreferences.edit().putBoolean(KEY_USE_24H_TIME_FORMAT, use).apply()
    }

    override fun getUse24hTimeFormat(): Boolean {
        if( !isUse24hFormatCached ) {
            cacheUse24hFormat = sharedPreferences.getBoolean(KEY_USE_24H_TIME_FORMAT, true)
            isUse24hFormatCached = true
        }

        return cacheUse24hFormat
    }

    override fun getInstallTimestamp(): Long {
        return sharedPreferences.getLong(KEY_INSTALL_TIMESTAMP, -1)
    }

    override fun hasRatingBeenDisplayed(): Boolean {
        return sharedPreferences.getBoolean(KEY_RATING_NOTIFICATION_SENT, false)
    }

    override fun setRatingDisplayed(sent: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_RATING_NOTIFICATION_SENT, sent).apply()
    }

    override fun getAppVersion(): Int {
        return sharedPreferences.getInt(KEY_APP_VERSION, DEFAULT_APP_VERSION)
    }

    override fun setAppVersion(version: Int) {
        sharedPreferences.edit().putInt(KEY_APP_VERSION, version).apply()
    }

    override fun shouldShowWearOSLogo(): Boolean {
        if( !shouldShowWearOSLogoCached ) {
            cacheShouldShowWearOSLogo = sharedPreferences.getBoolean(KEY_SHOW_WEAR_OS_LOGO, true)
            shouldShowWearOSLogoCached = true
        }

        return cacheShouldShowWearOSLogo
    }

    override fun setShouldShowWearOSLogo(shouldShowWearOSLogo: Boolean) {
        cacheShouldShowWearOSLogo = shouldShowWearOSLogo
        shouldShowWearOSLogoCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_WEAR_OS_LOGO, shouldShowWearOSLogo).apply()
    }

    override fun shouldShowComplicationsInAmbientMode(): Boolean {
        if( !shouldShowComplicationsInAmbientModeCached ) {
            cacheShouldShowComplicationsInAmbientMode = sharedPreferences.getBoolean(KEY_SHOW_COMPLICATIONS_AMBIENT, false)
            shouldShowComplicationsInAmbientModeCached = true
        }

        return cacheShouldShowComplicationsInAmbientMode
    }

    override fun setShouldShowComplicationsInAmbientMode(show: Boolean) {
        cacheShouldShowComplicationsInAmbientMode = show
        shouldShowComplicationsInAmbientModeCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_COMPLICATIONS_AMBIENT, show).apply()
    }

    override fun shouldShowFilledTimeInAmbientMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_FILLED_TIME_AMBIENT, false)
    }

    override fun setShouldShowFilledTimeInAmbientMode(showFilledTime: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FILLED_TIME_AMBIENT, showFilledTime).apply()
    }

    override fun getTimeSize(): Int {
        if( !timeSizeCached ) {
            cacheTimeSize = sharedPreferences.getInt(KEY_TIME_SIZE, DEFAULT_TIME_SIZE)
            timeSizeCached = true
        }

        return cacheTimeSize
    }

    override fun setTimeSize(timeSize: Int) {
        cacheTimeSize = timeSize
        timeSizeCached = true

        sharedPreferences.edit().putInt(KEY_TIME_SIZE, timeSize).apply()
    }

    override fun getDateAndBatterySize(): Int {
        if( !dateAndBatterySizeCached ) {
            cacheDateAndBatterySize = sharedPreferences.getInt(KEY_DATE_AND_BATTERY_SIZE, getTimeSize())
            dateAndBatterySizeCached = true
        }

        return cacheDateAndBatterySize
    }

    override fun setDateAndBatterySize(size: Int) {
        cacheDateAndBatterySize = size
        dateAndBatterySizeCached = true

        sharedPreferences.edit().putInt(KEY_DATE_AND_BATTERY_SIZE, size).apply()
    }

    override fun shouldShowSecondsRing(): Boolean {
        if( !shouldShowSecondsSettingCached ) {
            cacheShouldShowSecondsSetting = sharedPreferences.getBoolean(KEY_SECONDS_RING, false)
            shouldShowSecondsSettingCached = true
        }

        return cacheShouldShowSecondsSetting
    }

    override fun setShouldShowSecondsRing(showSecondsRing: Boolean) {
        cacheShouldShowSecondsSetting = showSecondsRing
        shouldShowSecondsSettingCached = true

        sharedPreferences.edit().putBoolean(KEY_SECONDS_RING, showSecondsRing).apply()
    }

    override fun shouldShowWeather(): Boolean {
        if( !shouldShowWeatherCached ) {
            cacheShouldShowWeather = sharedPreferences.getBoolean(KEY_SHOW_WEATHER, false)
            shouldShowWeatherCached = true
        }

        return cacheShouldShowWeather
    }

    override fun setShouldShowWeather(show: Boolean) {
        cacheShouldShowWeather = show
        shouldShowWeatherCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_WEATHER, show).apply()
    }

    override fun shouldShowBattery(): Boolean {
        if( !shouldShowBatteryCached ) {
            cacheShouldShowBattery = sharedPreferences.getBoolean(KEY_SHOW_BATTERY, false)
            shouldShowBatteryCached = true
        }

        return cacheShouldShowBattery
    }

    override fun setShouldShowBattery(show: Boolean) {
        cacheShouldShowBattery = show
        shouldShowBatteryCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()
    }

    override fun shouldShowPhoneBattery(): Boolean {
        if( !shouldShowPhoneBatteryCached ) {
            cacheShouldShowPhoneBattery = sharedPreferences.getBoolean(KEY_SHOW_PHONE_BATTERY, false)
            shouldShowPhoneBatteryCached = true
        }

        return cacheShouldShowPhoneBattery
    }

    override fun setShouldShowPhoneBattery(show: Boolean) {
        cacheShouldShowPhoneBattery = show
        shouldShowPhoneBatteryCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_PHONE_BATTERY, show).apply()
    }

    override fun getTimeAndDateColor(): Int {
        if( !timeAndDateColorCached ) {
            cacheTimeAndDateColor = sharedPreferences.getInt(KEY_TIME_AND_DATE_COLOR, appContext.getColor(R.color.white))
            cacheTimeAndDatePorterDuffColorFilter = PorterDuffColorFilter(cacheTimeAndDateColor, PorterDuff.Mode.SRC_IN)
            timeAndDateColorCached = true
        }

        return cacheTimeAndDateColor
    }

    override fun getTimeAndDateColorFilter(): PorterDuffColorFilter {
        if( !timeAndDateColorCached ) {
            cacheTimeAndDateColor = sharedPreferences.getInt(KEY_TIME_AND_DATE_COLOR, appContext.getColor(R.color.white))
            cacheTimeAndDatePorterDuffColorFilter = PorterDuffColorFilter(cacheTimeAndDateColor, PorterDuff.Mode.SRC_IN)
            timeAndDateColorCached = true
        }

        return cacheTimeAndDatePorterDuffColorFilter
    }

    override fun setTimeAndDateColor(color: Int) {
        cacheTimeAndDateColor = color
        cacheTimeAndDatePorterDuffColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        timeAndDateColorCached = true

        sharedPreferences.edit().putInt(KEY_TIME_AND_DATE_COLOR, color).apply()
    }

    override fun getBatteryIndicatorColor(): Int {
        if( !batteryIndicatorColorCached ) {
            cacheBatteryIndicatorColor = sharedPreferences.getInt(KEY_BATTERY_COLOR, appContext.getColor(R.color.white))
            cacheBatteryPorterDuffColorFilter = PorterDuffColorFilter(cacheBatteryIndicatorColor, PorterDuff.Mode.SRC_IN)
            batteryIndicatorColorCached = true
        }

        return cacheBatteryIndicatorColor
    }

    override fun getBatteryIndicatorColorFilter(): PorterDuffColorFilter {
        if( !batteryIndicatorColorCached ) {
            cacheBatteryIndicatorColor = sharedPreferences.getInt(KEY_BATTERY_COLOR, appContext.getColor(R.color.white))
            cacheBatteryPorterDuffColorFilter = PorterDuffColorFilter(cacheBatteryIndicatorColor, PorterDuff.Mode.SRC_IN)
            batteryIndicatorColorCached = true
        }

        return cacheBatteryPorterDuffColorFilter
    }

    override fun setBatteryIndicatorColor(color: Int) {
        cacheBatteryIndicatorColor = color
        cacheBatteryPorterDuffColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        batteryIndicatorColorCached = true

        sharedPreferences.edit().putInt(KEY_BATTERY_COLOR, color).apply()
    }

    override fun useAndroid12Style(): Boolean {
        if( !useAndroid12StyleCached ) {
            cacheUseAndroid12Style = sharedPreferences.getBoolean(KEY_USE_ANDROID_12_STYLE, false)
            useAndroid12StyleCached = true
        }

        return cacheUseAndroid12Style
    }

    override fun setUseAndroid12Style(useAndroid12Style: Boolean) {
        cacheUseAndroid12Style = useAndroid12Style
        useAndroid12StyleCached = true

        sharedPreferences.edit().putBoolean(KEY_USE_ANDROID_12_STYLE, useAndroid12Style).apply()
    }

    override fun shouldHideBatteryInAmbient(): Boolean {
        if( !hideBatteryInAmbientCached ) {
            cacheHideBatteryInAmbient = sharedPreferences.getBoolean(KEY_HIDE_BATTERY_IN_AMBIENT, false)
            hideBatteryInAmbientCached = true
        }

        return cacheHideBatteryInAmbient
    }

    override fun setShouldHideBatteryInAmbient(hide: Boolean) {
        cacheHideBatteryInAmbient = hide
        hideBatteryInAmbientCached = true

        sharedPreferences.edit().putBoolean(KEY_HIDE_BATTERY_IN_AMBIENT, hide).apply()
    }

    override fun getSecondRingColor(): PorterDuffColorFilter {
        if( !secondRingColorCached ) {
            cacheSecondRingColor = sharedPreferences.getInt(KEY_SECONDS_RING_COLOR, appContext.getColor(R.color.white))
            cacheSecondRingPorterDuffColorFilter = PorterDuffColorFilter(cacheSecondRingColor, PorterDuff.Mode.SRC_IN)
            secondRingColorCached = true
        }

        return cacheSecondRingPorterDuffColorFilter
    }

    override fun setSecondRingColor(@ColorInt color: Int) {
        cacheSecondRingColor = color
        cacheSecondRingPorterDuffColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        secondRingColorCached = true

        sharedPreferences.edit().putInt(KEY_SECONDS_RING_COLOR, color).apply()
    }

    override fun getWidgetsSize(): Int {
        if( !widgetsSizeCached ) {
            cacheWidgetsSize = sharedPreferences.getInt(KEY_WIDGETS_SIZE, DEFAULT_TIME_SIZE)
            widgetsSizeCached = true
        }

        return cacheWidgetsSize
    }

    override fun setWidgetsSize(widgetsSize: Int) {
        cacheWidgetsSize = widgetsSize
        widgetsSizeCached = true

        sharedPreferences.edit().putInt(KEY_WIDGETS_SIZE, widgetsSize).apply()
    }

    override fun hasFeatureDropSummer2021NotificationBeenShown(): Boolean {
        return sharedPreferences.getBoolean(KEY_FEATURE_DROP_2021_NOTIFICATION, false)
    }

    override fun setFeatureDropSummer2021NotificationShown() {
        sharedPreferences.edit().putBoolean(KEY_FEATURE_DROP_2021_NOTIFICATION, true).apply()
    }

    override fun getUseShortDateFormat(): Boolean {
        if( !useShortDateFormatCached ) {
            cacheUseShortDateFormat = sharedPreferences.getBoolean(KEY_USE_SHORT_DATE_FORMAT, false)
            useShortDateFormatCached = true
        }

        return cacheUseShortDateFormat
    }

    override fun setUseShortDateFormat(useShortDateFormat: Boolean) {
        cacheUseShortDateFormat = useShortDateFormat
        useShortDateFormatCached = true

        sharedPreferences.edit().putBoolean(KEY_USE_SHORT_DATE_FORMAT, useShortDateFormat).apply()
    }

    override fun setShowDateInAmbient(showDateInAmbient: Boolean) {
        cacheShowDateAmbient = showDateInAmbient
        showDateAmbientCached = true

        sharedPreferences.edit().putBoolean(KEY_SHOW_DATE_AMBIENT, showDateInAmbient).apply()
    }

    override fun getShowDateInAmbient(): Boolean {
        if( !showDateAmbientCached ) {
            cacheShowDateAmbient = sharedPreferences.getBoolean(KEY_SHOW_DATE_AMBIENT, true)
            showDateAmbientCached = true
        }

        return cacheShowDateAmbient
    }
}
