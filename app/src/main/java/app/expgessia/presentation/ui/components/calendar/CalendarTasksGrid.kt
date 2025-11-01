package app.expgessia.presentation.ui.components.calendar

// ⭐️ АЛИАС: Используем алиас для Domain Task
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.presentation.viewmodel.CalendarViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt
import app.expgessia.domain.model.Task as DomainTask

val defaultBorderColor = Color(0xFFCCD7CC)

private fun toUITaskMap(
    domainTaskMap: Map<LocalDate, List<DomainTask>>,
): Map<LocalDate, List<CalendarSimpleTask>> {
    return domainTaskMap.mapValues { (_, domainTasks) ->
        domainTasks.map { domainTask ->
            // Маппинг: DomainTask -> локальный UI Task
            CalendarSimpleTask(id = domainTask.id, name = domainTask.title)
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

    val currentMonthTasks by viewModel.getTasksForMonth(currentMonth).map(::toUITaskMap)
        .collectAsState(initial = emptyMap())

    val previousMonth = remember(currentMonth) { currentMonth.minusMonths(1) }
    val previousMonthTasks by viewModel.getTasksForMonth(previousMonth).map(::toUITaskMap)
        .collectAsState(initial = emptyMap())

    val nextMonth = remember(currentMonth) { currentMonth.plusMonths(1) }
    val nextMonthTasks by viewModel.getTasksForMonth(nextMonth).map(::toUITaskMap)
        .collectAsState(initial = emptyMap())


    // --- Состояния анимации/жестов ---
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    var fullWidthPx by remember { mutableStateOf(0f) }
    var fullHeightPx by remember { mutableStateOf(0f) } // <-- новоe
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }
    val dividerColor = Color(0xFF807B7B)

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
    val dayHeaderHeightDp = 26.dp // <- твой Row с днями недели занимает ~30–40dp
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(dayHeaderHeightDp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .drawBehind {
                    val strokeWidth = 0.1.dp.toPx()
                    val columnWidth = size.width / 7f
                    val halfHeight = size.height / 2f

                    // Рисуем вертикальные линии между заголовками
                    for (i in 1 until 7) {
                        val x = columnWidth * i
                        drawLine(
                            color = Color(0xFF979797),
                            start = androidx.compose.ui.geometry.Offset(x, halfHeight),   // половина высоты
                            end = androidx.compose.ui.geometry.Offset(x, size.height),   // вниз до конца
                            strokeWidth = strokeWidth
                        )
                    }
                }
                .padding(vertical = 2.dp)
        ) {
            mondayFirstDays.forEach { day ->
                Text(
                    text = day.name.substring(0, 3),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }


        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                fullWidthPx = coordinates.size.width.toFloat()
                fullHeightPx = coordinates.size.height.toFloat() // <-- читаем высоту
            }
            .pointerInput(currentMonth) {
                detectHorizontalDragGestures(
                    onDragEnd = { onSwipeAction(dragOffset) },
                    onDragCancel = { onSwipeAction(dragOffset) },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    })
            }) {
            val totalOffsetPx = animatedOffset.value + dragOffset
            val totalOffsetDp = (totalOffsetPx / density).dp

            // высоту хедера лучше измерить, но можно временно фиксированно:

            val cellHeightDp = if (fullHeightPx > 0f) {
                ((fullHeightPx / 5f) / density).dp
            } else {
                0.dp
            }


            CalendarMonthGrid(
                month = currentMonth,
                tasksByDate = currentMonthTasks,
                offset = Modifier.offset(x = totalOffsetDp),
                dividerColor = dividerColor,
                onDayClicked = onDayClicked,
                cellHeight = cellHeightDp // <-- передаём сюда
            )

            if (totalOffsetPx < 0) {
                CalendarMonthGrid(
                    month = nextMonth,
                    tasksByDate = nextMonthTasks,
                    offset = Modifier.offset(
                        x = (totalOffsetPx + fullWidthPx).roundToInt().div(density).dp
                    ),
                    dividerColor = dividerColor,
                    onDayClicked = onDayClicked,
                    cellHeight = cellHeightDp
                )
            }

            if (totalOffsetPx > 0) {
                CalendarMonthGrid(
                    month = previousMonth,
                    tasksByDate = previousMonthTasks,
                    offset = Modifier.offset(
                        x = (totalOffsetPx - fullWidthPx).roundToInt().div(density).dp
                    ),
                    dividerColor = dividerColor,
                    onDayClicked = onDayClicked,
                    cellHeight = cellHeightDp
                )
            }
        }
    }
}