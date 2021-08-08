package com.benoitletondor.pixelminimalwatchfacecompanion.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.benoitletondor.pixelminimalwatchfacecompanion.ui.productSansFontFamily

@Composable
fun AppTopBarScaffold(
    navController: NavController,
    showBackButton: Boolean,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    @Composable
    fun NavigationIcon(
        navController: NavController,
        showBackButton: Boolean,
    ): @Composable (() -> Unit)? {
        if (!showBackButton) {
            return null
        }

        return { IconButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Up button")
        } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Medium,
                    )
                },
                actions = actions,
                navigationIcon = NavigationIcon(navController, showBackButton),
                elevation = 0.dp,
            )
        },
        content = content
    )
}