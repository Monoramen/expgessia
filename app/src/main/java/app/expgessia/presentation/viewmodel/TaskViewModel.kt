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
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.domain.usecase.CompleteTaskUseCase
import app.expgessia.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
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

    private val _todayTasks = MutableStateFlow<List<TaskUiModel>>(emptyList())
    val todayTasksFlow: Flow<List<TaskUiModel>> = _todayTasks
    val tasksUiState: StateFlow<List<TaskUiModel>> =
        _todayTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _tomorrowTasks = MutableStateFlow<List<TaskUiModel>>(emptyList())
    val tomorrowTasksFlow: Flow<List<TaskUiModel>> = _tomorrowTasks
    private val _completedTasks = MutableStateFlow<List<TaskUiModel>>(emptyList())
    val completedTasksFlow: Flow<List<TaskUiModel>> = _completedTasks

    init {
        Log.d("TaskViewModel", "🔄 Initializing TaskViewModel")
        ensureTasksAreScheduled()
    }
    fun syncAllTasks() {
        viewModelScope.launch {
            ensureTasksAreScheduled()
            // 🔥 ДОБАВЛЯЕМ: Создаем инстансы на завтра
            ensureTomorrowInstances()
            forceRefresh()
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
            // 🔥 Теперь просто маппим без фильтрации
            taskWithInstanceList.map { taskWithInstance ->
                val iconName = getCharacteristicIconName(taskWithInstance.task.characteristicId)
                TaskUiModel(
                    id = taskWithInstance.task.id,
                    title = taskWithInstance.task.title,
                    description = taskWithInstance.task.description,
                    xpReward = taskWithInstance.task.xpReward,
                    // 🔥 Берем актуальный статус из инстанса
                    isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                    characteristicIconResName = iconName,
                    date = LocalDate.now()
                )
            }
        }
    }

    private fun getTomorrowTasks(): Flow<List<TaskUiModel>> {
        val tomorrowStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis() + TimeUtils.DAY_IN_MILLIS)

        return taskCompletionRepository.getTomorrowScheduledTaskDetailsStream(tomorrowStart)
            .map { taskWithInstanceList ->
                taskWithInstanceList.map { taskWithInstance ->
                    val iconName = getCharacteristicIconName(taskWithInstance.task.characteristicId)
                    TaskUiModel(
                        id = taskWithInstance.task.id,
                        title = taskWithInstance.task.title,
                        description = taskWithInstance.task.description,
                        xpReward = taskWithInstance.task.xpReward,
                        // 🔥 ИСПРАВИТЬ: Брать актуальный статус из инстанса
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
                val task = runBlocking { taskRepository.getTaskById(instance.taskId) } // This is not ideal, but will work for now
                TaskUiModel(
                    id = instance.taskId,
                    title = task?.title ?: "Задача ${instance.taskId}",
                    description = task?.description ?: "",
                    xpReward = instance.xpEarned,
                    isCompleted = true,
                    characteristicIconResName = task?.let {
                        runBlocking { characteristicRepository.getCharacteristicById(it.characteristicId)?.iconResName }
                    } ?: "",
                    date = instance.completedAt?.let { TimeUtils.millisToLocalDate(it) } ?: LocalDate.now()
                )
            }.also { tasks ->
                Log.d("TaskViewModel", "✅ Completed tasks today: ${tasks.size}")
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
                ensureTomorrowInstances()

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
                ensureTomorrowInstances()

            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to update task", e)
            }
        }
    }


    // В TaskViewModel.kt - обновите метод onTaskCheckClickedForDate
    fun onTaskCheckClickedForDate(taskId: Long, date: LocalDate, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)
                val isCompleted = taskCompletionRepository.isTaskCompletedForDate(taskId, startOfDayMillis)

                Log.d("TaskViewModel", "🔄 Changing task $taskId status for $date (currently completed: $isCompleted)")

                if (isCompleted) {
                    taskCompletionRepository.undoCompleteTask(taskId)
                    Log.d("TaskViewModel", "📝 Task $taskId marked as NOT completed for $date")
                } else {
                    completeTaskUseCase(taskId, System.currentTimeMillis())
                    Log.d("TaskViewModel", "✅ Task $taskId marked as completed for $date")
                }

                onComplete?.invoke()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to change task status for date", e)
            }
        }
    }

    // 🔥 УБИРАЕМ лишнюю задержку в forceRefresh


    fun onTaskCheckClicked(taskId: Long, onComplete: (() -> Unit)? = null) {
        onTaskCheckClickedForDate(taskId, LocalDate.now(), onComplete)
    }







    private fun ensureTasksAreScheduled() {
        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "🔄 Ensuring daily task instances...")
                taskCompletionRepository.ensureDailyTaskInstances(System.currentTimeMillis())
                Log.d("TaskViewModel", "✅ Daily task instances ensured")
                // Принудительно обновляем данные после создания инстансов
                refreshData()
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Failed to ensure daily task instances", e)
            }
        }
    }

    private suspend fun ensureTomorrowInstances() {
        try {
            val tomorrow = LocalDate.now().plusDays(1)
            taskCompletionRepository.ensureTaskInstancesForDate(tomorrow)
            Log.d("TaskViewModel", "✅ Tomorrow instances ensured")
        } catch (e: Exception) {
            Log.e("TaskViewModel", "❌ Failed to ensure tomorrow instances", e)
        }
    }
    private fun refreshData() {
        viewModelScope.launch {
            try {
                // Обновляем всё состояние через collectLatest или first()
                // Чтобы избежать дублирования логики, соберём все три потока
                _allTasks.value = TaskState.Loading

                val today = getTodayTasks().first()
                val tomorrow = getTomorrowTasks().first()
                val completed = getCompletedTasks().first()

                // Обновляем частные MutableStateFlow (если они используются отдельно в UI)
                _todayTasks.value = today
                _tomorrowTasks.value = tomorrow
                _completedTasks.value = completed

                // Обновляем обобщённое состояние
                _allTasks.value = TaskState.Success(
                    todayTasks = today,
                    tomorrowTasks = tomorrow,
                    completedTasks = completed
                )
            } catch (e: Exception) {
                Log.e("TaskViewModel", "❌ Failed to refresh data", e)
                _allTasks.value = TaskState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Публичный метод, вызываемый извне (например, из syncAllTasks())
    fun forceRefresh() {
        refreshData()
    }

}