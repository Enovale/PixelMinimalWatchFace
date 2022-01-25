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
package com.benoitletondor.pixelminimalwatchfacecompanion.view.debugphonebatterysync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.AppTopBarScaffold

@Composable
fun DebugPhoneBatterySync(
    navController: NavController,
    viewModel: DebugPhoneBatterySyncViewModel,
) {
    val state: DebugPhoneBatterySyncViewModel.State
        by viewModel.stateFlow.collectAsState()

    AppTopBarScaffold(
        navController = navController,
        showBackButton = true,
        title = "Debug phone battery sync",
        content = {
            DebugPhoneBatterySyncLayout(
                isBatteryOptimizationOff = state.isBatteryOptimizationOff,
            )
        }
    )
}

@Composable
private fun DebugPhoneBatterySyncLayout(
    isBatteryOptimizationOff: Boolean,
) {

}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization off")
private fun PreviewBatteryOptimOff() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = true,
        )
    }
}

@Composable
@Preview(showSystemUi = true, name = "Battery optimization on")
private fun PreviewBatteryOptimOn() {
    AppMaterialTheme {
        DebugPhoneBatterySyncLayout(
            isBatteryOptimizationOff = false,
        )
    }
}