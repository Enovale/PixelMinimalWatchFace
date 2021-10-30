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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.blueButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryBlue
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.primaryGreen
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun Premium(viewModel: MainViewModel) {
    PremiumLayout(
        installWatchFaceButtonPressed = viewModel::onGoToInstallWatchFaceButtonPressed,
        syncPremiumStatusButtonPressed = viewModel::triggerSync,
        donateButtonPressed = viewModel::onDonateButtonPressed,
        onSupportButtonPressed = viewModel::onSupportButtonPressed,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PremiumLayout(
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    donateButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )
    val coroutineScope = rememberCoroutineScope()
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Troubleshooting(
                installWatchFaceButtonPressed = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                    installWatchFaceButtonPressed()
                },
                syncPremiumStatusButtonPressed = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                    syncPremiumStatusButtonPressed()
                } ,
                onSupportButtonPressed = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                    onSupportButtonPressed()
                },
                onCloseButtonPressed = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            )
        },
        sheetPeekHeight = 0.dp,
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
                text = "You're premium!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Thank you so much for your support :)",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
            )

            Spacer(modifier = Modifier.height(30.dp))

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

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Troubleshooting",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "If you have any issue, tap here to troubleshoot",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        } else {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    }
                },
                colors = blueButtonColors(),
            ) {
                Text(text = "Troubleshoot".uppercase())
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = primaryGreen.copy(alpha = 0.7f), shape = RoundedCornerShape(10))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Feel like helping even more with a tip?",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = donateButtonPressed,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = primaryGreen,
                    ),
                ) {
                    Text(text = "Donate".uppercase())
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Troubleshooting(
    installWatchFaceButtonPressed: () -> Unit,
    syncPremiumStatusButtonPressed: () -> Unit,
    onSupportButtonPressed: () -> Unit,
    onCloseButtonPressed: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopEnd,
    ) {
        Button(
            onClick = onCloseButtonPressed,
            shape = CircleShape,
            modifier = Modifier
                .padding(10.dp)
                .width(40.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black,
            ),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = "X")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Troubleshooting",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "- Watch face is not installed on your watch?",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.Bold,
            )

            TextButton(
                onClick = installWatchFaceButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(text = "Install watch face".uppercase())
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "- Watch face doesn't recognize you as premium?",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.Bold,
            )

            TextButton(
                onClick = syncPremiumStatusButtonPressed,
                colors = blueButtonColors(),
            ) {
                Text(text = "Sync premium with Watch".uppercase())
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
                onClick = onSupportButtonPressed,
            ) {
                Text(text = "Contact me for support".uppercase())
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
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
            onSupportButtonPressed = {},
        )
    }
}

@Composable
@Preview(name = "Troubleshooting")
private fun TroubleshootPreview() {
    AppMaterialTheme {
        Troubleshooting(
            onSupportButtonPressed = {},
            installWatchFaceButtonPressed = {},
            syncPremiumStatusButtonPressed = {},
            onCloseButtonPressed = {},
        )
    }
}