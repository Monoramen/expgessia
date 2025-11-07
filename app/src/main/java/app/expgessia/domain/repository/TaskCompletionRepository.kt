package app.expgessia.domain.repository

import app.expgessia.data.entity.TaskWithInstance
import app.expgessia.domain.model.TaskInstance
import app.expgessia.domain.model.TaskUiModel // <--- Используем твой UI класс
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskCompletionRepository {
    suspend fun refreshStats()
    suspend fun completeTask(taskId: Long, completionTimestamp: Long)
    suspend fun undoCompleteTaskForDate(taskId: Long, date: LocalDate)


    fun getTodayActiveTaskDetailsStream(startOfDay: Long): Flow<List<TaskWithInstance>>
    fun getTomorrowScheduledTaskDetailsStream(startOfTomorrow: Long): Flow<List<TaskWithInstance>>
    fun getCompletedTaskInstancesStream(): Flow<List<TaskInstance>> // Для истории
    fun getCompletedTasksWithDetailsStream(): Flow<List<TaskWithInstance>>


    // Функции для статистики
    fun getTotalCompletedTasksCount(): Flow<Int>
    fun getXpEarnedByCharacteristic(characteristicId: Int): Flow<Int>
    suspend fun ensureTaskInstancesForDate(date: Long)

    suspend fun isTaskCompletedForDate(taskId: Long, date: Long): Boolean

    fun getTasksForDateWithStatus(date: LocalDate): Flow<List<TaskUiModel>>


    fun getTasksForCalendarDate(date: LocalDate): Flow<List<TaskWithInstance>>

    suspend fun createTaskInstancesForTask(taskId: Long)

    fun getCompletedTasksInDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<TaskInstance>>


}