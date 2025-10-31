package app.expgessia.presentation.ui.components.navbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CustomTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = Icons.Default.ArrowBackIosNew,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    // ⭐️ НОВЫЙ ПАРАМЕТР: Слот для контента под основным баром (например, для табов)
    bottomContent: @Composable () -> Unit = {},
    mainContent: @Composable (() -> Unit)? = null,
) {

    // ⭐️ ВЕРХНИЙ УРОВЕНЬ: COLUMN для объединения главного бара и нижнего контента
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        if (mainContent != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                mainContent()

            }
        } else {
            // --- 1. ОСНОВНОЙ БАР (ЗАГОЛОВОК, КНОПКИ) ---
            Row(
                // Отступы для статус-бара применяются только к этой верхней Row
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (navigationIcon != null && onNavigationClick != null) {
                        IconButton(
                            onClick = onNavigationClick,
                        ) {
                            Icon(
                                imageVector = navigationIcon,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = title.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .offset(y = (-10).dp)
                    )
                }

                Row(
                    // ... (основной код Actions)
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    actions()
                }
            }
        }

        // --- 2. НИЖНИЙ КОНТЕНТ (ТАБЫ) ---
        bottomContent()
    }
}