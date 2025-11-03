package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.data.entity.TaskWithInstance
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskInstanceDao {

    // CRUD-операции для экземпляров
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: TaskInstanceEntity)

    @Update
    suspend fun update(instance: TaskInstanceEntity)

    // Найти экземпляр задачи на сегодня/конкретный день (для логики завершения)
    @Query("""
        SELECT * FROM task_instances
        WHERE task_id = :taskId AND scheduled_for = :startOfDay
        LIMIT 1
    """)
    suspend fun getTaskInstanceForDay(taskId: Long, startOfDay: Long): TaskInstanceEntity?

    // Запросы, перенесенные из TaskDao:

    // 1. Получение активных задач на сегодня (JOIN TaskEntity + TaskInstanceEntity)
    @Transaction
    @Query("""
        SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned, ti.is_undone
        FROM tasks AS t
        INNER JOIN task_instances AS ti ON t.id = ti.task_id 
        WHERE ti.scheduled_for = :startOfDay 
        AND ti.is_undone = 0 
        ORDER BY ti.is_completed ASC, t.id ASC
    """)
    fun getTodayTasksWithInstance(startOfDay: Long): Flow<List<TaskWithInstance>>

    // 2. Получение задач, запланированных на завтра (JOIN)
    @Transaction
    @Query("""
        SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned, ti.is_undone
        FROM tasks AS t
        INNER JOIN task_instances AS ti ON t.id = ti.task_id 
        WHERE ti.scheduled_for = :startOfTomorrow
        AND ti.is_undone = 0 
        ORDER BY t.id ASC
    """)
    fun getTomorrowScheduledTasksWithInstance(startOfTomorrow: Long): Flow<List<TaskWithInstance>>

    // 3. Получение завершенных экземпляров (для истории)
    @Query("""
        SELECT * FROM task_instances
        WHERE is_completed = 1
        ORDER BY completed_at DESC
    """)
    fun getCompletedTaskInstances(): Flow<List<TaskInstanceEntity>>

    // 4. Логика сброса/удаления просроченных экземпляров (вместо обновления TaskEntity)
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
        AND is_undone = 0
    """)
    suspend fun getCompletedInstancesByDate(date: Long): List<TaskInstanceEntity>

    // Дополнительный метод для получения задач по дате
    @Transaction
    @Query("""
        SELECT t.*, ti.id as instance_id, ti.task_id, ti.scheduled_for, ti.is_completed, ti.completed_at, ti.xp_earned, ti.is_undone
        FROM tasks AS t
        INNER JOIN task_instances AS ti ON t.id = ti.task_id 
        WHERE ti.scheduled_for = :date
        AND ti.is_undone = 0
        ORDER BY t.id ASC
    """)
    fun getTasksWithInstancesByDate(date: Long): Flow<List<TaskWithInstance>>
}