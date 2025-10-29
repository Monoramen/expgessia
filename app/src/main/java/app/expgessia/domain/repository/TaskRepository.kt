package app.expgessia.domain.repository

import app.expgessia.data.entity.TaskEntity
import app.expgessia.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)

    suspend fun getIconResNameByCharacteristicId(id: Int): String?

     fun getTodayActiveTasks(): Flow<List<Task>>
     fun getCompletedTasksStream(): Flow<List<Task>>
     fun getTomorrowScheduledTasks(): Flow<List<Task>>

    suspend fun resetOverdueRepeatingTasks()

}
