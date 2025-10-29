package app.expgessia.data.repository

import app.expgessia.data.dao.UserDao
import app.expgessia.data.mapper.toDomain
import app.expgessia.data.mapper.toEntity
import app.expgessia.domain.model.User
import app.expgessia.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getUserStream().map { it?.toDomain() }
    }

    override suspend fun getCurrentUserOnce(): User? {
        return userDao.getUser()?.toDomain()
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
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
                luck = currentUser.luck + 1
            )

            updateUser(updatedUser)
        }
    }

    override suspend fun updateUserName(newName: String) {
        val currentUser = getCurrentUserOnce() ?: return
        val updatedUser = currentUser.copy(name = newName)
        updateUser(updatedUser)
    }


    override suspend fun updateLastLogin(lastLogin: Long) {
        val currentUser = getCurrentUserOnce() ?: return
        val updatedUser = currentUser.copy(lastLogin = lastLogin)
        userDao.updateUser(updatedUser.toEntity())
    }

    override fun getLastLogin(): Flow<Long?> {
        return userDao.getUserStream().map { userEntity ->
            userEntity?.lastLogin
        }
    }
}