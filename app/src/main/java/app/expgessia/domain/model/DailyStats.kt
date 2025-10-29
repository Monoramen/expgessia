package app.expgessia.domain.model

data class DailyStats(
    val date: Long,
    val totalXpEarned: Int,
    val timeInAppMs: Long = 0,
    val tasksCompletedCount: Int = 0,
)


