package app.expgessia.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.data.entity.TaskEntity
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import app.expgessia.domain.usecase.CompleteTaskUseCase


// Используем Hilt для внедрения репозитория
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characteristicRepository: CharacteristicRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {

    init {
        // Вызываем логику сброса задач сразу при создании ViewModel
        resetTasksIfOverdue()
    }

    // 1. Приватный поток, который собирает сырые данные из базы
    private val rawTasksFlow: Flow<List<Task>> = taskRepository.getAllTasks()


    val characteristicsUiState: StateFlow<List<Characteristic>> = characteristicRepository
        .getAllCharacteristics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Публичный StateFlow, который Composable будет собирать.
    // Этот поток МАППИРУЕТ сырые данные в UI-модель, обогащая их иконкой.
    val tasksUiState: StateFlow<List<TaskUiModel>> = rawTasksFlow
        .map { tasksList ->
            mapToUiModel(tasksList)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayTasks: StateFlow<List<TaskUiModel>> =
        taskRepository.getTodayActiveTasks()
            .map { tasks -> mapToUiModel(tasks) } // ✅ Используем ваш маппер
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Поток завершенных (но еще не сброшенных) задач.
     */
    val completedTasks: StateFlow<List<TaskUiModel>> =
        taskRepository.getCompletedTasksStream()
            .map { tasks -> mapToUiModel(tasks) } // ✅ Используем ваш маппер
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Поток задач, запланированных на завтра.
     */
    val tomorrowTasks: StateFlow<List<TaskUiModel>> =
        taskRepository.getTomorrowScheduledTasks()
            .map { tasks -> mapToUiModel(tasks) } // ✅ Используем ваш маппер
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )



    /**
     * Преобразует список Task в список TaskUiModel, асинхронно загружая iconResName для каждой задачи.
     */
    suspend fun mapToUiModel(tasks: List<Task>): List<TaskUiModel> {
        // Используем IO-диспетчер для сетевых/базовых операций (хотя Room уже это делает,
        // это хорошая практика для async/awaitAll)
        return withContext(Dispatchers.IO) {
            // Создаем список асинхронных задач (Deferred)
            val mappedTasks = tasks.map { task ->
                async {
                    // Асинхронно получаем имя ресурса иконки по ID
                    val iconName =
                        taskRepository.getIconResNameByCharacteristicId(task.characteristicId)

                    // Создаем UI-модель
                    TaskUiModel(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        xpReward = task.xpReward,
                        isCompleted = task.isCompleted,
                        characteristicIconResName = iconName
                    )
                }
            }
            mappedTasks.awaitAll()
        }
    }

    fun onAddTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.addTask(task) // ✅ ПРЯМОЙ ВЫЗОВ РЕПОЗИТОРИЯ
                Log.d("TaskViewModel", "Task saved: ${task.title}")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to save task", e)
            }
        }
    }


    fun onTaskCheckClicked(taskId: Long) {
        viewModelScope.launch {
            try {
                completeTaskUseCase(taskId, System.currentTimeMillis())
                Log.d("TaskViewModel", "Task with ID $taskId completed via Use Case.")
            } catch (e: Exception) {
                Log.d( "TasksViewModel", "Failed to complete task $taskId: ${e.stackTraceToString()}")
                // В реальном приложении здесь можно было бы показать Toast/Snackbar
            }
        }
    }




    fun onUpdateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                Log.d("TaskViewModel", "Task updated: ${task.title}")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to update task", e)
            }
        }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return withContext(Dispatchers.IO) {
            taskRepository.getTaskById(taskId)
        }
    }


    private fun resetTasksIfOverdue() {
        viewModelScope.launch {
            try {
                // Вызываем новую suspend-функцию репозитория
                taskRepository.resetOverdueRepeatingTasks()
                Log.d("TaskViewModel", "Overdue repeating tasks have been reset.")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to reset overdue tasks", e)
            }
        }
    }


}
