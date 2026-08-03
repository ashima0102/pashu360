package com.pashu360.app.feature.health.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.pashu360.app.core.domain.model.HealthEventType
import com.pashu360.app.core.domain.model.HealthRecord
import com.pashu360.app.core.domain.model.Severity
import com.pashu360.app.core.domain.model.Vaccination
import com.pashu360.app.core.domain.model.VetContact
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel(),
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val vaccinationForm by viewModel.vaccinationForm.collectAsStateWithLifecycle()
    val healthEventForm by viewModel.healthEventForm.collectAsStateWithLifecycle()
    val vetContactForm by viewModel.vetContactForm.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Records", "Vaccinations", "Vet Contacts")

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HealthEvent.Saved -> Toast.makeText(
                    context, event.message, Toast.LENGTH_SHORT
                ).show()
                is HealthEvent.ShowError -> Toast.makeText(
                    context, event.message, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── HEADER ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(colors = listOf(PashuGreenLight, PashuGreen))
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                ) {
                    PashuAppBar(
                        title = "Health",
                        alertCount = alertCount,
                        onMenuClick = onMenuClick,
                        onBellClick = onBellClick,
                        onProfileClick = onProfileClick
                    )

                    // Summary chips
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryChip(
                            label = "Overdue",
                            value = "${state.overdueCount}",
                            color = ColorOverdue,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryChip(
                            label = "Due Soon",
                            value = "${state.dueSoonCount}",
                            color = PashuAmber,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryChip(
                            label = "Active Issues",
                            value = "${state.activeIssueCount}",
                            color = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.weight(1f),
                            fgOnLight = PashuGreenDark
                        )
                    }
                }
            }

            // ── TABS ─────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PashuGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // ── CONTENT ──────────────────────
            when (selectedTab) {
                0 -> RecordsTab(state.records, onAddClick = { /* opens sheet in a later PR */ })
                1 -> VaccinationsTab(state.vaccinations, today = state.today.toString())
                2 -> VetContactsTab(state.vetContacts)
            }
        }

        // ── FAB (contextual per-tab) ────────
        ExtendedFloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> viewModel.openHealthEventForm()
                    1 -> viewModel.openVaccinationForm()
                    2 -> viewModel.openVetContactForm()
                }
            },
            containerColor = PashuGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, null) },
            text = {
                Text(
                    when (selectedTab) {
                        0 -> "Log Event"
                        1 -> "Add Vaccine"
                        else -> "Add Contact"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    // ── ADD SHEETS ─────────────────────────────────
    if (vaccinationForm.show) {
        AddVaccinationSheet(
            state = vaccinationForm,
            animals = state.animals,
            onDismiss = viewModel::closeVaccinationForm,
            onAnimalChange = viewModel::onVaccinationAnimalChanged,
            onTemplateChange = viewModel::onVaccinationTemplateChanged,
            onCustomNameChange = viewModel::onCustomVaccineNameChanged,
            onAdministeredDateChange = viewModel::onVaccinationAdministeredDateChanged,
            onNextDueDateChange = viewModel::onVaccinationNextDueDateChanged,
            onFieldChange = viewModel::onVaccinationFieldChanged,
            onSave = viewModel::saveVaccination
        )
    }

    if (healthEventForm.show) {
        AddHealthEventSheet(
            state = healthEventForm,
            animals = state.animals,
            onDismiss = viewModel::closeHealthEventForm,
            onAnimalChange = viewModel::onHealthAnimalChanged,
            onEventTypeChange = viewModel::onEventTypeChanged,
            onSeverityChange = viewModel::onSeverityChanged,
            onSymptomToggle = viewModel::onSymptomToggled,
            onFieldChange = viewModel::onHealthFieldChanged,
            onSave = viewModel::saveHealthEvent
        )
    }

    if (vetContactForm.show) {
        AddVetContactSheet(
            state = vetContactForm,
            onDismiss = viewModel::closeVetContactForm,
            onFieldChange = viewModel::onVetContactFieldChanged,
            onSave = viewModel::saveVetContact
        )
    }
}

// ─────────────────────────────────────────────────────────
// SUMMARY CHIP
// ─────────────────────────────────────────────────────────
@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    fgOnLight: Color = Color.White
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (color == Color.White || color.alpha > 0.9f && color.red > 0.9f) fgOnLight else Color.White
            )
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (color == Color.White || color.alpha > 0.9f && color.red > 0.9f)
                    fgOnLight.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// RECORDS TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun RecordsTab(records: List<HealthRecord>, onAddClick: () -> Unit) {
    if (records.isEmpty()) {
        EmptyPane(
            emoji = "🩺",
            title = "No health records yet",
            subtitle = "Log checkups, diseases, and treatments to build each animal's medical history"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(records, key = { it.id }) { record ->
            HealthRecordCard(record)
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord) {
    val severityColor = when (record.severity) {
        Severity.MILD -> PashuGreen
        Severity.MODERATE -> PashuAmber
        Severity.SEVERE -> ColorSick
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(4.dp, 56.dp).clip(RoundedCornerShape(2.dp))
                    .background(severityColor)
            )
            Spacer(Modifier.width(12.dp))
            Text(record.eventType.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        record.diagnosis?.takeIf { it.isNotBlank() } ?: record.eventType.displayName,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (record.isResolved) {
                        Icon(Icons.Filled.Check, null, tint = PashuGreen,
                            modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    "${record.eventDate} • ${record.severity.displayName}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (record.symptoms.isNotEmpty()) {
                    Text(
                        record.symptoms.joinToString(", "),
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                if (record.medicineName != null) {
                    Text("💊 ${record.medicineName}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// VACCINATIONS TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun VaccinationsTab(vaccinations: List<Vaccination>, today: String) {
    if (vaccinations.isEmpty()) {
        EmptyPane(
            emoji = "💉",
            title = "No vaccinations recorded",
            subtitle = "Add vaccination records to get automatic reminders when the next dose is due"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(vaccinations, key = { it.id }) { vacc ->
            VaccinationCard(vacc, today)
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun VaccinationCard(v: Vaccination, todayIso: String) {
    val (badge, badgeColor) = when {
        v.nextDueDate == null -> "" to Color.Transparent
        v.nextDueDate.toString() < todayIso -> "OVERDUE" to ColorOverdue
        v.nextDueDate.toString() == todayIso -> "DUE TODAY" to PashuAmber
        else -> "" to Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Vaccines, null, tint = PashuGreen,
                    modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(v.vaccineName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    v.diseaseTarget?.let {
                        Text(it, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (badge.isNotEmpty()) {
                    Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
                        Text(badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Given", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v.administeredDate.toString(),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Next Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v.nextDueDate?.toString() ?: "—",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (badge == "OVERDUE") ColorOverdue else MaterialTheme.colorScheme.onSurface)
                }
                v.administeredBy?.let {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("By", fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// VET CONTACTS TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun VetContactsTab(contacts: List<VetContact>) {
    if (contacts.isEmpty()) {
        EmptyPane(
            emoji = "👨‍⚕️",
            title = "No vet contacts yet",
            subtitle = "Save your vet's phone numbers for quick access during emergencies"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            VetContactCard(contact)
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun VetContactCard(c: VetContact) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(PashuGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = PashuGreen,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(c.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(c.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
                c.specialty?.let {
                    Text(it, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_DIAL,
                    android.net.Uri.parse("tel:${c.phone}")
                )
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.Call, "Call", tint = PashuGreen)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// EMPTY STATES
// ─────────────────────────────────────────────────────────
@Composable
private fun EmptyPane(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape)
                .background(PashuGreen.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 56.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}
