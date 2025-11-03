package app.expgessia.domain.repository

import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow



    interface DailyStatsRepository {
        // 💡 ОБНОВЛЕННЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ИНСТАНСАМИ
        fun getTotalTasksCompleted(): Flow<Int>
        fun getTotalXpEarned(): Flow<Int>
        fun getRecordXpDay(): Flow<Int>
        fun getCurrentStreak(): Flow<Int>
        fun getTodayXp(): Flow<Int>
        fun getTimeInApp(): Flow<Long>

        // 💡 НОВЫЕ МЕТОДЫ ДЛЯ ИНСТАНСОВ
        suspend fun updateStatsFromTaskInstances()
        fun getXpByCharacteristic(characteristicId: Int): Flow<Int>
        suspend fun updateStatsFromTaskCompletionIncrement(taskInstance: TaskInstanceEntity)

        suspend fun recordUserLogin(timestamp: Long, timeInAppMs: Long)

        suspend fun refreshStats()

    }


