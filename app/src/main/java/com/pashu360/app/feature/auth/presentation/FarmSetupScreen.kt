package com.pashu360.app.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@Composable
fun FarmSetupScreen(
    onFarmCreated: () -> Unit
) {
    var farmName by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var animalCount by remember { mutableStateOf("") }

    val isValid = farmName.isNotBlank() && village.isNotBlank()

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
            // ── HERO ────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = statusBarPadding.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Agriculture,
                        contentDescription = null,
                        tint = PashuGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Welcome badge
                Surface(
                    color = PashuAmber.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "🎉 Welcome to the family!",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Let's set up your farm",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "This takes less than a minute",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(32.dp))
            }

            // ── FORM CARD ───────────────
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
                    // Progress dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProgressDot(active = true)
                        Spacer(Modifier.size(6.dp))
                        ProgressDot(active = false)
                        Spacer(Modifier.size(6.dp))
                        ProgressDot(active = false)
                    }
                    Spacer(Modifier.height(24.dp))

                    Text("Farm Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("These help us personalize your dashboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp))

                    Spacer(Modifier.height(24.dp))

                    Field(
                        label = "Farm Name",
                        value = farmName,
                        onChange = { farmName = it },
                        placeholder = "Sharma Dairy Farm",
                        leading = { Icon(Icons.Filled.Agriculture, null, tint = PashuGreen) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Field(
                        label = "Village / City",
                        value = village,
                        onChange = { village = it },
                        placeholder = "Bhubaneswar",
                        leading = { Icon(Icons.Filled.LocationOn, null, tint = PashuGreen) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Field(
                        label = "State (optional)",
                        value = state,
                        onChange = { state = it },
                        placeholder = "Odisha"
                    )

                    Spacer(Modifier.height(16.dp))

                    Field(
                        label = "How many animals? (approx)",
                        value = animalCount,
                        onChange = { animalCount = it.filter { c -> c.isDigit() }.take(4) },
                        placeholder = "25",
                        leading = { Icon(Icons.Filled.Pets, null, tint = PashuGreen) },
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onFarmCreated,
                        enabled = isValid,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PashuGreen,
                            disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("Start Managing My Farm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.size(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "You can edit these anytime in Settings",
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
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            leadingIcon = leading,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        )
    }
}

@Composable
private fun ProgressDot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 32.dp else 8.dp, 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) PashuGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

@Preview(showBackground = true, name = "Farm Setup — Light")
@Composable
private fun FarmSetupPreview() {
    Pashu360Theme(dynamicColor = false) {
        FarmSetupScreen(onFarmCreated = {})
    }
}
