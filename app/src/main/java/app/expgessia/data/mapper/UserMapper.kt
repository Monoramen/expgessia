// UserMapper.kt
package app.expgessia.data.mapper

import app.expgessia.data.entity.UserEntity
import app.expgessia.domain.model.User

fun UserEntity.toDomain(characteristics: Map<Int, Int> = emptyMap()): User = User(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    money = this.money,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri,
    characteristics = characteristics
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    experience = this.experience,
    level = this.level,
    money = this.money,
    lastLogin = this.lastLogin,
    photoUri = this.photoUri
)