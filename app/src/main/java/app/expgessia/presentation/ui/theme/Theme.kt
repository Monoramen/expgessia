package app.expgessia.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Цветовые схемы для стиля Fallout (Зеленый на Темном) ---

// Используем одни и те же цвета для темной и светлой схемы, чтобы сохранить эстетику терминала
private val FalloutDarkColorScheme = darkColorScheme(
    primary = FalloutPrimary,
    onPrimary = FalloutOnPrimary,
    primaryContainer = FalloutPrimaryContainer,
    onPrimaryContainer = FalloutOnPrimaryContainer,
    secondary = FalloutSecondary,
    onSecondary = FalloutOnSecondary,
    secondaryContainer = FalloutSecondaryContainer,
    onSecondaryContainer = FalloutOnSecondaryContainer,
    tertiary = FalloutTertiary,
    onTertiary = FalloutOnTertiary,
    tertiaryContainer = FalloutTertiaryContainer,
    onTertiaryContainer = FalloutOnTertiaryContainer,
    error = FalloutError,
    errorContainer = FalloutErrorContainer,
    onError = FalloutOnError,
    onErrorContainer = FalloutOnErrorContainer,
    background = FalloutBackground,
    onBackground = FalloutOnBackground,
    surface = FalloutSurface,
    onSurface = FalloutOnSurface,
    surfaceVariant = FalloutSurfaceVariant,
    onSurfaceVariant = FalloutOnSurfaceVariant,
    outline = FalloutOutline,
    inverseOnSurface = FalloutInverseOnSurface,
    inverseSurface = FalloutInverseSurface,
    inversePrimary = FalloutInversePrimary,
)

// Светлая схема также использует темную палитру
private val FalloutLightColorScheme = lightColorScheme(
    primary = FalloutPrimary,
    onPrimary = FalloutOnPrimary,
    primaryContainer = FalloutPrimaryContainer,
    onPrimaryContainer = FalloutOnPrimaryContainer,
    secondary = FalloutSecondary,
    onSecondary = FalloutOnSecondary,
    secondaryContainer = FalloutSecondaryContainer,
    onSecondaryContainer = FalloutOnSecondaryContainer,
    tertiary = FalloutTertiary,
    onTertiary = FalloutOnTertiary,
    tertiaryContainer = FalloutTertiaryContainer,
    onTertiaryContainer = FalloutOnTertiaryContainer,
    error = FalloutError,
    errorContainer = FalloutErrorContainer,
    onError = FalloutOnError,
    onErrorContainer = FalloutOnErrorContainer,
    background = FalloutBackground,
    onBackground = FalloutOnBackground,
    surface = FalloutSurface,
    onSurface = FalloutOnSurface,
    surfaceVariant = FalloutSurfaceVariant,
    onSurfaceVariant = FalloutOnSurfaceVariant,
    outline = FalloutOutline,
    inverseOnSurface = FalloutInverseOnSurface,
    inverseSurface = FalloutInverseSurface,
    inversePrimary = FalloutInversePrimary,
)


@Composable
fun expgessiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Выбираем темную схему для стиля Fallout
    val colorScheme = FalloutDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Устанавливаем статус-бар и навигационную панель в цвет фона терминала
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            // Настраиваем, чтобы иконки системных панелей были светлыми (так как фон темный)
            // false означает, что иконки будут светлыми (для темной темы)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
