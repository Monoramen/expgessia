package app.expgessia.domain.repository

import app.expgessia.data.entity.TaskWithInstance
import app.expgessia.domain.model.TaskInstance
import app.expgessia.domain.model.TaskUiModel // <--- Используем твой UI класс
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskCompletionRepository {

    suspend fun completeTask(taskId: Long, completionTimestamp: Long)
    suspend fun undoCompleteTask(taskId: Long)

    // Функции для UI (берут данные через JOIN)
    fun getTodayActiveTaskDetailsStream(startOfDay: Long): Flow<List<TaskWithInstance>>
    fun getTomorrowScheduledTaskDetailsStream(startOfTomorrow: Long): Flow<List<TaskWithInstance>>
    fun getCompletedTaskInstancesStream(): Flow<List<TaskInstance>> // Для истории

    // Функции для статистики
    fun getTotalCompletedTasksCount(): Flow<Int>
    fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int>
    suspend fun ensureDailyTaskInstances(currentTime: Long)

    suspend fun isTaskCompletedForDate(taskId: Long, date: Long): Boolean

    fun getTasksForDateWithStatus(date: LocalDate): Flow<List<TaskUiModel>>


    fun getTasksForCalendarDate(date: LocalDate): Flow<List<TaskWithInstance>>


    suspend fun ensureTaskInstancesForDate(date: LocalDate)
}