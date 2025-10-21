package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.AddTaskButton
import app.expgessia.ui.components.TaskItem

@Composable
fun TaskRoute(
    onAddTaskClicked: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    // 💡 Используем tasksUiState, который содержит List<TaskUiModel>
    val tasks by viewModel.tasksUiState.collectAsStateWithLifecycle()

    TaskScreen(
        tasks = tasks,
        onTaskCheckChanged = viewModel::onTaskCheckChanged,
        onAddTaskClicked = onAddTaskClicked
    )
}

@Composable
fun TaskScreen(
    // 💡 Принимаем список TaskUiModel
    tasks: List<TaskUiModel>,
    onAddTaskClicked: () -> Unit,
    onTaskCheckChanged: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),

    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding(), // Берем отступ от TopBar
                bottom = paddingValues.calculateBottomPadding() // Берем отступ от BottomBar
            )
        ) {
            // items теперь корректно работает с TaskUiModel
            items(tasks, key = { it.id }) { task ->
                // 💡 Передаем весь объект TaskUiModel, соответствуя сигнатуре TaskItem в Canvas
                TaskItem(
                    task = task,
                    onCheckClicked = onTaskCheckChanged
                )
            }
        }
    }
}
