package app.expgessia.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.usecase.TaskScheduler
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import toEntity
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository,
    private val characteristicRepository: CharacteristicRepository,
    private val taskScheduler: TaskScheduler,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _dailyTasksState = MutableStateFlow<DailyTasksState>(DailyTasksState.Loading)
    val dailyTasksState: StateFlow<DailyTasksState> = _dailyTasksState

    private val _refreshTrigger = MutableStateFlow(0)

    init {
        Log.d("CalendarViewModel", "🔄 Initializing CalendarViewModel")

        if (_selectedDate.value == null) {
            _selectedDate.value = LocalDate.now()
        }

        // Создаём задачи для видимого диапазона
        viewModelScope.launch {
            val visibleStart = LocalDate.now().minusMonths(1)
            val visibleEnd = LocalDate.now().plusMonths(1)
            taskScheduler.ensureInstancesForDateRange(visibleStart, visibleEnd)
        }

        // ✅ Новый реактивный поток: при смене даты или refresh обновляем список
        viewModelScope.launch {
            combine(_selectedDate, _refreshTrigger) { date, _ -> date }
                .filterNotNull()
                .flatMapLatest { date ->
                    Log.d("CalendarViewModel", "📅 Listening for task updates for $date")
                    taskScheduler.ensureInstancesForDate(date)
                    getTasksForDateStream(date)
                }
                .collect { tasks ->
                    _dailyTasksState.value = DailyTasksState.Success(tasks)
                    Log.d("CalendarViewModel", "📊 Updated ${tasks.size} tasks for selected date")
                }
        }
    }



    fun onDayClicked(date: LocalDate) {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "📅 Day clicked: $date")
            _selectedDate.value = date
        }
    }

    fun initializeCalendar(visibleMonth: LocalDate) {
        viewModelScope.launch {
            val startDate = visibleMonth.minusMonths(1).withDayOfMonth(1)
            val endDate = visibleMonth.plusMonths(1).withDayOfMonth(visibleMonth.plusMonths(1).lengthOfMonth())

            Log.d("CalendarViewModel", "📅 Initializing calendar with range: $startDate - $endDate")
            taskScheduler.ensureInstancesForDateRange(startDate, endDate)
        }
    }


    fun ensureInstancesAndRefresh(date: LocalDate) {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "🔄 Ensuring instances and refreshing for date: $date")
            _selectedDate.value = date

            // 🔥 ТОЛЬКО ОДИН вызов - через TaskScheduler
            taskScheduler.ensureInstancesForDate(date)

            _refreshTrigger.value += 1
            Log.d("CalendarViewModel", "✅ Instances ensured and data refreshed for $date")
        }
    }

    private fun getTasksForDateStream(date: LocalDate): Flow<List<TaskUiModel>> {
        return combine(
            taskCompletionRepository.getTasksForCalendarDate(date),
            taskCompletionRepository.getCompletedTasksInDateRange(date, date),
            _refreshTrigger
        ) { taskWithInstances, completedInstances, _ -> // ← добавлен refreshTrigger
            val completedTaskIds = completedInstances.map { it.taskId }.toSet()

            taskWithInstances.map { taskWithInstance ->
                val iconName = taskWithInstance.task.let { task ->
                    characteristicRepository.getCharacteristicById(task.characteristicId)?.iconResName ?: ""
                }

                val isCompleted = completedTaskIds.contains(taskWithInstance.task.id)

                TaskUiModel(
                    id = taskWithInstance.task.id,
                    title = taskWithInstance.task.title,
                    description = taskWithInstance.task.description,
                    xpReward = taskWithInstance.task.xpReward,
                    isCompleted = isCompleted,
                    characteristicIconResName = iconName,
                    date = date
                )
            }
        }
    }


    fun onTaskCheckClickedForDate(taskId: Long, date: LocalDate) {
        viewModelScope.launch {
            try {
                val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)
                val isCompleted = taskCompletionRepository.isTaskCompletedForDate(taskId, startOfDayMillis)

                Log.d("CalendarViewModel", "🔄 Changing task $taskId status for $date (currently completed: $isCompleted)")

                if (isCompleted) {
                    taskCompletionRepository.undoCompleteTaskForDate(taskId, date)
                    Log.d("CalendarViewModel", "📝 Task $taskId marked as NOT completed for $date")
                } else {
                    val completionTime = startOfDayMillis
                    taskCompletionRepository.completeTask(taskId, completionTime)
                    Log.d("CalendarViewModel", "✅ Task $taskId marked as completed for $date at $completionTime")
                }

                // 🔥 УБЕДИТЕСЬ, что refreshTrigger обновляется
                _refreshTrigger.value++

            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Failed to change task status for date", e)
            }
        }
    }





    sealed class DailyTasksState {
        object Loading : DailyTasksState()
        data class Success(val tasks: List<TaskUiModel>) : DailyTasksState()
        data class Error(val message: String) : DailyTasksState()
    }

    fun getTasksForMonth(month: LocalDate): Flow<Map<LocalDate, List<Task>>> {
        return taskRepository.getRepeatingTasks()
            .map { repeatingTasks ->
                val startOfMonth = month.withDayOfMonth(1)
                val daysInMonth = month.lengthOfMonth()
                val tasksByDate = mutableMapOf<LocalDate, MutableList<Task>>()

                for (i in 0 until daysInMonth) {
                    val date = startOfMonth.plusDays(i.toLong())
                    val tasksForDay = repeatingTasks.filter { task ->
                        TimeUtils.isTaskScheduledOnDate(task.toEntity(), date)
                    }
                    if (tasksForDay.isNotEmpty()) {
                        tasksByDate[date] = tasksForDay.toMutableList()
                    }
                }

                tasksByDate
            }
    }

    fun getCompletedTasksForMonth(month: LocalDate): Flow<Map<LocalDate, List<Long>>> {
        val startDate = month.withDayOfMonth(1)
        val endDate = month.withDayOfMonth(month.lengthOfMonth())

        return taskCompletionRepository.getCompletedTasksInDateRange(startDate, endDate)
            .map { completedInstances ->
                completedInstances.groupBy { instance ->
                    TimeUtils.millisToLocalDate(instance.completedAt!!)
                }.mapValues { (_, instances) ->
                    instances.map { it.taskId }
                }
            }
    }

    fun getTasksWithCompletionForMonth(month: LocalDate): Flow<Pair<Map<LocalDate, List<Task>>, Map<LocalDate, List<Long>>>> {
        return combine(
            getTasksForMonth(month),
            getCompletedTasksForMonth(month)
        ) { tasks, completed ->
            tasks to completed
        }
    }


    fun setSelectedDateFromCarousel(date: LocalDate) {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "📅 Date selected from carousel: $date")
            _selectedDate.value = date

            taskScheduler.ensureInstancesForDate(date)

            _refreshTrigger.value += 1
            Log.d("CalendarViewModel", "✅ Carousel date set and instances ensured for $date")
        }
    }

}