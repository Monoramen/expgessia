package app.expgessia


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import app.expgessia.presentation.screen.CalendarScreen
import app.expgessia.presentation.screen.CharacteristicScreen
import app.expgessia.presentation.screen.SkillScreen
import app.expgessia.presentation.screen.StatsScreen
import app.expgessia.presentation.screen.Task
import app.expgessia.presentation.screen.TaskScreen
import app.expgessia.presentation.screen.UserScreen
import app.expgessia.ui.components.AppBottomNavigation
import app.expgessia.ui.components.Tab
import app.expgessia.ui.components.TaskItemData
import app.expgessia.ui.components.TopAppHeroTabs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController? = null,
    modifier: Modifier = Modifier
) {

    var currentTab by remember { mutableStateOf(Tab.HERO) }
    var currentRoute by remember { mutableStateOf("hero") }

    val tasks = listOf(
        TaskItemData(
            title = "Learn something new",
            description = "anything. press when feel like it happened",
            xpReward = "1.1XP∞",
            isCompleted = true
        )
    )

    val demoTasks = listOf(
        Task("Выучить Jetpack Compose", "Пройти 3 урока", "+50 XP", false),
        Task("Сделать тренировку", "30 минут кардио", "+30 XP", true),
        Task("Почитать книгу", "20 страниц", "+20 XP", false)
    )

    var showToday by remember { mutableStateOf(true) }
    var showTomorrow by remember { mutableStateOf(true) }
    var showImportant by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            when (currentRoute) {
                "hero" -> {
                    TopAppBar(
                        title = { Text("expgessia") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Users"
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                        }
                    )
                }

                "tasks" -> {
                    TopAppBar(
                        title = { Text("All Tasks") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = { /* возможно открыть меню */ }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* поиск */ }) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search"
                                )
                            }
                            IconButton(onClick = { /* меню */ }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                        }
                    )
                }

                else -> { /* другие экраны */
                }
            }
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            when (currentRoute) {
                "hero" -> {
                    // 🔽 Показываем верхние табы ТОЛЬКО на экране Hero
                    TopAppHeroTabs(
                        currentTab = currentTab,
                        onTabChange = { currentTab = it }
                    )

                    when (currentTab) {
                        Tab.HERO -> UserScreen(
                            modifier = Modifier.fillMaxSize()
                        )

                        Tab.CHARACTERISTICS -> CharacteristicScreen()
                        Tab.SKILLS -> SkillScreen()
                    }
                }

                "tasks" -> TaskScreen(tasks = demoTasks)
                "calendar" -> CalendarScreen()
                "stats" -> StatsScreen()
            }
        }
    }
}
