package app.expgessia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map // Импорт map для работы с Flow
import toDomain
import toEntity // Предполагаем, что это extension-функция для Task
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository,
    private val characteristicRepository: CharacteristicRepository
) : ViewModel() {
    private val _refreshTrigger = MutableStateFlow(0)

    // Исправленный метод для получения задач по дате
    fun getTasksForDate(date: LocalDate): Flow<List<TaskUiModel>> {
        return _refreshTrigger.flatMapLatest {
            taskCompletionRepository.getTasksForCalendarDate(date).map { taskWithInstanceList ->
                taskWithInstanceList.map { taskWithInstance ->
                    // Получаем иконку характеристики
                    val iconName = taskWithInstance.task.let { task ->
                        characteristicRepository.getCharacteristicById(task.characteristicId)?.iconResName ?: ""
                    }

                    TaskUiModel(
                        id = taskWithInstance.task.id,
                        title = taskWithInstance.task.title,
                        description = taskWithInstance.task.description,
                        xpReward = taskWithInstance.task.xpReward,
                        isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                        characteristicIconResName = iconName
                    )
                }
            }
        }
    }

    // Упрощенный метод получения иконки
    private suspend fun getIconNameForTask(task: Task): String {
        return characteristicRepository.getCharacteristicById(task.characteristicId)?.iconResName ?: ""
    }

    // Обновленный метод для подготовки задач
    suspend fun prepareTasksForDate(date: LocalDate) {
        taskCompletionRepository.ensureTaskInstancesForDate(date)
        refreshTasksForDate(date)
    }

    fun refreshTasksForDate(date: LocalDate) {
        _refreshTrigger.value++
    }

    // Метод для получения задач на месяц (исправленный)
//    fun getTasksForMonth(month: LocalDate): Flow<Map<LocalDate, List<app.expgessia.domain.model.Task>>> {
//        return taskRepository.getAllTasks().map { allTasks ->
//            val startOfMonth = month.withDayOfMonth(1)
//            val daysInMonth = month.lengthOfMonth()
//            val tasksByDate = mutableMapOf<LocalDate, MutableList<app.expgessia.domain.model.Task>>()
//
//            // Проходим по каждому дню месяца
//            for (i in 0 until daysInMonth) {
//                val date = startOfMonth.plusDays(i.toLong())
//                val tasksForDay = allTasks.filter { task ->
//                    TimeUtils.isTaskScheduledOnDate(task, date)
//                }
//                if (tasksForDay.isNotEmpty()) {
//                    tasksByDate[date] = tasksForDay.toMutableList()
//                }
//            }
//            tasksByDate
//        }
//    }

    fun getTasksForMonth(month: LocalDate): Flow<Map<LocalDate, List<Task>>> {
        // Предполагается, что taskRepository.getRepeatingTasks() возвращает Flow<List<Task>>
        // и он реактивен, т.е. переиздает данные при изменении базы данных.
        return taskRepository.getRepeatingTasks()
            .map { repeatingTasks ->
                val startOfMonth = month.withDayOfMonth(1)
                val daysInMonth = month.lengthOfMonth()
                val tasksByDate = mutableMapOf<LocalDate, MutableList<Task>>()

                // Проходим по каждому дню месяца
                for (i in 0 until daysInMonth) {
                    val date = startOfMonth.plusDays(i.toLong())
                    val tasksForDay = repeatingTasks.filter { task ->
                        // Используем логику из TimeUtils для проверки
                        TimeUtils.isTaskScheduledOnDate(task.toEntity(), date)
                    }
                    if (tasksForDay.isNotEmpty()) {
                        tasksByDate[date] = tasksForDay.toMutableList()
                    }
                }

                // ДОБАВИТЬ: Здесь должна быть добавлена логика не повторяющихся задач,
                // если они еще не включены в getRepeatingTasks().

                tasksByDate
            }
    }




}
