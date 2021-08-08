package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benoitletondor.pixelminimalwatchfacecompanion.R

@Composable
fun ErrorLayout(
    errorTitle: String = stringResource(R.string.premium_error_title),
    errorMessage: String,
    onRetryButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 26.dp)
            .fillMaxHeight(fraction = 0.9f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = errorTitle,
            color = MaterialTheme.colors.onBackground,
            fontSize = 22.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colors.onBackground,
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onRetryButtonClicked) {
            Text(stringResource(R.string.error_retry_cta).uppercase())
        }
    }
}