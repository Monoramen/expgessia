// data/dao/TaskCompletionDao.kt
package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.expgessia.data.entity.TaskCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletionEntity): Long

    // Получить общее количество выполненных задач (для статистики "ВЫПОЛНЕНО ЗАДАЧ")
    @Query("SELECT COUNT(id) FROM task_completions")
    fun getTotalCompletedTasksCount(): Flow<Int>

    // Получить общее количество опыта, заработанного для конкретной характеристики
    // Использовать SUM(xp_earned) для статистики по S.P.E.C.I.A.L.
    @Query("SELECT SUM(xp_earned) FROM task_completions WHERE characteristic_id = :characteristicId")
    fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int?> // Flow<Int?> для обработки NULL

    // Получить все записи выполнения (для истории/детального просмотра)
    @Query("SELECT * FROM task_completions ORDER BY completion_date DESC")
    fun getAllCompletions(): Flow<List<TaskCompletionEntity>>
}