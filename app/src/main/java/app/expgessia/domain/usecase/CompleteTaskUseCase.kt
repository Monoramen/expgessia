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

        // Дополнительная проверка, чтобы избежать двойного завершения (если это не повторяющаяся задача)
        if (task.isCompleted && !task.repeatMode.equals("NONE")) {
            throw IllegalStateException("Task with ID $taskId is already completed and not repeating.")
        }

        // 2. Регистрируем завершение (это обновит XP, уровень и т.д.)
        // Мы предполагаем, что task.toEntity() внутри taskCompletionRepository.completeTask()
        // создает TaskCompletionEntity с необходимой логикой XP.
        taskCompletionRepository.completeTask(task.toEntity(), completionTimestamp)

        // 3. 💡 ОБНОВЛЯЕМ саму задачу в основном TaskRepository,
        // чтобы она была помечена как завершенная, и UI обновился.
        val updatedTask = task.copy(isCompleted = true)
        taskRepository.updateTask(updatedTask)
    }
}