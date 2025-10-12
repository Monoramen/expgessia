package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.expgessia.ui.components.TaskItem


@Composable
fun TaskScreen(
    tasks: List<Task>, // ← вот тут список
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        items(tasks.size) { index ->
            val task = tasks[index]
            TaskItem(
                title = task.title,
                description = task.description,
                xpReward = task.xpReward,
                isCompleted = task.isCompleted,
                onCheckClicked = { }
            )
        }
    }
}
data class Task(
    val title: String,
    val description: String,
    val xpReward: String,
    val isCompleted: Boolean
)
