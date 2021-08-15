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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.ErrorLayout
import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel

@Composable
fun Error(state: MainViewModel.State.Error, onRetryButtonClicked: () -> Unit) {
    ErrorLayout(
        errorMessage = stringResource(R.string.premium_error, state.error.localizedMessage ?: ""),
        onRetryButtonClicked = onRetryButtonClicked,
    )
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        ErrorLayout(
            errorMessage = "Preview error message",
            onRetryButtonClicked = {},
        )
    }
}