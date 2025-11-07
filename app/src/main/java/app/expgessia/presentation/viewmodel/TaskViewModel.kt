package app.expgessia.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.usecase.TaskScheduler
import app.expgessia.domain.mapper.TaskWithInstanceMapper
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.domain.usecase.CompleteTaskUseCase
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characteristicRepository: CharacteristicRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val taskCompletionRepository: TaskCompletionRepository,
    private val dailyStatsRepository: DailyStatsRepository,
    private val taskWithInstanceMapper: TaskWithInstanceMapper,
    private val taskScheduler: TaskScheduler
) : ViewModel() {

    private val _allTasks = MutableStateFlow<TaskState>(TaskState.Loading)
    val tasksState: StateFlow<TaskState> = _allTasks

    val characteristicsUiState: StateFlow<List<Characteristic>> = characteristicRepository
        .getAllCharacteristics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val combinedTasksFlow = combine(
        getTodayTasks(),
        getTomorrowTasks(),
        getCompletedTasks()
    ) { today, tomorrow, completed ->
        TaskState.Success(
            todayTasks = today,
            tomorrowTasks = tomorrow,
            completedTasks = completed
        )
    }


    init {
        Log.d("TaskViewModel", "🔄 Initializing TaskViewModel")

        viewModelScope.launch {
            combinedTasksFlow.collect { taskState ->
                _allTasks.value = taskState
            }
        }


    }

    fun syncAllTasks() {
        viewModelScope.launch {
            taskScheduler.ensureInstancesForDate(date = LocalDate.now())
            taskScheduler.ensureInstancesForDate(date = LocalDate.now().plusDays(1))

        }
    }


    fun forceRefresh() {
        viewModelScope.launch {
            taskCompletionRepository.refreshStats()
            dailyStatsRepository.refreshStats()
        }
    }


    sealed class TaskState {
        object Loading : TaskState()
        data class Success(
            val todayTasks: List<TaskUiModel>,
            val tomorrowTasks: List<TaskUiModel>,
            val completedTasks: List<TaskUiModel>,
        ) : TaskState()

        data class Error(val message: String) : TaskState()
    }



    private fun getTodayTasks(): Flow<List<TaskUiModel>> {
        return taskCompletionRepository.getTodayActiveTaskDetailsStream(
            TimeUtils.calculateStartOfDay(System.currentTimeMillis())
        ).map { taskWithInstanceList ->
            taskWithInstanceList.map { taskWithInstance ->
                val iconName = getCharacteristicIconName(taskWithInstance.task.characteristicId)
                TaskUiModel(
                    id = taskWithInstance.task.id,
                    title = taskWithInstance.task.title,
                    description = taskWithInstance.task.description,
                    xpReward = taskWithInstance.task.xpReward,
                    isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                    characteristicIconResName = iconName,
                    date = LocalDate.now()
                )
            }
        }
    }

    private fun getTomorrowTasks(): Flow<List<TaskUiModel>> {
        val tomorrowStart =
            TimeUtils.calculateStartOfDay(System.currentTimeMillis() + TimeUtils.DAY_IN_MILLIS)

        return taskCompletionRepository.getTomorrowScheduledTaskDetailsStream(tomorrowStart)
            .map { taskWithInstanceList ->
                taskWithInstanceList.map { taskWithInstance ->
                    val iconName = getCharacteristicIconName(taskWithInstance.task.characteristicId)
                    TaskUiModel(
                        id = taskWithInstance.task.id,
                        title = taskWithInstance.task.title,
                        description = taskWithInstance.task.description,
                        xpReward = taskWithInstance.task.xpReward,
                        isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                        characteristicIconResName = iconName,
                        date = LocalDate.now().plusDays(1)
                    )
                }
            }
    }


    private fun getCompletedTasks(): Flow<List<TaskUiModel>> {
        return taskCompletionRepository.getCompletedTaskInstancesStream().map { instances ->
            instances.map { instance ->
                val task = runBlocking { taskRepository.getTaskById(instance.taskId) }

                val safeDate = instance.completedAt?.let {
                    runCatching { TimeUtils.millisToLocalDate(it) }.getOrNull()
                } ?: LocalDate.now()

                TaskUiModel(
                    id = instance.taskId,
                    title = task?.title ?: "Задача ${instance.taskId}",
                    description = task?.description ?: "",
                    xpReward = instance.xpEarned,
                    isCompleted = true,
                    characteristicIconResName = task?.let {
                        runBlocking { characteristicRepository.getCharacteristicById(it.characteristicId)?.iconResName }
                    } ?: "",
                    date = safeDate
                )
            }
        }
    }


    private suspend fun getCharacteristicIconName(characteristicId: Int): String {
        return characteristicRepository.getCharacteristicById(characteristicId)?.iconResName ?: ""
    }


    fun onDeleteTask(taskId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                task?.let {
                    taskRepository.deleteTask(it)
                    Log.d("TaskViewModel", "Task deleted: ${it.title}")
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to delete task", e)
            }
        }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return withContext(Dispatchers.IO) {
            taskRepository.getTaskById(taskId)
        }
    }

    fun onAddTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.addTask(task)
                Log.d("TaskViewModel", "Task saved: ${task.title}")

                // 🔥 ВАЖНО: Создаем инстансы и ОБЯЗАТЕЛЬНО создаем завтрашние
                taskCompletionRepository.createTaskInstancesForTask(task.id)
                taskScheduler.ensureInstancesForDate(LocalDate.now())

            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to save task", e)
            }
        }
    }

    fun onUpdateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                Log.d("TaskViewModel", "Task updated: ${task.title}")

                // 🔥 ПЕРЕСОЗДАЕМ инстансы при обновлении и создаем завтрашние
                taskCompletionRepository.createTaskInstancesForTask(task.id)
                taskScheduler.ensureInstancesForDate(LocalDate.now())

            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to update task", e)
            }
        }
    }


    fun onTaskCheckClickedForDate(taskId: Long, date: LocalDate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)
                val isCompleted =
                    taskCompletionRepository.isTaskCompletedForDate(taskId, startOfDayMillis)

                Log.d(
                    "TaskViewModel",
                    "🔄 Changing task $taskId status for $date (currently completed: $isCompleted)"
                )

                if (isCompleted) {
                    taskCompletionRepository.undoCompleteTaskForDate(taskId, date)
                    Log.d("TaskViewModel", "📝 Task $taskId marked as NOT completed for $date")
                } else {
                    // 🔥 ВАЖНО: Всегда используем completeTask с правильной датой
                    val completionTime = if (date.isAfter(LocalDate.now())) {
                        // Для будущих дат используем начало этого дня как время выполнения
                        startOfDayMillis
                    } else {
                        // Для сегодняшнего дня используем текущее время
                        System.currentTimeMillis()
                    }
                    taskCompletionRepository.completeTask(taskId, completionTime)
                    Log.d("TaskViewModel", "✅ Task $taskId marked as completed for $date")
                }

                onComplete?.invoke()
                forceRefresh()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Failed to change task status", e)
            }
        }
    }






}