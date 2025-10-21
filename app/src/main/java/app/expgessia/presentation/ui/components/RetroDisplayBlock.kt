package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

val FrameMetalColor = Color(0xFF342F2F)

val FrameHighlight = Color(0xFF5A5A5A)

val InnerBezelColor = Color(0xFF242424)

// Фон дисплея - глубокий черный
val DisplayBackgroundColor = Color(0xFF101010)
private val CornerRadius = 8.dp
val ContentPaddingHorizontal = 4.dp
val ContentPaddingTextVertical = 2.dp
private val BoltEdgePadding = 5.dp // Отступ болтов от края рамки
private val BoltSize = 8.dp // Размер болта

/**
 * Универсальный Composable для создания ретро-рамки с эффектом потертости.
 * Используйте его для любого блока интерфейса, которому нужен такой стиль.
 *
 * @param modifier Модификатор для внешнего Box.
 * @param content Composable-функция, которая будет отображаться внутри дисплея.
 */
@Composable
fun RetroFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {

    Box(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                // 1. Внешний корпус (Metal Frame)
                .clip(RoundedCornerShape(CornerRadius))
                .background(FrameMetalColor)
                .border(2.dp, FrameHighlight, RoundedCornerShape(CornerRadius))
                .padding(12.dp)
        ) {
            // 2. Внутренняя рамка
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerRadius - 4.dp))
                    .background(InnerBezelColor)
                    .padding(2.dp) // Толщина внутренней линии рамки (темно-серый цвет)
            ) {
                // 3. Поверхность экрана (Display Area)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // ✅ CLIP для DisplayBackgroundColor (радиус 2.dp)
                        .clip(RoundedCornerShape(CornerRadius - 4.dp))
                        .background(DisplayBackgroundColor)
                    // Отступы для контента внутри дисплея

                ) {
                    // --- Пользовательский Контент ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content
                    )
                }
            }
        }

        // --- Декоративные Болты (Сверху слева) ---
        Bolt(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = BoltEdgePadding, y = BoltEdgePadding)
        )
        // --- Декоративные Болты (Сверху справа) ---
        Bolt(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = -BoltEdgePadding, y = BoltEdgePadding)
        )
        // --- Декоративные Болты (Снизу слева) ---
        Bolt(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = BoltEdgePadding, y = -BoltEdgePadding)
        )
        // --- Декоративные Болты (Снизу справа) ---
        Bolt(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = -BoltEdgePadding, y = -BoltEdgePadding)
        )
    }
}

/**
 * Стилизованный круглый болт.
 */

@Composable
fun Bolt(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(app.expgessia.R.drawable.bolt),
        contentDescription = null,
        modifier = modifier.size(BoltSize),
        tint = Color.Unspecified
    )
}


