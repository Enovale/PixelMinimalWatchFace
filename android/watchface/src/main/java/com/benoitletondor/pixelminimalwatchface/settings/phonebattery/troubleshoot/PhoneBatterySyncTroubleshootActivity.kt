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
package com.benoitletondor.pixelminimalwatchface.settings.phonebattery.troubleshoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Text
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.compose.WearTheme
import com.benoitletondor.pixelminimalwatchface.compose.component.ExplanationText
import com.benoitletondor.pixelminimalwatchface.compose.component.RotatoryAwareLazyColumn

class PhoneBatterySyncTroubleshootActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearTheme {
                RotatoryAwareLazyColumn(
                    horizontalPadding = 20.dp,
                ) {
                    item {
                        Text(
                            text = "(Beta) Phone battery sync troubleshoot",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                    }

                    item {
                        ExplanationText(
                            text = "To sync your phone battery with your watch, your phone needs to be able to send updates to your watch.",
                        )
                    }

                    item {
                        ExplanationText(
                            text = "This is still in beta as multiple things can fail during this process, from bluetooth issues to WearOS specific problems.",
                        )
                    }

                    item {
                        Column {
                            ExplanationText(
                                text = "Here are a few things you can try to make it work:",
                            )

                            ExplanationText(
                                text = "1. Make sure you have \"Pixel Minimal Watch Face\" app installed on your phone too\n(open it once to make sure it's alive)",
                            )

                            ExplanationText(
                                text = "2. Ensure both \"Pixel Minimal Watch Face\" and \"WearOS\" apps on your phone are up-to-date",
                            )

                            ExplanationText(
                                text = "3. Try disabling battery optimisation for \"Pixel Minimal Watch Face\" on your phone.",
                            )
                        }
                    }

                    item {
                        Column {
                            ExplanationText(
                                text = "If you cannot make it work, please send me an email and I'll try to help:",
                            )

                            Text(
                                text = stringResource(R.string.rating_feedback_email),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(bottom = 20.dp),
                            )
                        }

                    }
                }
            }
        }
    }
}