package app.expgessia.data.repository

import android.util.Log
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskInstanceDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.DailyStatsEntity
import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.time.LocalDate

class DailyStatsRepositoryImpl @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val taskInstanceDao: TaskInstanceDao,
    private val userDao: UserDao
) : DailyStatsRepository {

    private val _refreshTrigger = MutableStateFlow(0)



    override fun getTotalTasksCompleted(): Flow<Int> {
        return _refreshTrigger.flatMapLatest {
            taskInstanceDao.getCompletedTaskInstances()
                .map { instances -> instances.count { it.isCompleted } }
        }
    }
    override fun getTotalXpEarned(): Flow<Int> {
        return taskInstanceDao.getCompletedTaskInstances()
            .map { instances ->
                instances
                    .filter { it.isCompleted  }
                    .sumOf { it.xpEarned }
            }
    }



    override fun getRecordXpDay(): Flow<Int> {
        // Получаем максимальный XP за один день из daily_stats
        return dailyStatsDao.getRecordXpDay().map { it ?: 0 }
    }

    override fun getCurrentStreak(): Flow<Int> {
        return taskInstanceDao.getCompletedTaskInstances()
            .map { instances ->
                calculateCurrentStreak(instances)
            }
    }

    override fun getTodayXp(): Flow<Int> {
        val todayStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
        return taskInstanceDao.getCompletedTaskInstances()
            .map { instances ->
                instances
                    .filter {
                        it.isCompleted &&
                                it.completedAt != null &&
                                it.completedAt >= todayStart &&
                                it.completedAt < todayStart + TimeUtils.DAY_IN_MILLIS
                    }
                    .sumOf { it.xpEarned }
            }
    }

    override fun getTimeInApp(): Flow<Long> {
        return dailyStatsDao.getTimeInApp().map { it ?: 0L }
    }

    override fun getXpByCharacteristic(characteristicId: Int): Flow<Int> {
        // TODO: Реализовать когда будет связь TaskInstance с Characteristic
        // Временная заглушка
        return taskInstanceDao.getCompletedTaskInstances()
            .map { instances ->
                instances.sumOf { it.xpEarned } / 7 // Равномерно по всем характеристикам
            }
    }

    override suspend fun updateStatsFromTaskInstances() {
        val todayStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
        val completedInstances = taskInstanceDao.getCompletedInstancesByDate(todayStart)

        var dailyStats = dailyStatsDao.getStatsByDate(todayStart)

        if (dailyStats == null) {
            dailyStats = DailyStatsEntity(
                date = todayStart,
                totalXpEarned = 0,
                tasksCompletedCount = 0,
                timeInAppMs = 0
            )
        }

        val totalXp = completedInstances.sumOf { it.xpEarned }
        val tasksCompleted = completedInstances.size

        dailyStats.totalXpEarned = totalXp
        dailyStats.tasksCompletedCount = tasksCompleted

        dailyStatsDao.insertOrUpdate(dailyStats)
    }

    override suspend fun updateStatsFromTaskCompletionIncrement(taskInstance: TaskInstanceEntity) {
        val todayStart = TimeUtils.calculateStartOfDay(System.currentTimeMillis())

        var dailyStats = dailyStatsDao.getStatsByDate(todayStart) ?: DailyStatsEntity(
            date = todayStart,
            totalXpEarned = 0,
            tasksCompletedCount = 0,
            timeInAppMs = 0
        )

        if (taskInstance.isCompleted) {
            dailyStats.totalXpEarned += taskInstance.xpEarned
            dailyStats.tasksCompletedCount += 1
        } else {
            dailyStats.totalXpEarned -= taskInstance.xpEarned
            dailyStats.tasksCompletedCount -= 1
        }

        dailyStats.totalXpEarned = maxOf(0, dailyStats.totalXpEarned)
        dailyStats.tasksCompletedCount = maxOf(0, dailyStats.tasksCompletedCount)

        dailyStatsDao.insertOrUpdate(dailyStats)
        _refreshTrigger.value++
    }

    // 💡 УЛУЧШЕННЫЙ МЕТОД ДЛЯ РАСЧЕТА СТРИКА
    private fun calculateCurrentStreak(instances: List<TaskInstanceEntity>): Int {
        if (instances.isEmpty()) return 0

        // Получаем уникальные даты завершения задач
        val completedDates = instances
            .filter { it.isCompleted && it.completedAt != null  }
            .map { TimeUtils.millisToLocalDate(it.completedAt!!) }
            .distinct()
            .sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        var currentDate = LocalDate.now()
        val sortedDates = completedDates.sortedDescending()

        // Проверяем сегодняшний день
        if (sortedDates.first() == currentDate) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        // Проверяем предыдущие дни подряд
        for (date in sortedDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date < currentDate) {
                break
            }
        }

        return streak
    }

    override suspend fun recordUserLogin(timestamp: Long, timeInAppMs: Long) {
        try {
            val todayStart = TimeUtils.calculateStartOfDay(timestamp)

            var dailyStats = dailyStatsDao.getStatsByDate(todayStart)

            if (dailyStats == null) {
                dailyStats = DailyStatsEntity(
                    date = todayStart,
                    totalXpEarned = 0,
                    tasksCompletedCount = 0,
                    timeInAppMs = timeInAppMs
                )
            } else {
                dailyStats.timeInAppMs += timeInAppMs
            }

            dailyStatsDao.insertOrUpdate(dailyStats)

        } catch (e: Exception) {
            Log.e("AppTimeTracker", "Error recording user login time", e)
        }
    }


     override suspend fun refreshStats() {
        _refreshTrigger.value++
    }
}