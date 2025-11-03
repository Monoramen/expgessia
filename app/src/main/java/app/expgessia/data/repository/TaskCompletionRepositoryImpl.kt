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

    // !!! ВАЖНО: Тебе нужно создать этот маппер в своем проекте: TaskWithInstance -> TaskUiModel
    // Эта функция объединяет шаблон (TaskEntity) и состояние (TaskInstanceEntity) для UI.
    fun mapToTaskUiModel(taskWithInstance: TaskWithInstance): TaskUiModel {
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
            // Состояние берется из экземпляра, если он есть
            isCompleted = instance?.isCompleted ?: false,
            characteristicIconResName = iconResName
        )
    }

    // --- Функции для UI ---
    override fun getTodayActiveTaskDetailsStream(startOfDay: Long): Flow<List<TaskWithInstance>> {
        return taskInstanceDao.getTodayTasksWithInstance(startOfDay)
    }

    override fun getTomorrowScheduledTaskDetailsStream(startOfTomorrow: Long): Flow<List<TaskWithInstance>> {
        return taskInstanceDao.getTomorrowScheduledTasksWithInstance(startOfTomorrow)
    }


    override fun getCompletedTaskInstancesStream(): Flow<List<TaskInstance>> {
        return taskInstanceDao.getCompletedTaskInstances().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    override suspend fun completeTask(taskId: Long, completionTimestamp: Long) {
        db.withTransaction {
            val startOfDay = TimeUtils.calculateStartOfDay(completionTimestamp)

            val taskTemplate = taskDao.getTaskById(taskId) ?: throw NoSuchElementException("Task template not found for ID: $taskId")
            var instance = taskInstanceDao.getTaskInstanceForDay(taskId, startOfDay)

            if (instance == null) {
                instance = TaskInstanceEntity(taskId = taskId, scheduledFor = startOfDay)
                taskInstanceDao.insert(instance)
                instance = taskInstanceDao.getTaskInstanceForDay(taskId, startOfDay)!!
            }

            if (instance.isCompleted) return@withTransaction

            val user = userDao.getUser() ?: throw NoSuchElementException("User not found")
            val xpEarned = calculateXpEarned(taskTemplate.xpReward, user, taskTemplate.characteristicId)

            val completedInstance = instance.copy(
                isCompleted = true,
                completedAt = completionTimestamp,
                xpEarned = xpEarned,
                isUndone = false // 💡 Явно устанавливаем
            )
            taskInstanceDao.update(completedInstance)

            // 💡 ВАЖНО: Обновляем статистику
            dailyStatsRepository.updateStatsFromTaskInstances()
                // dailyStatsRepository.refreshStats()
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
                isUndone = false // 💡 ИСПРАВЛЯЕМ: Устанавливаем false вместо true
            )
            taskInstanceDao.update(undoneInstance)

            dailyStatsRepository.updateStatsFromTaskInstances()
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

            // 1. ЛОГИКА СБРОСА
            val deletedCount = taskInstanceDao.deleteOverdueUncompletedInstances(startOfDay)
            Log.d("TaskCompletionRepo", "Deleted $deletedCount overdue uncompleted instances.")

            // 2. ПОЛУЧЕНИЕ ВСЕХ АКТИВНЫХ ЗАДАЧ (включая не-повторяющиеся)
            val allActiveTasks = taskDao.getAllTasksSync() // 💡 НУЖНО СОЗДАТЬ ЭТОТ МЕТОД

            // 3. РАСЧЕТ ДНЯ НЕДЕЛИ
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime
            }
            val currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

            // 4. ФИЛЬТРАЦИЯ И СОЗДАНИЕ ЭКЗЕМПЛЯРОВ ДЛЯ ВСЕХ ЗАДАЧ
            allActiveTasks.forEach { task ->
                val shouldBeScheduledToday = when (task.repeatMode) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val days = task.repeatDetails?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                        days.contains(currentDayOfWeek)
                    }
                    "NONE" -> true // 💡 ВАЖНО: одноразовые задачи тоже создают инстансы
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
                    } else {
                        Log.d("TaskCompletionRepo", "ℹ️ Instance already exists for task: ${task.title}")
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
                    characteristicIconResName = "" // TODO: получить иконку из характеристики
                )
            }
        }
    }



    // В TaskCompletionRepositoryImpl.kt
    override fun getTasksForCalendarDate(date: LocalDate): Flow<List<TaskWithInstance>> {
        val startOfDay = TimeUtils.localDateToStartOfDayMillis(date)

        return taskInstanceDao.getTasksWithInstancesByDate(startOfDay).map { taskWithInstances ->
            // Фильтруем только актуальные инстансы
            taskWithInstances.filter { it.taskInstance?.isUndone != true }
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
                    "NONE" -> true // Разовые задачи
                    else -> false
                }

                if (shouldBeScheduled) {
                    val existingInstance = taskInstanceDao.getTaskInstanceForDay(task.id, startOfDay)
                    if (existingInstance == null) {
                        val newInstance = TaskInstanceEntity(
                            taskId = task.id,
                            scheduledFor = startOfDay,
                            isCompleted = false,
                            xpEarned = 0
                        )
                        taskInstanceDao.insert(newInstance)
                    }
                }
            }
        }
    }

}