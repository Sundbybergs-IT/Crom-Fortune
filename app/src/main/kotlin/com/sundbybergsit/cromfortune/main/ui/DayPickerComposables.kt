package com.sundbybergsit.cromfortune.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek

@Composable
fun DayPicker(
    modifier : Modifier = Modifier,
    selectedDays: MutableState<Set<DayOfWeek>>,
    onDaySelected: (DayOfWeek) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DayOfWeek.values().forEach { day ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selectedDays.value.contains(day)) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(1.dp, Color.Gray, CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        onDaySelected(day)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.name.take(1),
                    color = if (selectedDays.value.contains(day)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
