package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.expgessia.R
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.RetroTaskCategoryCompact
import app.expgessia.ui.components.TaskItem
import java.time.LocalDate

@Composable
fun TaskRoute(
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()

    // 🔥 Берем ВСЕ задачи из состояния
    val (todayTasks, tomorrowTasks, completedTasks) = when (tasksState) {
        is TaskViewModel.TaskState.Success -> {
            val state = tasksState as TaskViewModel.TaskState.Success
            Triple(state.todayTasks, state.tomorrowTasks, state.completedTasks)
        }
        else -> Triple(emptyList(), emptyList(), emptyList())
    }

    TaskScreen(
        todayTasks = todayTasks,
        tomorrowTasks = tomorrowTasks,
        completedTasks = completedTasks,
        onTaskCheckClicked = { taskId ->
            viewModel.onTaskCheckClickedForDate(taskId, LocalDate.now())
        },
        onEditTaskClicked = onEditTaskClicked,
        onAddTaskClicked = onAddTaskClicked
    )
}

@Composable
fun TaskScreen(
    // 💡 Принимаем ВСЕ списки задач
    todayTasks: List<TaskUiModel>,
    tomorrowTasks: List<TaskUiModel>,
    completedTasks: List<TaskUiModel>,
    onAddTaskClicked: () -> Unit,
    onTaskCheckClicked: (Long) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = hiltViewModel(),
) {
    // 🔥 Состояния для управления видимостью секций
    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(true) }
    var showCompleted by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        taskViewModel.syncAllTasks()
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(
                start = 2.dp,
                end = 2.dp,
                top = 2.dp,
                bottom = paddingValues.calculateBottomPadding() + 4.dp
            )
        ) {
            // --- СЕКЦИЯ СЕГОДНЯ ---
            item {
                RetroTaskCategoryCompact(
                    title = stringResource(R.string.label_today),
                    count = todayTasks.size,
                    isExpanded = showToday,
                    onToggle = { showToday = !showToday }
                )
            }

            if (showToday) {
                items(todayTasks, key = { "today_${it.id}" }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckClicked = onTaskCheckClicked,
                        onTaskEditClicked = onEditTaskClicked,
                    )
                }
            }

            // --- СЕКЦИЯ ЗАВТРА ---
            item {
                RetroTaskCategoryCompact(
                    title = stringResource(R.string.label_tomorrow),
                    count = tomorrowTasks.size,
                    isExpanded = showTomorrow,
                    onToggle = { showTomorrow = !showTomorrow }
                )
            }

            if (showTomorrow) {
                items(tomorrowTasks, key = { "tomorrow_${it.id}" }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckClicked = { taskId ->
                            val tomorrow = LocalDate.now().plusDays(1)
                            // 🔥 Для завтрашних задач используем завтрашнюю дату
                            onTaskCheckClicked(taskId) // или можно передать отдельный обработчик
                        },
                        onTaskEditClicked = onEditTaskClicked,
                    )
                }
            }

            // --- СЕКЦИЯ ЗАВЕРШЕННЫХ ---
            item {
                RetroTaskCategoryCompact(
                    title = stringResource(R.string.label_completed),
                    count = completedTasks.size,
                    isExpanded = showCompleted,
                    onToggle = { showCompleted = !showCompleted }
                )
            }

            if (showCompleted) {
                items(completedTasks, key = { "completed_${it.id}" }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckClicked = onTaskCheckClicked,
                        onTaskEditClicked = onEditTaskClicked,
                    )
                }
            }
        }
    }
}