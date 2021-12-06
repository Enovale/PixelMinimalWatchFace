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
import androidx.core.content.pm.PackageInfoCompat

fun Context.getTopAndBottomMargins(): Float {
    return when {
        Device.isOppoWatch -> dpToPx(5).toFloat()
        Device.isSamsungGalaxyWatchBigScreen(this) -> dpToPx(29).toFloat()
        Device.isSamsungGalaxyWatch -> dpToPx(26).toFloat()
        else -> resources.getDimension(R.dimen.screen_top_and_bottom_margin)
    }
}

fun ComplicationData.sanitize(context: Context): ComplicationData {
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

    if (sHealthVersion != S_HEALTH_6_20_0_016) {
        return false
    }

    return isSamsungDailyActivityBadComplicationData() ||
        isSamsungStepsBadComplicationData(context) ||
        isSamsungSleepBadComplicationData() ||
        isSamsungWaterBadComplicationData()
}

private fun ComplicationData.isSamsungDailyActivityBadComplicationData(): Boolean {
    return icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId == 2131231593
}

private fun ComplicationData.isSamsungStepsBadComplicationData(context: Context): Boolean {
    return shortTitle != null && samsungStepComplicationShortTextValues.contains(shortTitle.getText(context, System.currentTimeMillis()))
}

private fun ComplicationData.isSamsungSleepBadComplicationData(): Boolean {
    return icon != null &&
        icon.type == Icon.TYPE_RESOURCE &&
        icon.resPackage == S_HEALTH_PACKAGE_NAME &&
        icon.resId == 2131231610
}

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
        sHealthVersion != null && sHealthVersion >= S_HEALTH_6_20_0_016 -> this == 2131231607
        else -> this == 2131231612
    }
}

private fun Context.getShealthAppVersion(): Long {
    val packageInfo = packageManager.getPackageInfo(S_HEALTH_PACKAGE_NAME, 0);
    return PackageInfoCompat.getLongVersionCode(packageInfo)
}

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

private const val S_HEALTH_PACKAGE_NAME = "com.samsung.android.wear.shealth"
private const val S_HEALTH_6_20_0_016 = 6200016L

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
