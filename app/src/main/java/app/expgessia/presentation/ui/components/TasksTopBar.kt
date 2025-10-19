package app.expgessia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppHeroTabs(
    currentTab: Tab,
    onTabChange: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        TabRow(
            selectedTabIndex = when (currentTab) {
                Tab.HERO -> 0
                Tab.CHARACTERISTICS -> 1
                Tab.SKILLS -> 2
            },
            // Фон табов: темный (secondaryContainer) для контраста
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            // Цвет текста табов: неоновый зеленый (onSurface)
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[currentTab.ordinal])
                        .height(2.dp)
                        .fillMaxWidth()
                        // Индикатор: яркий неоновый зеленый (primary)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) {
            Tab(
                selected = currentTab == Tab.HERO,
                onClick = { onTabChange(Tab.HERO) },
                text = {
                    Text(
                        "HERO",
                        fontSize = 12.sp,
                        // Используем цвет текста из TabRow, который установлен как onSurface (неоновый зеленый)
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            )

            Tab(
                selected = currentTab == Tab.CHARACTERISTICS,
                onClick = { onTabChange(Tab.CHARACTERISTICS) },
                text = {
                    Text(
                        "CHARACTERISTICS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            )

            /* Tab(
                            selected = currentTab == Tab.SKILLS,
                            onClick = { onTabChange(Tab.SKILLS) },
                            text = {
                                Text(
                                    "SKILLS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        )*/
        }
    }
}


enum class Tab {
    HERO, CHARACTERISTICS, SKILLS
}
