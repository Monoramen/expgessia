package app.expgessia.domain.repository

import app.expgessia.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // CRUD для шаблонов Task (TaskEntity)
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)

    // Вспомогательная функция
    suspend fun getIconResNameByCharacteristicId(id: Int): String?

    fun getRepeatingTasks(): Flow<List<Task>>
    fun getDailyTasks(): Flow<List<Task>>
    fun getWeeklyTasks(): Flow<List<Task>>
    fun getMonthlyTasks(): Flow<List<Task>>
    suspend fun getAllTasksSync(): List<Task>
}