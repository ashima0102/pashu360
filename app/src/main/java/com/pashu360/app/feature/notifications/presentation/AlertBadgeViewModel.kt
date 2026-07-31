package com.pashu360.app.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pashu360.app.core.data.SessionStore
import com.pashu360.app.feature.notifications.domain.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Tiny always-on VM that top-level screens observe to drive the header bell
 * badge count. Kept separate from AlertsViewModel so the badge stays live
 * on Dashboard/Animals/Milk/Health/Finance without spinning up the full
 * alerts feature ViewModel on each of those screens.
 */
@HiltViewModel
class AlertBadgeViewModel @Inject constructor(
    repository: AlertRepository,
    sessionStore: SessionStore
) : ViewModel() {
    val unresolvedCount: StateFlow<Int> = repository
        .countUnresolved(sessionStore.getActiveFarmId())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
