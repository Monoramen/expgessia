package app.expgessia.presentation.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate

data class Task(val id: Long, val name: String)

val dailyTasks = listOf(
    Task(1, "Read 25 pages "),
    Task(2, "Morning running"),
    Task(3, "Make the bed"),
    Task(4, "Relax eyes for 15 min"),
    Task(5, "Contact relatives"),
    Task(6, "Review my tasks"),
    Task(7, "Brush teeth")
)
// ⭐️ НОВАЯ ВСПОМОГАТЕЛЬНАЯ КОМПОНЕНТА ДЛЯ ОТОБРАЖЕНИЯ ОДНОГО МЕСЯЦА
@Composable
fun CalendarMonthGrid(
    month: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    offset: Modifier, // Modifier уже содержит offset.dp
    dividerColor: Color,
    onDayClicked: (LocalDate) -> Unit,
) {
    val gridBackgroundColor = Color(0xFF1E1E1E)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.SpaceBetween, // ⭐️ Гарантирует заполнение всей высоты
        modifier = offset
            .fillMaxHeight()
            .fillMaxWidth()
            .background(dividerColor)
            .border(1.dp, defaultBorderColor)
            .padding(1.dp).background(gridBackgroundColor)
    ) {
        LazyGridContent(month,tasksByDate, onDayClicked, ) // Вызываем функцию расширения
    }
}


// ⭐️ ФУНКЦИЯ РАСШИРЕНИЯ ДЛЯ LazyGridScope
// В CalendarMonthGrid.kt
// ⭐️ ФУНКЦИЯ РАСШИРЕНИЯ ДЛЯ LazyGridScope
fun LazyGridScope.LazyGridContent(
    month: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>, // ⭐️ Используем переданные данные
    onDayClicked: (LocalDate) -> Unit,
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.dayOfWeek
    val offset = (firstDayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7

    // 2. Дни предыдущего месяца для заполнения
    items(offset) { index ->
        val prevMonth = month.minusMonths(1)
        val day = prevMonth.lengthOfMonth() - offset + index + 1
        val dateLocal = prevMonth.withDayOfMonth(day) // ⭐️ ИСПРАВЛЕНИЕ: Правильно вычисляем dateLocal
        TaskDayCell(
            dateLocal = dateLocal,
            tasks = tasksByDate[dateLocal] ?: emptyList(), // ✅ Используем tasksByDate
            isToday = false,
            isCurrentMonth = false,
            onDayClicked = onDayClicked,
        )
    }

    // Дни текущего месяца
    items(daysInMonth) { index ->
        val day = index + 1
        val dateLocal = month.withDayOfMonth(day)
        TaskDayCell(
            dateLocal = dateLocal,
            tasks = tasksByDate[dateLocal] ?: emptyList(), // ✅ Используем tasksByDate
            isToday = dateLocal == LocalDate.now(),
            isCurrentMonth = true,
            onDayClicked = onDayClicked,
        )
    }

    // 3. Заполнение оставшихся ячеек днями следующего месяца
    val currentCells = offset + daysInMonth
    val totalNextMonthCells = (7 - (currentCells % 7)) % 7

    if (totalNextMonthCells > 0) {
        val nextMonth = month.plusMonths(1)
        items(totalNextMonthCells) { index ->
            val dateLocal = nextMonth.withDayOfMonth(index + 1) // ⭐️ ИСПРАВЛЕНИЕ: Правильно вычисляем dateLocal
            TaskDayCell(
                dateLocal = dateLocal,
                tasks = tasksByDate[dateLocal] ?: emptyList(), // ⭐️ ИСПОЛЬЗУЕМ tasksByDate
                isToday = false,
                isCurrentMonth = false,
                onDayClicked = onDayClicked,
            )
        }
    }
}
