package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "task_completions",
    primaryKeys = ["id"], // Хотя автогенерация удобнее, можно использовать составной ключ, но пока id - проще
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE // Если задача удаляется, удаляются и ее выполнения
        ),
        ForeignKey(
            entity = CharacteristicEntity::class,
            parentColumns = ["id"],
            childColumns = ["characteristic_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class TaskCompletionEntity(
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "task_id", index = true)
    val taskId: Long,

    @ColumnInfo(name = "completion_date")
    val completionDate: Long,

    @ColumnInfo(name = "xp_earned")
    val xpEarned: Int,

    @ColumnInfo(name = "characteristic_id")
    val characteristicId: Int,

    @ColumnInfo(name = "is_repeating")
    val isRepeating: Boolean,

)