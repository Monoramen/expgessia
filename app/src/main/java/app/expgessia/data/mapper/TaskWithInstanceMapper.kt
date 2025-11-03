
package app.expgessia.domain.mapper

import android.util.Log
import app.expgessia.data.entity.TaskWithInstance
import app.expgessia.domain.model.TaskUiModel
import app.expgessia.domain.repository.CharacteristicRepository
import javax.inject.Inject

class TaskWithInstanceMapper @Inject constructor(
    private val characteristicRepository: CharacteristicRepository
) {
    suspend fun mapToUiModel(taskWithInstance: TaskWithInstance): TaskUiModel {
        Log.d("TaskMapper", "Mapping task: ${taskWithInstance.task.title}")
        val task = taskWithInstance.task
        val instance = taskWithInstance.taskInstance

        // Получаем характеристику для иконки
        val characteristic = characteristicRepository.getCharacteristicById(task.characteristicId)
        Log.d("TaskMapper", "Characteristic found: ${characteristic?.iconResName}")
        return TaskUiModel(
            id = task.id,
            title = task.title,
            description = task.description,
            xpReward = task.xpReward,
            isCompleted = instance?.isCompleted ?: false,
            characteristicIconResName = characteristic?.iconResName
        )
    }

    suspend fun mapToUiModelList(taskWithInstances: List<TaskWithInstance>): List<TaskUiModel> {
        return taskWithInstances.map { mapToUiModel(it) }
    }
}