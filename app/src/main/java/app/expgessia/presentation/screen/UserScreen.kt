package app.expgessia.presentation.screen

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()

    // 💡 Локальные состояния для управления категориями задач
    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(true) }
    var showImportant by remember { mutableStateOf(true) }
    var showCompleted by remember { mutableStateOf(false) } // 💡 СОСТОЯНИЕ ДЛЯ ЗАВЕРШЕННЫХ ЗАДАЧ




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
                count = 0,
                isExpanded = showToday,
                onToggle = { showToday = !showToday }
            )
        }
        // УДАЛЕНО: items(tasks.filter { it.category == "today" && !it.isCompleted })

// Tomorrow
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_tomorrow),
                count = 0,
                isExpanded = showTomorrow,
                onToggle = { showTomorrow = !showTomorrow }
            )
        }
        // УДАЛЕНО: items(tasks.filter { it.category == "tomorrow" && !it.isCompleted })

// Important
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_important),
                count = 0,
                isExpanded = showImportant,
                onToggle = { showImportant = !showImportant }
            )
        }
        // УДАЛЕНО: items(tasks.filter { it.category == "important" && !it.isCompleted })

        // ⭐️ Дополнительный блок: завершенные задачи
        item {
            RetroTaskCategoryCompact(
                title = stringResource(R.string.label_completed),
                count = 0,
                isExpanded = showCompleted,
                onToggle = { showCompleted = !showCompleted }
            )
        }

        // УДАЛЕНО: items(tasks.filter { it.isCompleted })
    }
}