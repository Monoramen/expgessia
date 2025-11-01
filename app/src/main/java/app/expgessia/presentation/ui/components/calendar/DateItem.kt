package app.expgessia.presentation.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateItem(
    date: LocalDate,
    isSelected: Boolean,
    onDateClicked: (LocalDate) -> Unit,
    scaleFactor: Float = 1f,
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    val month = date.format(monthFormatter).uppercase(Locale.getDefault())
    val dayOfWeek = date.format(dayOfWeekFormatter)
    val dayOfMonth = date.dayOfMonth.toString()
    val contentColor = MaterialTheme.colorScheme.onSurface

    val indicatorColor = androidx.compose.ui.graphics.Color(0xFFFFC107)

    Column(
        modifier = Modifier
            .graphicsLayer(scaleX = scaleFactor, scaleY = scaleFactor)
            .width(46.dp) // Фиксированная ширина ячейки
            .fillMaxHeight()
            .clickable { onDateClicked(date) }
            // Уменьшаем вертикальный отступ, чтобы дать больше места контенту
            .padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // ✅ ВЕРТИКАЛЬНОЕ ЦЕНТРИРОВАНИЕ КОНТЕНТА
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Сокращенный месяц
        Text(
            text = month,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor
        )

        Text(
            text = dayOfMonth,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(4.dp)
                    .background(indicatorColor), // Желтый цвет

            )
        } else {
            // Заглушка, чтобы не менялась общая высота ячейки при отсутствии индикатора
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}