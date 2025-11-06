package app.expgessia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.domain.usecase.CompleteTaskUseCase
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {


    suspend fun prepareTasksForDate(date: LocalDate) {
        taskCompletionRepository.ensureTaskInstancesForDate(date)
    }

    fun onDayClicked(date: LocalDate) {
        viewModelScope.launch {
            prepareTasksForDate(date)
        }
    }

    // 🔥 ДОБАВЛЕНО: Метод для переключения статуса задачи в календаре
    fun onTaskCheckClickedForDate(taskId: Long, date: LocalDate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)
                val isCompleted = taskCompletionRepository.isTaskCompletedForDate(taskId, startOfDayMillis)

                android.util.Log.d("CalendarViewModel", "🔄 Changing task $taskId status for $date (currently completed: $isCompleted)")

                if (isCompleted) {
                    taskCompletionRepository.undoCompleteTask(taskId)
                    android.util.Log.d("CalendarViewModel", "📝 Task $taskId marked as NOT completed for $date")
                } else {
                    // Используем CompleteTaskUseCase для выполнения задачи
                    completeTaskUseCase(taskId, System.currentTimeMillis())
                    android.util.Log.d("CalendarViewModel", "✅ Task $taskId marked as completed for $date")
                }
                onComplete?.invoke()
            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "Failed to change task status for date", e)
            }
        }
    }

    // 🔥 ИСПРАВЛЕНО: Убираем дублирующий метод
    fun getTasksForDate(date: LocalDate): Flow<List<TaskUiModel>> {
        return taskCompletionRepository.getTasksForCalendarDate(date).map { taskWithInstanceList ->
            taskWithInstanceList.map { taskWithInstance ->
                val iconName = taskWithInstance.task.let { task ->
                    characteristicRepository.getCharacteristicById(task.characteristicId)?.iconResName ?: ""
                }

                TaskUiModel(
                    id = taskWithInstance.task.id,
                    title = taskWithInstance.task.title,
                    description = taskWithInstance.task.description,
                    xpReward = taskWithInstance.task.xpReward,
                    isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                    characteristicIconResName = iconName,
                    date = date
                )
            }
        }
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

    private fun getCompletedTasksForMonth(month: LocalDate): Flow<Map<LocalDate, List<Long>>> {
        val startDate = month.withDayOfMonth(1)
        val endDate = month.withDayOfMonth(month.lengthOfMonth())

        return taskCompletionRepository.getCompletedTasksInDateRange(startDate, endDate)
            .map { completedInstances ->
                completedInstances.groupBy { instance ->
                    LocalDate.ofEpochDay(instance.completedAt!! / (24 * 60 * 60 * 1000))
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
}