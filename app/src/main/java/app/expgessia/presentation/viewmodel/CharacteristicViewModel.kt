package app.expgessia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.expgessia.domain.model.Characteristic
import app.expgessia.domain.repository.CharacteristicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel для управления данными и состоянием характеристик пользователя.
 * Использует Hilt для инъекции зависимостей.
 */
@HiltViewModel
class CharacteristicViewModel @Inject constructor(
    private val repository: CharacteristicRepository
) : ViewModel() {

    /**
     * Состояние, содержащее список характеристик.
     * Использует StateFlow для наблюдения за данными в реальном времени.
     */
    val characteristics: StateFlow<List<Characteristic>> = repository
        .getAllCharacteristics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Начальное значение, пока данные загружаются
        )
}
