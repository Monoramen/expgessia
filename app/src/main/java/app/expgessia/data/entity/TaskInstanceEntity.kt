package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.expgessia.data.converter.TaskConverters // Импортируем наш конвертер


@Entity(
    tableName = "task_instances",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("task_id", "scheduled_for")] // Для быстрого поиска по задаче и дате
)
data class TaskInstanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "scheduled_for") // Когда должна быть выполнена
    val scheduledFor: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at") // Время завершения (если завершена)
    val completedAt: Long? = null,

    @ColumnInfo(name = "xp_earned") // XP за это конкретное выполнение (можно пересчитывать)
    val xpEarned: Int = 0,

    // Можно добавить флаг is_undone, если хочешь soft undo
    @ColumnInfo(name = "is_undone")
    val isUndone: Boolean = false
)