package app.expgessia.presentation.screen



import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.R
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.presentation.viewmodel.UserViewModel
import app.expgessia.ui.components.RetroTaskCategoryCompact
import app.expgessia.ui.components.TaskItem
import app.expgessia.ui.components.UserCard
import java.time.LocalDate

@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        Log.d("UserScreen", "🔄 Syncing all tasks...")
        taskViewModel.syncAllTasks()
    }

    val user by userViewModel.user.collectAsState(initial = null)
    val isLoading by userViewModel.isLoading.collectAsState()
    val tasksState by taskViewModel.tasksState.collectAsState()

    // Состояния для управления видимостью
    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }

    // 🔥 ОБНОВЛЯЕМ ДАННЫЕ ТОЛЬКО КОГДА СЕКЦИЯ ОТКРЫТА
    LaunchedEffect(showToday, showTomorrow, showCompleted) {
        if (showToday || showTomorrow || showCompleted) {
            // Обновляем данные только при открытых секциях
            taskViewModel.forceRefresh()
            Log.d("UserScreen", "🔄 Refreshing data for open sections")
        }
    }

    // 🔥 ИСПОЛЬЗУЕМ ДАННЫЕ ИЗ STATE ДЛЯ ОТОБРАЖЕНИЯ ВСЕГДА
    val (todayTasks, tomorrowTasks, completedTasks) = when (tasksState) {
        is TaskViewModel.TaskState.Success -> {
            val state = tasksState as TaskViewModel.TaskState.Success
            Triple(state.todayTasks, state.tomorrowTasks, state.completedTasks)
        }
        else -> Triple(emptyList(), emptyList(), emptyList())
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                UserCard(
                    user = user,
                    onNameEdit = { newName ->
                        userViewModel.updateUserName(newName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // --- TODAY SECTION ---
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_today),
                count = todayTasks.size, // 🔥 СЧЕТЧИК ВСЕГДА ОТОБРАЖАЕТСЯ
                isExpanded = showToday,
                onToggle = { showToday = !showToday }
            )
        }

        if (showToday) {
            items(todayTasks, key = { "today_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onTaskCheckClicked = { taskId ->
                        taskViewModel.onTaskCheckClickedForDate(taskId, LocalDate.now()) {
                            // 🔥 ОБНОВЛЯЕМ ПОСЛЕ ИЗМЕНЕНИЯ СТАТУСА
                            taskViewModel.forceRefresh()
                        }
                        Log.d("UserScreen", "✅ Today task $taskId status changed")
                    },
                    onTaskEditClicked = { taskId ->
                        // TODO: Навигация для редактирования
                    },
                )
            }
        }

        // --- TOMORROW SECTION ---
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_tomorrow),
                count = tomorrowTasks.size, // 🔥 СЧЕТЧИК ВСЕГДА ОТОБРАЖАЕТСЯ
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
                        taskViewModel.onTaskCheckClickedForDate(taskId, tomorrow) {
                            // 🔥 ОБНОВЛЯЕМ ПОСЛЕ ИЗМЕНЕНИЯ СТАТУСА
                            taskViewModel.forceRefresh()
                        }
                        Log.d("UserScreen", "✅ Tomorrow task $taskId status changed")
                    },
                    onTaskEditClicked = { taskId ->
                        // TODO: Навигация для редактирования
                    },
                )
            }
        }

        // --- COMPLETED SECTION ---
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_completed),
                count = completedTasks.size, // 🔥 СЧЕТЧИК ВСЕГДА ОТОБРАЖАЕТСЯ
                isExpanded = showCompleted,
                onToggle = { showCompleted = !showCompleted }
            )
        }

        if (showCompleted) {
            items(completedTasks, key = { "completed_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onTaskCheckClicked = { taskId ->
                        // Найдем задачу в списке выполненных, чтобы получить ее дату
                        val task = completedTasks.find { it.id == taskId }
                        task?.let {
                            taskViewModel.onTaskCheckClickedForDate(taskId, it.date) {
                                taskViewModel.forceRefresh()
                            }
                            Log.d("UserScreen", "✅ Completed task $taskId status changed for date ${it.date}")
                        }
                    },
                    onTaskEditClicked = { /* TODO */ },
                )
            }
        }
    }
}