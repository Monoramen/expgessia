package app.expgessia.domain.usecase

import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import toEntity
import javax.inject.Inject


class CompleteTaskUseCase @Inject constructor(
    private val taskCompletionRepository: TaskCompletionRepository
) {
    suspend operator fun invoke(taskId: Long, completionTime: Long = System.currentTimeMillis()) {
        taskCompletionRepository.completeTask(taskId, completionTime)
    }
}