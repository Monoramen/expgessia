package app.expgessia

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.expgessia.presentation.screen.AddTaskScreen
import app.expgessia.presentation.screen.CalendarScreen
import app.expgessia.presentation.screen.CharacteristicScreen
import app.expgessia.presentation.screen.SkillScreen
import app.expgessia.presentation.screen.TaskRoute
import app.expgessia.presentation.screen.UserScreen
import app.expgessia.presentation.ui.components.HeroTopBar
import app.expgessia.presentation.ui.components.SimpleBackAppBar
import app.expgessia.presentation.ui.screens.StatsScreen
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.AppBottomNavigation
import app.expgessia.ui.components.Tab
import app.expgessia.ui.components.TasksTopBar
import app.expgessia.ui.components.TopAppHeroTabs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController? = null,
    modifier: Modifier = Modifier
) {

    var currentTab by remember { mutableStateOf(Tab.HERO) }
    var currentRoute by remember { mutableStateOf("hero") }
    var showAddTask by remember { mutableStateOf(false) }

    val taskViewModel: TaskViewModel = hiltViewModel()

    if (showAddTask) {
        AddTaskScreen(
            viewModel = taskViewModel,
            onBackClicked = {
                showAddTask = false
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // ⬇️ ОСНОВНОЙ СКРИН
    Scaffold(
        topBar = {
            when (currentRoute) {
                "tasks" -> {
                    TasksTopBar(onAddTaskClicked = { showAddTask = true }
                    )
                }

                "hero" -> {
                    HeroTopBar()
                }

                "stats" -> {
                    SimpleBackAppBar(
                        title = stringResource(R.string.nav_stats).uppercase(),
                        onBackClicked = {
                            currentRoute = "hero"
                        }
                    )
                }

                "calendar" -> {
                    SimpleBackAppBar(
                        title = stringResource(R.string.nav_calendar).uppercase(),
                        onBackClicked = {
                            currentRoute = "hero"
                        }
                    )
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentRoute) {
                "hero" -> {
                    TopAppHeroTabs(
                        currentTab = currentTab,
                        onTabChange = { currentTab = it }
                    )

                    when (currentTab) {
                        Tab.HERO -> UserScreen(
                            modifier = Modifier.fillMaxSize()
                        )

                        Tab.CHARACTERISTICS -> CharacteristicScreen()
                    }
                }

                "tasks" -> TaskRoute(
                    onAddTaskClicked = { showAddTask = true }
                )

                "calendar" -> CalendarScreen()

                // ⭐️ ИЗМЕНЕНИЕ 2: УДАЛЯЕМ onBackClicked из вызова StatsScreen,
                // так как его навигацией теперь управляет Scaffold в topBar.
                "stats" -> StatsScreen()
            }
        }
    }
}