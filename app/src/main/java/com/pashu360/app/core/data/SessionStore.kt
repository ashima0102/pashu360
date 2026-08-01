package com.pashu360.app.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the active farm ID for the current user session.
 * Later, this will be backed by DataStore + Supabase auth session.
 * For now, uses a hardcoded demo farm so we can build UI + local DB.
 */
@Singleton
class SessionStore @Inject constructor() {
    private val _activeFarmId = MutableStateFlow(DEMO_FARM_ID)
    val activeFarmId: StateFlow<String> = _activeFarmId

    // In-memory handoff from RegisterScreen → FarmSetupScreen so the owner
    // name typed in "Full Name" pre-fills the setup wizard. Not persisted;
    // cleared once Farm row is written.
    private var pendingOwnerName: String = ""

    fun setActiveFarmId(id: String) {
        _activeFarmId.value = id
    }

    fun getActiveFarmId(): String = _activeFarmId.value

    fun setPendingOwnerName(name: String) { pendingOwnerName = name.trim() }
    fun consumePendingOwnerName(): String {
        val v = pendingOwnerName
        pendingOwnerName = ""
        return v
    }

    companion object {
        // Fixed farm ID until Supabase auth wires in the real farmer's farm.
        // Room stores everything under this ID so data survives app restart.
        const val DEMO_FARM_ID = "demo-farm-local"
    }
}
