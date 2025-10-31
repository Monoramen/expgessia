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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.expgessia.R
import app.expgessia.presentation.ui.theme.FalloutFontFamily


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
            },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[currentTab.ordinal])
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        ) {
            Tab(
                selected = currentTab == Tab.HERO,
                onClick = { onTabChange(Tab.HERO) },
                modifier = Modifier.height(36.dp),
                text = {
                    Text(
                        stringResource(R.string.nav_hero).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        fontFamily = FalloutFontFamily
                    )
                }
            )

            Tab(
                selected = currentTab == Tab.CHARACTERISTICS,
                onClick = { onTabChange(Tab.CHARACTERISTICS) },
                modifier = Modifier.height(36.dp),
                text = {
                    Text(
                        stringResource(R.string.nav_characteristics).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        fontFamily = FalloutFontFamily
                    )
                }
            )


        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

enum class Tab {
    HERO, CHARACTERISTICS
}
