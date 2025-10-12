package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val datePickerState = rememberDatePickerState()

    // Конвертируем миллисекунды в читаемую дату
    val selectedDateText = datePickerState.selectedDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    } ?: "No date selected"

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Selected: $selectedDateText",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Главное: showModeToggle = false
        DatePicker(
            state = datePickerState,
            showModeToggle = false, // ← Только календарь, без кнопки "Edit"
            modifier = Modifier.fillMaxWidth()
        )
    }
}