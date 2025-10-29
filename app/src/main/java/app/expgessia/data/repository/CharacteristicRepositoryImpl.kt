package app.expgessia.data.repository

import app.expgessia.data.dao.CharacteristicDao
import app.expgessia.data.mapper.toDomain
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.repository.CharacteristicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CharacteristicRepositoryImpl @Inject constructor(
    private val characteristicDao: CharacteristicDao,
) : CharacteristicRepository {

    override fun getAllCharacteristics(): Flow<List<Characteristic>> {

        return characteristicDao.getAllCharacteristics().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCharacteristicById(id: Int): Characteristic? {
        return characteristicDao.getCharacteristicById(id)?.toDomain()
    }
}
