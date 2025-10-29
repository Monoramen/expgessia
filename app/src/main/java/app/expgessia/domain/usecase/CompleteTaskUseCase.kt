package app.expgessia.domain.usecase

import app.expgessia.domain.repository.TaskCompletionRepository
import app.expgessia.domain.repository.TaskRepository
import toEntity
import javax.inject.Inject

class CompleteTaskUseCase  @Inject constructor(
    private val taskCompletionRepository: TaskCompletionRepository,
    private val taskRepository: TaskRepository
){

    suspend operator fun invoke(taskId: Long, completionTimestamp: Long) {
        val task = taskRepository.getTaskById(taskId) ?: throw IllegalArgumentException("Task not found with ID: $taskId")


        if (task.isCompleted) {
            throw IllegalStateException("Task with ID $taskId is already completed.")
        }

        // 1. Регистрируем завершение. ЭТОТ ВЫЗОВ УЖЕ ОБНОВЛЯЕТ TaskEntity (scheduledFor, isCompleted)
        taskCompletionRepository.completeTask(task.toEntity(), completionTimestamp)

    }
}