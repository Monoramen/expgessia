// app.expgessia.ui.components.AppBottomNavigation.kt
package app.expgessia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.expgessia.R

// Список для упрощения итерации по элементам
private data class NavItem(
    val route: String,
    val iconRes: Int,
    val contentDescRes: Int,
)

private val navItems = listOf(
    NavItem("hero", R.drawable.hero, R.string.nav_hero),
    NavItem("tasks", R.drawable.tasks, R.string.nav_tasks),
    NavItem("calendar", R.drawable.calendar, R.string.nav_calendar),
    NavItem("stats", R.drawable.statistic, R.string.nav_stats)
)

/**
 * Модификатор для отрисовки круглой обводки с эффектом исчезающих краев (радиальный градиент).
 * Обводка рисуется только в том случае, если isSelected = true.
 */

@Composable
private fun CustomBottomNavItem(
    item: NavItem,
    isSelected: Boolean,
    onNavigate: (String) -> Unit,
    outlineColor: Color,
    iconSize: Dp,
    outlineWidth: Dp,
) {
    // ⭐️ АНИМАЦИЯ: Плавное изменение масштаба
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 250),
        label = "nav_icon_scale"
    )

    val iconColor = if (isSelected) outlineColor else MaterialTheme.colorScheme.onSurfaceVariant

    // 1. Column занимает всю высоту и реагирует на нажатие
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            // ⭐ КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: Используем Modifier.clickable с indication = null
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Полностью отключает эффект ряби (ripple)
                onClick = { onNavigate(item.route) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 2. Box для контейнера обводки
        Box(
            modifier = Modifier
                .size(iconSize + outlineWidth * 2),
            contentAlignment = Alignment.Center
        ) {
            // 3. Сама иконка
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = stringResource(item.contentDescRes),
                modifier = Modifier
                    .size(iconSize)
                    .scale(scale), // Применяем анимацию масштаба
                tint = iconColor
            )
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.primary
    val iconSize = 36.dp
    val outlineWidth = 8.dp

    NavigationBar(
        modifier = modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    CustomBottomNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onNavigate = onNavigate,
                        outlineColor = outlineColor,
                        iconSize = iconSize,
                        outlineWidth = outlineWidth
                    )
                }
            }
        }
    }
}