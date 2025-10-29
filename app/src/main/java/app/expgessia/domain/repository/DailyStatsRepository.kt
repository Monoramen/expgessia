package app.expgessia.domain.repository

import app.expgessia.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

interface DailyStatsRepository {

    fun getAllDailyStats(): Flow<List<DailyStats>>

    suspend fun getStatsByDate(date: Long): DailyStats?

    suspend fun insertOrUpdateStats(stats: DailyStats)

    fun getRecordXpDay(): Flow<Int?>

    /**
     * Важная функция для учета времени в приложении и статуса входа.
     */
    suspend fun recordUserLogin(currentTimestamp: Long, timeInAppMs: Long)

    fun getCurrentStreak(): Flow<Int>


    fun getTotalXpEarned(): Flow<Int>

    fun getTotalTasksCompleted(): Flow<Int>

    fun getTodayXp(): Flow<Int>

    fun getTimeInApp(): Flow<Long?>
}