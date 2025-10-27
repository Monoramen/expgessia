// data/dao/DailyStatsDao.kt
package app.expgessia.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.expgessia.data.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {

    // Вставка или обновление (так как date - Primary Key)
    // Используем REPLACE, чтобы гарантировать, что за одну дату будет только одна запись
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: DailyStatsEntity)

    // Получить статистику за конкретный день
    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: Long): DailyStatsEntity?

    // Получить все записи (для расчета серии входов и рекордного дня)
    @Query("SELECT * FROM daily_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<DailyStatsEntity>>

    // Получить максимальный опыт за день (для статистики "РЕКОРДНЫЙ ДЕНЬ")
    @Query("SELECT MAX(total_xp_earned) FROM daily_stats")
    fun getRecordXpDay(): Flow<Int?>
}