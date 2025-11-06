package app.expgessia.domain.model

import java.time.LocalDate

data class TaskUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean,
    val characteristicIconResName: String?,
    val date: LocalDate // Добавлено поле даты

)
