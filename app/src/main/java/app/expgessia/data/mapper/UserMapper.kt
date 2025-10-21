package app.expgessia.data.mapper

import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    money = this.money,
    strength = this.strength,
    perception = this.perception,
    endurance = this.endurance,
    charisma = this.charisma,
    intelligence = this.intelligence,
    agility = this.agility,

    luck = this.luck,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    money = this.money,
    strength = this.strength,
    perception = this.perception,
    endurance = this.endurance,
    charisma = this.charisma,
    intelligence = this.intelligence,
    agility = this.agility,
    luck = this.luck,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)