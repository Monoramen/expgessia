package app.expgessia.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.expgessia.R
import app.expgessia.presentation.ui.theme.DigitLargeStyle
import app.expgessia.presentation.ui.theme.FalloutFontFamilyDigits
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    var displayedDateTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            // Формат: "22 октября 2025, 14:35"
            displayedDateTime = now.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.getDefault())
            ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            val secondsUntilNextMinute = 60 - now.second
            delay(secondsUntilNextMinute * 1000L)
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 🔹 Первая строка: текущая дата и время
        Text(
            text = stringResource(R.string.label_today) + displayedDateTime,
            style = DigitLargeStyle,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Вторая строка: выбранная дата (из календаря)
        Text(
            text = "Selected: ${
                selectedDate?.let { millis ->
                    Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                } ?: "No date selected"
            }",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomCalendar(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}




@Composable
fun CustomCalendar(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.dayOfWeek

    // Формат для отображения "Март 2025"
    val monthYearText = currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()).withLocale(Locale.getDefault()))

    // Дни недели: Пн, Вт...
    val daysOfWeek = DayOfWeek.values().toList().map {
        it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replace(".", "")
    }

    Column(modifier = modifier) {
        // Заголовок с месяцем/годом и кнопками навигации
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous month"
                )
            }

            Text(
                text = monthYearText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                currentMonth = currentMonth.plusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next month"
                )
            }
        }

        // Сам календарь
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(360.dp) // компактно
        ) {
            // Заголовки дней недели
            items(daysOfWeek) { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Пустые ячейки до первого дня
            val offset = (firstDayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
            items(offset) {
                Box(modifier = Modifier.fillMaxSize())
            }

            // Дни месяца
            items(daysInMonth) { index ->
                val day = index + 1
                val dateLocal = currentMonth.withDayOfMonth(day)
                val dateMillis = dateLocal.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                val isSelected = selectedDate == dateMillis
                val isToday = dateLocal == LocalDate.now()

                val containerColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surface
                }

                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable { onDateSelected(dateMillis) }
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FalloutFontFamilyDigits,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}