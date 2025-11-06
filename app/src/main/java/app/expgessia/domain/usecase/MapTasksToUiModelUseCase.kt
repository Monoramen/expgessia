package app.expgessia.domain.usecase

import app.expgessia.domain.model.Task
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.utils.TimeUtils
import jakarta.inject.Inject
import java.time.LocalDate

// domain/usecase/MapTasksToUiModelUseCase.kt
class MapTasksToUiModelUseCase @Inject constructor(
    private val taskCompletionRepository: TaskCompletionRepository,
    private val characteristicRepository: CharacteristicRepository
) {
    suspend operator fun invoke(tasks: List<Task>, date: LocalDate): List<TaskUiModel> {
        val startOfDayMillis = TimeUtils.localDateToStartOfDayMillis(date)

        return tasks.map { task ->
            val isCompleted = taskCompletionRepository.isTaskCompletedForDate(
                task.id,
                startOfDayMillis
            )
            val characteristic = characteristicRepository.getCharacteristicById(task.characteristicId)

            TaskUiModel(
                id = task.id,
                title = task.title,
                description = task.description,
                xpReward = task.xpReward,
                isCompleted = if (date.isAfter(LocalDate.now())) false else isCompleted,
                characteristicIconResName = characteristic?.iconResName,
                date = date
            )
        }
    }
}