package app.expgessia.data.repository

import app.expgessia.data.dao.UserCharacteristicDao
import app.expgessia.data.dao.UserDao
import app.expgessia.data.entity.UserCharacteristicEntity
import app.expgessia.data.mapper.toDomain
import app.expgessia.data.mapper.toEntity
import app.expgessia.domain.model.User
import app.expgessia.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject



class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userCharacteristicDao: UserCharacteristicDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getUserStream().combine(
            userCharacteristicDao.getUserCharacteristicsStream(1) // userId = 1
        ) { userEntity, characteristics ->
            userEntity?.toDomain(characteristics.associate { it.characteristicId to it.value })
        }
    }

    override suspend fun getCurrentUserOnce(): User? {
        val userEntity = userDao.getUser() ?: return null
        val characteristics = userCharacteristicDao.getUserCharacteristics(userEntity.id)
        return userEntity.toDomain(characteristics.associate { it.characteristicId to it.value })
    }

    override suspend fun createUser(user: User) {
        userDao.insertUser(user.toEntity())
        // Создаем записи характеристик
        user.characteristics.forEach { (characteristicId, value) ->
            userCharacteristicDao.insertUserCharacteristic(
                UserCharacteristicEntity(
                    userId = user.id,
                    characteristicId = characteristicId,
                    value = value
                )
            )
        }
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
        // Обновляем характеристики
        user.characteristics.forEach { (characteristicId, value) ->
            userCharacteristicDao.insertUserCharacteristic(
                UserCharacteristicEntity(
                    userId = user.id,
                    characteristicId = characteristicId,
                    value = value
                )
            )
        }
    }

    override suspend fun deleteUser() {
        userCharacteristicDao.deleteUserCharacteristics(1) // userId = 1
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
        updateCharacteristic(1, amount) // characteristicId для strength = 1
    }

    override suspend fun increaseIntelligence(amount: Int) {
        updateCharacteristic(5, amount) // characteristicId для intelligence = 5
    }

    override suspend fun increaseAgility(amount: Int) {
        updateCharacteristic(6, amount) // characteristicId для agility = 6
    }

    private suspend fun updateCharacteristic(characteristicId: Int, amount: Int) {
        val currentUser = getCurrentUserOnce() ?: return
        val currentValue = currentUser.characteristics[characteristicId] ?: 0
        val newCharacteristics = currentUser.characteristics.toMutableMap().apply {
            put(characteristicId, currentValue + amount)
        }
        val updatedUser = currentUser.copy(characteristics = newCharacteristics)
        updateUser(updatedUser)
    }

    override suspend fun levelUpIfPossible() {
        val currentUser = getCurrentUserOnce() ?: return
        val expNeeded = currentUser.level * 100

        if (currentUser.experience >= expNeeded) {
            val newLevel = currentUser.level + 1
            val remainingExp = currentUser.experience - expNeeded

            val newCharacteristics = currentUser.characteristics.toMutableMap().apply {
                // Увеличиваем все основные характеристики при повышении уровня
                put(1, (get(1) ?: 0) + 1) // strength
                put(5, (get(5) ?: 0) + 1) // intelligence
                put(6, (get(6) ?: 0) + 1) // agility
                put(7, (get(7) ?: 0) + 1) // luck
            }

            val updatedUser = currentUser.copy(
                level = newLevel,
                experience = remainingExp,
                characteristics = newCharacteristics
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