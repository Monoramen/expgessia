package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.data.entity.TaskWithInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    // CRUD-операции для экземпляров

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(instances: List<TaskInstanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: TaskInstanceEntity)

    @Update
    suspend fun update(instance: TaskInstanceEntity)



    @Query("""
        SELECT * FROM task_instances
        WHERE task_id = :taskId AND scheduled_for = :date
        LIMIT 1
    """)
    suspend fun getTaskInstanceForDay(taskId: Long, date: Long): TaskInstanceEntity?




    @Transaction
    @Query("""
    SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned
    FROM tasks AS t
    INNER JOIN task_instances AS ti ON t.id = ti.task_id 
    WHERE ti.scheduled_for = :startOfDay
    AND ti.is_completed = 0  -- 🔥 ТОЛЬКО НЕВЫПОЛНЕННЫЕ
    ORDER BY t.id ASC
""")
    fun getTodayTasksWithInstance(startOfDay: Long): Flow<List<TaskWithInstance>>

    // 2. Получение задач, запланированных на завтра (JOIN)
    @Transaction
    @Query("""
        SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned
        FROM tasks AS t
        INNER JOIN task_instances AS ti ON t.id = ti.task_id 
        WHERE ti.scheduled_for = :startOfTomorrow
        AND ti.is_completed = 0  -- 🔥 ТОЛЬКО НЕВЫПОЛНЕННЫЕ
        ORDER BY t.id ASC
    """)
    fun getTomorrowScheduledTasksWithInstance(startOfTomorrow: Long): Flow<List<TaskWithInstance>>

    @Query("""
        SELECT * FROM task_instances
        WHERE is_completed = 1
        ORDER BY completed_at DESC
    """)
    fun getCompletedTaskInstances(): Flow<List<TaskInstanceEntity>>



    @Query("""
    SELECT * FROM task_instances 
    WHERE task_id = :taskId 
    AND scheduled_for = :date 
    AND is_completed = 1
    LIMIT 1
""")
    suspend fun getCompletedInstanceForDate(taskId: Long, date: Long): TaskInstanceEntity?

    @Transaction
    @Query("""
    SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned
    FROM tasks AS t
    INNER JOIN task_instances AS ti ON t.id = ti.task_id 
    WHERE ti.is_completed = 1
    ORDER BY ti.completed_at DESC
""")
    fun getCompletedTasksWithInstance(): Flow<List<TaskWithInstance>>

    @Query("""
        DELETE FROM task_instances
        WHERE is_completed = 0 
        AND scheduled_for IS NOT NULL 
        AND scheduled_for < :currentTime
    """)
    suspend fun deleteOverdueUncompletedInstances(currentTime: Long)

    @Query("""
        SELECT * FROM task_instances 
        WHERE scheduled_for = :date 
        AND is_completed = 1 

    """)
    suspend fun getCompletedInstancesByDate(date: Long): List<TaskInstanceEntity>

    @Transaction
    @Query("""
    SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned
    FROM tasks AS t
    LEFT JOIN task_instances AS ti ON t.id = ti.task_id AND ti.scheduled_for = :date
    WHERE (ti.scheduled_for = :date OR ti.scheduled_for IS NULL)
    ORDER BY t.id ASC
""")
    fun getTasksWithInstancesByDate(date: Long): Flow<List<TaskWithInstance>>


    @Query("SELECT COUNT(*) FROM task_instances WHERE task_id = :taskId AND scheduled_for = :date")
    suspend fun hasInstanceForDate(taskId: Long, date: Long): Boolean


    @Query("SELECT COUNT(*) FROM task_instances WHERE task_id = :taskId")
    suspend fun hasAnyInstanceForTask(taskId: Long): Boolean



    @Query("DELETE FROM task_instances WHERE task_id = :taskId AND scheduled_for >= :startDate")
    suspend fun deleteFutureInstances(taskId: Long, startDate: Long)

    @Query("""
    SELECT * FROM task_instances 
    WHERE is_completed = 1 
    AND completed_at BETWEEN :startDate AND :endDate
    ORDER BY completed_at DESC
""")
    fun getCompletedInstancesInDateRange(startDate: Long, endDate: Long): Flow<List<TaskInstanceEntity>>




}