package com.example.jetcaster.ui.v2.favourite

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Favourite() {
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
            )
    ) {
        Text(text = "Favourite")
    }
}