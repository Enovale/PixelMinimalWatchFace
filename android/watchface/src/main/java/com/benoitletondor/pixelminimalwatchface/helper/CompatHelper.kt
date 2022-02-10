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
package com.benoitletondor.pixelminimalwatchface.helper

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationText
import android.util.Log
import com.benoitletondor.pixelminimalwatchface.Device
import com.benoitletondor.pixelminimalwatchface.R
import org.json.JSONObject
import kotlin.math.roundToInt
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.support.wearable.complications.ComplicationData.TYPE_NO_DATA
import androidx.core.content.pm.PackageInfoCompat
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import java.text.SimpleDateFormat
import java.util.*

val isGalaxyWatch4BuggyWearOSVersion = Device.isSamsungGalaxyWatch && Build.VERSION.SECURITY_PATCH.startsWith("2022")

fun Context.getTopAndBottomMargins(): Float {
    return when {
        Device.isOppoWatch -> dpToPx(5).toFloat()
        Device.isSamsungGalaxyWatchBigScreen(this) -> dpToPx(29).toFloat()
        Device.isSamsungGalaxyWatch -> dpToPx(26).toFloat()
        else -> resources.getDimension(R.dimen.screen_top_and_bottom_margin)
    }
}

private val timeDateFormatter = SimpleDateFormat("HH:mm", Locale.US)

fun ComplicationData.sanitize(context: Context, watchFaceComplicationId: Int): ComplicationData {
    try {
        if (!Device.isSamsungGalaxyWatch) {
            return this
        }

        return when {
            isSamsungHeartRateBadComplicationData(context) -> {
                val shortText = context.getSamsungHeartRateData() ?: "?"

                val builder = ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                    .setTapAction(tapAction)
                    .setShortText(ComplicationText.plainText(shortText))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_heart_complication))
                builder.build()
            }
            isSamsungHealthBadComplicationData(context) -> {
                ComplicationData.Builder(this)
                    .setTapAction(
                        PendingIntent.getActivity(
                            context,
                            0,
                            Intent().apply {
                                component = getSamsungHealthHomeComponentName()
                            },
                            PendingIntent.FLAG_IMMUTABLE,
                        )
                    )
                    .build()
            }
            isSamsungCalendarBadComplicationData() -> {
                val nextEvent = context.getNextCalendarEvent() ?: return this
                val isLargeWidget = PixelMinimalWatchFace.BOTTOM_COMPLICATION_ID == watchFaceComplicationId

                val builder = ComplicationData.Builder(if (isLargeWidget) { ComplicationData.TYPE_LONG_TEXT } else { ComplicationData.TYPE_SHORT_TEXT })
                    .setTapAction(PendingIntent.getActivity(
                        context,
                        0,
                        Intent().apply {
                            component = getSamsungCalendarHomeComponentName()
                        },
                        PendingIntent.FLAG_IMMUTABLE,
                    ))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_calendar_complication))

                if (isLargeWidget) {
                    builder.setLongText(ComplicationText.plainText(nextEvent.title))
                } else {
                    val formattedTime = timeDateFormatter.format(Date(nextEvent.startTimestamp))
                    builder.setShortText(ComplicationText.plainText(formattedTime))
                }

                builder.build()
            }
            else -> this
        }
    } catch (t: Throwable) {
        Log.e("PixelWatchFace", "Error while sanitizing complication data", t)
        return this
    }
}

private fun ComplicationData.isSamsungHealthBadComplicationData(context: Context): Boolean {
    val sHealthVersion = try {
        context.getShealthAppVersion()
    } catch (e: Throwable) {
        return false
    }

    return when(sHealthVersion) {
        S_HEALTH_6_20_0_016 -> isSamsungDailyActivityBadComplicationData(sHealthVersion) ||
            isSamsungStepsBadComplicationData(context) ||
            isSamsungSleepBadComplicationData() ||
            isSamsungWaterBadComplicationData()
        S_HEALTH_6_21_0_051 -> isSamsungDailyActivityBadComplicationData(sHealthVersion)
        else -> false
    }
}

@SuppressLint("NewApi")
private fun ComplicationData.isSamsungDailyActivityBadComplicationData(sHealthVersion: Long): Boolean {
    return icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId == when(sHealthVersion) {
            S_HEALTH_6_21_0_051 -> 2131231581
            else -> 2131231593
        }
}

private fun ComplicationData.isSamsungCalendarBadComplicationData(): Boolean {
    return isGalaxyWatch4BuggyWearOSVersion && type == TYPE_NO_DATA
}

private fun ComplicationData.isSamsungStepsBadComplicationData(context: Context): Boolean {
    return shortTitle != null &&
        samsungStepComplicationShortTextValues.contains(shortTitle.getText(context, System.currentTimeMillis())) &&
        imageContentDescription != null
}

@SuppressLint("NewApi")
private fun ComplicationData.isSamsungSleepBadComplicationData(): Boolean {
    return icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId == 2131231610
}

@SuppressLint("NewApi")
private fun ComplicationData.isSamsungWaterBadComplicationData(): Boolean {
    return icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId == 2131231614
}

@SuppressLint("NewApi")
private fun ComplicationData.isSamsungHeartRateBadComplicationData(context: Context): Boolean {
    if (shortText != null && samsungHeartRateComplicationShortTextValues.contains(shortText.getText(context, System.currentTimeMillis()))) {
        return true
    }

    if (icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId.matchesHR(context)) {
        return true
    }

    return false
}

private fun Int.matchesHR(context: Context): Boolean {
    val sHealthVersion = try {
        context.getShealthAppVersion()
    } catch (e: Throwable) {
        null
    }

    return when {
        sHealthVersion == S_HEALTH_6_21_0_051 -> this == 2131231595
        sHealthVersion != null && sHealthVersion >= S_HEALTH_6_20_0_016 -> this == 2131231607
        else -> this == 2131231612
    }
}

private fun Context.getShealthAppVersion(): Long {
    val packageInfo = packageManager.getPackageInfo(S_HEALTH_PACKAGE_NAME, 0);
    return PackageInfoCompat.getLongVersionCode(packageInfo)
}

@SuppressLint("NewApi", "Range")
private fun Context.getNextCalendarEvent(): CalendarEvent? {
    try {
        val uri = "content://com.samsung.android.calendar.watch/nextEvents"

        contentResolver.query(Uri.parse(uri), null, null, null)?.use { query ->
            if (query.count == 0) {
                return null
            }

            query.moveToFirst()
            val eventTitle = query.getString(query.getColumnIndex("title"))
            val eventStartTimestamp = query.getLong(query.getColumnIndex("begin"))
            val isAllDay = query.getInt(query.getColumnIndex("allDay")) != 1

            return CalendarEvent(eventTitle, eventStartTimestamp, isAllDay)
        }

        return null
    } catch (e: Exception) {
        Log.e("CompatHelper", "Error while getting next event", e)
        return null
    }
}

private data class CalendarEvent(
    val title: String,
    val startTimestamp: Long,
    val isAllDay: Boolean,
)

private fun Context.getSamsungHeartRateData(): String? {
    val uri = "content://$S_HEALTH_PACKAGE_NAME.healthdataprovider"

    val bundle = contentResolver.call(Uri.parse(uri), "heart_rate", null, null)
    if (bundle != null) {
        val error = bundle.getString("error")
        if (error != null) {
            return null
        }

        val data = bundle.getString("data") ?: return null
        val json = JSONObject(data)
        val hr = json.optDouble("value", -1.0)
        return if (hr > 0) {
            hr.roundToInt().toString()
        } else {
            null
        }
    }

    return null
}

private fun getSamsungHealthHomeComponentName() = ComponentName(
    S_HEALTH_PACKAGE_NAME,
    "com.samsung.android.wear.shealth.app.home.HomeActivity"
)

private fun getSamsungCalendarHomeComponentName() = ComponentName(
    S_CALENDAR_PACKAGE_NAME,
    "com.samsung.android.app.calendar.view.daily.DailyActivity"
)

private const val S_HEALTH_PACKAGE_NAME = "com.samsung.android.wear.shealth"
private const val S_CALENDAR_PACKAGE_NAME = "com.samsung.android.calendar"
private const val S_HEALTH_6_20_0_016 = 6200016L
private const val S_HEALTH_6_21_0_051 = 6210051L

private val samsungStepComplicationShortTextValues = setOf(
    "Steps",
    "الخطوات",
    "খোজ",
    "Addımlar",
    "Крокі",
    "Крачки",
    "পদক্ষেপ",
    "পদক্ষেপগুলি",
    "གོམ་གྲངས་འཇལ",
    "Koraci",
    "Passes",
    "Kroky",
    "Skridt",
    "Schritte",
    "Βήματα",
    "Pasos",
    "Sammud",
    "Pausoak",
    "قدم‌ها",
    "Askeleet",
    "Pas",
    "Céim",
    "પગલાં",
    "कदम",
    "Lépések",
    "Քայլեր",
    "Langkah",
    "Skref",
    "Passi",
    "צעדים",
    "歩",
    "ნაბიჯები",
    "Қадамдар",
    "ជំហាន",
    "ಹೆಜ್ಜೆಗಳು",
    "걸음 수",
    "ກ້າວ",
    "Žingsniai",
    "Soļi",
    "Чекори",
    "ചുവടുകൾ",
    "Алхам",
    "पाऊले",
    "Langkah",
    "ခြေလှမ်းများ",
    "ေျခလွမ္းမ်ား",
    "Skritt",
    "चरणहरू",
    "Stappen",
    "ପାଦଗୁଡିକ",
    "ਕਦਮ",
    "Kroki",
    "Krokōw",
    "Passos",
    "Pași",
    "Шаги",
    "පියවර",
    "Kroky",
    "Koraki",
    "Hapat",
    "Koraci",
    "Steg",
    "காலடிகள்",
    "అడుగులు",
    "Қадамҳо",
    "ก้าว",
    "Ädimler",
    "Hakbang",
    "Adım",
    "计步",
    "Кроки",
    "مراحل,",
    "Qadaml",
    "Các bước",
    "计步",
)

private val samsungHeartRateComplicationShortTextValues = setOf(
    "心跳率",
    "心率",
    "Nhịp tim",
    "Yurak puls",
    "شرح قلب",
    "Пульс",
    "Klp atş hz",
    "Heart rate",
    "Ýürek ritm",
    "อัตราการเต้นหัวใจ",
    "Тапиши дил",
    "హృదయ స్పందన రేటు",
    "இ.து.விகி.",
    "Puls",
    "Rr. zemrës",
    "Srč. utrip",
    "Srdcový tep",
    "හෘද වේගය",
    "Пульс",
    "Freq. card.",
    "Freq. car.",
    "Tyntno",
    "ਦਿਲ ਦੀ ਗਤੀ",
    "ହାର୍ଟ୍ ରେଟ୍",
    "Hartslag",
    "हृदय गति<",
    "ႏွလုံး ခုန္ႏႈန္း",
    "နှလုံး ခုန်နှုန်း",
    "Kdr jntung",
    "हृदय गती",
    "Зүрхний цохилт",
    "ഹൃദയമിടി.",
    "Пулс",
    "Sirds rit.",
    "Šird. rit.",
    "ອັດຕາຫົວໃຈເຕັ້ນ",
    "Жүрөк согушу",
    "심박수",
    "ಹೃದಯ ಬಡಿತದ ದರ",
    "Жүрек соғ.",
    "პულსი",
    "心拍数",
    "דופק לב",
    "Freq. card.",
    "Púls",
    "Dnyt jntng",
    "Սրտխփ. հճխ",
    "Pulzus",
    "Otk. srca",
    "हृदय गति",
    "હૃદય દર",
    "Ritmo car.",
    "Croíráta",
    "Fréq. car.",
    "Cardio",
    "Syke",
    "ضربان قلب",
    "Bihotz frek.",
    "Süd. löög.",
    "RC",
    "FC",
    "Καρ. παλμ.",
    "Srd. tep",
    "Ritme card",
    "སྙིང་འཕར་ཚད།",
    "হৃদয. হার",
    "হৃদস্পন্দনের হার",
    "Сър. ритъм",
    "Част. пул.",
    "Ürək ritmi",
    "হৃদ হাৰ",
    "سرعة ضربات القلب",
    "HeartRate",
)
