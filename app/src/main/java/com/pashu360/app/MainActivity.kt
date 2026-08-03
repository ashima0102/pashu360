package com.pashu360.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pashu360.app.core.presentation.navigation.Pashu360NavHost
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sole activity. Handles cold-start deep links (`pashu360://animal/{id}`)
 * fired by notification tap and hands them to the nav host to consume.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<DeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = parseDeepLink(intent)

        setContent {
            Pashu360Theme {
                Pashu360NavHost(
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseDeepLink(intent)?.let { pendingDeepLink = it }
    }

    private fun parseDeepLink(intent: Intent?): DeepLink? {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return null
        val uri: Uri = intent.data ?: return null
        if (uri.scheme != "pashu360") return null
        return when (uri.host) {
            "animal" -> uri.pathSegments.firstOrNull()?.let { DeepLink.Animal(it) }
            else -> null
        }
    }
}

sealed class DeepLink {
    data class Animal(val animalId: String) : DeepLink()
}
