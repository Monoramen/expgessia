package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "daily_stats",
    primaryKeys = ["date"]
)
data class DailyStatsEntity(


    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "total_xp_earned")
    val totalXpEarned: Int,

    // Добавлено: Время в игре (игровое время, не общее время работы приложения)
    @ColumnInfo(name = "time_in_app_ms")
    val timeInAppMs: Long = 0,

    // Добавлено: Количество выполненных задач
    @ColumnInfo(name = "tasks_completed_count")
    val tasksCompletedCount: Int = 0

)