package com.pashu360.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pashu360.app.core.presentation.navigation.Pashu360NavHost
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pashu360Theme {
                Pashu360NavHost()
            }
        }
    }
}
