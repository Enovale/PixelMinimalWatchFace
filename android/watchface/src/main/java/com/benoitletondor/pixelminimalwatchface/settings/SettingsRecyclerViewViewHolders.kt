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

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationProviderInfo
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.helper.fontDisplaySizeToHumanReadableString
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.ComplicationLocation

class RegularComplicationsViewHolder(
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

    fun updateComplicationViews(
        location: ComplicationLocation,
        complicationProviderInfo: ComplicationProviderInfo?,
        complicationColors: ComplicationColors,
    ) {
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
            ComplicationLocation.ANDROID_12_TOP_LEFT, ComplicationLocation.ANDROID_12_TOP_RIGHT, ComplicationLocation.ANDROID_12_BOTTOM_LEFT, ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> Unit
        }
    }

    private fun updateComplicationView(
        complicationProviderInfo: ComplicationProviderInfo?,
        button: ImageButton,
        background: ImageView,
        complicationColors: ComplicationColors,
    ) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            background.setImageDrawable(addedComplicationDrawable)
        } else {
            button.setImageIcon(null)
            background.setImageDrawable(addComplicationDrawable)
        }

        updateComplicationsAccentColor(complicationColors)
    }

    private fun updateComplicationsAccentColor(colors: ComplicationColors) {
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

class Android12ComplicationsViewHolder(
    view: View,
    private val listener: (location: ComplicationLocation) -> Unit
) : RecyclerView.ViewHolder(view), View.OnClickListener {
    var bound = false

    private val topLeftComplicationBackground: ImageView = view.findViewById(R.id.top_left_complication_background)
    private val topRightComplicationBackground: ImageView = view.findViewById(R.id.top_right_complication_background)
    private val bottomLeftComplicationBackground: ImageView = view.findViewById(R.id.bottom_left_complication_background)
    private val bottomRightComplicationBackground: ImageView = view.findViewById(R.id.bottom_right_complication_background)
    private val topLeftComplication: ImageButton = view.findViewById(R.id.top_left_complication)
    private val topRightComplication: ImageButton = view.findViewById(R.id.top_right_complication)
    private val bottomLeftComplication: ImageButton = view.findViewById(R.id.bottom_left_complication)
    private val bottomRightComplication: ImageButton = view.findViewById(R.id.bottom_right_complication)
    private var addComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.add_complication)!!
    private var addedComplicationDrawable: Drawable = ContextCompat.getDrawable(view.context, R.drawable.added_complication)!!

    init {
        topLeftComplication.setOnClickListener(this)
        topRightComplication.setOnClickListener(this)
        bottomLeftComplication.setOnClickListener(this)
        bottomRightComplication.setOnClickListener(this)
    }

    fun setDefaultComplicationDrawable() {
        topLeftComplication.setImageDrawable(addComplicationDrawable)
        topRightComplication.setImageDrawable(addComplicationDrawable)
        bottomLeftComplication.setImageDrawable(addComplicationDrawable)
        bottomRightComplication.setImageDrawable(addComplicationDrawable)
    }

    override fun onClick(view: View) {
        when (view) {
            topLeftComplication -> { listener(ComplicationLocation.ANDROID_12_TOP_LEFT) }
            topRightComplication -> { listener(ComplicationLocation.ANDROID_12_TOP_RIGHT) }
            bottomLeftComplication -> { listener(ComplicationLocation.ANDROID_12_BOTTOM_LEFT) }
            bottomRightComplication -> { listener(ComplicationLocation.ANDROID_12_BOTTOM_RIGHT) }
        }
    }

    fun updateComplicationViews(
        location: ComplicationLocation,
        complicationProviderInfo: ComplicationProviderInfo?,
        complicationColors: ComplicationColors,
    ) {
        when (location) {
            ComplicationLocation.ANDROID_12_TOP_LEFT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    topLeftComplication,
                    topLeftComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.ANDROID_12_TOP_RIGHT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    topRightComplication,
                    topRightComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.ANDROID_12_BOTTOM_LEFT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    bottomLeftComplication,
                    bottomLeftComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.ANDROID_12_BOTTOM_RIGHT -> {
                updateComplicationView(
                    complicationProviderInfo,
                    bottomRightComplication,
                    bottomRightComplicationBackground,
                    complicationColors
                )
            }
            ComplicationLocation.LEFT, ComplicationLocation.MIDDLE, ComplicationLocation.RIGHT, ComplicationLocation.BOTTOM -> Unit
        }
    }

    private fun updateComplicationView(
        complicationProviderInfo: ComplicationProviderInfo?,
        button: ImageButton,
        background: ImageView,
        complicationColors: ComplicationColors,
    ) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            background.setImageDrawable(addedComplicationDrawable)
        } else {
            button.setImageIcon(null)
            background.setImageDrawable(addComplicationDrawable)
        }

        updateComplicationsAccentColor(complicationColors)
    }

    private fun updateComplicationsAccentColor(colors: ComplicationColors) {
        if( topLeftComplication.drawable == addComplicationDrawable ) {
            topLeftComplication.setColorFilter(Color.WHITE)
        } else {
            topLeftComplication.setColorFilter(colors.android12TopLeftColor.color)
        }

        if( topRightComplication.drawable == addComplicationDrawable ) {
            topRightComplication.setColorFilter(Color.WHITE)
        } else {
            topRightComplication.setColorFilter(colors.android12TopRightColor.color)
        }

        if( bottomLeftComplication.drawable == addComplicationDrawable ) {
            bottomLeftComplication.setColorFilter(Color.WHITE)
        } else {
            bottomLeftComplication.setColorFilter(colors.android12BottomLeftColor.color)
        }

        if( bottomRightComplication.drawable == addComplicationDrawable ) {
            bottomRightComplication.setColorFilter(Color.WHITE)
        } else {
            bottomRightComplication.setColorFilter(colors.android12BottomRightColor.color)
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

class PremiumViewHolder(
    view: View,
    premiumClickListener: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val premiumButton: Button = view.findViewById(R.id.premium_button)

    init {
        premiumButton.setOnClickListener {
            premiumClickListener()
        }
    }
}

class HourFormatViewHolder(
    view: View,
    hourFormatClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class ShowWearOSLogoViewHolder(
    view: View,
    showWearOSLogoClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val wearOSLogoSwitch: Switch = view as Switch

    init {
        wearOSLogoSwitch.setOnCheckedChangeListener { _, checked ->
            showWearOSLogoClickListener(checked)
        }
    }

    fun setShowWearOSLogoSwitchChecked(checked: Boolean) {
        wearOSLogoSwitch.isChecked = checked
    }

    fun setPremiumTitle(userPremium: Boolean, android12Style: Boolean) {
        wearOSLogoSwitch.text = itemView.context.getString(if(userPremium && !android12Style) {
            R.string.config_show_wear_os_logo_premium
        } else {
            R.string.config_show_wear_os_logo
        })
    }
}

class SendFeedbackViewHolder(
    view: View,
    onFeedbackButtonPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onFeedbackButtonPressed()
        }
    }
}

class ShowComplicationsAmbientViewHolder(
    view: View,
    showComplicationsAmbientClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class UseNormalTimeStyleInAmbientModeViewHolder(
    view: View,
    useNormalTimeStyleInAmbientModeClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val useNormalTimeStyleInAmbientSwitch: Switch = view as Switch

    init {
        useNormalTimeStyleInAmbientSwitch.setOnCheckedChangeListener { _, checked ->
            useNormalTimeStyleInAmbientModeClickListener(checked)
        }
    }

    fun setUseNormalTimeStyleInAmbientSwitchChecked(checked: Boolean) {
        useNormalTimeStyleInAmbientSwitch.isChecked = checked
    }
}

class UseThinTimeStyleInRegularModeViewHolder(
    view: View,
    useThinTimeStyleInRegularModeClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val useThinTimeStyleInRegularSwitch: Switch = view as Switch

    init {
        useThinTimeStyleInRegularSwitch.setOnCheckedChangeListener { _, checked ->
            useThinTimeStyleInRegularModeClickListener(checked)
        }
    }

    fun setUseThinTimeInRegularSwitchChecked(checked: Boolean) {
        useThinTimeStyleInRegularSwitch.isChecked = checked
    }
}

class TimeSizeViewHolder(
    view: View,
    timeSizeChanged: (Int) -> Unit,
) : RecyclerView.ViewHolder(view) {
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
            itemView.context.fontDisplaySizeToHumanReadableString(size)
        )
    }
}

class DateAndBatterySizeViewHolder(
    view: View,
    dateAndBatterySizeChanged: (Int) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val dateAndBatterySizeSeekBar: SeekBar = view.findViewById(R.id.date_size_seek_bar)
    private val dateAndBatterySizeText: TextView = view.findViewById(R.id.date_size_text)
    private val stepSize = 25

    init {
        dateAndBatterySizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val convertedProgress = (progress / stepSize) * stepSize
                seekBar.progress = convertedProgress
                setText(convertedProgress)

                dateAndBatterySizeChanged(convertedProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun setDateAndBatterySize(size: Int) {
        dateAndBatterySizeSeekBar.setProgress(size, false)
        setText(size)
    }

    private fun setText(size: Int) {
        dateAndBatterySizeText.text = itemView.context.getString(
            R.string.config_date_and_battery_size,
            itemView.context.fontDisplaySizeToHumanReadableString(size)
        )
    }
}

class ShowSecondsRingViewHolder(
    view: View,
    showSecondsRingClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class ShowBatteryViewHolder(
    view: View,
    showBatteryViewHolderClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class ShowBatteryInAmbientViewHolder(
    view: View,
    showBatteryInAmbientClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val showBatteryInAmbientViewSwitch: Switch = view as Switch

    init {
        showBatteryInAmbientViewSwitch.setOnCheckedChangeListener { _, checked ->
            showBatteryInAmbientClickListener(checked)
        }
    }

    fun setShowBatteryInAmbientSwitchChecked(checked: Boolean) {
        showBatteryInAmbientViewSwitch.isChecked = checked
    }
}

class DateFormatViewHolder(
    view: View,
    dateFormatClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class ShowDateAmbientViewHolder(
    view: View,
    showDateAmbientClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
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

class DonateViewHolder(
    view: View,
    onDonateButtonPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
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

    fun setSectionText(text: String) {
        sectionTextView.text = text
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

class UseAndroid12StyleViewHolder(
    view: View,
    useAndroid12StyleClickListener: (Boolean) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val useAndroid12StyleSwitch: Switch = view as Switch

    init {
        useAndroid12StyleSwitch.setOnCheckedChangeListener { _, checked ->
            useAndroid12StyleClickListener(checked)
        }
    }

    fun setUseAndroid12StyleSwitchChecked(checked: Boolean) {
        useAndroid12StyleSwitch.isChecked = checked
    }
}

class SecondsRingColorViewHolder(
    view: View,
    onChangeSecondsRingColorPressed: () -> Unit,
) : RecyclerView.ViewHolder(view) {
    init {
        view.setOnClickListener {
            onChangeSecondsRingColorPressed()
        }
    }
}

class WidgetsSizeViewHolder(
    view: View,
    widgetsSizeChanged: (Int) -> Unit,
) : RecyclerView.ViewHolder(view) {
    private val widgetsSizeSeekBar: SeekBar = view.findViewById(R.id.widgets_size_seek_bar)
    private val widgetsSizeText: TextView = view.findViewById(R.id.widgets_size_text)
    private val stepSize = 25

    init {
        widgetsSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val convertedProgress = (progress / stepSize) * stepSize
                seekBar.progress = convertedProgress
                setText(convertedProgress)

                widgetsSizeChanged(convertedProgress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun setWidgetsSize(size: Int) {
        widgetsSizeSeekBar.setProgress(size, false)
        setText(size)
    }

    private fun setText(size: Int) {
        widgetsSizeText.text = itemView.context.getString(
            R.string.config_widgets_size,
            itemView.context.fontDisplaySizeToHumanReadableString(size)
        )
    }
}