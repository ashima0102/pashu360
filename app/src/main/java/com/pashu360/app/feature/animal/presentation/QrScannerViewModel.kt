package com.pashu360.app.feature.animal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.feature.animal.domain.usecase.GetAnimalByQrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QrScannerEvent {
    data class NavigateToAnimal(val animalId: String) : QrScannerEvent()
    data class NotFound(val code: String) : QrScannerEvent()
}

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val getAnimalByQrUseCase: GetAnimalByQrUseCase,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _events = Channel<QrScannerEvent>()
    val events = _events.receiveAsFlow()

    private var lastScanned: String? = null

    fun onCodeScanned(code: String) {
        if (code == lastScanned) return
        lastScanned = code

        viewModelScope.launch {
            val animal = getAnimalByQrUseCase(sessionStore.getActiveFarmId(), code)
            if (animal != null) {
                _events.send(QrScannerEvent.NavigateToAnimal(animal.id))
            } else {
                _events.send(QrScannerEvent.NotFound(code))
                // Allow re-scan of same code after 2 seconds
                kotlinx.coroutines.delay(2000)
                lastScanned = null
            }
        }
    }
}
