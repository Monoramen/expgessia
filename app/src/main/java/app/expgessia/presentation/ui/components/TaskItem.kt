package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.expgessia.domain.model.TaskUiModel

// components/TaskItem.kt
@Composable
fun TaskItem(
    task: TaskUiModel,
    onTaskCheckClicked: (Long) -> Unit, // Колбэк для завершения/отмены
    onTaskEditClicked: (Long) -> Unit, // Колбэк для редактирования
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val iconResId = remember(task.characteristicIconResName) {
        if (task.characteristicIconResName.isNullOrBlank()) {
            0
        } else {
            // Получаем ID ресурса из строки (например, "strength" -> R.drawable.strength)
            context.resources.getIdentifier(
                task.characteristicIconResName, // Имя ресурса (strength)
                "drawable",               // Тип ресурса (drawable)
                context.packageName       // Пакет приложения
            )
        }
    }

    // Используем ваш RetroFrame (предполагается, что он реализован отдельно)
    RetroFrame(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            // Добавляем небольшой эффект "потускнения" для завершенных задач
            .background(
                if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else Color.Transparent // Цвет фона будет задан внутри RetroFrame (DisplayBackgroundColor)
            )
    ) {
        Row(
            modifier = Modifier
                // 💡 ИЗМЕНЕНИЕ: Клик по элементу теперь вызывает редактирование
                .clickable { onTaskEditClicked(task.id) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Иконка для характеристики
            if (iconResId != 0) {
                // Если resource ID найден, используем Compose 'Painter' для Drawable
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = task.characteristicIconResName ?: "Characteristic icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary // Используем цвет из темы
                )
            } else {
                // Если resource ID НЕ найден (или null), используем дефолтную иконку
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Task icon (default)",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Текст задачи (Title & Description)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                    ),
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Thin),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            // 💡 УДАЛЕНО: Блок с наградой XP (Badge/Chip)

            // 💡 УДАЛЕНО: Кнопка Редактировать

            // Кнопка/Иконка статуса завершения
            IconButton(
                // 💡 ПЕРЕДАЕМ ID для смены статуса
                onClick = { onTaskCheckClicked(task.id) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Завершена" else "Завершить",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}