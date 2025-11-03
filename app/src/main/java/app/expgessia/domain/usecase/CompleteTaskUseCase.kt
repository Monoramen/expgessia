package app.expgessia.domain.usecase

import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import toEntity
import javax.inject.Inject


class CompleteTaskUseCase  @Inject constructor(
    private val taskCompletionRepository: TaskCompletionRepository,
    ){
    /**
     * Запускает полную транзакцию по выполнению задачи.
     * Репозиторий выполнения сам найдет шаблон задачи и экземпляр выполнения.
     */
    suspend operator fun invoke(taskId: Long, completionTimestamp: Long) {
        taskCompletionRepository.completeTask(taskId, completionTimestamp)
    }
}