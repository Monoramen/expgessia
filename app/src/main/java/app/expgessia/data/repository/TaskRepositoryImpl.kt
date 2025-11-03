package app.expgessia.data.repository


import android.util.Log
import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.dao.TaskDao
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

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun getIconResNameByCharacteristicId(id: Int): String? {
        return characteristicsDao.getIconResNameById(id)
    }

    // Функции для планировщика
    override fun getRepeatingTasks(): Flow<List<Task>> {
        return taskDao.getAllRepeatingTasks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getDailyTasks(): Flow<List<Task>> {
        return taskDao.getDailyTasks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getWeeklyTasks(): Flow<List<Task>> {
        return taskDao.getWeeklyTasks().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMonthlyTasks(): Flow<List<Task>> {
        return taskDao.getMonthlyTasks().map { entities -> entities.map { it.toDomain() } }
    }
    override suspend fun getAllTasksSync(): List<Task> {
        return taskDao.getAllTasksSync().map { it.toDomain() }
    }


}