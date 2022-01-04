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
package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.drawer.digital.android12.Android12DigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.drawer.digital.regular.RegularDigitalWatchFaceDrawer
import com.benoitletondor.pixelminimalwatchface.helper.isPermissionGranted
import com.benoitletondor.pixelminimalwatchface.helper.isScreenRound
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.settings.SettingsActivity.Companion.COMPLICATION_CONFIG_REQUEST_CODE
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

private const val TYPE_HEADER = 0
private const val TYPE_REGULAR_COMPLICATIONS_CONFIG = 1
private const val TYPE_FOOTER = 3
private const val TYPE_BECOME_PREMIUM = 4
private const val TYPE_HOUR_FORMAT = 5
private const val TYPE_SEND_FEEDBACK = 6
private const val TYPE_SHOW_WEAR_OS_LOGO = 7
private const val TYPE_SHOW_COMPLICATIONS_AMBIENT = 8
private const val TYPE_USE_NORMAL_TIME_STYLE_IN_AMBIENT_MODE = 9
private const val TYPE_TIME_SIZE = 10
private const val TYPE_SHOW_SECONDS_RING = 11
private const val TYPE_SHOW_WEATHER = 12
private const val TYPE_SHOW_BATTERY = 13
private const val TYPE_DATE_FORMAT = 14
private const val TYPE_SHOW_DATE_AMBIENT = 15
private const val TYPE_DONATE = 16
private const val TYPE_SHOW_PHONE_BATTERY = 17
private const val TYPE_SECTION_BATTERY = 18
private const val TYPE_SECTION_DATE_AND_TIME = 19
private const val TYPE_SECTION_AMBIENT = 20
private const val TYPE_SECTION_SUPPORT = 21
private const val TYPE_SECTION_WIDGETS = 22
private const val TYPE_BATTERY_SECTION_HEADER = 23
private const val TYPE_TIME_AND_DATE_COLOR = 24
private const val TYPE_BATTERY_INDICATOR_COLOR = 25
private const val TYPE_ANDROID_12_STYLE = 26
private const val TYPE_DATE_AND_BATTERY_SIZE = 27
private const val TYPE_ANDROID_12_COMPLICATIONS_CONFIG = 28
private const val TYPE_SHOW_BATTERY_IN_AMBIENT = 29
private const val TYPE_SECONDS_RING_COLOR = 30
private const val TYPE_WIDGETS_SIZE = 31
private const val TYPE_USE_THIN_TIME_STYLE_IN_REGULAR_MODE = 32
private const val TYPE_SECTION_TIME_STYLE = 33

class ComplicationConfigRecyclerViewAdapter(
    private val context: Context,
    private val storage: Storage,
    private val premiumClickListener: () -> Unit,
    private val hourFormatSelectionListener: (Boolean) -> Unit,
    private val onFeedbackButtonPressed: () -> Unit,
    private val showWearOSButtonListener: (Boolean) -> Unit,
    private val showComplicationsAmbientListener: (Boolean) -> Unit,
    private val useNormalTimeStyleInAmbientModeListener: (Boolean) -> Unit,
    private val useThinTimeStyleInRegularModeListener: (Boolean) -> Unit,
    private val timeSizeChangedListener: (Int) -> Unit,
    private val dateAndBatterySizeChangedListener: (Int) -> Unit,
    private val showSecondsRingListener: (Boolean) -> Unit,
    private val showWeatherListener: (Boolean) -> Unit,
    private val openWeatherAppListener: () -> Unit,
    private val showBatteryListener: (Boolean) -> Unit,
    private val showBatteryInAmbientListener: (Boolean) -> Unit,
    private val dateFormatSelectionListener: (Boolean) -> Unit,
    private val showDateAmbientListener: (Boolean) -> Unit,
    private val donateButtonPressed: () -> Unit,
    private val phoneBatteryButtonPressed: () -> Unit,
    private val changeTimeAndDateColorButtonPressed: () -> Unit,
    private val changeBatteryIndicatorColorButtonPressed: () -> Unit,
    private val useAndroid12StyleCheckedListener: (Boolean) -> Unit,
    private val changeSecondsRingColorButtonPressed: () -> Unit,
    private val widgetsSizeChangedListener: (Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedComplicationLocation: ComplicationLocation? = null

    private val watchFaceComponentName = ComponentName(context, PixelMinimalWatchFace::class.java)
    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())
    private var regularComplicationsViewHolder: RegularComplicationsViewHolder? = null
    private var android12ComplicationsViewHolder: Android12ComplicationsViewHolder? = null
    private var showWeatherViewHolder: ShowWeatherViewHolder? = null
    private var showBatteryViewHolder: ShowBatteryViewHolder? = null
    private var settings: List<Int> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_header,
                    parent,
                    false
                )
            )
            TYPE_REGULAR_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder =
                    RegularComplicationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.config_list_regular_complications_item, parent, false)) { location ->
                        selectedComplicationLocation = location

                        (context as Activity).startActivityForResult(
                            WidgetConfigurationActivity.createIntent(context, location),
                            COMPLICATION_CONFIG_REQUEST_CODE,
                        )
                    }

                this.regularComplicationsViewHolder = previewAndComplicationsViewHolder
                return previewAndComplicationsViewHolder
            }
            TYPE_ANDROID_12_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder =
                    Android12ComplicationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.config_list_android12_complications_item, parent, false)) { location ->
                        selectedComplicationLocation = location

                        (context as Activity).startActivityForResult(
                            WidgetConfigurationActivity.createIntent(context, location),
                            COMPLICATION_CONFIG_REQUEST_CODE,
                        )
                    }

                this.android12ComplicationsViewHolder = previewAndComplicationsViewHolder
                return previewAndComplicationsViewHolder
            }
            TYPE_FOOTER -> return FooterViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_footer,
                    parent,
                    false
                )
            )
            TYPE_BECOME_PREMIUM -> return PremiumViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_premium,
                    parent,
                    false
                ),
                premiumClickListener
            )
            TYPE_HOUR_FORMAT -> return HourFormatViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_hour_format,
                    parent,
                    false
                ),
                hourFormatSelectionListener
            )
            TYPE_USE_THIN_TIME_STYLE_IN_REGULAR_MODE -> return UseThinTimeStyleInRegularModeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_thin_time_regular,
                    parent,
                    false
                ),
                useThinTimeStyleInRegularModeListener
            )
            TYPE_SEND_FEEDBACK -> return SendFeedbackViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_feedback,
                    parent,
                    false
                ),
                onFeedbackButtonPressed
            )
            TYPE_SHOW_WEAR_OS_LOGO -> return ShowWearOSLogoViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_wearos_logo,
                    parent,
                    false
                )) { showWearOSLogo ->
                    showWearOSButtonListener(showWearOSLogo)
                    regularComplicationsViewHolder?.showMiddleComplication(!showWearOSLogo)
                }
            TYPE_SHOW_COMPLICATIONS_AMBIENT -> return ShowComplicationsAmbientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_complications_ambient,
                    parent,
                    false
                ),
                showComplicationsAmbientListener
            )
            TYPE_USE_NORMAL_TIME_STYLE_IN_AMBIENT_MODE -> return UseNormalTimeStyleInAmbientModeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_filled_time_ambient,
                    parent,
                    false
                ),
                useNormalTimeStyleInAmbientModeListener
            )
            TYPE_TIME_SIZE -> return TimeSizeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_time_size,
                    parent,
                    false
                ),
                timeSizeChangedListener
            )
            TYPE_DATE_AND_BATTERY_SIZE -> return DateAndBatterySizeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_date_size,
                    parent,
                    false
                ),
                dateAndBatterySizeChangedListener
            )
            TYPE_SHOW_SECONDS_RING -> return ShowSecondsRingViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_seconds_ring,
                    parent,
                    false
                )) { showSecondsRing ->
                    if (showSecondsRing != storage.shouldShowSecondsRing()) {
                        showSecondsRingListener(showSecondsRing)
                        notifyDataSetChanged()
                    }
                }
            TYPE_SHOW_WEATHER -> {
                val showWeatherViewHolder = ShowWeatherViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.config_list_show_weather,
                        parent,
                        false
                    ),
                    { showWeather ->
                        if( showWeather ) {
                            (context as Activity).startActivityForResult(
                                ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                    context,
                                    watchFaceComponentName
                                ),
                                SettingsActivity.COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE
                            )
                        } else {
                            showWeatherListener(false)
                            updateWeatherButton()
                        }
                    },
                    openWeatherAppListener,
                )
                this.showWeatherViewHolder = showWeatherViewHolder
                return showWeatherViewHolder
            }
            TYPE_SHOW_BATTERY -> {
                val showBatteryViewHolder = ShowBatteryViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.config_list_show_battery,
                        parent,
                        false
                    )
                ) { showBattery ->
                    if( showBattery ) {
                        (context as Activity).startActivityForResult(
                            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                context,
                                watchFaceComponentName
                            ),
                            SettingsActivity.COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        showBatteryListener(false)
                        regularComplicationsViewHolder?.showBottomComplication(!storage.shouldShowPhoneBattery())
                        notifyDataSetChanged()
                    }
                }
                this.showBatteryViewHolder = showBatteryViewHolder
                return showBatteryViewHolder
            }
            TYPE_DATE_FORMAT -> return DateFormatViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_date_format,
                    parent,
                    false
                ),
                dateFormatSelectionListener
            )
            TYPE_SHOW_DATE_AMBIENT -> return ShowDateAmbientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_date_ambient,
                    parent,
                    false
                ),
                showDateAmbientListener
            )
            TYPE_DONATE -> return DonateViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_donate,
                    parent,
                    false
                ),
                donateButtonPressed
            )
            TYPE_SHOW_PHONE_BATTERY -> return PhoneBatteryViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_phone_battery,
                    parent,
                    false,
                ),
                phoneBatteryButtonPressed,
            )
            TYPE_SECTION_BATTERY, TYPE_SECTION_AMBIENT, TYPE_SECTION_DATE_AND_TIME, TYPE_SECTION_SUPPORT, TYPE_SECTION_WIDGETS, TYPE_SECTION_TIME_STYLE -> return SectionViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_section,
                    parent,
                    false,
                )
            )
            TYPE_BATTERY_SECTION_HEADER -> return BatterySectionHeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_battery_header,
                    parent,
                    false,
                )
            )
            TYPE_TIME_AND_DATE_COLOR -> return TimeAndDateColorViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_time_and_date_color,
                    parent,
                    false,
                ),
                changeTimeAndDateColorButtonPressed
            )
            TYPE_BATTERY_INDICATOR_COLOR -> return BatteryIndicatorColorViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_battery_color,
                    parent,
                    false,
                ),
                changeBatteryIndicatorColorButtonPressed
            )
            TYPE_ANDROID_12_STYLE -> return UseAndroid12StyleViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_use_android_12_style,
                    parent,
                    false,
                )
            ) { useAndroid12Style ->
                if (useAndroid12Style != storage.useAndroid12Style()) {
                    notifyDataSetChanged()
                    useAndroid12StyleCheckedListener(useAndroid12Style)
                }
            }
            TYPE_SHOW_BATTERY_IN_AMBIENT -> return ShowBatteryInAmbientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_battery_in_ambient,
                    parent,
                    false,
                ),
                showBatteryInAmbientListener,
            )
            TYPE_SECONDS_RING_COLOR -> return SecondsRingColorViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_second_ring_color,
                    parent,
                    false,
                ),
                changeSecondsRingColorButtonPressed
            )
            TYPE_WIDGETS_SIZE -> return WidgetsSizeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_widgets_size,
                    parent,
                    false
                ),
                widgetsSizeChangedListener
            )
        }
        throw IllegalStateException("Unknown option type: $viewType")
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            TYPE_REGULAR_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder = viewHolder as RegularComplicationsViewHolder

                if( !previewAndComplicationsViewHolder.bound ) {
                    previewAndComplicationsViewHolder.bound = true
                    updateRegularComplications()
                }
            }
            TYPE_ANDROID_12_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder = viewHolder as Android12ComplicationsViewHolder

                if( !previewAndComplicationsViewHolder.bound ) {
                    previewAndComplicationsViewHolder.bound = true
                    updateAndroid12Complications()
                }
            }
            TYPE_HOUR_FORMAT -> {
                val use24hTimeFormat = storage.getUse24hTimeFormat()
                (viewHolder as HourFormatViewHolder).setHourFormatSwitchChecked(use24hTimeFormat)
            }
            TYPE_USE_THIN_TIME_STYLE_IN_REGULAR_MODE -> {
                val useThinTimeInRegular = storage.shouldUseThinTimeStyleInRegularMode()
                (viewHolder as UseThinTimeStyleInRegularModeViewHolder).setUseThinTimeInRegularSwitchChecked(useThinTimeInRegular)
            }
            TYPE_SHOW_WEAR_OS_LOGO -> {
                (viewHolder as ShowWearOSLogoViewHolder).apply {
                    setShowWearOSLogoSwitchChecked(storage.shouldShowWearOSLogo())
                    setPremiumTitle(storage.isUserPremium(), storage.useAndroid12Style())
                }
            }
            TYPE_SHOW_COMPLICATIONS_AMBIENT -> {
                val showComplicationsAmbient = storage.shouldShowComplicationsInAmbientMode()
                (viewHolder as ShowComplicationsAmbientViewHolder).setShowComplicationsAmbientSwitchChecked(showComplicationsAmbient)
            }
            TYPE_USE_NORMAL_TIME_STYLE_IN_AMBIENT_MODE -> {
                val useNormalTimeStyleInAmbientMode = storage.shouldUseNormalTimeStyleInAmbientMode()
                (viewHolder as UseNormalTimeStyleInAmbientModeViewHolder).setUseNormalTimeStyleInAmbientSwitchChecked(useNormalTimeStyleInAmbientMode)
            }
            TYPE_TIME_SIZE -> {
                val size = storage.getTimeSize()
                (viewHolder as TimeSizeViewHolder).setTimeSize(size)
            }
            TYPE_DATE_AND_BATTERY_SIZE -> {
                val size = storage.getDateAndBatterySize()
                (viewHolder as DateAndBatterySizeViewHolder).setDateAndBatterySize(size)
            }
            TYPE_SHOW_SECONDS_RING -> {
                val showSeconds = storage.shouldShowSecondsRing()
                (viewHolder as ShowSecondsRingViewHolder).setShowSecondsRingSwitchChecked(showSeconds)
            }
            TYPE_SHOW_WEATHER -> {
                val showWeather = storage.shouldShowWeather()
                (viewHolder as ShowWeatherViewHolder).setShowWeatherViewSwitchChecked(showWeather)
            }
            TYPE_SHOW_BATTERY -> {
                val showBattery = storage.shouldShowBattery()
                (viewHolder as ShowBatteryViewHolder).setShowBatteryViewSwitchChecked(showBattery)
            }
            TYPE_DATE_FORMAT -> {
                val useShortDateFormat = storage.getUseShortDateFormat()
                (viewHolder as DateFormatViewHolder).setDateFormatSwitchChecked(useShortDateFormat)
            }
            TYPE_SHOW_DATE_AMBIENT -> {
                val showDateInAmbient = storage.getShowDateInAmbient()
                (viewHolder as ShowDateAmbientViewHolder).setShowDateAmbientSwitchChecked(showDateInAmbient)
            }
            TYPE_ANDROID_12_STYLE -> {
                val useAndroid12Style = storage.useAndroid12Style()
                (viewHolder as UseAndroid12StyleViewHolder).setUseAndroid12StyleSwitchChecked(useAndroid12Style)
            }
            TYPE_SECTION_BATTERY, TYPE_SECTION_AMBIENT, TYPE_SECTION_DATE_AND_TIME, TYPE_SECTION_SUPPORT, TYPE_SECTION_WIDGETS, TYPE_SECTION_TIME_STYLE -> {
                val text = viewHolder.itemView.context.getString(when(viewHolder.itemViewType) {
                    TYPE_SECTION_SUPPORT -> R.string.config_section_support
                    TYPE_SECTION_DATE_AND_TIME -> R.string.config_section_date_and_time
                    TYPE_SECTION_BATTERY -> R.string.config_section_battery
                    TYPE_SECTION_AMBIENT -> R.string.config_section_ambient
                    TYPE_SECTION_WIDGETS -> R.string.config_section_widgets
                    TYPE_SECTION_TIME_STYLE -> R.string.config_section_time_style
                    else -> throw IllegalStateException("Unknown section type: ${viewHolder.itemViewType}")
                })

                (viewHolder as SectionViewHolder).setSectionText(text)
            }
            TYPE_SHOW_BATTERY_IN_AMBIENT -> {
                val showBatteryInAmbient = !storage.shouldHideBatteryInAmbient()
                (viewHolder as ShowBatteryInAmbientViewHolder).setShowBatteryInAmbientSwitchChecked(showBatteryInAmbient)
            }
            TYPE_WIDGETS_SIZE -> {
                val size = storage.getWidgetsSize()
                (viewHolder as WidgetsSizeViewHolder).setWidgetsSize(size)
            }
        }
    }

    fun updateRegularComplications() {
        regularComplicationsViewHolder?.bound = true
        
        regularComplicationsViewHolder?.setDefaultComplicationDrawable()
        regularComplicationsViewHolder?.showMiddleComplication(!storage.shouldShowWearOSLogo())
        regularComplicationsViewHolder?.showBottomComplication(!storage.shouldShowBattery() && !storage.shouldShowPhoneBattery())
        initializesRegularColorsAndComplications()
    }

    fun updateAndroid12Complications() {
        android12ComplicationsViewHolder?.bound = true

        android12ComplicationsViewHolder?.setDefaultComplicationDrawable()
        initializesAndroid12ColorsAndComplications()
    }

    private fun initializesRegularColorsAndComplications() {
        val complicationIds = RegularDigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS

        providerInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    val complicationLocation = when (watchFaceComplicationId) {
                        getComplicationId(ComplicationLocation.LEFT) -> { ComplicationLocation.LEFT }
                        getComplicationId(ComplicationLocation.MIDDLE) -> { ComplicationLocation.MIDDLE }
                        getComplicationId(ComplicationLocation.BOTTOM) -> { ComplicationLocation.BOTTOM }
                        getComplicationId(ComplicationLocation.RIGHT) -> { ComplicationLocation.RIGHT  }
                        else -> null
                    } ?: return

                    regularComplicationsViewHolder?.updateComplicationViews(
                        complicationLocation,
                        complicationProviderInfo,
                        storage.getComplicationColors()
                    )
                }
            },
            watchFaceComponentName,
            *complicationIds
        )
    }

    private fun initializesAndroid12ColorsAndComplications() {
        val complicationIds = Android12DigitalWatchFaceDrawer.ACTIVE_COMPLICATIONS

        providerInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    val complicationLocation = when (watchFaceComplicationId) {
                        getComplicationId(ComplicationLocation.ANDROID_12_TOP_LEFT) -> { ComplicationLocation.ANDROID_12_TOP_LEFT }
                        getComplicationId(ComplicationLocation.ANDROID_12_TOP_RIGHT) -> { ComplicationLocation.ANDROID_12_TOP_RIGHT }
                        getComplicationId(ComplicationLocation.ANDROID_12_BOTTOM_LEFT) -> { ComplicationLocation.ANDROID_12_BOTTOM_LEFT }
                        getComplicationId(ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) -> { ComplicationLocation.ANDROID_12_BOTTOM_RIGHT }
                        else -> null
                    } ?: return

                    android12ComplicationsViewHolder?.updateComplicationViews(
                        complicationLocation,
                        complicationProviderInfo,
                        storage.getComplicationColors()
                    )
                }
            },
            watchFaceComponentName,
            *complicationIds
        )
    }

    override fun getItemViewType(position: Int): Int = settings[position]

    override fun getItemCount(): Int {
        settings = generateSettingsList(context, storage)
        return settings.size
    }

    private fun generateSettingsList(context: Context, storage: Storage): List<Int> {
        val isUserPremium = storage.isUserPremium()
        val isScreenRound = context.isScreenRound()
        val useAndroid12Style = storage.useAndroid12Style()

        val list = ArrayList<Int>()

        list.add(TYPE_HEADER)
        list.add(TYPE_ANDROID_12_STYLE)

        // TYPE_SECTION_WIDGETS
        list.add(TYPE_SECTION_WIDGETS)
        if( isUserPremium ) {
            if (useAndroid12Style) {
                list.add(TYPE_ANDROID_12_COMPLICATIONS_CONFIG)
            } else {
                list.add(TYPE_REGULAR_COMPLICATIONS_CONFIG)
            }

            list.add(TYPE_WIDGETS_SIZE)
        } else {
            list.add(TYPE_BECOME_PREMIUM)
        }
        list.add(TYPE_SHOW_WEAR_OS_LOGO)

        // TYPE_SECTION_BATTERY
        if( isUserPremium ) {
            list.add(TYPE_SECTION_BATTERY)
            if (!useAndroid12Style) {
                list.add(TYPE_BATTERY_SECTION_HEADER)
            }
            list.add(TYPE_SHOW_BATTERY)
            list.add(TYPE_SHOW_PHONE_BATTERY)
            list.add(TYPE_BATTERY_INDICATOR_COLOR)
        }

        // TYPE_SECTION_DATE_AND_TIME
        list.add(TYPE_SECTION_DATE_AND_TIME)
        list.add(TYPE_DATE_FORMAT)
        if( isUserPremium && context.getWeatherProviderInfo() != null ) {
            list.add(TYPE_SHOW_WEATHER)
        }
        list.add(TYPE_HOUR_FORMAT)
        list.add(TYPE_TIME_SIZE)
        list.add(TYPE_DATE_AND_BATTERY_SIZE)
        list.add(TYPE_TIME_AND_DATE_COLOR)
        if( isScreenRound ) {
            list.add(TYPE_SHOW_SECONDS_RING)
            if (storage.shouldShowSecondsRing()) {
                list.add(TYPE_SECONDS_RING_COLOR)
            }
        }

        // TYPE_SECTION_TIME_STYLE
        list.add(TYPE_SECTION_TIME_STYLE)
        list.add(TYPE_USE_THIN_TIME_STYLE_IN_REGULAR_MODE)
        list.add(TYPE_USE_NORMAL_TIME_STYLE_IN_AMBIENT_MODE)

        // TYPE_SECTION_AMBIENT
        list.add(TYPE_SECTION_AMBIENT)
        list.add(TYPE_SHOW_DATE_AMBIENT)
        if (isUserPremium) {
            list.add(TYPE_SHOW_COMPLICATIONS_AMBIENT)
        }
        if (isUserPremium && (storage.shouldShowBattery() || storage.shouldShowPhoneBattery())) {
            list.add(TYPE_SHOW_BATTERY_IN_AMBIENT)
        }

        // TYPE_SECTION_SUPPORT
        list.add(TYPE_SECTION_SUPPORT)
        list.add(TYPE_SEND_FEEDBACK)
        if( isUserPremium ) {
            list.add(TYPE_DONATE)
        }

        list.add(TYPE_FOOTER)

        return list
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        providerInfoRetriever.init()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        onDestroy()
    }

    fun onDestroy() {
        providerInfoRetriever.release()
    }

    fun weatherComplicationPermissionFinished() {
        val granted = context.isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")

        showWeatherViewHolder?.setShowWeatherViewSwitchChecked(granted)
        showWeatherListener(granted)
        updateWeatherButton()
    }

    private fun updateWeatherButton() {
        notifyDataSetChanged()
    }

    fun batteryComplicationPermissionFinished() {
        val granted = context.isPermissionGranted("com.google.android.wearable.permission.RECEIVE_COMPLICATION_DATA")

        showBatteryViewHolder?.setShowBatteryViewSwitchChecked(granted)
        regularComplicationsViewHolder?.showBottomComplication(!granted)
        showBatteryListener(granted)
        notifyDataSetChanged()
    }
}
