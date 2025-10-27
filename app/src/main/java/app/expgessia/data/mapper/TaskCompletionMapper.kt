package app.expgessia.data.mapper

import app.expgessia.data.entity.TaskCompletionEntity
import app.expgessia.domain.model.TaskCompletion

fun TaskCompletionEntity.toDomain(): TaskCompletion = TaskCompletion(
    id = this.id,
    taskId = this.taskId,
    completionDate = this.completionDate,
    xpEarned = this.xpEarned,
    characteristicId = this.characteristicId,
    isRepeating = this.isRepeating
)


fun TaskCompletion.toEntity(): TaskCompletionEntity = TaskCompletionEntity(
    id = this.id,
    taskId = this.taskId,
    completionDate = this.completionDate,
    xpEarned = this.xpEarned,
    characteristicId = this.characteristicId,
    isRepeating = this.isRepeating
)
