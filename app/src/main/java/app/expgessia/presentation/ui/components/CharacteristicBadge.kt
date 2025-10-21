package app.expgessia.ui.components // Или ваш пакет компонентов

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun CharacteristicIcon(
    iconResName: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,

    defaultIcon: @Composable (() -> Unit) = {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Default icon",
            tint = tint,
            modifier = modifier
        )
    }
) {
    val context = LocalContext.current

    val iconResId = remember(iconResName) {
        if (iconResName.isNullOrBlank()) {
            0
        } else {
            // Получаем ID ресурса из строки (например, "strength" -> R.drawable.strength)
            context.resources.getIdentifier(
                iconResName,          // Имя ресурса (strength)
                "drawable",           // Тип ресурса (drawable)
                context.packageName   // Пакет приложения
            )
        }
    }

    if (iconResId != 0) {
        // Если resource ID найден, используем Compose 'Painter' для Drawable
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint // Используем заданный цвет
        )
    } else {
        // Если resource ID НЕ найден (или null), используем дефолтную иконку
        defaultIcon()
    }
}// Предполагаемый код CharacteristicBadge

@Composable
fun CharacteristicBadge(
    iconName: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Заменяем старую логику иконки на CharacteristicIcon
        CharacteristicIcon(
            iconResName = iconName,
            contentDescription = name,
            modifier = Modifier.size(24.dp), // Например, меньший размер
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, style = MaterialTheme.typography.labelLarge)
    }
}