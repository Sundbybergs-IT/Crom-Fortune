package com.sundbybergsit.cromfortune.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(useDarkColors: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (useDarkColors) DarkColors else LightColors,
        shapes = Shapes(),
        content = content
    )
}

// Wrapper object to communicate explicit context for this composable function
object MenuColorComposables {

    @Composable
    fun translucentBarAlpha(): Float = when {
        // We use a more opaque alpha in light theme
        MaterialTheme.colors.isLight -> 0.97f
        else -> 0.94f
    }

}
