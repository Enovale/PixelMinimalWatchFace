package com.benoitletondor.pixelminimalwatchfacecompanion.view.donation.subviews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.AppMaterialTheme
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.components.LoadingLayout

@Composable
fun Loading() {
    LoadingLayout()
}

@Composable
@Preview(showSystemUi = true)
private fun LoadingPreview() {
    AppMaterialTheme {
        Loading()
    }
}