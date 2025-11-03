package app.expgessia.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithInstance(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id", // id из TaskEntity
        entityColumn = "task_id" // task_id из TaskInstanceEntity
    )
    val taskInstance: TaskInstanceEntity?
)