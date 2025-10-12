// presentation/screen/UserScreen.kt
package app.expgessia.presentation.screen

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.presentation.ui.components.XpChart
import app.expgessia.presentation.viewmodel.UserViewModel
import app.expgessia.ui.components.TaskCategoryCompact
import app.expgessia.ui.components.TaskItem
import app.expgessia.ui.components.TaskItemData
import app.expgessia.ui.components.UserCard
@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState()
    println("UserScreen: user = $user, isLoading = $isLoading")
    // Локальные состояния для управления категориями задач
    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(true) }
    var showImportant by remember { mutableStateOf(true) }

    // Временные статические задачи для демонстрации
    val tasks = remember {
        listOf(
            TaskItemData(
                title = "Learn something new",
                description = "anything. press when feel like it happened",
                xpReward = "1.1XP∞",
                isCompleted = true
            )
        )
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

        item {
            XpChart()
        }

        // Today
        item {
            TaskCategoryCompact(
                title = "Today",
                count = tasks.count { it.category == "today" },
                isExpanded = showToday,
                onToggle = { showToday = !showToday }
            )
        }
        if (showToday) {
            items(tasks.filter { it.category == "today" }) { task ->
                TaskItem(
                    title = task.title,
                    description = task.description,
                    xpReward = task.xpReward,
                    isCompleted = task.isCompleted,
                    onCheckClicked = {}
                )
            }
        }

// Tomorrow
        item {
            TaskCategoryCompact(
                title = "Tomorrow",
                count = tasks.count { it.category == "tomorrow" },
                isExpanded = showTomorrow,
                onToggle = { showTomorrow = !showTomorrow }
            )
        }
        if (showTomorrow) {
            items(tasks.filter { it.category == "tomorrow" }) { task ->
                TaskItem(
                    title = task.title,
                    description = task.description,
                    xpReward = task.xpReward,
                    isCompleted = task.isCompleted,
                    onCheckClicked = {}
                )
            }
        }

// Important
        item {
            TaskCategoryCompact(
                title = "Important",
                count = tasks.count { it.category == "important" },
                isExpanded = showImportant,
                onToggle = { showImportant = !showImportant }
            )
        }
        if (showImportant) {
            items(tasks.filter { it.category == "important" }) { task ->
                TaskItem(
                    title = task.title,
                    description = task.description,
                    xpReward = task.xpReward,
                    isCompleted = task.isCompleted,
                    onCheckClicked = {}
                )
            }
        }
    }
}