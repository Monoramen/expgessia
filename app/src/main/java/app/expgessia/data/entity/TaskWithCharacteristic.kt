package app.expgessia.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithCharacteristic(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "characteristic_id",
        entityColumn = "id"
    )
    val characteristic: CharacteristicEntity
)