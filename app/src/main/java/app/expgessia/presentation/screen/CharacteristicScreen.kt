package app.expgessia.presentation.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.R
import app.expgessia.domain.model.Characteristic
import app.expgessia.presentation.ui.theme.DigitLargeStyle
import app.expgessia.presentation.viewmodel.CharacteristicViewModel
import app.expgessia.presentation.viewmodel.UserViewModel
import app.expgessia.ui.components.RetroFrame
import app.expgessia.ui.components.RollerDigit

/**
 * Вспомогательная функция для динамического получения ID ресурса drawable по его строковому имени.
 */
@Composable
fun getDrawableResourceId(name: String): Int {
    val context = LocalContext.current
    val resourceName = name.lowercase()

    val resourceId = context.resources.getIdentifier(
        resourceName, "drawable", context.packageName
    )

    return resourceId
}

@StringRes
fun Characteristic.getLocalizedNameResId(): Int {
    return when (this.name.uppercase()) {
        "STRENGTH" -> R.string.stat_strength
        "PERCEPTION" -> R.string.stat_perception
        "ENDURANCE" -> R.string.stat_endurance
        "CHARISMA" -> R.string.stat_charisma
        "INTELLIGENCE" -> R.string.stat_intelligence
        "AGILITY" -> R.string.stat_agility
        "LUCK" -> R.string.stat_luck
        else -> R.string.placeholder_unknown_stat
    }
}

@StringRes
fun Characteristic.getLocalizedDescriptionResId(): Int {
    return when (this.name.uppercase()) {
        "STRENGTH" -> R.string.stat_strength_desc
        "PERCEPTION" -> R.string.stat_perception_desc
        "ENDURANCE" -> R.string.stat_endurance_desc
        "CHARISMA" -> R.string.stat_charisma_desc
        "INTELLIGENCE" -> R.string.stat_intelligence_desc
        "AGILITY" -> R.string.stat_agility_desc
        "LUCK" -> R.string.stat_luck_desc
        // Возвращаем заглушку, если характеристика не найдена
        else -> R.string.placeholder_unknown_desc
    }
}

@Composable
private fun getProcessedText(text: String, style: TextStyle): String {
    val uppercaseStyles = listOf(
        MaterialTheme.typography.headlineLarge,
        MaterialTheme.typography.titleLarge,
        MaterialTheme.typography.titleSmall,
        MaterialTheme.typography.labelLarge,
        MaterialTheme.typography.labelMedium
    )

    val isUppercaseStyle = uppercaseStyles.any {
        it.fontSize == style.fontSize && it.fontWeight == style.fontWeight
    }

    return if (isUppercaseStyle) {
        text.uppercase()
    } else {
        text
    }
}

@Composable
fun CharacteristicScreen(
    viewModel: CharacteristicViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel() // ✅ Добавляем UserViewModel
) {
    // Наблюдаем за StateFlow характеристик
    val characteristics by viewModel.characteristics.collectAsState()

    // ✅ Наблюдаем за данными пользователя
    val user by userViewModel.user.collectAsState()

    // Устанавливаем фон экрана в цвет фона терминала
    val screenBackground = MaterialTheme.colorScheme.background

    if (characteristics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(characteristics) { char ->
                // ✅ Получаем значение характеристики из данных пользователя
                val userValue = when (char.name.uppercase()) {
                    "STRENGTH" -> user?.strength
                    "PERCEPTION" -> user?.perception
                    "ENDURANCE" -> user?.endurance
                    "CHARISMA" -> user?.charisma
                    "INTELLIGENCE" -> user?.intelligence
                    "AGILITY" -> user?.agility
                    "LUCK" -> user?.luck
                    else -> null
                }

                CharacteristicCard(
                    characteristic = char,
                    userValue = userValue // ✅ Передаем значение
                )
            }
        }
    }
}





@Composable
fun CharacteristicCard(
    characteristic: Characteristic,
    userValue: Int? = null, // ✅ Добавляем значение характеристики пользователя
    modifier: Modifier = Modifier,
) {
    // Получаем ID ресурса динамически по имени характеристики
    val painterResourceId = getDrawableResourceId(characteristic.iconResName)

    val textColor = MaterialTheme.colorScheme.onSurface

    // Цвет фона иконки: FalloutSurfaceVariant (Немного отличается от фона карточки)
    val iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    // Цвет иконки: FalloutOnSurface (Неоновый зеленый)
    val iconTint = Color(0xFFFFF7F7)

    // Цвет фона блока уровня: FalloutPrimaryContainer (Темно-зеленый)
    val levelBoxColor = Color(0xFF60656C) // Используем как boxColor для RollerDigit

    // Цвет текста уровня: FalloutOnPrimaryContainer (Яркий зеленый)
    val levelDigitColor = Color(0xFFFCFCFC) // Используем как digitColor для RollerDigit

    // Получаем стили из темы
    val titleStyle = MaterialTheme.typography.titleSmall
    val bodyStyle = MaterialTheme.typography.bodySmall
    // Используем ваш собственный стиль для цифр, если он есть, иначе возьмем из темы
    val digitStyle = DigitLargeStyle // Используем стиль, подходящий для цифр

    // ✅ Форматируем значение в 3-значный формат
    val formattedValue = userValue?.let {
        String.format("%03d", it.coerceIn(0, 999))
    } ?: "000"

    RetroFrame() {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (painterResourceId != 0) {
                Icon(
                    painter = painterResource(id = painterResourceId),
                    contentDescription = characteristic.name,
                    modifier = Modifier.size(32.dp),
                    tint = iconTint // Цвет иконки из темы
                )
            } else {
                // Заглушка, если ресурс не найден
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Placeholder",
                    modifier = Modifier.size(32.dp),
                    tint = iconTint // Цвет иконки из темы
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // ЗАГОЛОВОК (UPPERCASE)
                Text(
                    text = getProcessedText(
                        stringResource(characteristic.getLocalizedNameResId()),
                        titleStyle
                    ), // Используем вспомогательную функцию
                    style = titleStyle,
                    color = textColor // Неоновый зеленый
                )
                // ОПИСАНИЕ
                Text(
                    text = stringResource(characteristic.getLocalizedDescriptionResId()),
                    style = bodyStyle,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ✅ ДОБАВЛЯЕМ БЛОК СО ЗНАЧЕНИЕМ ХАРАКТЕРИСТИКИ
            Spacer(modifier = Modifier.width(8.dp))

            // Контейнер для отображения цифр значения в ряд
            Row(
                modifier = Modifier.width(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Перебираем каждую цифру в форматированном значении и отображаем RollerDigit
                formattedValue.forEach { digitChar ->
                    RollerDigit(
                        digit = digitChar,
                        style = digitStyle,
                        digitColor = levelDigitColor,
                        boxColor = levelBoxColor
                    )
                }
            }
        }
    }
}
