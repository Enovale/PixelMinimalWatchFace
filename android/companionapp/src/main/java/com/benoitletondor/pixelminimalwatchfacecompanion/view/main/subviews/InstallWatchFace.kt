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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.whiteButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel

@Composable
fun InstallWatchFace(
    step: MainViewModel.Step.InstallWatchFace,
    viewModel: MainViewModel,
) {
    InstallWatchFaceLayout(
        appInstalledStatus = step.appInstalledStatus,
    )
}

@Composable
private fun InstallWatchFaceLayout(
    appInstalledStatus: MainViewModel.AppInstalledStatus,
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
            text = "Install the watch face on your watch",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(20.dp))

        when(appInstalledStatus) {
            is MainViewModel.AppInstalledStatus.Result -> when(appInstalledStatus.wearableStatus) {
                Sync.WearableStatus.AvailableAppInstalled -> {
                    Installed()
                }
                Sync.WearableStatus.AvailableAppNotInstalled -> {
                    AutoInstall()
                }
                is Sync.WearableStatus.Error, Sync.WearableStatus.NotAvailable -> {
                    ManualInstall()
                }
            }
            MainViewModel.AppInstalledStatus.Unknown -> {
                ManualInstall()
            }
            MainViewModel.AppInstalledStatus.Verifying -> {
                VerifyingInstallStatus()
            }
        }
    }
}

@Composable
private fun Installed() {
    Text(
        text = "Watch face detected on your watch",
        color = MaterialTheme.colors.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Image(
        painter = painterResource(R.drawable.ic_baseline_check_24),
        contentDescription = null,
        modifier = Modifier.size(50.dp),
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Make sure you activate it as your watch face, either directly on your watch or by using your phone.",
        color = MaterialTheme.colors.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = {},
        colors = whiteButtonColors(),
    ) {
        Text("Continue".uppercase())
    }

    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Watch face not installed?",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    InstallManuallyLayout()
}

@Composable
private fun AutoInstall() {
    InstallAutoLayout()
    SkipInstallLayout()
}

@Composable
private fun ManualInstall() {
    InstallManuallyLayout()
    SkipInstallLayout()
}

@Composable
private fun VerifyingInstallStatus() {
    Spacer(modifier = Modifier.height(10.dp))
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Verifying install status...",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )
}

@Composable
private fun InstallAutoLayout() {
    Text(
        text = "Automatic install",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "To install the watch face, you need to download it from the PlayStore on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = { },
    ) {
        Text(text = "Try launching PlayStore on watch".uppercase())
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Now continue on your watch and follow those steps:",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "1. Tap the install button",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "2. Once installed, activate it as your watch face either directly on your watch or from your phone",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = {},
        colors = whiteButtonColors(),
    ) {
        Text("Continue".uppercase())
    }

    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Automatic install not working?",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Sometimes automatic install is not working, no worries, you can still do it manually.",
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    InstallManuallyLayout()
}

@Composable
private fun InstallManuallyLayout() {
    Text(
        text = "Follow those 4 steps to install it on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "You need to do that on your watch directly, not from your phone",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
            .background(color = Color(0x22FFFFFF), shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = MaterialTheme.colors.onBackground,
        fontSize = 13.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "1. Open the PlayStore on your watch",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "2. Search for \"Pixel Minimal Watch Face\"",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "3. Tap the install button",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "4. Once installed, activate it as your watch face either directly on your watch or from your phone",
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = {},
        colors = whiteButtonColors(),
    ) {
        Text("Continue".uppercase())
    }
}

@Composable
private fun SkipInstallLayout() {
    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Watch face already installed?",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
        fontSize = 18.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "If the watch face is already installed on your watch, make sure you activate it and you can continue",
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.onBackground,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = {},
        colors = whiteButtonColors(),
    ) {
        Text("Continue".uppercase())
    }
}

@Composable
@Preview(showSystemUi = true, name = "Verifying")
private fun VerifyingPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Verifying)
}

@Composable
@Preview(showSystemUi = true, name = "Installed")
private fun InstalledPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.AvailableAppInstalled))
}

@Composable
@Preview(showSystemUi = true, name = "Not installed - manual")
private fun NotInstalledManualPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.NotAvailable))
}

@Composable
@Preview(showSystemUi = true, name = "Not installed - auto")
private fun NotInstalledAutoPreview() {
    Preview(appInstalledStatus = MainViewModel.AppInstalledStatus.Result(Sync.WearableStatus.AvailableAppNotInstalled))
}

@Composable
private fun Preview(
    appInstalledStatus: MainViewModel.AppInstalledStatus,
) {
    AppMaterialTheme {
        InstallWatchFaceLayout(
            appInstalledStatus = appInstalledStatus,
        )
    }
}