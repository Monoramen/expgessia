package app.expgessia.data.repository

import android.util.Log
import androidx.room.withTransaction
import app.expgessia.data.AppDatabase
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.data.dao.TaskInstanceDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.data.entity.TaskWithInstance // <--- Используем твой класс
import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.TaskInstance
import app.expgessia.domain.model.TaskUiModel // <--- Используем твой UI класс
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.utils.TimeUtils
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import toDomain // Предполагаемый маппер TaskInstanceEntity -> TaskInstance
import app.expgessia.data.mapper.toDomain // Маппер TaskCompletionEntity -> TaskCompletion (если нужен)
import app.expgessia.domain.repository.DailyStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar

class TaskCompletionRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val userDao: UserDao,
    private val dailyStatsRepository: DailyStatsRepository,
    private val db: AppDatabase,
) : TaskCompletionRepository {
    private val _refreshTrigger = MutableStateFlow(0)
    // !!! ВАЖНО: Тебе нужно создать этот маппер в своем проекте: TaskWithInstance -> TaskUiModel
    // Эта функция объединяет шаблон (TaskEntity) и состояние (TaskInstanceEntity) для UI.
    fun mapToTaskUiModel(taskWithInstance: TaskWithInstance, date: LocalDate): TaskUiModel {
        // Поскольку TaskWithInstance использует @Relation, TaskInstance может быть null
        val instance = taskWithInstance.taskInstance
        val taskEntity = taskWithInstance.task

        // В реальном проекте здесь будет логика поиска иконки через characteristicRepository
        val iconResName = "..." // TODO: Получить имя ресурса иконки

        return TaskUiModel(
            id = taskEntity.id,
            title = taskEntity.title,
            description = taskEntity.description,
            xpReward = taskEntity.xpReward,
            isCompleted = instance?.isCompleted ?: false,
            characteristicIconResName = iconResName,
            date = date
        )
    }

    // --- Функции для UI ---
// 🔥 ИСПРАВЛЯЕМ: Методы должны возвращать Flow, который обновляется при изменениях
// ЗАМЕНИТЕ эти методы:
    override fun getTodayActiveTaskDetailsStream(startOfDay: Long): Flow<List<TaskWithInstance>> {
        return _refreshTrigger.flatMapLatest {
            taskInstanceDao.getTodayTasksWithInstance(startOfDay)
        }

    }

    override fun getTomorrowScheduledTaskDetailsStream(startOfTomorrow: Long): Flow<List<TaskWithInstance>> {
        return _refreshTrigger.flatMapLatest {
            taskInstanceDao.getTomorrowScheduledTasksWithInstance(startOfTomorrow)
        }
    }


    override fun getCompletedTaskInstancesStream(): Flow<List<TaskInstance>> {
        return taskInstanceDao.getCompletedTaskInstances().map { entities ->
            entities
                .filter {
                    it.isCompleted  &&
                            TimeUtils.isToday(it.completedAt ?: System.currentTimeMillis())
                }
                .map { it.toDomain() }
        }
    }

    override fun getCompletedTasksWithDetailsStream(): Flow<List<TaskWithInstance>> {
        return taskInstanceDao.getCompletedTasksWithInstance()
    }

    override suspend fun completeTask(taskId: Long, completionTimestamp: Long) {
        db.withTransaction {
            val startOfDay = TimeUtils.calculateStartOfDay(completionTimestamp)

            val taskTemplate = taskDao.getTaskById(taskId) ?: throw NoSuchElementException("Task template not found for ID: $taskId")

            // 🔥 ВАЖНОЕ ИСПРАВЛЕНИЕ: Создаем инстанс, если его нет
            var instance = taskInstanceDao.getTaskInstanceForDay(taskId, startOfDay)

            if (instance == null) {
                // Создаем новый инстанс для сегодня, если его нет
                instance = TaskInstanceEntity(
                    taskId = taskId,
                    scheduledFor = startOfDay,
                    isCompleted = false,
                    xpEarned = 0,

                )
                taskInstanceDao.insert(instance)
                Log.d("TaskCompletionRepo", "🆕 Created new instance for task $taskId on ${LocalDate.now()}")
            }

            // 🔥 ИСПРАВЛЕНИЕ: Если уже выполнена, выходим
            if (instance.isCompleted) {
                Log.d("TaskCompletionRepo", "Task $taskId already completed, skipping")
                return@withTransaction
            }

            val user = userDao.getUser() ?: throw NoSuchElementException("User not found")
            val xpEarned = calculateXpEarned(taskTemplate.xpReward, user, taskTemplate.characteristicId)

            val completedInstance = instance.copy(
                isCompleted = true,
                completedAt = completionTimestamp,
                xpEarned = xpEarned,
            )
            taskInstanceDao.update(completedInstance)

            dailyStatsRepository.updateStatsFromTaskInstances()

            Log.d("TaskCompletionRepo", "✅ Task $taskId marked as completed at $completionTimestamp")
            _refreshTrigger.value++ // 🔥 ДОБАВИТЬ
        }
    }

    override suspend fun undoCompleteTask(taskId: Long) {
        db.withTransaction {
            val startOfDay = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
            val instance = taskInstanceDao.getTaskInstanceForDay(taskId, startOfDay)
                ?: return@withTransaction

            if (!instance.isCompleted) return@withTransaction

            val undoneInstance = instance.copy(
                isCompleted = false,
                completedAt = null,
                xpEarned = 0,

            )
            taskInstanceDao.update(undoneInstance)

            dailyStatsRepository.updateStatsFromTaskInstances()

            Log.d("TaskCompletionRepo", "↩️ Task $taskId completion undone")
            _refreshTrigger.value++ // 🔥 ДОБАВИТЬ
        }
    }

    // --- Методы статистики ---
    override fun getTotalCompletedTasksCount(): Flow<Int> {
        return taskInstanceDao.getCompletedTaskInstances().map { it.size }
    }

    private fun calculateXpEarned(baseXP: Int, user: UserEntity, characteristicId: Int): Int {
        // TODO: Перенести эту логику в Use Case
        return baseXP
    }

    override fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun ensureDailyTaskInstances(currentTime: Long) {
        db.withTransaction {
            val startOfDay = TimeUtils.calculateStartOfDay(currentTime)

            // 1. ЛОГИКА СБРОСА - только для незавершенных просроченных задач
            val deletedCount = taskInstanceDao.deleteOverdueUncompletedInstances(startOfDay)
            Log.d("TaskCompletionRepo", "Deleted $deletedCount overdue uncompleted instances.")

            // 2. ПОЛУЧЕНИЕ ВСЕХ АКТИВНЫХ ЗАДАЧ
            val allActiveTasks = taskDao.getAllTasksSync()

            // 3. РАСЧЕТ ДНЯ НЕДЕЛИ
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime
            }
            val currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

            // 4. ФИЛЬТРАЦИЯ И СОЗДАНИЕ ЭКЗЕМПЛЯРОВ ТОЛЬКО ДЛЯ НЕСУЩЕСТВУЮЩИХ
            allActiveTasks.forEach { task ->
                val shouldBeScheduledToday = when (task.repeatMode) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val days = task.repeatDetails?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                        days.contains(currentDayOfWeek)
                    }
                    "NONE" -> {
                        // 🔥 ВАЖНОЕ ИСПРАВЛЕНИЕ: Для разовых задач проверяем, не был ли уже создан инстанс
                        val hasExistingInstance = taskInstanceDao.hasAnyInstanceForTask(task.id)
                        !hasExistingInstance
                    }
                    else -> false
                }

                if (shouldBeScheduledToday) {
                    val existingInstance = taskInstanceDao.getTaskInstanceForDay(task.id, startOfDay)

                    if (existingInstance == null) {
                        val newInstance = TaskInstanceEntity(
                            taskId = task.id,
                            scheduledFor = startOfDay,
                            isCompleted = false,
                            xpEarned = 0
                        )
                        taskInstanceDao.insert(newInstance)
                        Log.d("TaskCompletionRepo", "✅ Created instance for task: ${task.title} (${task.repeatMode})")
                    }
                }
            }

            Log.d("TaskCompletionRepo", "✅ Daily instances ensured for ${allActiveTasks.size} tasks")
        }
    }




    override suspend fun isTaskCompletedForDate(taskId: Long, date: Long): Boolean {
        return withContext(Dispatchers.IO) {

            val instance = taskInstanceDao.getTaskInstanceForDay(taskId, date)

            // 3. Если экземпляр найден, возвращаем его статус isCompleted, иначе false.
            instance?.isCompleted ?: false
        }
    }

    // 💡 ДОБАВЛЯЕМ: Метод для получения задач по дате с UI-моделями
// В TaskCompletionRepositoryImpl.kt
    override fun getTasksForDateWithStatus(date: LocalDate): Flow<List<TaskUiModel>> {
        val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

        return taskInstanceDao.getTasksWithInstancesByDate(startOfDay).map { taskWithInstances ->
            taskWithInstances.map { taskWithInstance ->
                TaskUiModel(
                    id = taskWithInstance.task.id,
                    title = taskWithInstance.task.title,
                    description = taskWithInstance.task.description,
                    xpReward = taskWithInstance.task.xpReward,
                    isCompleted = taskWithInstance.taskInstance?.isCompleted ?: false,
                    characteristicIconResName = "", // Иконка будет добавлена в ViewModel
                    date = date
                )
            }
        }
    }



    // В TaskCompletionRepositoryImpl.kt
    override fun getTasksForCalendarDate(date: LocalDate): Flow<List<TaskWithInstance>> {
        val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

        return taskInstanceDao.getTasksWithInstancesByDate(startOfDay).map { taskWithInstances ->
            // Показываем ВСЕ задачи для выбранной даты в календаре
            taskWithInstances.filter { it.taskInstance?.isCompleted != true }
        }
    }

    // В TaskCompletionRepositoryImpl.kt
    override suspend fun ensureTaskInstancesForDate(date: LocalDate) {
        val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

        db.withTransaction {
            // Получаем все активные задачи
            val allActiveTasks = taskDao.getAllTasksSync()

            // Фильтруем задачи, которые должны быть в эту дату
            allActiveTasks.forEach { task ->
                val shouldBeScheduled = when (task.repeatMode) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val currentDayOfWeek = date.dayOfWeek.value // 1-7 (Monday-Sunday)
                        val days = task.repeatDetails?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                        days.contains(currentDayOfWeek)
                    }
                    "MONTHLY" -> {
                        val selectedDay = task.repeatDetails?.toIntOrNull()
                        selectedDay == date.dayOfMonth
                    }
                    "NONE" -> {
                        // Для разовых задач проверяем, был ли создан инстанс
                        val hasInstance = taskInstanceDao.hasAnyInstanceForTask(task.id)
                        !hasInstance && date == LocalDate.now()
                    }
                    else -> false
                }

                if (shouldBeScheduled) {
                    val existingInstance = taskInstanceDao.getTaskInstanceForDay(task.id, startOfDay)
                    if (existingInstance == null) {
                        val newInstance = TaskInstanceEntity(
                            taskId = task.id,
                            scheduledFor = startOfDay,
                            isCompleted = false,
                            xpEarned = 0,

                        )
                        taskInstanceDao.insert(newInstance)
                        Log.d("TaskCompletionRepo", "✅ Created instance for ${task.title} on $date (${task.repeatMode})")
                    }
                }
            }
        }
    }





    override suspend fun createTaskInstancesForTask(taskId: Long) {
        db.withTransaction {
            val task = taskDao.getTaskById(taskId) ?: return@withTransaction

            // 🔥 ВАЖНОЕ ИСПРАВЛЕНИЕ: Сначала удаляем все будущие инстансы
            val todayStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
            taskInstanceDao.deleteFutureInstances(taskId, todayStart)

            // 🔥 ИСПРАВЛЕНИЕ: Создаем инстансы на ближайшие 60 дней (включая завтра)
            for (i in 0..60) {
                val date = LocalDate.now().plusDays(i.toLong())
                val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

                val shouldBeScheduled = when (task.repeatMode) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val currentDayOfWeek = date.dayOfWeek.value // 1-7 (Monday-Sunday)
                        val days = task.repeatDetails?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                        days.contains(currentDayOfWeek)
                    }
                    "MONTHLY" -> {
                        val selectedDay = task.repeatDetails?.toIntOrNull()
                        selectedDay == date.dayOfMonth
                    }
                    "NONE" -> i == 0 // Только на сегодня для разовых задач
                    else -> false
                }

                if (shouldBeScheduled) {
                    val newInstance = TaskInstanceEntity(
                        taskId = taskId,
                        scheduledFor = startOfDay,
                        isCompleted = false,
                        xpEarned = 0,

                    )
                    taskInstanceDao.insert(newInstance)
                    Log.d("TaskCompletionRepo", "✅ Created instance for task ${task.title} on $date (day ${date.dayOfWeek})")
                }
            }
        }
    }


    // В TaskCompletionRepositoryImpl.kt - реализуйте метод
    override fun getCompletedTasksInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TaskInstance>> {
        val startMillis = TimeUtils.localDateToStartOfDayMillis(startDate)
        val endMillis = TimeUtils.localDateToStartOfDayMillis(endDate.plusDays(1)) - 1

        return taskInstanceDao.getCompletedInstancesInDateRange(startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

}