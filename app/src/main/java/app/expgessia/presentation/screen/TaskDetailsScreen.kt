package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.ui.components.calendar.DateCarousel
import app.expgessia.presentation.viewmodel.CalendarViewModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.TaskItem
import app.expgessia.utils.TimeUtils
import java.time.LocalDate


@Composable
fun TaskDetailsScreen(
    date: LocalDate,
    onSelectedDateChange: (LocalDate) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
) {
    var selectedDate by remember { mutableStateOf(date) }

    val dailyTasksUi by calendarViewModel.getTasksForDate(selectedDate)
        .collectAsState(initial = emptyList())

    val refreshTrigger by taskViewModel.refreshTrigger.collectAsState()

    LaunchedEffect(selectedDate, refreshTrigger) {
        onSelectedDateChange(selectedDate)
        // 💡 ВАЖНО: Подготавливаем задачи для выбранной даты
        calendarViewModel.prepareTasksForDate(selectedDate)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DateCarousel(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    selectedDate = newDate
                }
            )

            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(dailyTasksUi, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckClicked = { taskId ->
                            taskViewModel.onTaskCheckClickedForDate(taskId, selectedDate) {
                                // После изменения статуса обновляем оба ViewModel
                                taskViewModel.forceRefresh()
                                calendarViewModel.refreshTasksForDate(selectedDate)
                            }
                        },
                        onTaskEditClicked = { taskId ->
                            onEditTaskClicked(taskId)
                        }
                    )
                }
            }
        }
    }
}