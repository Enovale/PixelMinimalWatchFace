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

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.productSansFontFamily
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.whiteButtonColors
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@Composable
fun NotPremium(viewModel: MainViewModel) {
    val context = LocalContext.current

    NotPremiumLayout(
        becomePremiumButtonPressed = {
            viewModel.launchPremiumBuyFlow(context as Activity)
        },
        redeemPromoCodeButtonPressed = viewModel::onRedeemPromoCodeButtonPressed,
        installWatchFaceButtonPressed = viewModel::onGoToInstallWatchFaceButtonPressed,
    )
}

@Composable
private fun NotPremiumLayout(
    becomePremiumButtonPressed: () -> Unit,
    redeemPromoCodeButtonPressed: () -> Unit,
    installWatchFaceButtonPressed: () -> Unit,
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

        SetupWatchFace(installWatchFaceButtonPressed = installWatchFaceButtonPressed)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Unlock premium features",
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(15.dp))

        Pager()

        Spacer(Modifier.height(16.dp))

        Text(
            text = "To unlock widgets, weather and battery indicators, become a premium user:",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = becomePremiumButtonPressed) {
            Text(text = "Become premium".uppercase())
        }
        Text(
            text = "(1 time payment, no hidden fees)",
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.onBackground,
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(onClick = redeemPromoCodeButtonPressed) {
            Text(text = "Redeem code".uppercase())
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun Pager() {
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalPager(
            count = 3,
            state = pagerState,
        ) { page ->
            PagerPage(page)
        }

        Spacer(Modifier.height(10.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun PagerPage(page: Int) {
    val (imagePainterResource, title) = when(page) {
        0 -> Pair(painterResource(R.drawable.inapp1), "4 customisable widgets, weather info, battery indicators, beautifully integrated")
        1 -> Pair(painterResource(R.drawable.inapp2), "Choose the widgets you want")
        else -> Pair(painterResource(R.drawable.inapp3), "Customize widgets & battery indicators accent color")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontFamily = productSansFontFamily,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(5.dp))

        Image(
            painter = imagePainterResource,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .height(200.dp),
        )
    }
}

@Composable
private fun SetupWatchFace(
    installWatchFaceButtonPressed: () -> Unit,
) {
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

    Spacer(modifier = Modifier.height(10.dp))

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = Color.DarkGray, shape = RoundedCornerShape(10))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Watch face not installed on your watch?",
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        TextButton(
            onClick = installWatchFaceButtonPressed,
            colors = whiteButtonColors(),
        ) {
            Text(text = "Install watch face".uppercase())
        }
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        NotPremiumLayout(
            becomePremiumButtonPressed = {},
            redeemPromoCodeButtonPressed = {},
            installWatchFaceButtonPressed = {},
        )
    }
}