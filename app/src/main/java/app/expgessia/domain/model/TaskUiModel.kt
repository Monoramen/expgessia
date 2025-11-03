package app.expgessia.domain.model

data class TaskUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean,
    val characteristicIconResName: String?,

)
