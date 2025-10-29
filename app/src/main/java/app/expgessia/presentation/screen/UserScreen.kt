package app.expgessia.presentation.screen

import android.content.res.Resources
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
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.presentation.viewmodel.UserViewModel
import app.expgessia.ui.components.RetroTaskCategoryCompact
import app.expgessia.ui.components.TaskItem
import app.expgessia.ui.components.UserCard

// ⚠️ Локальная модель для статических демо-данных (оставляем для подсчета count)
private data class DemoTaskModel(
    val id: Long, // Требуется для onCheckClicked
    val title: String,
    val description: String,
    val xpReward: Int,
    var isCompleted: Boolean,
    val category: String,
    val characteristicIconResName: String? = null // Требуется для TaskUiModel
) {
    // Метод для преобразования локальной модели в UI-модель, ожидаемую TaskItem
    fun toUiModel() = TaskUiModel(
        id = id,
        title = title,
        description = description,
        xpReward = xpReward,
        isCompleted = isCompleted,
        characteristicIconResName = characteristicIconResName
            ?: "strength" // Дефолтная иконка для демо
    )
}

@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()

) {
    val user by userViewModel.user.collectAsState(initial = null)
    val isLoading by userViewModel.isLoading.collectAsState()

    // 💡 Локальные состояния для управления категориями задач
    val todayTasks by taskViewModel.todayTasks.collectAsState(initial = emptyList())
    val completedTasks by taskViewModel.completedTasks.collectAsState(initial = emptyList())
    val tomorrowTasks by taskViewModel.tomorrowTasks.collectAsState(initial = emptyList())

    // 💡 Состояния для управления видимостью (раскрытием) категорий
    var showToday by remember { mutableStateOf(true) } // По умолчанию показываем сегодня
    var showTomorrow by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }



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
                count = todayTasks.size, // ✅ Реальное количество
                isExpanded = showToday,
                onToggle = { showToday = !showToday }
            )
        }
        // ✅ Условно отображаем список задач на сегодня
        if (showToday) {
            items(todayTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    // ✅ Вызываем функцию ViewModel для выполнения задачи
                    onTaskCheckClicked = { taskViewModel.onTaskCheckClicked(task.id) },
                    onTaskEditClicked = { /* TODO: Добавить логику навигации для редактирования */ },
                )
            }
        }

// --- TOMORROW SECTION (Запланированные задачи) ---
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_tomorrow),
                count = tomorrowTasks.size, // ✅ Используем реальное количество
                isExpanded = showTomorrow,
                onToggle = { showTomorrow = !showTomorrow }
            )
        }
        // ✅ Условно отображаем список задач на завтра
        if (showTomorrow) {
            items(tomorrowTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    // ✅ Вызываем функцию ViewModel для выполнения задачи
                    onTaskCheckClicked = { taskViewModel.onTaskCheckClicked(task.id) },
                    onTaskEditClicked = { /* TODO: Добавить логику навигации для редактирования */ },
                )
            }
        }
// --- END TOMORROW SECTION ---


// --- COMPLETED SECTION (Завершенные задачи) ---
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_completed),
                count = completedTasks.size, // ✅ Используем реальное количество
                isExpanded = showCompleted,
                onToggle = { showCompleted = !showCompleted }
            )
        }
        // ✅ Условно отображаем список завершенных задач
        if (showCompleted) {
            items(completedTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    // ⚠️ Для завершенных задач, обычно, действие "чек" означает "отменить завершение"
                    // или "скрыть". Оставляем заглушку, так как логика uncheck отсутствует в TaskViewModel
                    onTaskCheckClicked = { /* TODO: Логика uncheck (отменить завершение) */ },
                    onTaskEditClicked = { /* TODO: Логика просмотра/редактирования */ },
                )
            }
        }
// --- END COMPLETED SECTION ---
    }
}