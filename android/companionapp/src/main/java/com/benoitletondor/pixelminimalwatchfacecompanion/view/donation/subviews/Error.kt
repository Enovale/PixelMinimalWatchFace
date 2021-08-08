package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.ErrorLayout

@Composable
fun Error(error: Throwable, onRetryButtonClicked: () -> Unit) {
    ErrorLayout(
        errorMessage = stringResource(R.string.donation_loading_error, error.message ?: ""),
        onRetryButtonClicked = onRetryButtonClicked,
    )
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    AppMaterialTheme {
        Error(RuntimeException("Test error")) {}
    }
}