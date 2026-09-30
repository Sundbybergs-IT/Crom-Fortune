package com.sundbybergsit.cromfortune.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.format.TextStyle

@Composable
fun DayPicker(
    modifier : Modifier = Modifier,
    selectedDays: MutableState<Set<DayOfWeek>>,
    onDaySelected: (DayOfWeek) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val rows = DayOfWeek.entries.chunked(4)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { days ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                days.forEach { day ->
                    val isSelected = day in selectedDays.value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .semantics { selected = isSelected }
                            .clip(CircleShape)
                            .clickable(role = Role.Checkbox) { onDaySelected(day) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DayCircle(
                            label = day.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                            selected = isSelected
                        )
                    }
                }
                repeat(4 - days.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCircle(label: String, selected: Boolean) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
}
