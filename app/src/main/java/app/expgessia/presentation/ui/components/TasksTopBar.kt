package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.R

/**
 * Верхняя панель для экрана задач в стиле Fallout (темный фон, неоновый текст).
 * Реализован как кастомный Row для точного контроля высоты и вертикального центрирования.
 */
@Composable
fun TasksTopBar(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddTaskClicked: (() -> Unit)? = null
) {
    // ⭐️ Используем Row вместо TopAppBar для точного контроля вертикального центрирования
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface).padding(horizontal = 8.dp),
        // Фон
        verticalAlignment = Alignment.Bottom, // ⭐️ Гарантирует вертикальное центрирование
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Иконка Назад
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface // Неоновый зеленый
                )
            }
            // Заголовок
            Text(
                stringResource(R.string.nav_tasks).uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold, // Сделал жирным
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface, // Неоновый зеленый
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Кнопки Действий
        Row(
            modifier = Modifier.padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            onAddTaskClicked?.let { onClick ->
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить задачу",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconButton(onClick = onSearch) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
