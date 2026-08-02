package com.pashu360.app.feature.breeding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.BreedingRecordDetail
import com.pashu360.app.core.domain.model.BreedingType
import com.pashu360.app.core.domain.model.CalvingOutcome
import com.pashu360.app.core.domain.model.ConceptionStatus
import com.pashu360.app.core.domain.model.Gender
import com.pashu360.app.core.domain.model.HeatIntensity
import com.pashu360.app.core.domain.model.HeatRecordDetail
import com.pashu360.app.core.domain.model.HeatSymptomCatalog
import com.pashu360.app.core.domain.model.PdMethod
import com.pashu360.app.core.domain.model.PregnancyDetail
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.ColorPregnant
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BreedingScreen(
    viewModel: BreedingViewModel = hiltViewModel(),
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val heatForm by viewModel.heatForm.collectAsStateWithLifecycle()
    val breedingForm by viewModel.breedingForm.collectAsStateWithLifecycle()
    val pregnancyForm by viewModel.pregnancyForm.collectAsStateWithLifecycle()
    val calvingForm by viewModel.calvingForm.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Heat", "Mating", "Pregnancy", "Calving")

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BreedingEvent.Saved ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is BreedingEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        title = "Breeding",
                        alertCount = alertCount,
                        onMenuClick = onMenuClick,
                        onBellClick = onBellClick,
                        onProfileClick = onProfileClick
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Chip("Pregnant", "${state.activePregnancyCount}",
                            ColorPregnant, Modifier.weight(1f))
                        Chip("Calving Soon", "${state.calvingDueThisMonth}",
                            PashuAmber, Modifier.weight(1f))
                        Chip("Awaiting PD", "${state.awaitingPd.size}",
                            Color.White.copy(alpha = 0.95f),
                            Modifier.weight(1f), PashuGreenDark)
                    }
                }
            }

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
                            Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold
                                                    else FontWeight.Medium)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> HeatTab(state.heatRecords, state.today)
                1 -> MatingTab(state.breedingRecords, state.awaitingPd,
                    onConfirmedClick = { viewModel.markConceptionStatus(it, ConceptionStatus.CONFIRMED) },
                    onFailedClick = { viewModel.markConceptionStatus(it, ConceptionStatus.FAILED) })
                2 -> PregnancyTab(
                    state.activePregnancies,
                    state.today,
                    onCalveClick = { pd ->
                        viewModel.openCalvingForm(pd.pregnancy.id,
                            pd.animalName ?: "Tag #${pd.animalTag}")
                    }
                )
                3 -> CalvingTab(state.completedPregnancies)
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> viewModel.openHeatForm()
                    1 -> viewModel.openBreedingForm()
                    2 -> viewModel.openPregnancyForm()
                    3 -> Toast.makeText(context,
                        "Open Pregnancy tab → tap 'Log Calving' on an active pregnancy",
                        Toast.LENGTH_LONG).show()
                }
            },
            containerColor = PashuGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, null) },
            text = {
                Text(when (selectedTab) {
                    0 -> "Log Heat"
                    1 -> "Log Mating"
                    2 -> "Confirm Pregnancy"
                    else -> "Log Calving"
                }, fontWeight = FontWeight.SemiBold)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (heatForm.show) {
        HeatSheet(
            state = heatForm,
            animals = state.animals,
            onDismiss = viewModel::closeHeatForm,
            onAnimalChange = viewModel::onHeatAnimalChanged,
            onSymptomToggle = viewModel::onHeatSymptomToggled,
            onIntensityChange = viewModel::onHeatIntensityChanged,
            onDetectedByChange = viewModel::onHeatDetectedByChanged,
            onNotesChange = viewModel::onHeatNotesChanged,
            onSave = viewModel::saveHeat
        )
    }
    if (breedingForm.show) {
        BreedingSheet(
            state = breedingForm,
            animals = state.animals,
            onDismiss = viewModel::closeBreedingForm,
            onAnimalChange = viewModel::onBreedingAnimalChanged,
            onTypeChange = viewModel::onBreedingTypeChanged,
            onBullNameChange = viewModel::onBullNameChanged,
            onSemenBatchChange = viewModel::onSemenBatchChanged,
            onTechnicianChange = viewModel::onAiTechnicianChanged,
            onCostChange = viewModel::onBreedingCostChanged,
            onNotesChange = viewModel::onBreedingNotesChanged,
            onSave = viewModel::saveBreeding
        )
    }
    if (pregnancyForm.show) {
        PregnancySheet(
            state = pregnancyForm,
            animals = state.animals,
            onDismiss = viewModel::closePregnancyForm,
            onAnimalChange = viewModel::onPregnancyAnimalChanged,
            onPdMethodChange = viewModel::onPdMethodChanged,
            onNotesChange = viewModel::onPregnancyNotesChanged,
            onSave = viewModel::savePregnancy
        )
    }
    if (calvingForm.show) {
        CalvingSheet(
            state = calvingForm,
            onDismiss = viewModel::closeCalvingForm,
            onDifficultyChange = viewModel::onCalvingDifficultyChanged,
            onOutcomeChange = viewModel::onCalvingOutcomeChanged,
            onNotesChange = viewModel::onCalvingNotesChanged,
            onCreateCalfToggle = viewModel::onCreateCalfToggled,
            onCalfTagChange = viewModel::onCalfTagChanged,
            onCalfNameChange = viewModel::onCalfNameChanged,
            onCalfGenderChange = viewModel::onCalfGenderChanged,
            onSave = viewModel::saveCalving
        )
    }
}

@Composable
private fun HeatTab(records: List<HeatRecordDetail>, today: LocalDate) {
    if (records.isEmpty()) {
        EmptyPane("♨️", "No heat records yet",
            "Log heat detection events. Pashu360 predicts next heat 21 days later.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(records, key = { it.heat.id }) { detail -> HeatCard(detail, today) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun HeatCard(d: HeatRecordDetail, today: LocalDate) {
    val nextHeat = d.heat.expectedNextHeat()
    val intensityColor = when (d.heat.intensity) {
        HeatIntensity.WEAK -> Color.Gray
        HeatIntensity.MEDIUM -> PashuAmber
        HeatIntensity.STRONG -> ColorOverdue
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(4.dp, 60.dp).clip(RoundedCornerShape(2.dp))
                .background(intensityColor))
            Spacer(Modifier.width(12.dp))
            Text("♨️", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(d.animalName ?: "Tag #${d.animalTag}",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Detected ${d.heat.detectionDate} • ${d.heat.intensity.displayName}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
                if (d.heat.symptoms.isNotEmpty()) {
                    Text(d.heat.symptoms.joinToString(", "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = if (nextHeat >= today) PashuGreen.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Next expected: $nextHeat",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (nextHeat >= today) PashuGreenDark else Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun MatingTab(
    records: List<BreedingRecordDetail>,
    awaitingPd: List<BreedingRecordDetail>,
    onConfirmedClick: (String) -> Unit,
    onFailedClick: (String) -> Unit
) {
    if (records.isEmpty()) {
        EmptyPane("💉", "No mating records yet",
            "Log AI or natural mating events. Track conception status per attempt.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (awaitingPd.isNotEmpty()) {
            item {
                Text("Awaiting PD (${awaitingPd.size})",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = PashuAmber,
                    modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
        items(records, key = { it.breeding.id }) { detail ->
            MatingCard(detail,
                onConfirmed = { onConfirmedClick(detail.breeding.id) },
                onFailed = { onFailedClick(detail.breeding.id) })
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun MatingCard(
    d: BreedingRecordDetail,
    onConfirmed: () -> Unit,
    onFailed: () -> Unit
) {
    val statusColor = when (d.breeding.conceptionStatus) {
        ConceptionStatus.CONFIRMED -> PashuGreen
        ConceptionStatus.FAILED -> ColorOverdue
        ConceptionStatus.PENDING -> PashuAmber
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(d.breeding.breedingType.emoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(d.animalName ?: "Tag #${d.animalTag}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${d.breeding.breedingType.displayName} • ${d.breeding.breedingDate}",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium)
                    d.breeding.bullName?.let {
                        Text("🐂 $it", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    d.breeding.aiTechnician?.let {
                        Text("👨‍⚕️ $it", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text("${d.breeding.conceptionStatus.emoji} ${d.breeding.conceptionStatus.displayName}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
            if (d.breeding.conceptionStatus == ConceptionStatus.PENDING) {
                Spacer(Modifier.height(10.dp))
                Text("PD expected around ${d.breeding.expectedPdDate()}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onConfirmed,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Confirmed", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onFailed,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Failed", fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun PregnancyTab(
    pregnancies: List<PregnancyDetail>,
    today: LocalDate,
    onCalveClick: (PregnancyDetail) -> Unit
) {
    if (pregnancies.isEmpty()) {
        EmptyPane("🤰", "No active pregnancies",
            "After conception confirmation, add a pregnancy record. Calving alerts fire 7 days before due date.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pregnancies, key = { it.pregnancy.id }) { detail ->
            PregnancyCard(detail, today, onLogCalving = { onCalveClick(detail) })
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun PregnancyCard(
    d: PregnancyDetail,
    today: LocalDate,
    onLogCalving: () -> Unit
) {
    val daysUntil = d.pregnancy.expectedCalvingDate.toEpochDays() - today.toEpochDays()
    val (label, color) = when {
        daysUntil < 0 -> "OVERDUE" to ColorOverdue
        daysUntil <= 7 -> "DUE THIS WEEK" to PashuAmber
        daysUntil <= 30 -> "DUE THIS MONTH" to ColorPregnant
        else -> "" to PashuGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤰", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(d.animalName ?: "Tag #${d.animalTag}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Confirmed ${d.pregnancy.confirmationDate} • ${d.pregnancy.pdMethod.displayName}",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium)
                }
                if (label.isNotEmpty()) {
                    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
                        Text(label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Expected", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(d.pregnancy.expectedCalvingDate.toString(),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dry period", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(d.pregnancy.dryPeriodStart.toString(),
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Days to go", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (daysUntil >= 0) "$daysUntil" else "OVERDUE",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (daysUntil < 0) ColorOverdue else PashuGreenDark)
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onLogCalving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🐄 Log Calving", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun CalvingTab(completed: List<PregnancyDetail>) {
    if (completed.isEmpty()) {
        EmptyPane("🐄", "No calvings yet",
            "Completed pregnancies with calving records appear here.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(completed, key = { it.pregnancy.id }) { detail -> CalvingHistoryCard(detail) }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun CalvingHistoryCard(d: PregnancyDetail) {
    val outcome = d.pregnancy.calvingOutcome
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
            Text(outcome?.emoji ?: "🐄", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(d.animalName ?: "Tag #${d.animalTag}",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Calved ${d.pregnancy.actualCalvingDate}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
                outcome?.let {
                    Text(it.displayName, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                d.pregnancy.calfAnimalId?.let {
                    Text("Calf registered",
                        fontSize = 11.sp, color = PashuGreen, fontWeight = FontWeight.SemiBold)
                }
            }
            d.pregnancy.calvingDifficulty?.let {
                val diffColor = when (it) {
                    1 -> PashuGreen
                    2 -> PashuAmber
                    else -> ColorOverdue
                }
                Surface(color = diffColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text("Diff $it/4",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = diffColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HeatSheet(
    state: HeatFormState,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onAnimalChange: (String) -> Unit,
    onSymptomToggle: (String) -> Unit,
    onIntensityChange: (HeatIntensity) -> Unit,
    onDetectedByChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("♨️ Log Heat", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            AnimalPicker(animals, state.animalId, onAnimalChange, "Animal")
            Spacer(Modifier.height(16.dp))
            Text("Symptoms observed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeatSymptomCatalog.symptoms.forEach { s ->
                    val sel = s in state.selectedSymptoms
                    Surface(
                        onClick = { onSymptomToggle(s) },
                        color = if (sel) PashuGreen else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (sel) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(s,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Intensity", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeatIntensity.entries.forEach { i ->
                    Surface(
                        onClick = { onIntensityChange(i) },
                        color = if (state.intensity == i) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.intensity == i) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(i.displayName,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (state.intensity == i) Color.White
                                    else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.detectedBy, onValueChange = onDetectedByChange,
                label = { Text("Detected by (optional)") },
                singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.notes, onValueChange = onNotesChange,
                label = { Text("Notes") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                maxLines = 3, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            SaveButton(state.isValid && !state.isSaving, state.isSaving, onSave)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedingSheet(
    state: BreedingFormState,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onAnimalChange: (String) -> Unit,
    onTypeChange: (BreedingType) -> Unit,
    onBullNameChange: (String) -> Unit,
    onSemenBatchChange: (String) -> Unit,
    onTechnicianChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("💉 Log Mating", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            AnimalPicker(animals, state.animalId, onAnimalChange, "Animal")
            Spacer(Modifier.height(16.dp))
            Text("Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreedingType.entries.forEach { t ->
                    Surface(
                        onClick = { onTypeChange(t) },
                        color = if (state.breedingType == t) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.breedingType == t) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(t.emoji, fontSize = 20.sp)
                            Text(if (t == BreedingType.AI) "AI" else "Natural",
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (state.breedingType == t) Color.White
                                        else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (state.breedingType == BreedingType.AI) {
                OutlinedTextField(
                    value = state.bullName, onValueChange = onBullNameChange,
                    label = { Text("Bull / Semen name") },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.semenBatch, onValueChange = onSemenBatchChange,
                        label = { Text("Batch #") },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.aiTechnician, onValueChange = onTechnicianChange,
                        label = { Text("Technician") },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = state.bullName, onValueChange = onBullNameChange,
                    label = { Text("Bull name / tag") },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.cost, onValueChange = onCostChange,
                label = { Text("Cost ₹ (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.notes, onValueChange = onNotesChange,
                label = { Text("Notes") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                maxLines = 2, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            SaveButton(state.isValid && !state.isSaving, state.isSaving, onSave)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PregnancySheet(
    state: PregnancyFormState,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onAnimalChange: (String) -> Unit,
    onPdMethodChange: (PdMethod) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("🤰 Confirm Pregnancy", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            AnimalPicker(animals, state.animalId, onAnimalChange, "Animal")
            Spacer(Modifier.height(16.dp))
            Text("Confirmation Method", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PdMethod.entries.forEach { method ->
                    Surface(
                        onClick = { onPdMethodChange(method) },
                        color = if (state.pdMethod == method) PashuGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.pdMethod == method) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.pdMethod == method,
                                onClick = { onPdMethodChange(method) },
                                colors = RadioButtonDefaults.colors(selectedColor = PashuGreen)
                            )
                            Text(method.displayName, fontSize = 14.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                color = PashuGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Expected calving date",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(state.expectedCalvingDate.toString(),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PashuGreenDark)
                    Text("Dry period starts 60 days before calving",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.notes, onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                maxLines = 2, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            SaveButton(state.isValid && !state.isSaving, state.isSaving, onSave)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalvingSheet(
    state: CalvingFormState,
    onDismiss: () -> Unit,
    onDifficultyChange: (Int) -> Unit,
    onOutcomeChange: (CalvingOutcome) -> Unit,
    onNotesChange: (String) -> Unit,
    onCreateCalfToggle: (Boolean) -> Unit,
    onCalfTagChange: (String) -> Unit,
    onCalfNameChange: (String) -> Unit,
    onCalfGenderChange: (Gender) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("🐄 Log Calving", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Mother: ${state.motherName}",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Outcome", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalvingOutcome.entries.forEach { o ->
                    Surface(
                        onClick = { onOutcomeChange(o) },
                        color = if (state.outcome == o) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.outcome == o) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(o.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(o.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (state.outcome == o) Color.White
                                        else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Difficulty (1 = Normal … 4 = Vet required)",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { level ->
                    Surface(
                        onClick = { onDifficultyChange(level) },
                        color = if (state.difficulty == level) PashuGreen
                                else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (state.difficulty == level) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$level",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = if (state.difficulty == level) Color.White
                                    else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (state.outcome == CalvingOutcome.LIVE_CALF ||
                state.outcome == CalvingOutcome.TWINS) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = if (state.createCalf) PashuGreen.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (state.createCalf) PashuGreen
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onCreateCalfToggle(!state.createCalf) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.createCalf,
                            onCheckedChange = onCreateCalfToggle,
                            colors = CheckboxDefaults.colors(checkedColor = PashuGreen)
                        )
                        Column {
                            Text("Create calf animal record",
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Adds the calf to your herd automatically",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (state.createCalf) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.calfTagId, onValueChange = onCalfTagChange,
                        label = { Text("Calf Tag ID *") },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.calfName, onValueChange = onCalfNameChange,
                        label = { Text("Calf Name (optional)") },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Gender.entries.forEach { g ->
                            Surface(
                                onClick = { onCalfGenderChange(g) },
                                color = if (state.calfGender == g) PashuGreen
                                        else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (state.calfGender == g) PashuGreen
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(g.displayName,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (state.calfGender == g) Color.White
                                            else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.calvingNotes, onValueChange = onNotesChange,
                label = { Text("Calving notes") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                maxLines = 2, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            SaveButton(state.isValid && !state.isSaving, state.isSaving, onSave)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Chip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    fgOnLight: Color = Color.White
) {
    val isLight = color == Color.White || color.alpha > 0.9f && color.red > 0.9f
    Surface(color = color, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = if (isLight) fgOnLight else Color.White)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = if (isLight) fgOnLight.copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.85f))
        }
    }
}

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
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected?.displayName ?: "Select animal",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ExpandMore, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (animals.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No active animals — add in Animals tab") },
                        onClick = { expanded = false }
                    )
                }
                animals.forEach { a ->
                    DropdownMenuItem(
                        text = { Text("${a.displayName} • #${a.tagId}") },
                        onClick = { onSelect(a.id); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
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
            Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

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
        ) { Text(emoji, fontSize = 56.sp) }
        Spacer(Modifier.height(20.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}
