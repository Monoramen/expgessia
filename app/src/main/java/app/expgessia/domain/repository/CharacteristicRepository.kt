package app.expgessia.domain.repository

import app.expgessia.domain.model.Characteristic
import kotlinx.coroutines.flow.Flow


interface CharacteristicRepository {

    fun getAllCharacteristics(): Flow<List<Characteristic>>

    suspend fun getCharacteristicById(id: Int): Characteristic?


}
