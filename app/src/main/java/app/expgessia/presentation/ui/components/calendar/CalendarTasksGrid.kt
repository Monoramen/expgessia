package app.expgessia.presentation.ui.components.calendar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.presentation.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

// ⭐️ АЛИАС: Используем алиас для Domain Task
import app.expgessia.domain.model.Task as DomainTask
import kotlinx.coroutines.flow.map
val defaultBorderColor = Color(0xFF656865)
/**
 * Преобразует Map<LocalDate, List<DomainTask>> (из ViewModel)
 * в Map<LocalDate, List<Task>> (для UI-компонента CalendarMonthGrid).
 */
private fun toUITaskMap(
    domainTaskMap: Map<LocalDate, List<DomainTask>>
): Map<LocalDate, List<Task>> {
    return domainTaskMap.mapValues { (_, domainTasks) ->
        domainTasks.map { domainTask ->
            // Маппинг: DomainTask -> локальный UI Task
            Task(id = domainTask.id, name = domainTask.title)
        }
    }
}


@Composable
fun CalendarTasksGrid(
    currentMonth: LocalDate,
    onDayClicked: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    // ==========================================================
    // ДАННЫЕ И СОСТОЯНИЯ
    // ==========================================================

    // --- Получение задач для 3 месяцев ---
    val currentMonthTasks by viewModel
        .getTasksForMonth(currentMonth)
        .map(::toUITaskMap)
        .collectAsState(initial = emptyMap())

    val previousMonth = remember(currentMonth) { currentMonth.minusMonths(1) }
    val previousMonthTasks by viewModel
        .getTasksForMonth(previousMonth)
        .map(::toUITaskMap)
        .collectAsState(initial = emptyMap())

    val nextMonth = remember(currentMonth) { currentMonth.plusMonths(1) }
    val nextMonthTasks by viewModel
        .getTasksForMonth(nextMonth)
        .map(::toUITaskMap)
        .collectAsState(initial = emptyMap())

    // --- Состояния анимации/жестов ---
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    var fullWidthPx by remember { mutableStateOf(0f) }
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }
    val dividerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    val onSwipeAction: (Float) -> Unit = { finalOffset ->
        coroutineScope.launch {
            if (finalOffset > fullWidthPx / 4) {
                animatedOffset.animateTo(fullWidthPx, animationSpec = spring(dampingRatio = 0.8f))
                onPreviousMonth()
            } else if (finalOffset < -fullWidthPx / 4) {
                animatedOffset.animateTo(-fullWidthPx, animationSpec = spring(dampingRatio = 0.8f))
                onNextMonth()
            } else {
                animatedOffset.animateTo(0f, animationSpec = spring())
            }
            dragOffset = 0f
            if (animatedOffset.value != 0f) {
                animatedOffset.snapTo(0f)
            }
        }
    }

    LaunchedEffect(currentMonth) {
        animatedOffset.snapTo(0f)
        dragOffset = 0f
    }

    // --- Заголовки дней недели ---
    val daysOfWeek = remember { DayOfWeek.values() }
    val mondayFirstDays = remember { daysOfWeek.slice(1..6) + daysOfWeek[0] }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // ⭐️ ИСПРАВЛЕНО: Устанавливаем серый фон для Row
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 4.dp)
        ) {
            mondayFirstDays.forEach { day ->
                Text(
                    text = day.name.substring(0, 3),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    // Цвет текста сделаем более контрастным, чем приглушенный фон
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Сетка Календаря с Анимацией ---
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .border(1.dp, defaultBorderColor)
                .onGloballyPositioned { coordinates ->
                    fullWidthPx = coordinates.size.width.toFloat()
                }
                .pointerInput(currentMonth) {
                    detectHorizontalDragGestures(
                        onDragEnd = { onSwipeAction(dragOffset) },
                        onDragCancel = { onSwipeAction(dragOffset) },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                        }
                    )
                }
        ) {

            val totalOffsetPx = animatedOffset.value + dragOffset
            val totalOffsetDp = (totalOffsetPx / density).dp

            // ⭐️ 1. Сетка текущего месяца (А)
            CalendarMonthGrid(
                month = currentMonth,
                tasksByDate = currentMonthTasks,
                offset = Modifier.offset(x = totalOffsetDp),
                dividerColor = dividerColor,
                onDayClicked = onDayClicked
            )

            // ⭐️ 2. Сетка следующего месяца (B) - появляется справа
            if (totalOffsetPx < 0) {
                CalendarMonthGrid(
                    month = nextMonth,
                    tasksByDate = nextMonthTasks,
                    // Смещение: TotalOffset + FullWidth
                    offset = Modifier.offset(x = (totalOffsetPx + fullWidthPx).roundToInt().div(density).dp),
                    dividerColor = dividerColor,
                    onDayClicked = onDayClicked
                )
            }

            // ⭐️ 3. Сетка предыдущего месяца (С) - появляется слева
            if (totalOffsetPx > 0) {
                CalendarMonthGrid(
                    month = previousMonth,
                    tasksByDate = previousMonthTasks,
                    // Смещение: TotalOffset - FullWidth
                    offset = Modifier.offset(x = (totalOffsetPx - fullWidthPx).roundToInt().div(density).dp),
                    dividerColor = dividerColor,
                    onDayClicked = onDayClicked
                )
            }
        }
    }
}