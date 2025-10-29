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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.TaskItem

@Composable
fun TaskRoute(
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {

    // 💡 Используем tasksUiState, который содержит List<TaskUiModel>
    val tasks by viewModel.tasksUiState.collectAsStateWithLifecycle()

    TaskScreen(
        tasks = tasks,
        onTaskCheckClicked = viewModel::onTaskCheckClicked,
        onEditTaskClicked = onEditTaskClicked,
        onAddTaskClicked = onAddTaskClicked
    )
}

@Composable
fun TaskScreen(
    // 💡 Принимаем список TaskUiModel
    tasks: List<TaskUiModel>,
    onAddTaskClicked: () -> Unit,
    onTaskCheckClicked: (Long) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),

        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),

            contentPadding = PaddingValues(
                start = 2.dp,
                end = 2.dp,
                top = 2.dp, // Берем отступ от TopBar
                bottom = paddingValues.calculateBottomPadding() // Берем отступ от BottomBar
            )
        ) {
            // items теперь корректно работает с TaskUiModel
            items(tasks, key = { it.id }) { task ->
                // 💡 Передаем весь объект TaskUiModel, соответствуя сигнатуре TaskItem в Canvas
                TaskItem(
                    task = task,
                    onTaskCheckClicked = onTaskCheckClicked,
                    onTaskEditClicked = onEditTaskClicked,
                )
            }
        }
    }
}
