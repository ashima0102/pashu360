package com.pashu360.app.feature.health.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.HealthEventType
import com.pashu360.app.core.domain.model.Severity
import com.pashu360.app.core.domain.model.SymptomCatalog
import com.pashu360.app.core.domain.model.VaccineCatalog
import com.pashu360.app.core.domain.model.VaccineTemplate
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// ─────────────────────────────────────────────────────────
// ADD VACCINATION SHEET
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccinationSheet(
    state: VaccinationFormState,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onAnimalChange: (String) -> Unit,
    onTemplateChange: (VaccineTemplate?) -> Unit,
    onCustomNameChange: (String) -> Unit,
    onAdministeredDateChange: (LocalDate) -> Unit,
    onNextDueDateChange: (LocalDate?) -> Unit,
    onFieldChange: (VaccinationField, String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("💉 Add Vaccination", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            AnimalPicker(
                animals = animals,
                selectedId = state.animalId,
                onSelect = onAnimalChange,
                label = "Animal"
            )

            Spacer(Modifier.height(16.dp))

            Text("Vaccine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VaccineCatalog.templates.forEach { template ->
                    Surface(
                        onClick = { onTemplateChange(template) },
                        color = if (state.template == template) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.template == template) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            template.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = if (state.template == template) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.customVaccineName,
                onValueChange = onCustomNameChange,
                label = { Text("Or type custom vaccine") },
                enabled = state.template == null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            state.template?.disease?.let {
                Spacer(Modifier.height(4.dp))
                Text("↳ Protects against: $it",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatePickerField(
                    label = "Given on",
                    date = state.administeredDate,
                    onDateChange = onAdministeredDateChange,
                    modifier = Modifier.weight(1f)
                )
                DatePickerField(
                    label = "Next due (auto)",
                    date = state.nextDueDate,
                    onDateChange = { onNextDueDateChange(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.administeredBy,
                onValueChange = { onFieldChange(VaccinationField.ADMIN_BY, it) },
                label = { Text("Administered by (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.batchNumber,
                    onValueChange = { onFieldChange(VaccinationField.BATCH, it) },
                    label = { Text("Batch #") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.cost,
                    onValueChange = { onFieldChange(VaccinationField.COST, it) },
                    label = { Text("Cost ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            SaveButton(
                enabled = state.isValid && !state.isSaving,
                loading = state.isSaving,
                onClick = onSave
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────
// ADD HEALTH EVENT SHEET
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthEventSheet(
    state: HealthEventFormState,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onAnimalChange: (String) -> Unit,
    onEventTypeChange: (HealthEventType) -> Unit,
    onSeverityChange: (Severity) -> Unit,
    onSymptomToggle: (String) -> Unit,
    onFieldChange: (HealthField, String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("🩺 Log Health Event", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            AnimalPicker(
                animals = animals,
                selectedId = state.animalId,
                onSelect = onAnimalChange,
                label = "Animal"
            )

            Spacer(Modifier.height(16.dp))

            // Event type chips
            Text("Event Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthEventType.entries.forEach { type ->
                    Surface(
                        onClick = { onEventTypeChange(type) },
                        color = if (state.eventType == type) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.eventType == type) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                type.displayName,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (state.eventType == type) Color.White
                                        else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Symptoms multi-select
            Text("Symptoms", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Tap all that apply", fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRowSymptoms(
                selected = state.selectedSymptoms,
                onToggle = onSymptomToggle
            )

            Spacer(Modifier.height(16.dp))

            // Severity
            Text("Severity", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Severity.entries.forEach { sev ->
                    val color = when (sev) {
                        Severity.MILD -> PashuGreen
                        Severity.MODERATE -> PashuAmber
                        Severity.SEVERE -> ColorSick
                    }
                    Surface(
                        onClick = { onSeverityChange(sev) },
                        color = if (state.severity == sev) color
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.severity == sev) color
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            sev.displayName,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (state.severity == sev) Color.White
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.diagnosis,
                onValueChange = { onFieldChange(HealthField.DIAGNOSIS, it) },
                label = { Text("Diagnosis (optional)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                minLines = 2, maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.medicineName,
                    onValueChange = { onFieldChange(HealthField.MEDICINE_NAME, it) },
                    label = { Text("Medicine") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = state.medicineDose,
                    onValueChange = { onFieldChange(HealthField.MEDICINE_DOSE, it) },
                    label = { Text("Dose") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.vetName,
                    onValueChange = { onFieldChange(HealthField.VET_NAME, it) },
                    label = { Text("Vet name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(2f)
                )
                OutlinedTextField(
                    value = state.cost,
                    onValueChange = { onFieldChange(HealthField.COST, it) },
                    label = { Text("Cost ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            SaveButton(
                enabled = state.isValid && !state.isSaving,
                loading = state.isSaving,
                onClick = onSave
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────
// ADD VET CONTACT SHEET
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVetContactSheet(
    state: VetContactFormState,
    onDismiss: () -> Unit,
    onFieldChange: (VetField, String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("👨‍⚕️ Add Vet Contact", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { onFieldChange(VetField.NAME, it) },
                label = { Text("Vet's Name *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = { onFieldChange(VetField.PHONE, it) },
                label = { Text("Phone number *") },
                leadingIcon = {
                    Text("🇮🇳 +91 ", modifier = Modifier.padding(start = 12.dp),
                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.specialty,
                onValueChange = { onFieldChange(VetField.SPECIALTY, it) },
                label = { Text("Specialty (optional)") },
                placeholder = { Text("e.g. Cattle, Reproduction, Surgery") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.clinic,
                onValueChange = { onFieldChange(VetField.CLINIC, it) },
                label = { Text("Clinic (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            SaveButton(
                enabled = state.isValid && !state.isSaving,
                loading = state.isSaving,
                onClick = onSave
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────
// SHARED COMPONENTS
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalPicker(
    animals: List<Animal>,
    selectedId: String,
    onSelect: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = animals.find { it.id == selectedId }
    val display = selected?.displayName ?: "Select an animal"

    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = display,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select an animal") },
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
                if (animals.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No active animals — add one in the Animals tab") },
                        onClick = { expanded = false }
                    )
                }
                animals.forEach { animal ->
                    DropdownMenuItem(
                        text = {
                            Text("${animal.displayName} • #${animal.tagId}",
                                fontWeight = FontWeight.Medium)
                        },
                        onClick = {
                            onSelect(animal.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = date?.toString() ?: "—",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.CalendarToday, null,
                        tint = PashuGreen, modifier = Modifier.size(18.dp))
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
            modifier = Modifier.fillMaxWidth().clickable { showDialog = true }
        )
    }

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
                        val picked = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        onDateChange(picked)
                    }
                    showDialog = false
                }) {
                    Text("OK", color = PashuGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/** Simple symptom flow layout — 3 columns of chips. */
@Composable
private fun FlowRowSymptoms(
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SymptomCatalog.symptoms.forEach { s ->
            val isSelected = s in selected
            Surface(
                onClick = { onToggle(s) },
                color = if (isSelected) PashuGreen else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) PashuGreen
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    s,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PashuGreen,
                disabledContainerColor = PashuGreen.copy(alpha = 0.4f)
            )
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp),
                    color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Check, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Save", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
