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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.SkuDetails
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.helper.MutableLiveFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DonationViewModel @Inject constructor(
    private val billing: Billing,
) : ViewModel() {
    private val errorPayingEventMutableFlow = MutableLiveFlow<Throwable>()
    val errorPayingEventFlow: Flow<Throwable> = errorPayingEventMutableFlow

    private val donationSuccessEventMutableFlow = MutableLiveFlow<String>()
    val donationSuccessEventFlow: Flow<String> = donationSuccessEventMutableFlow

    private val stateMutableFlow = MutableStateFlow<State>(State.Loading)
    val stateFlow: Flow<State> = stateMutableFlow
    val state: State get() = stateMutableFlow.value

    init {
        loadSKUs()
    }

    private fun loadSKUs() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                stateMutableFlow.value = State.Loading

                try {
                    val skus = billing.getDonationsSKUs()
                    stateMutableFlow.value = State.Loaded(
                        items = skus.map { skuDetails ->
                            DonationItem(
                                sku = skuDetails.sku,
                                title = skuDetails.title.replace("(Pixel Minimal Watch Face - Watch Faces for WearOS)", ""),
                                description = skuDetails.description,
                                price = skuDetails.price,
                            )
                        },
                        SKUs = skus,
                    )
                } catch (error: Throwable) {
                    Log.e("DonationViewModel", "Error while loading SKUs", error)
                    stateMutableFlow.value = State.ErrorLoading(error)
                }
            }
        }
    }

    fun onRetryLoadSKUsButtonClicked() {
        loadSKUs()
    }

    fun onDonateButtonClicked(sku: String, activity: Activity) {
        val loadedState = state as? State.Loaded ?: return
        val skuDetails = loadedState.SKUs.firstOrNull { it.sku == sku } ?: return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val purchaseStatus = billing.launchDonationPurchaseFlow(activity, skuDetails)
                    if( purchaseStatus ) {
                        donationSuccessEventMutableFlow.emit(skuDetails.price)
                    }
                } catch (error: Throwable) {
                    Log.e("DonationViewModel", "Error while donation for SKU: $sku", error)
                    errorPayingEventMutableFlow.emit(error)
                }
            }
        }
    }

    sealed class State {
        object Loading : State()
        class ErrorLoading(val error: Throwable) : State()
        class Loaded(val items: List<DonationItem>, val SKUs: List<SkuDetails>) : State()
    }

    data class DonationItem(
        val sku: String,
        val title: String,
        val description: String,
        val price: String,
    )
}