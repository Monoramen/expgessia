package app.expgessia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import app.expgessia.domain.model.Task
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // Импорт map для работы с Flow
import toEntity // Предполагаем, что это extension-функция для Task
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel для управления данными и состоянием характеристик пользователя.
 * Использует Hilt для инъекции зависимостей.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    // ⭐️ ИСПРАВЛЕНИЕ: УДАЛЕНО ключевое слово 'suspend'
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
