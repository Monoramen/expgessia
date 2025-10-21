package app.expgessia.data.repository

import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.Task
import app.expgessia.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import toDomain
import toEntity

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val characteristicsDao: CharacteristicDao
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
        return  characteristicsDao.getIconResNameById(id)
    }

}

