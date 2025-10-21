package app.expgessia.presentation.ui.screens

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.presentation.ui.theme.FalloutFontFamilyDigits
import app.expgessia.presentation.ui.theme.FalloutOutline
import app.expgessia.presentation.ui.theme.FalloutPrimary
import kotlinx.coroutines.launch

data class PlayerStat(
    val title: String,
    val value: String
)

@Composable
fun StatRow(
    stat: PlayerStat,
    // ⭐️ ДОБАВЛЯЕМ ПАРАМЕТР ДЛЯ ШРИФТА ЦИФР
    digitsFontFamily: FontFamily
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ... (код анимаций и цветов остается без изменений)
    val infiniteTransition = rememberInfiniteTransition(label = "pressBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )
    val baseColor = MaterialTheme.colorScheme.onBackground
    val glowColor = FalloutPrimary
    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) glowColor else baseColor,
        animationSpec = tween(200),
        label = "pressColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            FalloutPrimary.copy(alpha = 0.15f * blinkAlpha)
        else
            Color.Transparent,
        animationSpec = tween(150),
        label = "pressBackground"
    )

    // ⭐️ 1. Определяем базовый и цифровой стили
    val baseTextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal // Используем базовый шрифт для букв
    )

    val baseSpanStyle = baseTextStyle.toSpanStyle()

    // Стиль для цифр: применяем только нужный FontFamily
    val digitsSpanStyle = SpanStyle(
        fontFamily = digitsFontFamily
    )

    // ⭐️ 2. Форматируем текст значения
    val formattedValue = formatStatValue(
        text = stat.value,
        baseStyle = baseSpanStyle,
        digitStyle = digitsSpanStyle
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(2.dp))
            .background(backgroundColor, shape = RoundedCornerShape(2.dp))
            .combinedClickable(
                onClick = {
                    // короткое нажатие с "мерцанием"
                    isPressed = true
                    scope.launch {
                        repeat(3) {
                            isPressed = !isPressed
                            kotlinx.coroutines.delay(80)
                        }
                        isPressed = false
                    }
                },
                onLongClick = {
                    // длинное нажатие = дольше мерцание
                    isPressed = true
                    scope.launch {
                        repeat(6) {
                            isPressed = !isPressed
                            kotlinx.coroutines.delay(80)
                        }
                        isPressed = false
                    }
                }
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Текст заголовка (без изменений)
        Text(
            text = stat.title,
            color = if (isPressed) glowColor.copy(alpha = blinkAlpha) else animatedColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        // ⭐️ Используем отформатированный AnnotatedString
        Text(
            text = formattedValue,
            color = if (isPressed) glowColor.copy(alpha = blinkAlpha) else animatedColor,
            // Передаем базовый стиль, чтобы сохранить размер и другие параметры
            style = baseTextStyle
        )
    }

    // Разделительная линия
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FalloutOutline.copy(alpha = 0.6f))
    )
}

// ---------------------------------------------------------------------

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val stats = listOf(
        PlayerStat("ВЫПОЛНЕНО ЗАДАЧ", "42"),
        PlayerStat("ПОЛУЧЕНО ОПЫТА (ВСЕГО)", "1280 XP"),
        PlayerStat("ОПЫТ ЗА СЕГОДНЯ", "150 XP"),
        PlayerStat("ПОСЛЕДНИЙ ВИЗИТ", "19 ОКТ 2025"),
        PlayerStat("ВРЕМЯ В ИГРЕ", "3 Ч 24 МИН"),
        PlayerStat("СЕРИЯ ВХОДОВ", "5 ДНЕЙ"),
        PlayerStat("РЕКОРДНЫЙ ДЕНЬ", "560 XP"),
        PlayerStat("СТАТУС", "АКТИВЕН")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ⛓ Фоновые эффекты CRT
        TerminalScanlines()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            stats.forEach { stat ->
                // ⭐️ ПЕРЕДАЕМ ШРИФТ ДЛЯ ЦИФР
                StatRow(stat = stat, digitsFontFamily = FalloutFontFamilyDigits)
            }

            Spacer(modifier = Modifier.height(32.dp))

            BlinkingFooter()
        }
    }
}

@Composable
fun TerminalScanlines() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val lineHeight = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color(0xFF00FF00).copy(alpha = 0.06f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineHeight
        }
    }
}


// ⭐️ ФУНКЦИЯ-ПОМОЩНИК ДЛЯ ФОРМАТИРОВАНИЯ
fun formatStatValue(
    text: String,
    baseStyle: SpanStyle,
    digitStyle: SpanStyle
) = buildAnnotatedString {
    text.forEach { char ->
        // Проверяем, является ли символ цифрой
        if (char.isDigit()) {
            // Если цифра, применяем стиль цифр
            withStyle(digitStyle) {
                append(char)
            }
        } else {
            // Если буква или другой символ (пробел, XP, Ч, МИН),
            // применяем базовый стиль
            withStyle(baseStyle) {
                append(char)
            }
        }
    }
}

// ---------------------------------------------------------------------
@Composable
fun BlinkingFooter() {
    // 🔸 Мигание курсора
    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "== END OF FILE ==",
            color = FalloutOutline,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(10.dp, 14.dp)
                .alpha(alpha)
                .background(FalloutPrimary)
        )
    }
}


