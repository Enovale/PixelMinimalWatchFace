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
package com.benoitletondor.pixelminimalwatchfacecompanion.sync

import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import com.google.android.gms.wearable.CapabilityClient

interface Sync {
    suspend fun sendPremiumStatus(isUserPremium: Boolean)
    suspend fun getWearableStatus(): WearableStatus
    suspend fun openPlayStoreOnWatch(): Boolean
    fun subscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener)
    fun unsubscribeToCapabilityChanges(listener: CapabilityClient.OnCapabilityChangedListener)

    suspend fun sendBatterySyncStatus(syncActivated: Boolean)
    suspend fun sendBatteryStatus(batteryPercentage: Int)

    suspend fun sendNotificationsSyncStatus(syncActivated: NotificationsSyncStatus)
    suspend fun sendActiveNotifications(notifications: NotificationsData)

    sealed class WearableStatus {
        object AvailableAppNotInstalled : WearableStatus()
        object AvailableAppInstalled: WearableStatus()
        object NotAvailable: WearableStatus()
        class Error(val error: Throwable): WearableStatus()
    }

    class NotificationsData(
        val iconIds: List<Int>,
        val iconIdsToIcons: Map<Int, Icon>,
    )

    enum class NotificationsSyncStatus(val intValue: Int) {
        DEACTIVATED(0),
        ACTIVATED(1),
        ACTIVATED_MISSING_PERMISSION(2),
    }
}