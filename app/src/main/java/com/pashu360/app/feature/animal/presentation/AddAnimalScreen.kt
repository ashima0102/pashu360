package com.pashu360.app.feature.animal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pashu360.app.core.domain.model.BreedCatalog
import com.pashu360.app.core.domain.model.Gender
import com.pashu360.app.core.presentation.theme.PashuGreen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalScreen(
    viewModel: AddAnimalViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddAnimalEvent.Saved -> {
                    Toast.makeText(context, "Animal added!", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                is AddAnimalEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Animal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.onSubmit() },
                    enabled = uiState.isValid && !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PashuGreen,
                        disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(Modifier.size(20.dp),
                            color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Check, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Animal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Photo placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PashuGreen.copy(alpha = 0.08f))
                    .clickable { /* TODO: pick photo */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Pets, null,
                        tint = PashuGreen, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Add Photo (optional)",
                        fontSize = 13.sp,
                        color = PashuGreen,
                        fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionHeading("Identification")

            InputBlock(label = "Tag ID *") {
                OutlinedTextField(
                    value = uiState.tagId,
                    onValueChange = viewModel::onTagIdChanged,
                    placeholder = { Text("Auto-generated (editable)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            InputBlock(label = "Animal Name (optional)") {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    placeholder = { Text("Gouri, Rani, Lakshmi...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionHeading("Details")

            InputBlock(label = "Breed") {
                BreedDropdown(
                    selected = uiState.breed,
                    onSelected = viewModel::onBreedChanged
                )
            }

            InputBlock(label = "Gender") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GenderChoice("Female", uiState.gender == Gender.FEMALE,
                        Modifier.weight(1f)) { viewModel.onGenderChanged(Gender.FEMALE) }
                    GenderChoice("Male", uiState.gender == Gender.MALE,
                        Modifier.weight(1f)) { viewModel.onGenderChanged(Gender.MALE) }
                }
            }

            InputBlock(label = "Date of Birth") {
                DobPicker(
                    dob = uiState.dob,
                    onDobChanged = viewModel::onDobChanged
                )
            }

            InputBlock(label = "Weight (kg)") {
                OutlinedTextField(
                    value = uiState.weight,
                    onValueChange = viewModel::onWeightChanged,
                    placeholder = { Text("420") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionHeading("Purchase Info (optional)")

            InputBlock(label = "Purchase Price (₹)") {
                OutlinedTextField(
                    value = uiState.purchasePrice,
                    onValueChange = viewModel::onPurchasePriceChanged,
                    placeholder = { Text("35000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            InputBlock(label = "Notes") {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    placeholder = { Text("Any notes about this animal...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            uiState.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = PashuGreen,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun InputBlock(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp))
        content()
    }
}

@Composable
private fun GenderChoice(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) PashuGreen else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (selected) PashuGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 14.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select breed") },
            trailingIcon = { Icon(Icons.Filled.ExpandMore, null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BreedCatalog.breeds.forEach { breed ->
                DropdownMenuItem(
                    text = { Text(breed) },
                    onClick = {
                        onSelected(breed)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DobPicker(dob: LocalDate?, onDobChanged: (LocalDate?) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dob?.toString() ?: "",
        onValueChange = {},
        readOnly = true,
        placeholder = { Text("DD/MM/YYYY") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.CalendarToday, null, tint = PashuGreen)
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
        modifier = Modifier.fillMaxWidth().clickable { showDialog = true }
    )

    if (showDialog) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        @OptIn(ExperimentalTime::class)
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        onDobChanged(date)
                    }
                    showDialog = false
                }) { Text("OK", color = PashuGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
