package com.pashu360.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import com.pashu360.app.core.data.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val sessionStore: SessionStore
) : ViewModel() {

    /** Stash the owner name so FarmSetupScreen can pre-fill it. */
    fun stashOwnerName(name: String) {
        sessionStore.setPendingOwnerName(name)
    }
}
