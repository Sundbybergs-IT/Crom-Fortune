package com.sundbybergsit.cromfortune.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val buttonHeightInDp = 44.dp

@Composable
internal fun DialogButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier.height(buttonHeightInDp), onClick = onClick, border = null,
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
    ) {
        ButtonText(text = text)
    }
}

@Composable
internal fun ButtonText(text: String, color: Color = Color.Unspecified) {
    Text(text = text, style = MaterialTheme.typography.button, color = color)
}
