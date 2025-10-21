package app.expgessia.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.expgessia.R

// Используем Monospace как стандартный шрифт для имитации пиксельного/терминального стиля
val FalloutFontFamily = FontFamily(
    Font(R.font.fixedsys, FontWeight.Normal)
)
val FalloutFontFamilyDigits = FontFamily(
    // Обычный (Regular)
    Font(R.font.overseer, FontWeight.Normal),

    // Жирный (Bold)
    Font(R.font.overseer_bold, FontWeight.Bold),

    // Курсив (Italic)
    Font(R.font.overseer_italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic),

    // Жирный Курсив (Bold Italic)
    Font(
        R.font.overseer_bold_italic,
        FontWeight.Bold,
        androidx.compose.ui.text.font.FontStyle.Italic
    )
)

val DigitMediumStyle = TextStyle(
    fontFamily = FalloutFontFamilyDigits,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

val DigitLargeStyle = TextStyle(
    fontFamily = FalloutFontFamilyDigits,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp
)
val Typography = Typography(
    // Большие заголовки
    headlineLarge = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    // Заголовки разделов, ключевая информация
    titleLarge = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // ⭐️ ДОБАВЛЕНО: Заголовок для карточек (CharacteristicCard name)
    titleSmall = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // Чуть меньше, чем titleLarge
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Основной текст
    bodyLarge = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Мелкий текст, подписи (CharacteristicCard description)
    bodySmall = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Текст кнопок/меток
    labelLarge = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // ⭐️ ДОБАВЛЕНО: Метка/Уровень (CharacteristicCard level)
    labelMedium = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = FalloutFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    )


)
