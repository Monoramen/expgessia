package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.TaskWithCharacteristic
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksSync(): List<TaskEntity>
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Запросы для получения шаблонов (повторяющиеся задачи для генератора)

    @Query("SELECT * FROM tasks WHERE repeat_mode != 'NONE'")
    suspend fun getAllRepeatingTasksSync(): List<TaskEntity>
    @Query("SELECT * FROM tasks WHERE repeat_mode = 'DAILY'")
    fun getDailyTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE repeat_mode = 'WEEKLY'")
    fun getWeeklyTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE repeat_mode = 'MONTHLY'")
    fun getMonthlyTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE repeat_mode != 'NONE'")
    fun getAllRepeatingTasks(): Flow<List<TaskEntity>>

    // Запрос для JOIN'а с характеристиками (если используется)
    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTasksWithCharacteristics(): Flow<List<TaskWithCharacteristic>>

}