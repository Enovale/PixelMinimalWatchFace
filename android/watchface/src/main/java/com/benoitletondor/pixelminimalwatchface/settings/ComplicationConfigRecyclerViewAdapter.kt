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
package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.*
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationIds
import com.benoitletondor.pixelminimalwatchface.helper.isPermissionGranted
import com.benoitletondor.pixelminimalwatchface.helper.isScreenRound
import com.benoitletondor.pixelminimalwatchface.helper.timeSizeToHumanReadableString
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.settings.ComplicationConfigActivity.Companion.COMPLICATION_CONFIG_REQUEST_CODE
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

private const val TYPE_HEADER = 0
private const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 1
private const val TYPE_FOOTER = 3
private const val TYPE_BECOME_PREMIUM = 4
private const val TYPE_HOUR_FORMAT = 5
private const val TYPE_SEND_FEEDBACK = 6
private const val TYPE_SHOW_WEAR_OS_LOGO = 7
private const val TYPE_SHOW_COMPLICATIONS_AMBIENT = 8
private const val TYPE_SHOW_FILLED_TIME_AMBIENT = 9
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

class ComplicationConfigRecyclerViewAdapter(
    private val context: Context,
    private val storage: Storage,
    private val premiumClickListener: () -> Unit,
    private val hourFormatSelectionListener: (Boolean) -> Unit,
    private val onFeedbackButtonPressed: () -> Unit,
    private val showWearOSButtonListener: (Boolean) -> Unit,
    private val showComplicationsAmbientListener: (Boolean) -> Unit,
    private val showFilledTimeAmbientListener: (Boolean) -> Unit,
    private val timeSizeChangedListener: (Int) -> Unit,
    private val showSecondsRingListener: (Boolean) -> Unit,
    private val showWeatherListener: (Boolean) -> Unit,
    private val openWeatherAppListener: () -> Unit,
    private val showBatteryListener: (Boolean) -> Unit,
    private val dateFormatSelectionListener: (Boolean) -> Unit,
    private val showDateAmbientListener: (Boolean) -> Unit,
    private val donateButtonPressed: () -> Unit,
    private val phoneBatteryButtonPressed: () -> Unit,
    private val changeTimeAndDateColorButtonPressed: () -> Unit,
    private val changeBatteryIndicatorColorButtonPressed: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedComplicationLocation: ComplicationLocation? = null

    private val watchFaceComponentName = ComponentName(context, PixelMinimalWatchFace::class.java)
    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())
    private var previewAndComplicationsViewHolder: PreviewAndComplicationsViewHolder? = null
    private var showWeatherViewHolder: ShowWeatherViewHolder? = null
    private var showBatteryViewHolder: ShowBatteryViewHolder? = null
    private val settings = generateSettingsList(context, storage)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_header,
                    parent,
                    false
                )
            )
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder =
                    PreviewAndComplicationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.config_list_preview_and_complications_item, parent, false)) { location ->
                        selectedComplicationLocation = location

                        (context as Activity).startActivityForResult(
                            WidgetConfigurationActivity.createIntent(context, location),
                            COMPLICATION_CONFIG_REQUEST_CODE,
                        )
                    }

                this.previewAndComplicationsViewHolder = previewAndComplicationsViewHolder
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
                    previewAndComplicationsViewHolder?.showMiddleComplication(!showWearOSLogo)
                }
            TYPE_SHOW_COMPLICATIONS_AMBIENT -> return ShowComplicationsAmbientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_complications_ambient,
                    parent,
                    false
                ),
                showComplicationsAmbientListener
            )
            TYPE_SHOW_FILLED_TIME_AMBIENT -> return ShowFilledTimeAmbientViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_filled_time_ambient,
                    parent,
                    false
                ),
                showFilledTimeAmbientListener
            )
            TYPE_TIME_SIZE -> return TimeSizeViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_time_size,
                    parent,
                    false
                ),
                timeSizeChangedListener
            )
            TYPE_SHOW_SECONDS_RING -> return ShowSecondsRingViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_show_seconds_ring,
                    parent,
                    false
                ),
                showSecondsRingListener
            )
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
                                ComplicationConfigActivity.COMPLICATION_WEATHER_PERMISSION_REQUEST_CODE
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
                            ComplicationConfigActivity.COMPLICATION_BATTERY_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        showBatteryListener(false)
                        previewAndComplicationsViewHolder?.showBottomComplication(!storage.shouldShowPhoneBattery())
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
            TYPE_SECTION_BATTERY, TYPE_SECTION_AMBIENT, TYPE_SECTION_DATE_AND_TIME, TYPE_SECTION_SUPPORT, TYPE_SECTION_WIDGETS -> return SectionViewHolder(
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
        }
        throw IllegalStateException("Unknown option type: $viewType")
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder = viewHolder as PreviewAndComplicationsViewHolder

                if( !previewAndComplicationsViewHolder.bound ) {
                    previewAndComplicationsViewHolder.bound = true
                    updateComplications()
                }
            }
            TYPE_HOUR_FORMAT -> {
                val use24hTimeFormat = storage.getUse24hTimeFormat()
                (viewHolder as HourFormatViewHolder).setHourFormatSwitchChecked(use24hTimeFormat)
            }
            TYPE_SHOW_WEAR_OS_LOGO -> {
                (viewHolder as ShowWearOSLogoViewHolder).apply {
                    setShowWearOSLogoSwitchChecked(storage.shouldShowWearOSLogo())
                    setPremiumTitle(storage.isUserPremium())
                }
            }
            TYPE_SHOW_COMPLICATIONS_AMBIENT -> {
                val showComplicationsAmbient = storage.shouldShowComplicationsInAmbientMode()
                (viewHolder as ShowComplicationsAmbientViewHolder).setShowComplicationsAmbientSwitchChecked(showComplicationsAmbient)
            }
            TYPE_SHOW_FILLED_TIME_AMBIENT -> {
                val showFilledTimeAmbient = storage.shouldShowFilledTimeInAmbientMode()
                (viewHolder as ShowFilledTimeAmbientViewHolder).setShowFilledTimeSwitchChecked(showFilledTimeAmbient)
            }
            TYPE_TIME_SIZE -> {
                val size = storage.getTimeSize()
                (viewHolder as TimeSizeViewHolder).setTimeSize(size)
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
            TYPE_SECTION_BATTERY, TYPE_SECTION_AMBIENT, TYPE_SECTION_DATE_AND_TIME, TYPE_SECTION_SUPPORT, TYPE_SECTION_WIDGETS -> {
                (viewHolder as SectionViewHolder).setSectionType(viewHolder.itemViewType)
            }
        }
    }

    fun updateComplications() {
        previewAndComplicationsViewHolder?.bound = true
        
        previewAndComplicationsViewHolder?.setDefaultComplicationDrawable()
        previewAndComplicationsViewHolder?.showMiddleComplication(!storage.shouldShowWearOSLogo())
        previewAndComplicationsViewHolder?.showBottomComplication(!storage.shouldShowBattery() && !storage.shouldShowPhoneBattery())
        initializesColorsAndComplications()
    }

    private fun initializesColorsAndComplications() {
        val complicationIds = getComplicationIds()

        providerInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {

                    previewAndComplicationsViewHolder?.updateComplicationViews(
                        when (watchFaceComplicationId) {
                            getComplicationId(ComplicationLocation.LEFT) -> { ComplicationLocation.LEFT }
                            getComplicationId(ComplicationLocation.MIDDLE) -> { ComplicationLocation.MIDDLE }
                            getComplicationId(ComplicationLocation.BOTTOM) -> { ComplicationLocation.BOTTOM }
                            else -> { ComplicationLocation.RIGHT }
                        },
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

    override fun getItemCount(): Int = settings.size

    private fun generateSettingsList(context: Context, storage: Storage): List<Int> {
        val isUserPremium = storage.isUserPremium()
        val isScreenRound = context.isScreenRound()

        val list = ArrayList<Int>()

        list.add(TYPE_HEADER)

        // TYPE_SECTION_WIDGETS
        list.add(TYPE_SECTION_WIDGETS)
        if( isUserPremium ) {
            list.add(TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG)
        } else {
            list.add(TYPE_BECOME_PREMIUM)
        }
        list.add(TYPE_SHOW_WEAR_OS_LOGO)

        // TYPE_SECTION_BATTERY
        if( isUserPremium ) {
            list.add(TYPE_SECTION_BATTERY)
            list.add(TYPE_BATTERY_SECTION_HEADER)
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
        list.add(TYPE_TIME_AND_DATE_COLOR)
        if( isScreenRound ) {
            list.add(TYPE_SHOW_SECONDS_RING)
        }

        // TYPE_SECTION_AMBIENT
        list.add(TYPE_SECTION_AMBIENT)
        list.add(TYPE_SHOW_DATE_AMBIENT)
        list.add(TYPE_SHOW_FILLED_TIME_AMBIENT)
        if (isUserPremium) {
            list.add(TYPE_SHOW_COMPLICATIONS_AMBIENT)
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
        previewAndComplicationsViewHolder?.showBottomComplication(false)
        showBatteryListener(granted)
    }
}

enum class ComplicationLocation : Parcelable {
    LEFT, MIDDLE, RIGHT, BOTTOM;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ComplicationLocation> {
        override fun createFromParcel(parcel: Parcel): ComplicationLocation {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<ComplicationLocation?> {
            return arrayOfNulls(size)
        }
    }
}

class PreviewAndComplicationsViewHolder(
    view: View,
    private val listener: (location: ComplicationLocation) -> Unit
) : RecyclerView.ViewHolder(view), View.OnClickListener {
    var bound = false

    private val wearOSLogoImageView: ImageView = view.findViewById(R.id.wear_os_logo_image_view)
    private val batteryIconImageView: ImageView = view.findViewById(R.id.battery_icon)
    private val leftComplicationBackground: ImageView = view.findViewById(R.id.left_complication_background)
    private val middleComplicationBackground: ImageView = view.findViewById(R.id.middle_complication_background)
    private val rightComplicationBackground: ImageView = view.findViewById(R.id.right_complication_background)
    private val bottomComplicationBackground: ImageView = view.findViewById(R.id.bottom_complication_background)
    private val leftComplication: ImageButton = view.findViewById(R.id.left_complication)
    private val middleComplication: ImageButton = view.findViewById(R.id.middle_complication)
    private val rightComplication: ImageButton = view.findViewById(R.id.right_complication)
    private val bottomComplication: ImageButton = view.findViewById(R.id.bottom_complication)
    private var addComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.add_complication)!!
    private var addedComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.added_complication)!!

    init {
        leftComplication.setOnClickListener(this)
        middleComplication.setOnClickListener(this)
        rightComplication.setOnClickListener(this)
        bottomComplication.setOnClickListener(this)
    }

    fun setDefaultComplicationDrawable() {
        leftComplication.setImageDrawable(addComplicationDrawable)
        middleComplication.setImageDrawable(addComplicationDrawable)
        rightComplication.setImageDrawable(addComplicationDrawable)
        bottomComplication.setImageDrawable(addComplicationDrawable)
    }

    override fun onClick(view: View) {
        when (view) {
            leftComplication -> { listener(ComplicationLocation.LEFT) }
            middleComplication -> { listener(ComplicationLocation.MIDDLE) }
            rightComplication -> { listener(ComplicationLocation.RIGHT) }
            bottomComplication -> { listener(ComplicationLocation.BOTTOM) }
        }
    }

    fun showMiddleComplication(showMiddleComplication: Boolean) {
        middleComplication.visibility = if( showMiddleComplication ) { View.VISIBLE } else { View.GONE }
        middleComplicationBackground.visibility = if( showMiddleComplication ) { View.VISIBLE } else { View.INVISIBLE }
        wearOSLogoImageView.visibility = if( !showMiddleComplication ) { View.VISIBLE } else { View.GONE }
    }

    fun showBottomComplication(showBottomComplication: Boolean) {
        bottomComplication.visibility = if( showBottomComplication ) { View.VISIBLE } else { View.GONE }
        bottomComplicationBackground.visibility = if( showBottomComplication ) { View.VISIBLE } else { View.INVISIBLE }
        batteryIconImageView.visibility = if( !showBottomComplication ) { View.VISIBLE } else { View.GONE }
    }

    fun updateComplicationViews(location: ComplicationLocation,
                                complicationProviderInfo: ComplicationProviderInfo?,
                                complicationColors: ComplicationColors) {
        when (location) {
            ComplicationLocation.LEFT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    leftComplication,
                    leftComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.MIDDLE -> {
                updateComplicationView(
                    complicationProviderInfo,
                    middleComplication,
                    middleComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.RIGHT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    rightComplication,
                    rightComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.BOTTOM -> {
                updateComplicationView(
                    complicationProviderInfo,
                    bottomComplication,
                    bottomComplicationBackground,
                    complicationColors
                )
            }
        }
    }

    private fun updateComplicationView(complicationProviderInfo: ComplicationProviderInfo?,
                                       button: ImageButton,
                                       background: ImageView,
                                       complicationColors: ComplicationColors) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            background.setImageDrawable(addedComplicationDrawable)
        } else {
            button.setImageIcon(null)
            background.setImageDrawable(addComplicationDrawable)
        }

        updateComplicationsAccentColor(complicationColors)
    }

    fun updateComplicationsAccentColor(colors: ComplicationColors) {
        if( rightComplication.drawable == addComplicationDrawable ) {
            rightComplication.setColorFilter(Color.WHITE)
        } else {
            rightComplication.setColorFilter(colors.rightColor.color)
        }

        if( leftComplication.drawable == addComplicationDrawable ) {
            leftComplication.setColorFilter(Color.WHITE)
        } else {
            leftComplication.setColorFilter(colors.leftColor.color)
        }

        if( middleComplication.drawable == addComplicationDrawable ) {
            middleComplication.setColorFilter(Color.WHITE)
        } else {
            middleComplication.setColorFilter(colors.middleColor.color)
        }

        if( bottomComplication.drawable == addComplicationDrawable ) {
            bottomComplication.setColorFilter(Color.WHITE)
        } else {
            bottomComplication.setColorFilter(colors.bottomColor.color)
        }
    }
}

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val versionTextView: TextView = view.findViewById(R.id.app_version)

    init {
        versionTextView.text = versionTextView.context.getString(R.string.config_version, BuildConfig.VERSION_NAME)
    }
}

class PremiumViewHolder(view: View,
                        premiumClickListener: () -> Unit) : RecyclerView.ViewHolder(view) {
    private val premiumButton: Button = view.findViewById(R.id.premium_button)

    init {
        premiumButton.setOnClickListener {
            premiumClickListener()
        }
    }
}

class HourFormatViewHolder(view: View,
                           hourFormatClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val hourFormatSwitch: Switch = view as Switch

    init {
        hourFormatSwitch.setOnCheckedChangeListener { _, checked ->
            hourFormatClickListener(checked)
        }
    }

    fun setHourFormatSwitchChecked(checked: Boolean) {
        hourFormatSwitch.isChecked = checked
    }
}

class ShowWearOSLogoViewHolder(view: View,
                               showWearOSLogoClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val wearOSLogoSwitch: Switch = view as Switch

    init {
        wearOSLogoSwitch.setOnCheckedChangeListener { _, checked ->
            showWearOSLogoClickListener(checked)
        }
    }

    fun setShowWearOSLogoSwitchChecked(checked: Boolean) {
        wearOSLogoSwitch.isChecked = checked
    }

    fun setPremiumTitle(userPremium: Boolean) {
        wearOSLogoSwitch.text = itemView.context.getString(if( userPremium ) {
            R.string.config_show_wear_os_logo_premium
        } else {
            R.string.config_show_wear_os_logo
        })
    }
}

class SendFeedbackViewHolder(view: View,
                             onFeedbackButtonPressed: () -> Unit) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onFeedbackButtonPressed()
        }
    }
}

class ShowComplicationsAmbientViewHolder(view: View,
                                         showComplicationsAmbientClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val showComplicationsAmbientSwitch: Switch = view as Switch

    init {
        showComplicationsAmbientSwitch.setOnCheckedChangeListener { _, checked ->
            showComplicationsAmbientClickListener(checked)
        }
    }

    fun setShowComplicationsAmbientSwitchChecked(checked: Boolean) {
        showComplicationsAmbientSwitch.isChecked = checked
    }
}

class ShowFilledTimeAmbientViewHolder(view: View,
                                      showFilledTimeClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val showFilledTimeSwitch: Switch = view as Switch

    init {
        showFilledTimeSwitch.setOnCheckedChangeListener { _, checked ->
            showFilledTimeClickListener(!checked)
        }
    }

    fun setShowFilledTimeSwitchChecked(checked: Boolean) {
        showFilledTimeSwitch.isChecked = !checked
    }
}

class TimeSizeViewHolder(view: View,
                         timeSizeChanged: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
    private val timeSizeSeekBar: SeekBar = view.findViewById(R.id.time_size_seek_bar)
    private val timeSizeText: TextView = view.findViewById(R.id.time_size_text)
    private val stepSize = 25

    init {
        timeSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val convertedProgress = (progress / stepSize) * stepSize
                seekBar.progress = convertedProgress
                setText(convertedProgress)

                timeSizeChanged(convertedProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun setTimeSize(size: Int) {
        timeSizeSeekBar.setProgress(size, false)
        setText(size)
    }

    private fun setText(size: Int) {
        timeSizeText.text = itemView.context.getString(
            R.string.config_time_size,
            itemView.context.timeSizeToHumanReadableString(size)
        )
    }
}

class ShowSecondsRingViewHolder(view: View,
                                showSecondsRingClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val showSecondsRingSwitch: Switch = view as Switch

    init {
        showSecondsRingSwitch.setOnCheckedChangeListener { _, checked ->
            showSecondsRingClickListener(checked)
        }
    }

    fun setShowSecondsRingSwitchChecked(checked: Boolean) {
        showSecondsRingSwitch.isChecked = checked
    }
}

class ShowWeatherViewHolder(
    view: View,
    showWeatherViewHolderClickListener: (Boolean) -> Unit,
    openWeatherAppClickListener: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val showWeatherViewSwitch: Switch = view.findViewById(R.id.config_list_show_weather_switch)
    private val goToWeatherAppButton: Button = view.findViewById(R.id.config_list_open_weather_app_button)

    init {
        showWeatherViewSwitch.setOnCheckedChangeListener { _, checked ->
            showWeatherViewHolderClickListener(checked)
        }

        goToWeatherAppButton.setOnClickListener {
            openWeatherAppClickListener()
        }
    }

    fun setShowWeatherViewSwitchChecked(checked: Boolean) {
        showWeatherViewSwitch.isChecked = checked
        goToWeatherAppButton.isVisible = checked
    }
}

class ShowBatteryViewHolder(view: View,
                            showBatteryViewHolderClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val showBatteryViewSwitch: Switch = view as Switch

    init {
        showBatteryViewSwitch.setOnCheckedChangeListener { _, checked ->
            showBatteryViewHolderClickListener(checked)
        }
    }

    fun setShowBatteryViewSwitchChecked(checked: Boolean) {
        showBatteryViewSwitch.isChecked = checked
    }
}

class DateFormatViewHolder(view: View,
                           dateFormatClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val dateFormatSwitch: Switch = view as Switch

    init {
        dateFormatSwitch.setOnCheckedChangeListener { _, checked ->
            dateFormatClickListener(checked)
        }
    }

    fun setDateFormatSwitchChecked(checked: Boolean) {
        dateFormatSwitch.isChecked = checked
    }
}

class ShowDateAmbientViewHolder(view: View,
                                showDateAmbientClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val showDateAmbientSwitch: Switch = view as Switch

    init {
        showDateAmbientSwitch.setOnCheckedChangeListener { _, checked ->
            showDateAmbientClickListener(checked)
        }
    }

    fun setShowDateAmbientSwitchChecked(checked: Boolean) {
        showDateAmbientSwitch.isChecked = checked
    }
}

class DonateViewHolder(view: View,
                       onDonateButtonPressed: () -> Unit) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onDonateButtonPressed()
        }
    }
}

class PhoneBatteryViewHolder(
    view: View,
    onPhoneBatteryButtonPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onPhoneBatteryButtonPressed()
        }
    }
}

class SectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val sectionTextView = view as TextView

    fun setSectionType(sectionType: Int) {
        when(sectionType) {
            TYPE_SECTION_SUPPORT -> sectionTextView.setText(R.string.config_section_support)
            TYPE_SECTION_DATE_AND_TIME -> sectionTextView.setText(R.string.config_section_date_and_time)
            TYPE_SECTION_BATTERY -> sectionTextView.setText(R.string.config_section_battery)
            TYPE_SECTION_AMBIENT -> sectionTextView.setText(R.string.config_section_ambient)
            TYPE_SECTION_WIDGETS -> sectionTextView.setText(R.string.config_section_widgets)
        }
    }
}

class BatterySectionHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

class TimeAndDateColorViewHolder(
    view: View,
    onChangeDateAndTimeColorPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onChangeDateAndTimeColorPressed()
        }
    }
}

class BatteryIndicatorColorViewHolder(
    view: View,
    onChangeBatteryIndicatorColorPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onChangeBatteryIndicatorColorPressed()
        }
    }
}