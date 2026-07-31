package com.pashu360.app.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSetupScreen(
    onFarmCreated: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Setup Your Farm") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Farm setup form coming in next commit", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onFarmCreated) { Text("Skip → Dashboard") }
        }
    }
}
