// app.expgessia.ui.components.AppBottomNavigation.kt
package app.expgessia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
// 💡 ИСПОЛЬЗУЕМ NavigationBar ВМЕСТО BottomAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale // Для применения анимации масштаба
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.expgessia.R

// Список для упрощения итерации по элементам
private data class NavItem(
    val route: String,
    val iconRes: Int,
    val contentDescRes: Int
)

private val navItems = listOf(
    NavItem("hero", R.drawable.hero, R.string.nav_hero),
    NavItem("tasks", R.drawable.tasks, R.string.nav_tasks),
    NavItem("calendar", R.drawable.calendar, R.string.nav_calendar),
    NavItem("stats", R.drawable.statistic, R.string.nav_stats)
)

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 💡 ИСПОЛЬЗУЕМ NavigationBar (более современный M3 компонент)
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // Можно убрать elevation, если не нужно тени
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            // Делаем текст менее заметным или убираем (если хотите только иконки)
            selectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            // Используем прозрачный индикатор, чтобы избежать цветной заливки под иконкой
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )

        navItems.forEach { item ->
            val isSelected = currentRoute == item.route

            // ⭐️ АНИМАЦИЯ: Плавное изменение масштаба
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f, // Увеличиваем на 15% при выборе
                animationSpec = tween(durationMillis = 200), // Длительность анимации
                label = "nav_icon_scale"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = stringResource(item.contentDescRes),
                        modifier = Modifier
                            .size(38.dp)
                            .scale(scale), // ⭐️ Применяем анимацию масштаба
                    )
                },
                // Если хотите добавить текст под иконкой (как в стандартной навигации)
                // label = { Text(stringResource(item.contentDescRes)) },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = itemColors
            )
        }
    }
}