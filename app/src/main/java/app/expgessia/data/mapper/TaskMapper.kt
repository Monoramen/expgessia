import app.expgessia.data.entity.TaskEntity
import app.expgessia.data.entity.TaskWithCharacteristic
import app.expgessia.domain.model.RepeatMode
import app.expgessia.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = this.id,
    title = this.title,
    description = this.description,
    characteristicId = this.characteristicId,
    repeatMode = RepeatMode.valueOf(this.repeatMode),
    repeatDetails = this.repeatDetails,
    xpReward = this.xpReward,

)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = this.id,
    title = this.title,
    description = this.description,
    characteristicId = this.characteristicId,
    repeatMode = this.repeatMode.name,
    repeatDetails = this.repeatDetails,
    xpReward = this.xpReward,

)

