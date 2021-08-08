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