package app.expgessia.data.mapper

import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    gold = this.gold,
    energy = this.mana,
    strength = this.strength,
    intelligence = this.intelligence,
    agility = this.agility,
    perception = this.perception,
    luck = this.luck,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    gold = this.gold,
    mana = this.energy,
    strength = this.strength,
    intelligence = this.intelligence,
    agility = this.agility,
    perception = this.perception,
    luck = this.luck,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)