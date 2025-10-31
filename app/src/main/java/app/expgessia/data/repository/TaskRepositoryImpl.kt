package app.expgessia.data.repository

import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.data.entity.TaskEntity
import app.expgessia.domain.model.Task
import app.expgessia.domain.repository.TaskRepository
import app.expgessia.utils.TimeUtils
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import toDomain
import toEntity
import java.util.concurrent.TimeUnit

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val characteristicsDao: CharacteristicDao,
) : TaskRepository {
    private val startOfTomorrow: Long
        get() = TimeUtils.calculateStartOfDay(
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        )
    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun addTask(task: Task) {
        // Suspend: Room автоматически выполнит на Dispatchers.IO
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        // Suspend: Room автоматически выполнит на Dispatchers.IO
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        // Suspend: Room автоматически выполнит на Dispatchers.IO
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun getIconResNameByCharacteristicId(id: Int): String? {
        return characteristicsDao.getIconResNameById(id)
    }

    override fun getTodayActiveTasks(): Flow<List<Task>> {
        // ✅ МАППИНГ: TaskEntity -> Task
        return taskDao.getTodayActiveTasks(startOfTomorrow).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Возвращает все завершенные задачи.
     */
    override fun getCompletedTasksStream(): Flow<List<Task>> {
        // ✅ МАППИНГ: TaskEntity -> Task
        return taskDao.getCompletedTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Возвращает задачи, запланированные на начало завтрашнего дня.
     */
    override fun getTomorrowScheduledTasks(): Flow<List<Task>> {
        // ✅ МАППИНГ: TaskEntity -> Task
        return taskDao.getTomorrowScheduledTasks(startOfTomorrow).map { entities ->
            entities.map { it.toDomain() }
        }
    }


    override suspend fun resetOverdueRepeatingTasks() {
        taskDao.resetOverdueRepeatingTasks(System.currentTimeMillis())
    }


    override fun getRepeatingTasks(): Flow<List<Task>> {
        return taskDao.getAllRepeatingTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }


}

