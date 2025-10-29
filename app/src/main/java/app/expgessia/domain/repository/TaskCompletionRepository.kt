
package app.expgessia.domain.repository

import app.expgessia.data.entity.TaskEntity
import app.expgessia.domain.model.TaskCompletion
import kotlinx.coroutines.flow.Flow

interface TaskCompletionRepository {

    /**
     * Основная транзакционная логика выполнения задачи.
     * Должна: обновить TaskEntity, UserEntity, DailyStatsEntity и создать TaskCompletionEntity.
     */
    suspend fun completeTask(taskEntity: TaskEntity, completionTimestamp: Long)

    fun getTotalCompletedTasksCount(): Flow<Int>

    fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int>

    fun getCompletionHistory(): Flow<List<TaskCompletion>>
}