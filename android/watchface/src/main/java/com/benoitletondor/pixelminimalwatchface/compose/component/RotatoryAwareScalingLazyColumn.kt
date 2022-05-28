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
@file:OptIn(ExperimentalComposeUiApi::class)

package com.benoitletondor.pixelminimalwatchface.compose.component

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListScope
import androidx.wear.compose.material.rememberScalingLazyListState
import kotlinx.coroutines.launch

@Composable
fun RotatoryAwareScalingLazyColumn(
    modifier: Modifier = Modifier,
    autoCentering: AutoCenteringParams = AutoCenteringParams(itemIndex = 0),
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    content: ScalingLazyListScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScalingLazyListState()
    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ScalingLazyColumn(
        modifier = modifier
            .onRotaryScrollEvent { scrollEvent ->
                lifecycleScope.launch {
                    scrollState.scrollBy(scrollEvent.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        state = scrollState,
        contentPadding = contentPadding,
        autoCentering = autoCentering,
        content = content,
    )
}