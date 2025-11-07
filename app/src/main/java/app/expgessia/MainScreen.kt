package app.expgessia

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import app.expgessia.presentation.screen.TaskDetailsScreen
import app.expgessia.presentation.screen.TaskRoute
import app.expgessia.presentation.screen.UserScreen
import app.expgessia.presentation.ui.components.navbar.CustomTopAppBar
import app.expgessia.presentation.ui.screens.StatsScreen
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.AppBottomNavigation
import app.expgessia.ui.components.Tab
import app.expgessia.ui.components.TopAppHeroTabs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

const val HERO_ROUTE = "hero"
const val TASK_ROUTE = "tasks"
const val CALENDAR_ROUTE = "calendar"
const val STATS_ROUTE = "stats"

const val ADD_EDIT_TASK_ROUTE = "add_edit_task_route?taskId={taskId}"
const val DAILY_TASKS_ROUTE = "daily_tasks_route?date={date}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(

    // Инициализируем NavController, если он не передан
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var currentTab by remember { mutableStateOf(Tab.HERO) }
    var topBarDate by remember { mutableStateOf(LocalDate.now()) }
    // 1. УДАЛЯЕМ taskToEditId и currentRoute (он будет из NavController)
    val taskViewModel: TaskViewModel = hiltViewModel()
    var selectedTaskFilter by remember { mutableStateOf("All Tasks") }
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
    val navigateToDailyTasks = { date: LocalDate ->
        // Форматируем дату в строку для передачи через аргумент
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val route = "daily_tasks_route?date=$dateString"
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
                        onNavigationClick = {
                            navController.popBackStack()
                        },
                        actions = {
                            IconButton(onClick = { navigateToTask(0L) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { /* TODO: onSearch */ }) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { /* TODO: onMenuClick */ }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        // ⭐️ ПЕРЕДАЕМ СОСТОЯНИЕ ФИЛЬТРА ИЗ MAINSCREEN
                        filterOptions = listOf("All Tasks", "Today", "Tomorrow", "Completed"),
                        selectedFilter = selectedTaskFilter,
                        onFilterSelected = { filter ->
                            selectedTaskFilter = filter
                        },
                        showFilter = true
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

                        title = "",
                        navigationIcon = null, // Не используем, заменено на навигацию по месяцам
                        onNavigationClick = null,
                        actions = {
                            // Если нужны иконки действий справа, добавьте их сюда
                        },
                        // ⭐️ ПЕРЕНОСИМ ЛОГИКУ МЕСЯЦА ПРЯМО В mainContent
                        mainContent = {

                            val monthYearText = currentMonth.format(
                                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                                    .withLocale(Locale.getDefault())
                            )
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            // ⭐️ 1. Кнопка "Назад" (ChevronLeft)
                            IconButton(
                                onClick = { currentMonth = currentMonth.minusMonths(1) },
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBackIosNew,
                                    contentDescription = "Previous month"
                                )
                            }

                            // ⭐️ 2. Месяц/Год
                            Text(
                                text = monthYearText.uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .offset(y = (-10).dp)
                            )

                            // ⭐️ 3. Кнопка "Вперед" (ChevronRight)
                            IconButton(
                                onClick = { currentMonth = currentMonth.plusMonths(1) },
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForwardIos,
                                    contentDescription = "Next month"
                                )
                            }
                        }
                    )
                }

                DAILY_TASKS_ROUTE.substringBefore("?") -> {
                    val dateString = navBackStackEntry?.arguments?.getString("date")
                    val initialDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

                    // 2. Инициализируем topBarDate при первом входе на экран.
                    LaunchedEffect(key1 = initialDate) {
                        topBarDate = initialDate
                    }

                    // 3. Используем topBarDate (обновляется из TaskDetailsScreen) для заголовка
                    val titleDate = topBarDate

                    // ⭐️ НОВОЕ ИСПРАВЛЕНИЕ: Отображаем ТОЛЬКО ГОД.
                    val dynamicTitle = titleDate.year.toString()
                    CustomTopAppBar(
                        title = dynamicTitle, // ⭐️ ИСПОЛЬЗУЕМ ДИНАМИЧЕСКИЙ ЗАГОЛОВОК
                        navigationIcon = Icons.Filled.ArrowBackIosNew,
                        onNavigationClick = { navController.popBackStack() },
                        actions = {
                            IconButton(onClick = { navigateToTask(0L) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
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
                // ⭐️ ПЕРЕДАЕМ СОСТОЯНИЕ ФИЛЬТРА В TASKROUTE
                TaskRoute(
                    selectedFilter = selectedTaskFilter,
                    onAddTaskClicked = { navigateToTask(0L) },
                    onEditTaskClicked = { taskId -> navigateToTask(taskId) }
                )
            }

            composable(CALENDAR_ROUTE) {
                CalendarScreen(
                    currentMonth = currentMonth,
                    // ⭐️ ПЕРЕДАЕМ ФУНКЦИИ ИЗ СТАНДАРТНОГО БАРА
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onDayClicked = navigateToDailyTasks
                )
            }

// В MainScreen в NavHost
            composable(
                route = DAILY_TASKS_ROUTE,
                arguments = listOf(navArgument("date") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date")
                val date = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

                TaskDetailsScreen(
                    date = date,
                    onSelectedDateChange = { newDate ->
                        topBarDate = newDate
                    },
                    // 💡 ПЕРЕДАЕМ ФУНКЦИЮ НАВИГАЦИИ НА РЕДАКТИРОВАНИЕ
                    onEditTaskClicked = { taskId ->
                        navigateToTask(taskId)
                    }
                )
            }

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