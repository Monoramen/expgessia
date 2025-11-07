package app.expgessia.data.repository

import android.util.Log
import androidx.room.withTransaction
import app.expgessia.data.AppDatabase
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.data.dao.TaskInstanceDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.data.entity.TaskWithInstance
import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.TaskInstance
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.utils.TimeUtils
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import toDomain
import app.expgessia.data.mapper.toDomain
import app.expgessia.domain.repository.DailyStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.min

class TaskCompletionRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val userDao: UserDao,
    private val dailyStatsRepository: DailyStatsRepository,
    private val db: AppDatabase,
) : TaskCompletionRepository {
    private val _refreshTrigger = MutableStateFlow(0)


    override suspend fun refreshStats() {
        _refreshTrigger.value++
    }

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
                .filter { it.isCompleted }
                .map { it.toDomain() }
        }
    }

    override fun getCompletedTasksWithDetailsStream(): Flow<List<TaskWithInstance>> {
        return taskInstanceDao.getCompletedTasksWithInstance()
    }




    override suspend fun completeTask(taskId: Long, completionTimestamp: Long) {
        db.withTransaction {
            // 🔥 ПРОБЛЕМА: calculateStartOfDay использует текущую дату, а не дату выполнения
            // val startOfDay = TimeUtils.calculateStartOfDay(completionTimestamp) // ❌ НЕПРАВИЛЬНО

            // 🔥 РЕШЕНИЕ: Используем дату из completionTimestamp
            val completionDate = TimeUtils.millisToLocalDate(completionTimestamp)
            val startOfDay = TimeUtils.localDateToStartOfDayMillis(completionDate) // ✅ ПРАВИЛЬНО

            val taskTemplate = taskDao.getTaskById(taskId) ?: throw NoSuchElementException("Task template not found for ID: $taskId")

            var instance = taskInstanceDao.getTaskInstanceForDay(taskId, startOfDay)

            if (instance == null) {
                instance = TaskInstanceEntity(
                    taskId = taskId,
                    scheduledFor = startOfDay,
                    isCompleted = false,
                    xpEarned = 0,
                )
                taskInstanceDao.insert(instance)
                Log.d("TaskCompletionRepo", "🆕 Created new instance for task $taskId on $completionDate")
            }

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

            Log.d("TaskCompletionRepo", "✅ Task $taskId marked as completed for $completionDate")
            refreshStats()
        }
    }

    override suspend fun undoCompleteTaskForDate(taskId: Long, date: LocalDate) {
        db.withTransaction {
            val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)
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

            Log.d("TaskCompletionRepo", "↩️ Task $taskId completion undone for date $date")
            refreshStats()
        }
    }

    // --- Statistics Methods ---
    override fun getTotalCompletedTasksCount(): Flow<Int> {
        return taskInstanceDao.getCompletedTaskInstances().map { it.size }
    }

    private fun calculateXpEarned(baseXP: Int, user: UserEntity, characteristicId: Int): Int {
        // TODO: Move this logic to a Use Case
        return baseXP
    }

    override fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun ensureTaskInstancesForDate(date: Long) {
        db.withTransaction {
            val targetDate = TimeUtils.millisToLocalDate(date)
            val activeTasks = taskDao.getAllTasksSync()

            activeTasks.forEach { task ->
                val shouldBeScheduled = when (task.repeatMode.uppercase()) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val days = task.repeatDetails
                            ?.split(",")
                            ?.mapNotNull { it.trim().toIntOrNull() }
                            ?.filter { it in 1..7 }
                            ?: emptyList()
                        days.contains(targetDate.dayOfWeek.value)
                    }
                    "MONTHLY" -> {
                        val dayOfMonth = targetDate.dayOfMonth
                        val days = task.repeatDetails
                            ?.split(",")
                            ?.mapNotNull { it.trim().toIntOrNull() }
                            ?.filter { it in 1..31 }
                            ?: emptyList()
                        days.contains(dayOfMonth)
                    }
                    "NONE" -> !taskInstanceDao.hasAnyInstanceForTask(task.id)
                    else -> false
                }

                if (shouldBeScheduled) {
                    val instanceExists = taskInstanceDao.hasInstanceForDate(task.id, date)
                    if (!instanceExists) {
                        val newInstance = TaskInstanceEntity(
                            taskId = task.id,
                            scheduledFor = date,
                            isCompleted = false,
                            xpEarned = 0
                        )
                        taskInstanceDao.insert(newInstance)
                    }
                }
            }
        }
    }



    override suspend fun isTaskCompletedForDate(taskId: Long, date: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val instance = taskInstanceDao.getTaskInstanceForDay(taskId, date)
            instance?.isCompleted ?: false
        }
    }


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
                    characteristicIconResName = "", // Icon will be added in the ViewModel
                    date = date
                )
            }
        }
    }



    override fun getTasksForCalendarDate(date: LocalDate): Flow<List<TaskWithInstance>> {
        val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

        Log.d("TaskCompletionRepo", "📅 Querying tasks for date: $date, startOfDay: $startOfDay")

        return _refreshTrigger.flatMapLatest {
            Log.d("TaskCompletionRepo", "🔄 Refreshing Flow for date: $date")
            // 🔥 ВАЖНО: Этот запрос должен возвращать ВСЕ задачи для даты (и выполненные, и нет)
            taskInstanceDao.getTasksWithInstancesByDate(startOfDay)
        }.onEach { tasks ->
            Log.d("TaskCompletionRepo", "📥 Loaded ${tasks.size} tasks for $date")
            tasks.forEach { task ->
                Log.d("TaskCompletionRepo",
                    "   📋 Task: ${task.task.title}, " +
                            "Instance: ${task.taskInstance?.id}, " +
                            "Completed: ${task.taskInstance?.isCompleted}")
            }
        }
    }



    override suspend fun createTaskInstancesForTask(taskId: Long) {
        db.withTransaction {
            val task = taskDao.getTaskById(taskId) ?: return@withTransaction

            // IMPORTANT FIX: First, delete all future instances
            val todayStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
            taskInstanceDao.deleteFutureInstances(taskId, todayStart)

            // FIX: Create instances for the next 60 days (including tomorrow)
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
                    "NONE" -> i == 0 || i == 1 // For today and tomorrow for one-time tasks
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


    // In TaskCompletionRepositoryImpl.kt - implement the method
    override fun getCompletedTasksInDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TaskInstance>> {
        val startMillis = TimeUtils.localDateToStartOfDayMillis(startDate)
        val endMillis = TimeUtils.localDateToStartOfDayMillis(endDate.plusDays(1)) - 1

        return taskInstanceDao.getCompletedInstancesInDateRange(startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }


    // В TaskCompletionRepositoryImpl.kt - УПРОСТИТЬ:
    private suspend fun shouldCreateInstanceForDate(task: TaskEntity, date: LocalDate): Boolean {
        return when (task.repeatMode.uppercase()) {
            "DAILY" -> true
            "WEEKLY" -> {
                val targetDays = task.repeatDetails
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: emptyList()
                targetDays.contains(date.dayOfWeek.value)
            }
            "MONTHLY" -> {
                val targetDay = task.repeatDetails?.toIntOrNull() ?: return false
                val maxDayInMonth = date.lengthOfMonth()
                val scheduledDay = min(targetDay, maxDayInMonth)
                date.dayOfMonth == scheduledDay
            }
            "NONE" -> {
                // Для разовых задач создаем инстанс только если это сегодня или будущая дата
                !date.isBefore(LocalDate.now())
            }
            else -> false
        }
    }


}