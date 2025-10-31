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

    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, scheduled_for ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // 1. Фильтр для ЕЖЕДНЕВНЫХ задач
    @Query("SELECT * FROM tasks WHERE repeat_mode = 'DAILY'")
    fun getDailyTasks(): Flow<List<TaskEntity>>

    // 2. Фильтр для ЕЖЕНЕДЕЛЬНЫХ задач
    @Query("SELECT * FROM tasks WHERE repeat_mode = 'WEEKLY'")
    fun getWeeklyTasks(): Flow<List<TaskEntity>>

    // 3. Фильтр для ЕЖЕМЕСЯЧНЫХ задач
    @Query("SELECT * FROM tasks WHERE repeat_mode = 'MONTHLY'")
    fun getMonthlyTasks(): Flow<List<TaskEntity>>

    @Query(
        """
    SELECT * FROM tasks
    WHERE is_completed = 0 
    AND (
        scheduled_for IS NULL 
        OR scheduled_for < :startOfTomorrow 
    )
    ORDER BY scheduled_for ASC, id ASC """
    )
    fun getTodayActiveTasks(startOfTomorrow: Long): Flow<List<TaskEntity>>

    @Query(
        """
    SELECT * FROM tasks
    WHERE is_completed = 0 
    AND scheduled_for = :startOfTomorrow
    ORDER BY id ASC """
    )
    fun getTomorrowScheduledTasks(startOfTomorrow: Long): Flow<List<TaskEntity>>


    @Query(
        """
    SELECT * FROM tasks
    WHERE is_completed = 1
    ORDER BY scheduled_for DESC, id DESC """
    )
    fun getCompletedTasks(): Flow<List<TaskEntity>>


    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)


    @Transaction
    @Query("SELECT * FROM tasks")
    fun getTasksWithCharacteristics(): Flow<List<TaskWithCharacteristic>> // или LiveData

    @Query(
        """
    UPDATE tasks
    SET is_completed = 0, scheduled_for = NULL
    WHERE is_completed = 1 
    AND repeat_mode != 'NONE' 
    AND scheduled_for IS NOT NULL 
    AND scheduled_for < :currentTime
    """
    )
    suspend fun resetOverdueRepeatingTasks(currentTime: Long)


    @Query("SELECT * FROM tasks WHERE repeat_mode != 'NONE'")
    fun getAllRepeatingTasks(): Flow<List<TaskEntity>>


}
