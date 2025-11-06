package app.expgessia.data.mapper

import app.expgessia.data.entity.TaskInstanceEntity
import app.expgessia.domain.model.TaskInstance

fun TaskInstanceEntity.toDomain(): TaskInstance = TaskInstance(
    id = this.id,
    taskId = this.taskId,
    scheduledFor = this.scheduledFor,
    isCompleted = this.isCompleted,
    completedAt = this.completedAt,
    xpEarned = this.xpEarned,
)

fun TaskInstance.toEntity(): TaskInstanceEntity = TaskInstanceEntity(
    id = this.id,
    taskId = this.taskId,
    scheduledFor = this.scheduledFor,
    isCompleted = this.isCompleted,
    completedAt = this.completedAt,
    xpEarned = this.xpEarned,

)