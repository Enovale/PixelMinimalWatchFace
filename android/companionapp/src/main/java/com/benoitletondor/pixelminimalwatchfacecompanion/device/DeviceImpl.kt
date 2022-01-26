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
package com.benoitletondor.pixelminimalwatchfacecompanion.device

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import com.benoitletondor.pixelminimalwatchfacecompanion.ForegroundService
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Exception
import javax.inject.Inject

private const val SHARED_PREFERENCES_FILE_NAME = "deviceSharedPref"
private const val SHOULD_ACTIVATE_FOREGROUND_SERVICE_KEY = "shouldActivatedForegroundService"

// Large parts of this code are from
// https://gitlab.inria.fr/stopcovid19/stopcovid-android/-/blob/master/stopcovid/src/main/java/com/lunabeestudio/stopcovid/manager/ProximityManager.kt
class DeviceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : Device {
    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    private val isForegroundServiceActivatedFlow = MutableStateFlow(isForegroundServiceEnabled())

    private val powerManagerIntents = arrayOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        ),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
        Intent().setComponent(ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
        Intent().setComponent(ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    )

    override fun isBatteryOptimizationOff(): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            (pm?.isIgnoringBatteryOptimizations(context.packageName) == true
                    || !hasActivityToResolveIgnoreBatteryOptimization(context))
        } catch (e: Exception) {
            Log.e("DeviceImpl", "Unable to fetch battery optimization state, defaulting to true", e)
            true
        }
    }

    override fun getBatteryOptimizationOptOutIntents(): List<Intent> = getIgnoreBatteryOptimizationIntents(context)

    override fun isForegroundServiceEnabled(): Boolean = sharedPreferences.getBoolean(
        SHOULD_ACTIVATE_FOREGROUND_SERVICE_KEY, false) && ForegroundService.isActivated()

    override fun isForegroundServiceEnabledFlow(): Flow<Boolean> = isForegroundServiceActivatedFlow

    override fun activateForegroundService() {
        ForegroundService.start(context)

        sharedPreferences.edit { putBoolean(SHOULD_ACTIVATE_FOREGROUND_SERVICE_KEY, true) }
        isForegroundServiceActivatedFlow.value = true
    }

    override fun deactivateForegroundService(killServiceIfOn: Boolean) {
        sharedPreferences.edit { putBoolean(SHOULD_ACTIVATE_FOREGROUND_SERVICE_KEY, false) }
        isForegroundServiceActivatedFlow.value = false

        if (killServiceIfOn) {
            ForegroundService.stop(context)
        }
    }

    override fun relaunchForegroundServiceIfNeeded() {
        if (sharedPreferences.getBoolean(SHOULD_ACTIVATE_FOREGROUND_SERVICE_KEY, false)) {
            ForegroundService.start(context)
            isForegroundServiceActivatedFlow.value = true
        }
    }

    private fun hasActivityToResolveIgnoreBatteryOptimization(context: Context): Boolean {
        val powerIntents = getIgnoreBatteryOptimizationIntents(context)
        for (intent in powerIntents) {
            intent.apply {
                putExtra("package_name", context.packageName)
                putExtra("package_label", context.getString(R.string.app_name))
            }
            val resolveInfo = intent.resolveActivityInfo(context.packageManager, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo?.exported == true) {
                return true
            }
        }

        return false
    }

    @SuppressLint("BatteryLife")
    private fun getIgnoreBatteryOptimizationIntents(context: Context): List<Intent> {
        val miuiIntent = Intent("miui.intent.action.HIDDEN_APPS_CONFIG_ACTIVITY")
        miuiIntent.putExtra("package_name", context.packageName)
        miuiIntent.putExtra("package_label", context.getString(R.string.app_name))
        val systemIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        val powerIntents = arrayListOf(miuiIntent, systemIntent)
        powerIntents.addAll(powerManagerIntents)
        return powerIntents
    }
}