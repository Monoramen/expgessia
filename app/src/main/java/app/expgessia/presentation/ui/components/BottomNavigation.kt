// ui/components/BottomNavigation.kt
package app.expgessia.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Hero") },
            selected = currentRoute == "hero",
            onClick = { onNavigate("hero") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Menu, contentDescription = null) },
            label = { Text("Tasks") },
            selected = currentRoute == "tasks",
            onClick = { onNavigate("tasks") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
            label = { Text("Calendar") },
            selected = currentRoute == "calendar",
            onClick = { onNavigate("calendar") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
            label = { Text("Stats") },
            selected = currentRoute == "stats",
            onClick = { onNavigate("stats") }
        )
    }
}