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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.SkuDetails
import com.benoitletondor.pixelminimalwatchfacecompanion.SingleLiveEvent
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(private val billing: Billing) : ViewModel(), CoroutineScope by MainScope() {
    val errorPayingEvent = SingleLiveEvent<Throwable>()
    val donationSuccessEvent = SingleLiveEvent<SkuDetails>()
    val stateEventStream = MutableLiveData<State>(State.Loading)

    init {
        loadSKUs()
    }

    private fun loadSKUs() {
        launch {
            withContext(Dispatchers.IO) {
                stateEventStream.postValue(State.Loading)

                try {
                    stateEventStream.postValue(State.Load(billing.getDonationsSKUs()))
                } catch (error: Throwable) {
                    Log.e("DonationViewModel", "Error while loading SKUs", error)
                    stateEventStream.postValue(State.ErrorLoading(error))
                }
            }
        }
    }

    fun onRetryLoadSKUsButtonClicked() {
        loadSKUs()
    }

    fun onDonateButtonClicked(sku: SkuDetails, activity: Activity) {
        launch {
            withContext(Dispatchers.IO) {
                try {
                    val purchaseStatus = billing.launchDonationPurchaseFlow(activity, sku)
                    if( purchaseStatus ) {
                        donationSuccessEvent.postValue(sku)
                    }
                } catch (error: Throwable) {
                    Log.e("DonationViewModel", "Error while donation for SKU: ${sku.sku}", error)
                    errorPayingEvent.postValue(error)
                }
            }
        }
    }

    sealed class State {
        object Loading : State()
        class ErrorLoading(val error: Throwable) : State()
        class Load(val SKUs: List<SkuDetails>) : State()
    }
}