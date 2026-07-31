package com.pashu360.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && phone.length == 10 && password.length >= 8

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PashuGreenLight, PashuGreen, PashuGreenDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // ── HEADER ────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarPadding.calculateTopPadding())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // ── HERO ─────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = PashuGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Join Pashu360", fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text("Register your farm in 30 seconds",
                    fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(28.dp))
            }

            // ── FORM CARD ────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Text("Create your account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("Free forever for up to 10 animals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp))

                    Spacer(Modifier.height(24.dp))

                    LabeledField("Your Name") {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Ramesh Sharma") },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = PashuGreen) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    LabeledField("Phone Number") {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("98765 43210") },
                            leadingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.size(12.dp))
                                    Text("🇮🇳 +91", fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.size(8.dp))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    LabeledField("Email (optional)") {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("you@example.com") },
                            leadingIcon = { Icon(Icons.Filled.Email, null, tint = PashuGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    LabeledField("Password") {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("At least 8 characters") },
                            leadingIcon = { Icon(Icons.Filled.Lock, null, tint = PashuGreen) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = onRegistered,
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PashuGreen,
                            disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("Create Account", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.size(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White,
                            modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "By registering, you agree to our Terms & Privacy Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already registered? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onBack, contentPadding = PaddingValues(4.dp)) {
                            Text("Sign in", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = PashuGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Preview(showBackground = true, name = "Register — Light")
@Composable
private fun RegisterScreenPreview() {
    Pashu360Theme(dynamicColor = false) {
        RegisterScreen(onRegistered = {}, onBack = {})
    }
}
