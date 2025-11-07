package app.expgessia.domain.model



data class TaskInstance(
    val id: Long = 0,
    val taskId: Long,
    val scheduledFor: Long? = null,
    var isCompleted: Boolean = false,
    var completedAt: Long? = null,
    var xpEarned: Int = 0,
)

