package com.sundbybergsit.cromfortune.main.ui

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary)
    ) {
        ButtonText(text = text)
    }
}

@Composable
internal fun ButtonText(text: String, color: Color = Color.Unspecified) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
}
