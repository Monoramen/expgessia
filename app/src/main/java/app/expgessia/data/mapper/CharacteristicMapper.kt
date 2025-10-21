package app.expgessia.data.mapper

import app.expgessia.data.entity.CharacteristicEntity
import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.model.User

fun CharacteristicEntity.toDomain(): Characteristic = Characteristic(
    id = this.id,
    name = this.name,
    description = this.description,
    iconResName = this.iconResName
)

fun Characteristic.toEntity(): CharacteristicEntity = CharacteristicEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    iconResName = this.iconResName
)
