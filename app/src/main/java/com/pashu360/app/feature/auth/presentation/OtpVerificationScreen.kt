package com.pashu360.app.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phone: String,
    onVerified: (isFirstLogin: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var resendTimer by remember { mutableStateOf(60) }

    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
    }

    // TODO: Wire to Supabase Auth verifyOtp
    LaunchedEffect(otp) {
        if (otp.length == 6) {
            delay(500)
            onVerified(true) // first login → go to farm setup
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Enter OTP", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Sent to $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                label = { Text("6-digit OTP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            if (resendTimer > 0) {
                Text("Resend OTP in ${resendTimer}s",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                TextButton(onClick = { resendTimer = 60 }) { Text("Resend OTP") }
            }
        }
    }
}
