package app.expgessia.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FalloutDarkColorScheme = darkColorScheme(
    primary = FalloutDarkPrimary,
    onPrimary = FalloutDarkOnPrimary,
    primaryContainer = FalloutDarkPrimaryContainer,
    onPrimaryContainer = FalloutDarkOnPrimaryContainer,
    secondary = FalloutDarkSecondary,
    onSecondary = FalloutDarkOnSecondary,
    secondaryContainer = FalloutDarkSecondaryContainer,
    onSecondaryContainer = FalloutDarkOnSecondaryContainer,
    tertiary = FalloutDarkTertiary,
    onTertiary = FalloutDarkOnTertiary,
    tertiaryContainer = FalloutDarkTertiaryContainer,
    onTertiaryContainer = FalloutDarkOnTertiaryContainer,
    error = FalloutDarkError,
    errorContainer = FalloutDarkErrorContainer,
    onError = FalloutDarkOnError,
    onErrorContainer = FalloutDarkOnErrorContainer,
    background = FalloutDarkBackground,
    onBackground = FalloutDarkOnBackground,
    surface = FalloutDarkSurface,
    onSurface = FalloutDarkOnSurface,
    surfaceVariant = FalloutDarkSurfaceVariant,
    onSurfaceVariant = FalloutDarkOnSurfaceVariant,
    outline = FalloutDarkOutline,
    inverseOnSurface = FalloutDarkInverseOnSurface,
    inverseSurface = FalloutDarkInverseSurface,
    inversePrimary = FalloutDarkInversePrimary,
)

private val FalloutLightColorScheme = lightColorScheme(
    primary = FalloutLightPrimary,
    onPrimary = FalloutLightOnPrimary,
    primaryContainer = FalloutLightPrimaryContainer,
    onPrimaryContainer = FalloutLightOnPrimaryContainer,
    secondary = FalloutLightSecondary,
    onSecondary = FalloutLightOnSecondary,
    secondaryContainer = FalloutLightSecondaryContainer,
    onSecondaryContainer = FalloutLightOnSecondaryContainer,
    tertiary = FalloutLightTertiary,
    onTertiary = FalloutLightOnTertiary,
    tertiaryContainer = FalloutLightTertiaryContainer,
    onTertiaryContainer = FalloutLightOnTertiaryContainer,
    error = FalloutLightError,
    errorContainer = FalloutLightErrorContainer,
    onError = FalloutLightOnError,
    onErrorContainer = FalloutLightOnErrorContainer,
    background = FalloutLightBackground,
    onBackground = FalloutLightOnBackground,
    surface = FalloutLightSurface,
    onSurface = FalloutLightOnSurface,
    surfaceVariant = FalloutLightSurfaceVariant,
    onSurfaceVariant = FalloutLightOnSurfaceVariant,
    outline = FalloutLightOutline,
    inverseOnSurface = FalloutLightInverseOnSurface,
    inverseSurface = FalloutLightInverseSurface,
    inversePrimary = FalloutLightInversePrimary,
)

@Composable
fun expgessiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ✅ ВЫКЛЮЧИЛИ dynamicColor
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // ❌ Убираем условие с dynamicColor
        darkTheme -> FalloutDarkColorScheme
        else -> FalloutLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}