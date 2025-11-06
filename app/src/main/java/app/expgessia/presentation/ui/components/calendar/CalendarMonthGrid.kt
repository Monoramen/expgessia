package app.expgessia.presentation.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate

data class CalendarSimpleTask(
    val id: Long,
    val name: String,
    val isCompleted: Boolean = false // 🔥 ДОБАВЛЕНО: статус выполнения
)

@Composable
fun CalendarMonthGrid(
    month: LocalDate,
    tasksByDate: Map<LocalDate, List<CalendarSimpleTask>>,
    offset: Modifier,
    dividerColor: Color,
    onDayClicked: (LocalDate) -> Unit,
    cellHeight: androidx.compose.ui.unit.Dp // <-- новый параметр
) {
    val gridBackgroundColor = Color(0xFF1A1D18)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.Top,
        horizontalArrangement = Arrangement.Center,
        modifier = offset
            .fillMaxSize()
            .background(gridBackgroundColor)
    ) {
        LazyGridContent(
            month,
            tasksByDate,
            onDayClicked,
            dividerColor,
            cellHeight // <-- прокидываем далее
        )
    }
}


fun LazyGridScope.LazyGridContent(
    month: LocalDate,
    tasksByDate: Map<LocalDate, List<CalendarSimpleTask>>,
    onDayClicked: (LocalDate) -> Unit,
    dividerColor: Color,
    cellHeight: androidx.compose.ui.unit.Dp
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.dayOfWeek
    val offset = (firstDayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val currentCells = offset + daysInMonth
    val totalNextMonthCells = (7 - (currentCells % 7)) % 7

    items(offset) { index ->
        val prevMonth = month.minusMonths(1)
        val day = prevMonth.lengthOfMonth() - offset + index + 1
        val dateLocal = prevMonth.withDayOfMonth(day)

        TaskDayCell(
            dateLocal = dateLocal,
            tasks = tasksByDate[dateLocal] ?: emptyList(),
            isToday = false,
            isCurrentMonth = false,
            onDayClicked = onDayClicked,
            cellHeight = cellHeight // <-- передаём высоту
        )
    }

    items(daysInMonth) { index ->
        val day = index + 1
        val dateLocal = month.withDayOfMonth(day)
        TaskDayCell(
            dateLocal = dateLocal,
            tasks = tasksByDate[dateLocal] ?: emptyList(),
            isToday = dateLocal == LocalDate.now(),
            isCurrentMonth = true,
            onDayClicked = onDayClicked,
            cellHeight = cellHeight
        )
    }

    if (totalNextMonthCells > 0) {
        val nextMonth = month.plusMonths(1)
        items(totalNextMonthCells) { index ->
            val dateLocal = nextMonth.withDayOfMonth(index + 1)
            TaskDayCell(
                dateLocal = dateLocal,
                tasks = tasksByDate[dateLocal] ?: emptyList(),
                isToday = false,
                isCurrentMonth = false,
                onDayClicked = onDayClicked,
                cellHeight = cellHeight
            )
        }
    }
}
