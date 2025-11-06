package app.expgessia.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.expgessia.presentation.ui.components.calendar.DateCarousel
import app.expgessia.presentation.viewmodel.CalendarViewModel
import app.expgessia.ui.components.TaskItem
import java.time.LocalDate

@Composable
fun TaskDetailsScreen(
    date: LocalDate,
    onSelectedDateChange: (LocalDate) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel(),
) {
    var selectedDate by remember { mutableStateOf(date) }

    val dailyTasksUi by calendarViewModel.getTasksForDate(selectedDate)
        .collectAsState(initial = emptyList())

    LaunchedEffect(selectedDate) {
        onSelectedDateChange(selectedDate)
        calendarViewModel.prepareTasksForDate(selectedDate)
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DateCarousel(
                selectedDate = selectedDate,
                onDateSelected = { newDate ->
                    selectedDate = newDate
                }
            )

            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(dailyTasksUi, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onTaskCheckClicked = { taskId ->
                            calendarViewModel.onTaskCheckClickedForDate(taskId, selectedDate) { }
                        },
                        onTaskEditClicked = onEditTaskClicked
                    )
                }
            }
        }
    }
}