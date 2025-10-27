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
import app.expgessia.presentation.viewmodel.UserViewModel
import app.expgessia.ui.components.RetroTaskCategoryCompact
import app.expgessia.ui.components.TaskItem
import app.expgessia.ui.components.UserCard

// ⚠️ Локальная модель для статических демо-данных
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
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()

    // 💡 Локальные состояния для управления категориями задач
    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(true) }
    var showImportant by remember { mutableStateOf(true) }
    var showCompleted by remember { mutableStateOf(false) } // 💡 НОВОЕ СОСТОЯНИЕ ДЛЯ ЗАВЕРШЕННЫХ ЗАДАЧ

    // 💡 Временные статические задачи для демонстрации
    var tasks by remember {
        mutableStateOf(
            listOf(
                DemoTaskModel(
                    id = 1L,
                    title = "Learn something new",
                    description = "anything. press when feel like it happened",
                    xpReward = 110,
                    isCompleted = false,
                    category = "today",
                    characteristicIconResName = "intelligence" // Иконка для демо
                ),
                DemoTaskModel(
                    id = 2L,
                    title = "Запланировать проект",
                    description = "Разбить на 5 подзадач",
                    xpReward = 80,
                    isCompleted = false,
                    category = "important",
                    characteristicIconResName = "perception" // Иконка для демо
                ),
                DemoTaskModel(
                    id = 3L,
                    title = "Выпить воды",
                    description = "Стакан 250 мл",
                    xpReward = 5,
                    isCompleted = true, // Эта задача завершена
                    category = "today",
                    characteristicIconResName = "endurance" // Иконка для демо
                ),
                DemoTaskModel(
                    id = 4L,
                    title = "Написать другу",
                    description = "Спросить, как дела",
                    xpReward = 10,
                    isCompleted = false,
                    category = "tomorrow",
                    characteristicIconResName = "charisma" // Иконка для демо
                )
            )
        )
    }

    // 💡 Локальная функция для обработки клика (обновление состояния в демо-режиме)
    val onTaskCheckClicked: (Long, Boolean) -> Unit = { taskId, isChecked ->
        val updatedList = tasks.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = isChecked)
            } else {
                task
            }
        }
        tasks = updatedList
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
                        viewModel.updateUserName(newName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }


        // Today
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_today),
                count = tasks.count { it.category == "today" && !it.isCompleted },
                isExpanded = showToday,
                onToggle = { showToday = !showToday }
            )
        }
        if (showToday) {
            items(
                tasks.filter { it.category == "today" && !it.isCompleted },
                key = { it.id }) { task ->
                TaskItem(
                    // 💡 Передаем TaskUiModel
                    task = task.toUiModel(),
                    onCheckClicked = onTaskCheckClicked
                )
            }
        }

// Tomorrow
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_tomorrow),
                count = tasks.count { it.category == "tomorrow" && !it.isCompleted },
                isExpanded = showTomorrow,
                onToggle = { showTomorrow = !showTomorrow }
            )
        }
        if (showTomorrow) {
            items(
                tasks.filter { it.category == "tomorrow" && !it.isCompleted },
                key = { it.id }) { task ->
                TaskItem(
                    // 💡 Передаем TaskUiModel
                    task = task.toUiModel(),
                    onCheckClicked = onTaskCheckClicked
                )
            }
        }

// Important
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_important),
                count = tasks.count { it.category == "important" && !it.isCompleted },
                isExpanded = showImportant,
                onToggle = { showImportant = !showImportant }
            )
        }
        if (showImportant) {
            items(
                tasks.filter { it.category == "important" && !it.isCompleted },
                key = { it.id }) { task ->
                TaskItem(
                    // 💡 Передаем TaskUiModel
                    task = task.toUiModel(),
                    onCheckClicked = onTaskCheckClicked
                )
            }
        }

        // ⭐️ Дополнительный блок: завершенные задачи
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_completed),
                count = tasks.count { it.isCompleted },
                isExpanded = showCompleted,
                onToggle = { showCompleted = !showCompleted }
            )
        }

        // Отображение всех завершенных задач
        if (showCompleted) { // 💡 ОТОБРАЖАЕМ ТОЛЬКО ЕСЛИ РАЗВЕРНУТО
            items(tasks.filter { it.isCompleted }, key = { it.id }) { task ->
                TaskItem(
                    task = task.toUiModel(),
                    onCheckClicked = onTaskCheckClicked
                )
            }
        }
    }
}
