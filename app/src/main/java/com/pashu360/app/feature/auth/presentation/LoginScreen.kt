package com.pashu360.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var useEmailLogin by remember { mutableStateOf(false) }

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
            // ── HERO SECTION ─────────────────────────────────
            HeroHeader(
                statusBarPadding = statusBarPadding.calculateTopPadding()
            )

            // ── WHITE CARD WITH FORM ────────────────────────
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
                    Text(
                        text = "Welcome back 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (useEmailLogin) "Sign in with your email"
                               else "Sign in with your phone number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    if (!useEmailLogin) {
                        PhoneLoginBlock(
                            phone = phone,
                            onPhoneChange = { new ->
                                if (new.length <= 10) phone = new.filter { it.isDigit() }
                            },
                            onContinue = { onNavigateToOtp("+91$phone") }
                        )
                    } else {
                        EmailLoginBlock(
                            email = email,
                            password = password,
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            onLogin = onLoginSuccess
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── OR DIVIDER ────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Text(
                            "  or  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── ALTERNATE LOGIN METHOD ───────────────
                    OutlinedButton(
                        onClick = { useEmailLogin = !useEmailLogin },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            if (useEmailLogin) Icons.Filled.Phone else Icons.Filled.Email,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (useEmailLogin) "Continue with Phone" else "Continue with Email",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── REGISTER PROMPT ──────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "New to Pashu360? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                "Register free",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PashuGreen
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Terms
                    Text(
                        text = "By continuing you agree to our Terms & Privacy Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(statusBarPadding: androidx.compose.ui.unit.Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Logo circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = PashuGreen,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Pashu360",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Your Farm, Smarter",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun PhoneLoginBlock(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Text(
        text = "Phone Number",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        placeholder = { Text("98765 43210", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "🇮🇳 +91",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.size(8.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .height(24.dp)
                        .size(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(Modifier.size(8.dp))
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PashuGreen,
            focusedLeadingIconColor = PashuGreen
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    )

    Spacer(Modifier.height(20.dp))

    Button(
        onClick = { if (phone.length == 10) onContinue() },
        enabled = phone.length == 10,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PashuGreen,
            disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
        )
    ) {
        Text(
            "Continue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.size(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmailLoginBlock(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Text(
        text = "Email",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text("you@example.com") },
        leadingIcon = { Icon(Icons.Filled.Email, null, tint = PashuGreen) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
        modifier = Modifier.fillMaxWidth().height(64.dp)
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = "Password",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("At least 8 characters") },
        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = PashuGreen) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
        modifier = Modifier.fillMaxWidth().height(64.dp)
    )

    Spacer(Modifier.height(8.dp))
    val ctx = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = {
            Toast.makeText(ctx,
                "Password reset needs a backend connection — coming with cloud sync.",
                Toast.LENGTH_LONG).show()
        }) {
            Text("Forgot password?", color = PashuGreen, fontWeight = FontWeight.Medium)
        }
    }

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = {
            // Backend auth lands with Phase 10 Supabase — until then the
            // email/password combo isn't verified against any server. Warn
            // the farmer but let them in so local-only testing works.
            Toast.makeText(ctx,
                "Demo mode: password not verified. Real auth arrives with cloud sync.",
                Toast.LENGTH_LONG).show()
            onLogin()
        },
        enabled = email.isNotBlank() && password.length >= 8,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PashuGreen,
            disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
        )
    ) {
        Text("Sign In", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Preview(showBackground = true, name = "Login — Light")
@Composable
private fun LoginScreenPreview() {
    Pashu360Theme(dynamicColor = false) {
        LoginScreen(
            onNavigateToOtp = {},
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}

@Preview(showBackground = true, name = "Login — Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenDarkPreview() {
    Pashu360Theme(darkTheme = true, dynamicColor = false) {
        LoginScreen(
            onNavigateToOtp = {},
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}
