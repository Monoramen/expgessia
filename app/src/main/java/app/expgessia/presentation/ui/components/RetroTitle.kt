package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.presentation.ui.theme.TitleTextColor

// --- Цвета (Общие для Ретро-Стиля) ---

val TitleShadowColor = Color(0xFF212121) // Очень темный серый (для эффекта тени)
val ContainerBackgroundColor = Color(0xFF424242) // Темно-серый фон контейнера
val RetroAccentColor = Color(0xFFE53935) // Красный акцент для иконок/маркеров

// --- Размеры (Общие для Ретро-Стиля) ---
val RetroTitlePadding = 8.dp // Внутренний отступ контейнера
val RetroTextSize = 24.sp // Размер шрифта
val ShadowOffset = 2.dp // Смещение для эффекта 3D тени

/**
 * Ретро-заголовок с эффектом 3D-тени и темным фоном.
 * Имитирует блочный шрифт и яркую надпись, характерные для старых игр.
 *
 * @param text Отображаемый текст. Будет преобразован в верхний регистр.
 * @param modifier Модификатор для применения к компоненту-контейнеру.
 */
@Composable
fun RetroTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    // Внешний Box имитирует темный, прямоугольный контейнер (как кнопка/вывеска)
    Box(
        modifier = modifier
            .background(ContainerBackgroundColor) // Темный фон
            .padding(RetroTitlePadding)
            .wrapContentSize(align = Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        // Внутренний Box для наложения текстов (создание 3D-эффекта)
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            val titleText = text.uppercase()

            // 1. Тень/Контур (Dark Text)
            // Смещена вниз и вправо для создания эффекта глубины/3D
            Text(
                text = titleText,
                color = TitleShadowColor,
                fontSize = RetroTextSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = ShadowOffset, top = ShadowOffset)
            )

            // 2. Основной текст (Bright Yellow)
            // Накладывается сверху, смещен наверх и влево относительно тени
            Text(
                text = titleText,
                color = TitleTextColor,
                fontSize = RetroTextSize,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
