package app.expgessia.data.mapper

import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    score = this.score,
    mana = this.mana,
    strength = this.strength,
    intelligence = this.intelligence,
    agility = this.agility,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    score = this.score,
    mana = this.mana,
    strength = this.strength,
    intelligence = this.intelligence,
    agility = this.agility,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)