package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.presentation.ui.components.calendar.CalendarTasksGrid
import app.expgessia.presentation.viewmodel.TaskViewModel
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClicked: (LocalDate) -> Unit,    taskViewModel: TaskViewModel = hiltViewModel(),

) {
    // При переходе на UserScreen, TaskScreen, CalendarScreen
    LaunchedEffect(Unit) {
        taskViewModel.syncAllTasks()
    }

    CalendarTasksGrid(
        currentMonth = currentMonth,
        onDayClicked = onDayClicked,
        onPreviousMonth = onPreviousMonth,
        onNextMonth = onNextMonth,
        modifier = Modifier.fillMaxSize()
    )
}

