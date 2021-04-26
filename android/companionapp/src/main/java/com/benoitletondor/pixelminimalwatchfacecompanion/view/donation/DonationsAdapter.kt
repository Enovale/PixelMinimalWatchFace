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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.*

class DonationsAdapter(
    private val SKUs: List<SkuDetails>,
    private val onDonateClicked: (SkuDetails) -> Unit,
) : RecyclerView.Adapter<DonationsAdapter.DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        return DonationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.donation_sku_item, parent, false))
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.setSku(SKUs[position], onDonateClicked)
    }

    override fun getItemCount(): Int = SKUs.size

    class DonationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.donation_sku_item_image_view)
        private val titleTextView: TextView = view.findViewById(R.id.donation_sku_item_title)
        private val descriptionTextView: TextView = view.findViewById(R.id.donation_sku_item_description)
        private val cta: Button = view.findViewById(R.id.donation_sku_item_cta)

        fun setSku(skuDetails: SkuDetails, onDonateClicked: (SkuDetails) -> Unit) {
            imageView.setImageDrawable(when(skuDetails.sku) {
                SKU_DONATION_TIER_1 -> ContextCompat.getDrawable(imageView.context, R.drawable.ic_coffee_cup)
                SKU_DONATION_TIER_2 -> ContextCompat.getDrawable(imageView.context, R.drawable.ic_beer_can)
                SKU_DONATION_TIER_3 -> ContextCompat.getDrawable(imageView.context, R.drawable.ic_beer)
                SKU_DONATION_TIER_4 -> ContextCompat.getDrawable(imageView.context, R.drawable.ic_hamburger)
                SKU_DONATION_TIER_5 -> ContextCompat.getDrawable(imageView.context, R.drawable.ic_burger_beer)
                else -> null
            })

            titleTextView.text = skuDetails.title.replace("(Pixel Minimal Watch Face - Watch Faces for WearOS)", "")
            descriptionTextView.text = skuDetails.description

            cta.text = skuDetails.price
            cta.setOnClickListener {
                onDonateClicked(skuDetails)
            }
        }
    }
}