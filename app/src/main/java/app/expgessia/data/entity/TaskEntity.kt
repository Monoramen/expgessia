package app.expgessia.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.expgessia.data.converter.TaskConverters // Импортируем наш конвертер

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = CharacteristicEntity::class,
        parentColumns = ["id"],
        childColumns = ["characteristic_id"],
        onDelete = ForeignKey.CASCADE
    )]
    , indices = [Index("characteristic_id")] // Добавьте это
)

@TypeConverters(TaskConverters::class)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    // Добавлено:
    val description: String,

    @ColumnInfo(name = "characteristic_id")
    val characteristicId: Int,

    // Добавлено: Режим повторения (хранится как String, обрабатывается конвертером)
    @ColumnInfo(name = "repeat_mode")
    val repeatMode: String,

    // Добавлено: Дополнительные детали повторения (например, "пн, ср, пт")
    @ColumnInfo(name = "repeat_details")
    val repeatDetails: String? = null,

    @ColumnInfo(name = "xp_reward")
    val xpReward: Int,

)
