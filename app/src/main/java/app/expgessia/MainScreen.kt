package app.expgessia

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.expgessia.presentation.screen.AddTaskScreen
import app.expgessia.presentation.screen.CalendarScreen
import app.expgessia.presentation.screen.CharacteristicScreen
import app.expgessia.presentation.screen.TaskRoute
import app.expgessia.presentation.screen.UserScreen
import app.expgessia.presentation.ui.components.CustomTopAppBar
import app.expgessia.presentation.ui.screens.StatsScreen
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.AppBottomNavigation
import app.expgessia.ui.components.Tab
import app.expgessia.ui.components.TopAppHeroTabs


// --- КОНСТАНТЫ МАРШРУТОВ (для ясности) ---
const val HERO_ROUTE = "hero"
const val TASK_ROUTE = "tasks"
const val CALENDAR_ROUTE = "calendar"
const val STATS_ROUTE = "stats"

// Маршрут для добавления/редактирования с аргументом taskId
const val ADD_EDIT_TASK_ROUTE = "add_edit_task_route?taskId={taskId}"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    // Инициализируем NavController, если он не передан
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {

    var currentTab by remember { mutableStateOf(Tab.HERO) }

    // 1. УДАЛЯЕМ taskToEditId и currentRoute (он будет из NavController)
    val taskViewModel: TaskViewModel = hiltViewModel()

    // Получаем текущий маршрут из NavController для TopBar и BottomBar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HERO_ROUTE

    // Определяем основной маршрут (без аргументов) для логики UI
    val mainTabRoute = currentRoute.substringBefore("?")
    val isMainScreen = mainTabRoute in listOf(HERO_ROUTE, TASK_ROUTE, CALENDAR_ROUTE, STATS_ROUTE)

    // 2. Унифицированная функция навигации к экрану задачи
    val navigateToTask = { taskId: Long ->
        val route = "add_edit_task_route?taskId=$taskId"
        navController.navigate(route)
    }


    Scaffold(
        topBar = {
            // TopBar теперь реагирует на основной маршрут
            when (mainTabRoute) {
                TASK_ROUTE -> {
                    CustomTopAppBar(
                        title = stringResource(R.string.nav_tasks),
                        navigationIcon = Icons.Filled.ArrowBackIosNew,
                        // Навигация назад к Hero, очищая бэкстек
                        onNavigationClick = {
                            navController.navigate(HERO_ROUTE) {
                                popUpTo(
                                    HERO_ROUTE
                                ) { inclusive = true }
                            }
                        },
                        actions = {
                            // ✅ Кнопка "Добавить" вызывает NavController.navigate
                            IconButton(onClick = { navigateToTask(0L) }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { /* TODO: onSearch */ }) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { /* TODO: onMenuClick */ }) {
                                androidx.compose.material3.Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }

                HERO_ROUTE -> {
                    CustomTopAppBar(
                        title = stringResource(R.string.app_name),
                        navigationIcon = null,
                        onNavigationClick = null,
                        bottomContent = {
                            TopAppHeroTabs(
                                currentTab = currentTab,
                                onTabChange = { newTab -> currentTab = newTab }
                            )
                        }
                    )
                }

                STATS_ROUTE -> {
                    CustomTopAppBar(
                        title = stringResource(R.string.nav_stats),
                        navigationIcon = Icons.Filled.ArrowBackIosNew,
                        onNavigationClick = {
                            navController.navigate(HERO_ROUTE) {
                                popUpTo(
                                    HERO_ROUTE
                                ) { inclusive = true }
                            }
                        }
                    )
                }

                CALENDAR_ROUTE -> {
                    CustomTopAppBar(
                        title = stringResource(R.string.nav_calendar),
                        navigationIcon = Icons.Filled.ArrowBackIosNew,
                        onNavigationClick = {
                            navController.navigate(HERO_ROUTE) {
                                popUpTo(
                                    HERO_ROUTE
                                ) { inclusive = true }
                            }
                        }
                    )
                }

                ADD_EDIT_TASK_ROUTE.substringBefore("?") -> {
                    // Определяем, режим это редактирования или добавления, для заголовка
                    val isEditMode =
                        navBackStackEntry?.arguments?.getLong("taskId")?.let { it != 0L } ?: false

                    CustomTopAppBar(
                        title = stringResource(
                            if (isEditMode) R.string.label_edit_task else R.string.label_add_new_task
                        ),
                        navigationIcon = Icons.Filled.ArrowBackIosNew,
                        onNavigationClick = { navController.popBackStack() } // Просто возвращаемся назад
                    )
                }
            }
        },
        bottomBar = {
            // Показываем BottomBar только на основных экранах
            if (isMainScreen) {
                AppBottomNavigation(
                    currentRoute = mainTabRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        // 3. NavHost заменяет старую логику Column + when(currentRoute)
        NavHost(
            navController = navController,
            startDestination = HERO_ROUTE,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // --- ОСНОВНЫЕ ТАБЫ ---
            composable(HERO_ROUTE) {
                when (currentTab) {
                    Tab.HERO -> UserScreen(modifier = Modifier.fillMaxSize())
                    Tab.CHARACTERISTICS -> CharacteristicScreen()
                }
            }

            composable(TASK_ROUTE) {
                TaskRoute(
                    onAddTaskClicked = { }, // Оставляем пустым, так как кнопка в TopBar
                    // ✅ Редактирование: TaskRoute вызывает навигацию
                    onEditTaskClicked = { taskId -> navigateToTask(taskId) }
                )
            }

            composable(CALENDAR_ROUTE) { CalendarScreen() }
            composable(STATS_ROUTE) { StatsScreen() }

            // --- НОВАЯ ДЕСТИНАЦИЯ: Add/Edit Task Screen ---
            composable(
                route = ADD_EDIT_TASK_ROUTE,
                arguments = listOf(navArgument("taskId") {
                    type = NavType.LongType; defaultValue = 0L
                })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId")

                AddTaskScreen(
                    viewModel = hiltViewModel(), // hiltViewModel для этого экрана
                    taskIdToEdit = taskId,
                    // Кнопка "Назад" просто возвращается к предыдущему экрану (TASK_ROUTE)
                    onBackClicked = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}