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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.main.subviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.whiteButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel

@Composable
fun Premium(viewModel: MainViewModel) {
    PremiumLayout(
        installWatchFaceButtonPressed = viewModel::onGoToInstallWatchFaceButtonPressed,
        syncPremiumStatusButtonPressed = viewModel::triggerSync,
        donateButtonPressed = viewModel::onDonateButtonPressed,
    )
}

@Composable
private fun PremiumLayout(
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    donateButtonPressed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "You're premium! Thank you so much for your support :)",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Setup the watch face",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.setup_watch_face_instructions),
            color = MaterialTheme.colors.onBackground,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Troubleshooting",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "- Watch face doesn't recognize you as premium?",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
        )

        TextButton(
            onClick = syncPremiumStatusButtonPressed,
            colors = whiteButtonColors(),
        ) {
            Text(text = "Sync premium with Watch".uppercase())
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "- Watch face is not installed on your watch?",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
        )

        TextButton(
            onClick = installWatchFaceButtonPressed,
            colors = whiteButtonColors(),
        ) {
            Text(text = "Install watch face".uppercase())
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "- Sync doesn't work? Have another issue? I'm here to help",
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = installWatchFaceButtonPressed,
        ) {
            Text(text = "Contact me for support".uppercase())
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = stringResource(R.string.donation_description),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = donateButtonPressed,
            colors = blueButtonColors(),
        ) {
            Text(text = stringResource(R.string.donation_cta).uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        PremiumLayout(
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            donateButtonPressed = {},
        )
    }
}