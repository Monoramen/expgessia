// data/repository/TaskCompletionRepositoryImpl.kt
package app.expgessia.data.repository

import androidx.room.withTransaction
import app.expgessia.data.AppDatabase
import app.expgessia.data.dao.DailyStatsDao
import app.expgessia.data.dao.TaskCompletionDao
import app.expgessia.data.dao.TaskDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.DailyStatsEntity
import app.expgessia.data.entity.TaskCompletionEntity
import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.UserEntity
import app.expgessia.data.mapper.toDomain
import app.expgessia.domain.model.TaskCompletion
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow

class TaskCompletionRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val userDao: UserDao, //
    private val taskCompletionDao: TaskCompletionDao,
    private val dailyStatsDao: DailyStatsDao,
    private val db: AppDatabase,
) : TaskCompletionRepository {

    override fun getTotalCompletedTasksCount(): Flow<Int> =
        taskCompletionDao.getTotalCompletedTasksCount()

    override fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int> {
        return taskCompletionDao.getXpEarnedByCharacteristic(characteristicId).map { it ?: 0 }
    }

    override fun getCompletionHistory(): Flow<List<TaskCompletion>> {
        // Конвертация Long (Entity) -> Date (Domain Model)
        return taskCompletionDao.getAllCompletions().map { entities ->
            entities.map { entity ->
                // Используем маппер, предполагая, что он корректно создан
                entity.toDomain()
            }
        }
    }

    /**
     * ГЛАВНАЯ ФУНКЦИЯ ПРОКАЧКИ И ВЫПОЛНЕНИЯ ЗАДАЧИ
     * Вся логика выполняется внутри Room Transaction.
     */
    override suspend fun completeTask(taskEntity: TaskEntity, completionTimestamp: Long) {
        val startOfDay = TimeUtils.calculateStartOfDay(completionTimestamp)

        // --- Room Transaction: обеспечивает атомарность всех обновлений ---
        db.withTransaction {
            val currentUser = userDao.getUser() ?: throw IllegalStateException("User not found")
            val xpEarned =
                calculateFinalXp(taskEntity, currentUser) // Расчет XP с учетом S.P.E.C.I.A.L.

            // 1. ОБНОВЛЕНИЕ USER ENTITY (XP и LEVEL UP)
            val updatedUser = processLevelUpAndXp(currentUser, xpEarned)
            userDao.updateUser(updatedUser) //

            // 2. ОБНОВЛЕНИЕ TASK ENTITY (Планирование следующей даты)
            val isRepeating = taskEntity.repeatMode.uppercase(Locale.ROOT) != "NONE"
            val updatedTask = if (isRepeating) {
                taskEntity.copy(
                    scheduledFor = TimeUtils.calculateNextScheduledDate(
                        taskEntity,
                        completionTimestamp
                    ),
                    isCompleted = false // Сброс статуса для новой итерации
                )
            } else {
                taskEntity.copy(isCompleted = true)
            }
            taskDao.updateTask(updatedTask) //

            // 3. СОЗДАНИЕ TASK COMPLETION ENTITY (История)
            val completion = TaskCompletionEntity(
                id = 0,
                taskId = taskEntity.id,
                completionDate = completionTimestamp, // Long
                xpEarned = xpEarned,
                characteristicId = taskEntity.characteristicId,
                isRepeating = isRepeating
            )
            taskCompletionDao.insert(completion)

            // 4. ОБНОВЛЕНИЕ DAILY STATS ENTITY
            val currentStats = dailyStatsDao.getStatsByDate(startOfDay)
            val newStats = currentStats?.copy(
                totalXpEarned = currentStats.totalXpEarned + xpEarned,
                tasksCompletedCount = currentStats.tasksCompletedCount + 1
            ) ?: DailyStatsEntity(
                date = startOfDay,
                totalXpEarned = xpEarned,
                tasksCompletedCount = 1,
                timeInAppMs = 0
            )
            dailyStatsDao.insertOrUpdate(newStats)
        }
    }

    // --- Вспомогательные функции (Helper Methods) ---

    private fun calculateFinalXp(task: TaskEntity, user: UserEntity): Int {
        val baseXP = task.xpReward
        // Бонус S.P.E.C.I.A.L.: +5% XP за каждую единицу соответствующего атрибута
        val bonusValue = when (task.characteristicId) {
            1 -> user.strength // Strength (S)
            2 -> user.perception // Perception (P)
            3 -> user.endurance // Endurance (E)
            4 -> user.charisma // Charisma (C)
            5 -> user.intelligence // Intelligence (I)
            6 -> user.agility // Agility (A)
            7 -> user.luck // Luck (L)
            else -> 0
        }

        val multiplier = 1.0 + (bonusValue * 0.05) // 5% за каждый поинт

        // Добавление случайного бонуса от Luck (LUCK)
        val luckBonus =
            if (user.luck > 0 && Math.random() < (user.luck * 0.03)) { // 3% шанс за поинт Luck
                0.15 // Критический успех: +15% XP
            } else {
                0.0
            }

        return (baseXP * (multiplier + luckBonus)).toInt()
    }

    private fun calculateXpNeeded(level: Int, baseXP: Int = 100, curveFactor: Double = 1.5): Int {
        // Формула: xpNeeded = baseXP * (Level ^ CurveFactor)
        if (level <= 1) return baseXP // Требуется 100 XP для перехода с 1 на 2 уровень
        return (baseXP * (level.toDouble().pow(curveFactor))).toInt()
    }

    private fun processLevelUpAndXp(user: UserEntity, xpEarned: Int): UserEntity {
        var newXp = user.experience + xpEarned
        var newLevel = user.level
        var attributePointsEarned = 0 // Количество очков атрибутов для распределения

        while (newXp >= calculateXpNeeded(newLevel)) {
            val xpNeeded = calculateXpNeeded(newLevel)
            newXp -= xpNeeded
            newLevel += 1
            attributePointsEarned += 1 // Начисление 1 очка S.P.E.C.I.A.L. за каждый Level Up
        }

        // Необходимо, чтобы в UserEntity было поле 'attributePoints' (Int)
        // Если это поле отсутствует, логика будет неполной.
        // Предполагаем, что оно существует для начисления:
        /* val currentAttributePoints = // ... получить текущие очки из UserEntity
        return user.copy(
            experience = newXp,
            level = newLevel,
            attributePoints = currentAttributePoints + attributePointsEarned // <-- Важно!
        )
        */

        // Временное решение без поля attributePoints, только XP и Level:
        return user.copy(
            experience = newXp,
            level = newLevel
            // Добавьте сюда логику обновления 'attributePoints', когда добавите поле
        )
    }
}