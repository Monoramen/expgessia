package app.expgessia.presentation.screen

// ⭐️ Импорт нового компонента
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.ui.components.calendar.DateCarousel
import app.expgessia.ui.components.TaskItem
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.domain.model.Task
import app.expgessia.presentation.viewmodel.CalendarViewModel
import app.expgessia.presentation.viewmodel.TaskViewModel

@Composable
fun TaskDetailsScreen(
    date: LocalDate, // Исходная дата из навигации
    onSelectedDateChange: (LocalDate) -> Unit,
    onTaskCheckClicked: (Long) -> Unit = {},
    onEditTaskClicked: (Long) -> Unit = {},
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    // ⭐️ ИСПОЛЬЗУЕМ СОСТОЯНИЕ ДЛЯ ТЕКУЩЕЙ ВЫБРАННОЙ ДАТЫ
    var selectedDate by remember { mutableStateOf(date) }

    // ⭐️ Определяем месяц для запроса задач
    val currentMonth = remember(selectedDate.year, selectedDate.month) {
        selectedDate.withDayOfMonth(1)
    }

    // 1. Загружаем все сырые задачи на месяц (Map<LocalDate, List<Task>>)
    // Этот Flow теперь реактивный благодаря исправлениям в CalendarViewModel.
    val allMonthlyTasksMap by calendarViewModel
        .getTasksForMonth(currentMonth)
        .collectAsState(initial = emptyMap())

    // 2. Состояние для списка задач в UI-модели (TaskUiModel)
    var dailyTasksUi by remember { mutableStateOf(emptyList<TaskUiModel>()) }

    // 3. Извлекаем сырые задачи List<Task> для выбранной даты
    val tasksForDay: List<Task> = allMonthlyTasksMap[selectedDate] ?: emptyList()

    // 4. Асинхронный маппинг и логика отображения статуса
    // Сработает повторно, когда tasksForDay изменится (т.е. после выполнения задачи)
    LaunchedEffect(tasksForDay, selectedDate) {
        // Вызываем suspend-функцию mapToUiModel из TaskViewModel
        val mappedTasks = taskViewModel.mapToUiModel(tasksForDay)
        onSelectedDateChange(selectedDate)
        // ФИКС ПРОБЛЕМЫ №2: Корректировка статуса выполнения на будущие дни
        val tasksForUi = mappedTasks.map { uiModel ->
            if (selectedDate.isAfter(LocalDate.now())) {
                // Если дата в будущем, сбрасываем статус (до фактического сброса в БД)
                uiModel.copy(isCompleted = false)
            } else {
                uiModel
            }
        }
        dailyTasksUi = tasksForUi
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ⭐️ 1. Карусель дат
            DateCarousel(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    selectedDate = newDate // Обновляем выбранную дату
                }
            )

            // ⭐️ 2. Список задач
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(dailyTasksUi) { task ->
                    TaskItem(
                        task = task,
                        // ⭐️ ФИКС ПРОБЛЕМЫ №1: Вызов логики выполнения задачи
                        onTaskCheckClicked = taskViewModel::onTaskCheckClicked,
                        onTaskEditClicked = { onEditTaskClicked(task.id) }
                    )
                }
            }
        }
    }
}