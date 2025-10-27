package app.expgessia.domain.model

data class TaskCompletion(
    val id: Long,
    val taskId: Long,
    val completionDate: Long,
    val xpEarned: Int,
    val characteristicId: Int,
    val isRepeating: Boolean,
)


