package app.expgessia.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.expgessia.domain.model.Characteristic
import app.expgessia.presentation.ui.theme.DigitLargeStyle
import app.expgessia.presentation.ui.theme.DigitMediumStyle
import app.expgessia.presentation.viewmodel.CharacteristicViewModel
import app.expgessia.ui.components.RetroFrame
import app.expgessia.ui.components.RollerDigit

/**
 * Вспомогательная функция для динамического получения ID ресурса drawable по его строковому имени.
 */
@Composable
fun getDrawableResourceId(name: String): Int {
    val context = LocalContext.current
    val resourceName = name.lowercase()

    // Пытаемся получить ID ресурса по имени
    val resourceId = context.resources.getIdentifier(
        resourceName, "drawable", context.packageName
    )

    // Если ресурс не найден, возвращаем 0, чтобы использовать заглушку
    return resourceId
}

/**
 * Вспомогательная функция для преобразования текста в верхний регистр
 * при использовании определенных стилей.
 */
@Composable
private fun getProcessedText(text: String, style: TextStyle): String {
    val uppercaseStyles = listOf(
        MaterialTheme.typography.headlineLarge,
        MaterialTheme.typography.titleLarge,
        MaterialTheme.typography.titleSmall,
        MaterialTheme.typography.labelLarge,
        MaterialTheme.typography.labelMedium
    )

    // Сравниваем только те свойства, которые вы задали в Typography,
    // чтобы определить, является ли это одним из "заглавных" стилей.
    // Примечание: Сравнение FontWeight и FontSize - это обходной путь.
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
    // Инъекция ViewModel через Hilt
    viewModel: CharacteristicViewModel = viewModel()
) {
    // Наблюдаем за StateFlow
    val characteristics by viewModel.characteristics.collectAsState()

    // Устанавливаем фон экрана в цвет фона терминала
    val screenBackground = MaterialTheme.colorScheme.background

    if (characteristics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground), // Используем фон из темы
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Используем основной цвет
            // В реальном приложении здесь лучше показать сообщение "Нет данных"
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground), // Используем фон из темы
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(characteristics) { char ->
                CharacteristicCard(char)
            }
        }
    }
}


@Composable
fun CharacteristicCard(
    characteristic: Characteristic,
    modifier: Modifier = Modifier
) {

    val level = 5 // Заглушка уровня.
    val levelText =  "0" + level.toString() // Преобразуем уровень в строку

    // Получаем ID ресурса динамически по имени характеристики
    val painterResourceId = getDrawableResourceId(characteristic.name)

    val textColor = MaterialTheme.colorScheme.onSurface

    // Цвет фона иконки: FalloutSurfaceVariant (Немного отличается от фона карточки)
    val iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    // Цвет иконки: FalloutOnSurface (Неоновый зеленый)
    val iconTint = textColor

    // Цвет фона блока уровня: FalloutPrimaryContainer (Темно-зеленый)
    val levelBoxColor = Color(0xFF60656C) // Используем как boxColor для RollerDigit

    // Цвет текста уровня: FalloutOnPrimaryContainer (Яркий зеленый)
    val levelDigitColor = Color(0xFFFCFCFC) // Используем как digitColor для RollerDigit

    // Получаем стили из темы
    val titleStyle = MaterialTheme.typography.titleSmall
    val bodyStyle = MaterialTheme.typography.bodySmall
    // Используем ваш собственный стиль для цифр, если он есть, иначе возьмем из темы
    // Здесь я использую `DigitMediumStyle`, который вы импортировали.
    val digitStyle = DigitLargeStyle // Используем стиль, подходящий для цифр

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
                        characteristic.name,
                        titleStyle
                    ), // Используем вспомогательную функцию
                    style = titleStyle,
                    color = textColor // Неоновый зеленый
                )
                // ОПИСАНИЕ
                Text(
                    text = characteristic.description
                        ?: "Нет описания",
                    style = bodyStyle,
                    // Используем немного приглушенный оттенок зеленого для описания
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // --- НОВЫЙ БЛОК УРОВНЯ С RollerDigit ---
            // Добавляем Spacer для визуального разделения
            Spacer(modifier = Modifier.width(8.dp))

            // Контейнер для отображения цифр уровня в ряд
            Row(
                modifier = Modifier.width(IntrinsicSize.Min), // Позволяет Row занять минимальную необходимую ширину
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Перебираем каждую цифру в строке уровня и отображаем RollerDigit
                levelText.forEach { digitChar ->

                    RollerDigit(
                        digit = digitChar,
                        style = digitStyle, // Используем подходящий стиль текста
                        digitColor = levelDigitColor,
                        boxColor = levelBoxColor
                    )
                }
            }
            // --- КОНЕЦ НОВОГО БЛОКА УРОВНЯ ---
        }
    }
}