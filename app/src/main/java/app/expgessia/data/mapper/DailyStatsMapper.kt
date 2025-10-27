package app.expgessia.data.mapper

import app.expgessia.data.entity.DailyStatsEntity
import app.expgessia.domain.model.DailyStats

fun DailyStatsEntity.toDomain(): DailyStats = DailyStats(
    date = this.date,
    totalXpEarned = this.totalXpEarned,
    timeInAppMs = this.timeInAppMs,
    tasksCompletedCount = this.tasksCompletedCount
)

fun DailyStats.toEntity(): DailyStatsEntity = DailyStatsEntity(
    date = this.date,
    totalXpEarned = this.totalXpEarned,
    timeInAppMs = this.timeInAppMs,
    tasksCompletedCount = this.tasksCompletedCount
)