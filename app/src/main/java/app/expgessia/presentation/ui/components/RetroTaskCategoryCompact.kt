package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.R
import app.expgessia.presentation.ui.theme.TitleTextColor

/**
 * Компактный компонент категории задач в ретро-стиле.
 * Выступает в качестве интерактивного заголовка, который можно развернуть/свернуть.
 *
 * @param title Заголовок категории (например, "Today").
 * @param count Количество незавершенных задач в этой категории.
 * @param isExpanded Состояние: развернута ли категория.
 * @param onToggle Функция-колбэк при нажатии для изменения состояния.
 * @param modifier Модификатор для применения к контейнеру.
 */


private val BoltEdgePadding = 6.dp // Отступ болтов от края рамки

@Composable
fun RetroTaskCategoryCompact(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Используем стили из RetroTitle.kt
    val compactTextSize = 18.sp
    val paddingVertical = 12.dp

    Box( // Оборачиваем все в Box для наложения болтов
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius))
            .background(FrameMetalColor)
            .border(2.dp, FrameHighlight, RoundedCornerShape(CornerRadius))
            .clickable(onClick = onToggle) // Клик по всему Box
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RetroTitlePadding * 2, vertical = paddingVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Заголовок и Счетчик
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Заголовок Категории (имитируем эффект тени, как в RetroTitle)
                Box(contentAlignment = Alignment.Center) {
                    val categoryTitle = title.uppercase()

                    // Тень (Dark Text)
                    Text(
                        text = categoryTitle,
                        color = TitleShadowColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = ShadowOffset, top = ShadowOffset)
                    )

                    // Основной текст (Bright Yellow)
                    Text(
                        text = categoryTitle,
                        color = TitleTextColor,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                // Счетчик задач (Просто яркий акцентный текст)
                Text(
                    text = "($count)",
                    color = RetroAccentColor, // Используем красный акцентный цвет
                    style = MaterialTheme.typography.titleMedium,
                    // Немного смещаем вверх для выравнивания
                    modifier = Modifier.padding(bottom = ShadowOffset)
                )
            }

            // 2. Иконка-переключатель (Up/Down Arrow)
            Icon(
                // 1. Используем painterResource для загрузки PNG из drawable
                painter = painterResource(
                    id = if (isExpanded) R.drawable.arrow_up else R.drawable.arrow_down
                ),
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",

                // 2. Используем ваш ретро-цвет (Tint)
                // Если PNG черно-белые, tint окрасит их в нужный цвет.
                tint = Color.Unspecified,

                // 3. Сохраняем модификаторы
                modifier = Modifier.clip(RectangleShape) // Используем RectangleShape для "блочности"
            )
        }

        // Болты по углам
        Bolt( // Верхний левый
            Modifier
                .align(Alignment.TopStart)
                .offset(x = BoltEdgePadding, y = BoltEdgePadding)
        )
        Bolt( // Верхний правый
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = -BoltEdgePadding, y = BoltEdgePadding)
        )
        Bolt( // Нижний левый
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = BoltEdgePadding, y = -BoltEdgePadding)
        )
        Bolt( // Нижний правый
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -BoltEdgePadding, y = -BoltEdgePadding)
        )
    }
}