// data/repository/DailyStatsRepositoryImpl.kt
package app.expgessia.data.repository

import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.DailyStatsEntity
import app.expgessia.data.mapper.toDomain
import app.expgessia.data.mapper.toEntity
import app.expgessia.domain.model.DailyStats
import app.expgessia.domain.repository.DailyStatsRepository
import app.expgessia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DailyStatsRepositoryImpl @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val userDao: UserDao,
) : DailyStatsRepository {

    override fun getAllDailyStats(): Flow<List<DailyStats>> {
        return dailyStatsDao.getAllStats().map { entities ->
            entities.map { it.toDomain() } // Используем маппер
        }
    }

    override suspend fun getStatsByDate(date: Long): DailyStats? {
        return dailyStatsDao.getStatsByDate(date)?.toDomain()
    }

    override suspend fun insertOrUpdateStats(stats: DailyStats) {
        dailyStatsDao.insertOrUpdate(stats.toEntity()) // Используем маппер
    }

    override fun getRecordXpDay(): Flow<Int?> {
        return dailyStatsDao.getRecordXpDay()
    }

    /**
     * Логика записи входа пользователя и обновления времени в приложении.
     */
    override suspend fun recordUserLogin(currentTimestamp: Long, timeInAppMs: Long) {
        val startOfDay = TimeUtils.calculateStartOfDay(currentTimestamp)

        // 1. Обновление DailyStatsEntity
        val currentStats = dailyStatsDao.getStatsByDate(startOfDay)
        val newStats = currentStats?.copy(
            timeInAppMs = currentStats.timeInAppMs + timeInAppMs
        ) ?: DailyStatsEntity(
            date = startOfDay,
            totalXpEarned = 0,
            tasksCompletedCount = 0,
            timeInAppMs = timeInAppMs
        )
        dailyStatsDao.insertOrUpdate(newStats)

        // 2. Обновление last_login в UserEntity (использует DateConverter)
        userDao.getUser()?.let { user ->
            // lastLogin теперь Long, передаем Timestamp напрямую
            userDao.updateUser(user.copy(lastLogin = currentTimestamp))
        }
    }

    /**
     * Расчет текущей серии входов (Streak).
     * Серия считается, если в последовательные дни была активность (XP > 0 ИЛИ Задачи > 0).
     */
    override fun getCurrentStreak(): Flow<Int> {
        return dailyStatsDao.getAllStats().map { stats ->
            // Сортировка по дате (Long) в порядке убывания
            val sortedStats = stats.sortedByDescending { it.date }

            var streak = 0
            var expectedDate = TimeUtils.calculateStartOfDay(System.currentTimeMillis())

            for (dailyStat in sortedStats) {
                // Если запись слишком старая (больше 1 дня назад), прерываем.
                // NOTE: Мы ищем только последовательную серию ДО СЕГОДНЯ.
                if (dailyStat.date < expectedDate) {
                    break
                }

                // Активность = XP > 0 ИЛИ Задачи > 0
                val hasActivity = dailyStat.totalXpEarned > 0 || dailyStat.tasksCompletedCount > 0

                // 1. Проверяем, совпадает ли дата с ожидаемой (сегодня, вчера, позавчера...)
                if (dailyStat.date == expectedDate && hasActivity) {
                    streak++
                    // Переходим к ожиданию предыдущего дня
                    expectedDate -= TimeUnit.DAYS.toMillis(1)
                } else if (dailyStat.date == expectedDate && !hasActivity) {
                    // 2. Если запись есть, но нет активности (пропуск дня), серия прерывается.
                    break
                }
                // Если dailyStat.date > expectedDate, это означает, что мы пропустили день в базе.
                // Это возможно, только если пользователь не заходил, но `getAllStats` не должно пропускать.
                // При корректном использовании TimeUtils.calculateStartOfDay() для expectedDate,
                // этот цикл должен быть точным.
            }
            streak
        }
    }


    override fun getTotalXpEarned(): Flow<Int> {
        return dailyStatsDao.getTotalXpEarned().map { it ?: 0 }
    }

    override fun getTotalTasksCompleted(): Flow<Int> {
        return dailyStatsDao.getTotalTasksCompleted().map { it ?: 0 }

    }

    override fun getTodayXp(): Flow<Int> {
        val startOfDay = TimeUtils.calculateStartOfDay(System.currentTimeMillis())
        return dailyStatsDao.getXpByDate(startOfDay).map { it ?: 0 }
    }

    override fun getTimeInApp(): Flow<Long?> {
        return dailyStatsDao.getTimeInApp()
    }
}