package com.pashu360.app.feature.farm.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@Composable
fun FarmInfoScreen(
    viewModel: FarmInfoViewModel = hiltViewModel(),
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FarmInfoEvent.Saved ->
                    Toast.makeText(context, "Farm info saved", Toast.LENGTH_SHORT).show()
                is FarmInfoEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.verticalGradient(colors = listOf(PashuGreenLight, PashuGreen))
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                PashuAppBar(
                    title = "Farm Info",
                    alertCount = alertCount,
                    onMenuClick = onMenuClick,
                    onBellClick = onBellClick,
                    onProfileClick = onProfileClick
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Edit your farm details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            InfoField(
                label = "Your Name",
                value = state.ownerName,
                onChange = viewModel::onOwnerNameChanged,
                placeholder = "Ramesh Sharma",
                leading = Icons.Filled.Person
            )
            Spacer(Modifier.height(12.dp))
            InfoField(
                label = "Farm Name",
                value = state.farmName,
                onChange = viewModel::onFarmNameChanged,
                placeholder = "Sharma Dairy Farm",
                leading = Icons.Filled.Agriculture
            )
            Spacer(Modifier.height(12.dp))
            InfoField(
                label = "Village / City",
                value = state.village,
                onChange = viewModel::onVillageChanged,
                placeholder = "Bhubaneswar",
                leading = Icons.Filled.LocationOn
            )
            Spacer(Modifier.height(12.dp))
            InfoField(
                label = "State (optional)",
                value = state.state,
                onChange = viewModel::onStateChanged,
                placeholder = "Odisha"
            )
            Spacer(Modifier.height(12.dp))
            InfoField(
                label = "Expected herd size (optional)",
                value = state.expectedHerdSize,
                onChange = viewModel::onHerdSizeChanged,
                placeholder = "25",
                leading = Icons.Filled.Pets,
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PashuGreen,
                    disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Check, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = leading?.let { { Icon(it, null, tint = PashuGreen) } },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )
    }
}
