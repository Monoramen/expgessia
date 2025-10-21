package app.expgessia.domain.model

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val characteristicId: Int,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val repeatDetails: String? = null,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val scheduledFor: Long? = null,
)
enum class RepeatMode {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}