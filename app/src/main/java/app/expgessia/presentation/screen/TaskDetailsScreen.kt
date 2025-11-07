package app.expgessia.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.expgessia.presentation.ui.components.calendar.DateCarousel
import app.expgessia.presentation.viewmodel.CalendarViewModel
import app.expgessia.presentation.viewmodel.TaskViewModel
import app.expgessia.ui.components.TaskItem
import java.time.LocalDate

@Composable
fun TaskDetailsScreen(
    date: LocalDate,
    onSelectedDateChange: (LocalDate) -> Unit,
    onEditTaskClicked: (Long) -> Unit,
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
) {
    val selectedDate by calendarViewModel.selectedDate.collectAsStateWithLifecycle()
    val dailyTasksState by calendarViewModel.dailyTasksState.collectAsStateWithLifecycle()

    val currentDisplayDate = selectedDate ?: date

    Column(modifier = Modifier.fillMaxSize()) {

        DateCarousel(
            selectedDate = currentDisplayDate,
            onDateSelected = { newDate ->
                Log.d("TaskDetailsScreen", "🎯 Date selected in carousel: $newDate")
                calendarViewModel.setSelectedDateFromCarousel(newDate)
                onSelectedDateChange(newDate)
            },
            calendarViewModel = calendarViewModel
        )

        when (dailyTasksState) {
            is CalendarViewModel.DailyTasksState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is CalendarViewModel.DailyTasksState.Success -> {
                val tasks = (dailyTasksState as CalendarViewModel.DailyTasksState.Success).tasks

                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onTaskCheckClicked = { taskId ->
                                calendarViewModel.onTaskCheckClickedForDate(
                                    taskId,
                                    currentDisplayDate
                                )
                            },
                            onTaskEditClicked = onEditTaskClicked
                        )
                    }

                    if (tasks.isEmpty()) {
                        item {
                            Text(
                                text = "No tasks for $currentDisplayDate",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is CalendarViewModel.DailyTasksState.Error -> {
                Text(
                    text = (dailyTasksState as CalendarViewModel.DailyTasksState.Error).message,
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
