package app.expgessia.domain.model



data class TaskInstance(
    val id: Long = 0,
    val taskId: Long,
    val scheduledFor: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val xpEarned: Int = 0,
)

