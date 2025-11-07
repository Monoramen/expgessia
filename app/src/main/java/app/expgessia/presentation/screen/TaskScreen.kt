package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import app.expgessia.presentation.ui.components.navbar.CustomTopAppBar
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.TaskItem
import java.time.LocalDate

@Composable
fun TaskRoute(
    selectedFilter: String, // ⭐️ ПОЛУЧАЕМ ВЫБРАННЫЙ ФИЛЬТР ИЗ MAINSCREEN
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()

    val (todayTasks, tomorrowTasks, completedTasks) = when (tasksState) {
        is TaskViewModel.TaskState.Success -> {
            val state = tasksState as TaskViewModel.TaskState.Success
            Triple(state.todayTasks, state.tomorrowTasks, state.completedTasks)
        }
        else -> Triple(emptyList(), emptyList(), emptyList())
    }

    // ⭐️ ФИЛЬТРАЦИЯ ПРОИСХОДИТ ЗДЕСЬ НА ОСНОВЕ ПЕРЕДАННОГО ФИЛЬТРА
    val filteredTasks = when (selectedFilter) {
        "Today" -> todayTasks
        "Tomorrow" -> tomorrowTasks
        "Completed" -> completedTasks
        else -> todayTasks + tomorrowTasks + completedTasks // All Tasks
    }

    TaskScreen(
        filteredTasks = filteredTasks, // ⭐️ ПЕРЕДАЕМ УЖЕ ОТФИЛЬТРОВАННЫЕ ЗАДАЧИ
        todayTasks = todayTasks,
        tomorrowTasks = tomorrowTasks,
        onTaskCheckClicked = { taskId ->
            viewModel.onTaskCheckClickedForDate(taskId, LocalDate.now())
        },
        onEditTaskClicked = onEditTaskClicked,
        onAddTaskClicked = onAddTaskClicked
    )
}

@Composable
fun TaskScreen(
    filteredTasks: List<TaskUiModel>, // ⭐️ ПОЛУЧАЕМ УЖЕ ОТФИЛЬТРОВАННЫЕ ЗАДАЧИ
    todayTasks: List<TaskUiModel>,
    tomorrowTasks: List<TaskUiModel>,
    onAddTaskClicked: () -> Unit,
    onTaskCheckClicked: (Long) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = hiltViewModel(),
) {
    // ⭐️ УБИРАЕМ СОСТОЯНИЕ ФИЛЬТРА ИЗ ЭТОГО ЭКРАНА

    LaunchedEffect(Unit) {
        taskViewModel.syncAllTasks()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // ⭐️ УБИРАЕМ CUSTOMTOPAPPBAR ОТСЮДА - ТЕПЕРЬ ОН В MAINSCREEN
        }
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
            items(filteredTasks, key = { "task_${it.id}_${it.date}" }) { task ->
                TaskItem(
                    task = task,
                    onTaskCheckClicked = { taskId ->
                        when {
                            todayTasks.any { it.id == taskId } ->
                                onTaskCheckClicked(taskId)
                            tomorrowTasks.any { it.id == taskId } -> {
                                val tomorrow = LocalDate.now().plusDays(1)
                                taskViewModel.onTaskCheckClickedForDate(taskId, tomorrow)
                            }
                            else -> onTaskCheckClicked(taskId)
                        }
                    },
                    onTaskEditClicked = onEditTaskClicked,
                )
            }
        }
    }
}