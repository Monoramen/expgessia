package app.expgessia.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.mapper.TaskWithInstanceMapper
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.domain.repository.TaskCompletionRepository // 💡 ИМПОРТИРУЕМ РЕПОЗИТОРИЙ ВЫПОЛНЕНИЯ
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.utils.TimeUtils // 💡 Необходим для расчета начала дня
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOn // 💡 Добавляем flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import app.expgessia.domain.usecase.CompleteTaskUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import java.util.concurrent.TimeUnit
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characteristicRepository: CharacteristicRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val taskCompletionRepository: TaskCompletionRepository,
    private val dailyStatsRepository: DailyStatsRepository,
    private val taskWithInstanceMapper: TaskWithInstanceMapper // 💡 ДОБАВЛЯЕМ МАППЕР
) : ViewModel() {

    init {
        Log.d("TaskViewModel", "🔄 Initializing TaskViewModel - ensuring tasks are scheduled")
        ensureTasksAreScheduled()
    }

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun forceRefresh() {
        _refreshTrigger.value++
    }

    private suspend fun getCharacteristicIconName(characteristicId: Int): String {
        return characteristicRepository.getCharacteristicById(characteristicId)?.iconResName ?: ""
    }

    // Обновите Flow для todayTasksFlow:
    val todayTasksFlow: Flow<List<TaskUiModel>> =
        _refreshTrigger.flatMapLatest {
            taskCompletionRepository.getTodayActiveTaskDetailsStream(
                TimeUtils.calculateStartOfDay(System.currentTimeMillis())
            ).map { taskWithInstanceList ->
                taskWithInstanceList
                    .filter {
                        // 💡 ФИЛЬТРУЕМ: только НЕ завершенные задачи для сегодня
                        it.taskInstance?.isUndone != true &&
                                !(it.taskInstance?.isCompleted ?: false)
                    }
                    .map { taskWithInstance ->
                        val iconName = viewModelScope.async {
                            getCharacteristicIconName(taskWithInstance.task.characteristicId)
                        }.await()

                        TaskUiModel(
                            id = taskWithInstance.task.id,
                            title = taskWithInstance.task.title,
                            description = taskWithInstance.task.description,
                            xpReward = taskWithInstance.task.xpReward,
                            isCompleted = false, // 💡 Сегодняшние активные задачи всегда не завершены
                            characteristicIconResName = iconName
                        )
                    }
            }
        }

    val tomorrowTasksFlow: Flow<List<TaskUiModel>> =
        _refreshTrigger.flatMapLatest {
            taskCompletionRepository.getTomorrowScheduledTaskDetailsStream(
                TimeUtils.calculateStartOfDay(System.currentTimeMillis() + TimeUtils.DAY_IN_MILLIS)
            ).map { taskWithInstanceList ->
                taskWithInstanceList

                    .filter { it.taskInstance?.isUndone != true }
                    .map { taskWithInstance ->
                        val iconName = viewModelScope.async {
                            getCharacteristicIconName(taskWithInstance.task.characteristicId)
                        }.await()
                        TaskUiModel(
                            id = taskWithInstance.task.id,
                            title = taskWithInstance.task.title,
                            description = taskWithInstance.task.description,
                            xpReward = taskWithInstance.task.xpReward,
                            isCompleted = false, // 💡 Завтрашние задачи всегда не выполнены
                            characteristicIconResName = iconName
                        )
                    }
            }
        }



    // 💡 УЛУЧШАЕМ: Flow для завершенных задач - получаем полную информацию о задаче
    val completedTasksFlow: Flow<List<TaskUiModel>>
        get() = _refreshTrigger.flatMapLatest {
            taskCompletionRepository.getCompletedTaskInstancesStream().map { instances ->
                instances
                    .filter { !it.isUndone }
                    .map { instance ->
                        // 💡 ПОЛУЧАЕМ полную информацию о задаче
                        val task = taskRepository.getTaskById(instance.taskId)

                        TaskUiModel(
                            id = instance.taskId,
                            title = task?.title ?: "Задача ${instance.taskId}",
                            description = task?.description ?: "",
                            xpReward = instance.xpEarned,
                            isCompleted = true,
                            characteristicIconResName = task?.let {
                                characteristicRepository.getCharacteristicById(it.characteristicId)?.iconResName
                            } ?: ""
                        )
                    }
            }
        }

    // Остальные методы без изменений...
    val tasksUiState: StateFlow<List<TaskUiModel>> =
        _refreshTrigger.flatMapLatest {
            taskCompletionRepository.getTodayActiveTaskDetailsStream(
                TimeUtils.calculateStartOfDay(System.currentTimeMillis())
            )
                .map { taskWithInstances ->
                    val filteredTasks = taskWithInstances.filter {
                        it.taskInstance?.isUndone != true
                    }
                    Log.d("TaskViewModel", "📊 Processing ${filteredTasks.size} task instances (after filtering)")
                    taskWithInstanceMapper.mapToUiModelList(filteredTasks)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private fun ensureTasksAreScheduled() {
        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "🔄 Ensuring daily task instances...")
                taskCompletionRepository.ensureDailyTaskInstances(System.currentTimeMillis())
                Log.d("TaskViewModel", "✅ Daily task instances ensured")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Failed to ensure daily task instances", e)
            }
        }
    }

    val characteristicsUiState: StateFlow<List<Characteristic>> = characteristicRepository
        .getAllCharacteristics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun refreshData() {
        viewModelScope.launch {
            _refreshTrigger.value++
            // 💡 Дополнительно обновляем статистику
            dailyStatsRepository.refreshStats()
        }
    }

    // 💡 ИСПРАВЛЯЕМ: Метод для даты с правильным обновлением
    fun onTaskCheckClickedForDate(taskId: Long, date: LocalDate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)
                val isCompleted = taskCompletionRepository.isTaskCompletedForDate(taskId, startOfDayMillis)

                if (isCompleted) {
                    taskCompletionRepository.undoCompleteTask(taskId)
                } else {
                    completeTaskUseCase(taskId, TimeUtils.localDateToStartOfDayMillis(date))
                }

                // 💡 ВАЖНО: Принудительно обновляем все Flow
                refreshData()
                onComplete?.invoke()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to change task status for date", e)
            }
        }
    }




        // Остальные методы остаются
    fun onAddTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.addTask(task)
                Log.d("TaskViewModel", "Task saved: ${task.title}")
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

    // В TaskViewModel добавьте:
    fun onDeleteTask(taskId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                task?.let {
                    taskRepository.deleteTask(it)
                    Log.d("TaskViewModel", "Task deleted: ${it.title}")
                    // Обновляем данные после удаления
                    refreshData()
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to delete task", e)
            }
        }
    }

    // Добавьте в TaskViewModel:
    fun onTaskCheckClicked(taskId: Long, onComplete: (() -> Unit)? = null) {
        onTaskCheckClickedForDate(taskId, LocalDate.now(), onComplete)
    }










}