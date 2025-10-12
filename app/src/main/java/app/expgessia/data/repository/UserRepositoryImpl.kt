package app.expgessia.data.repository

import androidx.compose.material3.DatePicker
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.UserEntity
import app.expgessia.data.mapper.toDomain
import app.expgessia.data.mapper.toEntity
import app.expgessia.domain.model.User
import app.expgessia.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    // ✅ Правильно: используем только Room Flow
    override fun getCurrentUser(): Flow<User?> {
        return userDao.getUserStream().map { it?.toDomain() }
    }

    override suspend fun ensureDefaultUserExists() {
        println("ensureDefaultUserExists called")

        val currentUser = userDao.getUser()
        println("Current user from DB: $currentUser")

        if (currentUser == null) {
            println("Creating default user...")
            val defaultUser = UserEntity(
                name = "Новый игрок",
                experience = 0,
                level = 1,
                score = 0,
                mana = 50,
                strength = 5,
                intelligence = 5,
                agility = 5,
                lastLogin = null, // ← Временно!
                photoUri = null
            )
            userDao.insertUser(defaultUser)
            println("Default user inserted")
        } else {
            println("User already exists, no action needed")
        }
    }

    override suspend fun getCurrentUserOnce(): User? {
        return userDao.getUser()?.toDomain()
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
        // ✅ Room Flow автоматически обновится после изменения БД
    }

    override suspend fun deleteUser() {
        userDao.deleteUser()
    }

    override suspend fun addExperience(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        val newExperience = currentUser.experience + amount
        val updatedUser = currentUser.copy(experience = newExperience)
        updateUser(updatedUser)
        levelUpIfPossible()
    }

    override suspend fun addScore(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        val newScore = currentUser.score + amount
        updateUser(currentUser.copy(score = newScore))
    }

    override suspend fun updateMana(value: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        updateUser(currentUser.copy(mana = value))
    }

    override suspend fun updateHealth(value: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun increaseStrength(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        updateUser(currentUser.copy(strength = currentUser.strength + amount))
    }

    override suspend fun increaseIntelligence(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        updateUser(currentUser.copy(intelligence = currentUser.intelligence + amount))
    }

    override suspend fun increaseAgility(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        updateUser(currentUser.copy(agility = currentUser.agility + amount))
    }

    override suspend fun restoreMana(amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        updateUser(currentUser.copy(mana = currentUser.mana + amount))
    }

    override suspend fun hasEnoughMana(required: Int): Boolean {
        val currentUser = getCurrentUserOnce() ?: return false
        return currentUser.mana >= required
    }

    override suspend fun levelUpIfPossible() {
        val currentUser = getCurrentUserOnce() ?: return
        val expNeeded = currentUser.level * 100

        if (currentUser.experience >= expNeeded) {
            val newLevel = currentUser.level + 1
            val remainingExp = currentUser.experience - expNeeded

            val updatedUser = currentUser.copy(
                level = newLevel,
                experience = remainingExp,
                strength = currentUser.strength + 1,
                intelligence = currentUser.intelligence + 1,
                agility = currentUser.agility + 1,
                mana = currentUser.mana + 5
            )

            updateUser(updatedUser)
        }
    }
// В UserRepositoryImpl.kt

    override suspend fun updateUserName(newName: String) {
        val currentUser = getCurrentUserOnce() ?: return
        val updatedUser = currentUser.copy(name = newName)
        updateUser(updatedUser) // Используем уже существующий updateUser, который обновляет всю сущность
    }
    // ✅ Уберите refreshUser - он больше не нужен
    // Room Flow автоматически обновляется при изменениях в БД
}