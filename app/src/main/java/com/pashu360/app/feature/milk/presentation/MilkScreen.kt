package com.pashu360.app.feature.milk.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.pashu360.app.core.domain.model.BulkMilkEntry
import com.pashu360.app.core.domain.model.DailyMilkTotal
import com.pashu360.app.core.domain.model.MilkSession
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkScreen(
    viewModel: MilkViewModel = hiltViewModel(),
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MilkEvent.Saved -> {
                    Toast.makeText(context, "Saved ${event.recordsSaved} records", Toast.LENGTH_SHORT).show()
                    scope.launch { sheetState.hide() }
                    showSheet = false
                }
                is MilkEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // ── HEADER ────────────────────
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
                        title = "Milk",
                        alertCount = alertCount,
                        onMenuClick = onMenuClick,
                        onBellClick = onBellClick,
                        onProfileClick = onProfileClick
                    )

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("Today · ${state.selectedDate}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium)

                        Spacer(Modifier.height(12.dp))

                        // Today's total card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(PashuGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.LocalDrink, null, tint = PashuGreen,
                                        modifier = Modifier.size(28.dp))
                                }
                                Spacer(Modifier.size(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Today's Total", fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("%.1f".format(state.dailyTotalLiters),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PashuGreenDark)
                                        Text(" L", fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PashuGreenDark,
                                            modifier = Modifier.padding(bottom = 4.dp))
                                    }
                                }
                                Button(
                                    onClick = { showSheet = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PashuGreen),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = Color.White,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Log", fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            // ── WEEKLY CHART ──────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text("Last 7 Days", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                WeeklyBarChart(state.weeklyTotals)
            }

            // ── SESSION BREAKDOWN ─────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SessionCard(
                        "🌅 Morning",
                        state.weeklyTotals.sumOf { it.morning },
                        PashuAmber,
                        Modifier.weight(1f)
                    )
                    SessionCard(
                        "🌆 Evening",
                        state.weeklyTotals.sumOf { it.evening },
                        PashuGreen,
                        Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(96.dp))
        }

        ExtendedFloatingActionButton(
            onClick = { showSheet = true },
            containerColor = PashuGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("Log Milk", fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            BulkMilkEntrySheet(
                state = state,
                onSessionChange = viewModel::onSessionChanged,
                onQuantityChange = viewModel::onQuantityChanged,
                onFatChange = viewModel::onFatChanged,
                onSnfChange = viewModel::onSnfChanged,
                onToggleQuality = viewModel::onToggleQualityFields,
                onSaveAll = viewModel::onSaveAll
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(data: List<DailyMilkTotal>) {
    if (data.isEmpty()) {
        Text("No data yet — start logging milk to see trends",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 20.dp))
        return
    }
    val maxTotal = (data.maxOfOrNull { it.total } ?: 0.0).coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                val fraction = (day.total / maxTotal).toFloat().coerceIn(0.05f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    if (day.total > 0) {
                        Text("%.0f".format(day.total),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PashuGreenDark)
                        Spacer(Modifier.height(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(fraction)
                            .width(28.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (day.total > 0) PashuGreen
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(day.date.dayOfWeek.name.take(3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    title: String,
    total: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text("%.1f L".format(total),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color)
            Text("Last 7 days",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────
// BULK MILK ENTRY BOTTOM SHEET
// ─────────────────────────────────────────────────────────
@Composable
private fun BulkMilkEntrySheet(
    state: MilkUiState,
    onSessionChange: (MilkSession) -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onFatChange: (String, String) -> Unit,
    onSnfChange: (String, String) -> Unit,
    onToggleQuality: () -> Unit,
    onSaveAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Log Milk", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("${state.selectedDate}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))

            // Session toggle
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MilkSession.entries.forEach { session ->
                    SessionChip(
                        label = "${session.emoji} ${session.displayName}",
                        selected = state.selectedSession == session,
                        onClick = { onSessionChange(session) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Show/hide quality toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total entered: %.1f L".format(state.enteredTotal),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PashuGreenDark
                )
                TextButton(onClick = onToggleQuality) {
                    Icon(Icons.Filled.Tune, null, tint = PashuGreen,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (state.showQualityFields) "Hide quality" else "Add fat/SNF",
                        color = PashuGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider()

        // Entry rows
        if (state.entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Pets, null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No active animals",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold)
                Text("Add animals in the Animals tab first",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)
            ) {
                items(state.entries, key = { it.animalId }) { entry ->
                    EntryRow(
                        entry = entry,
                        qty = state.quantityInputs[entry.animalId].orEmpty(),
                        fat = state.fatInputs[entry.animalId].orEmpty(),
                        snf = state.snfInputs[entry.animalId].orEmpty(),
                        showQuality = state.showQualityFields,
                        onQtyChange = { onQuantityChange(entry.animalId, it) },
                        onFatChange = { onFatChange(entry.animalId, it) },
                        onSnfChange = { onSnfChange(entry.animalId, it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }

        // Save button
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Button(
                onClick = onSaveAll,
                enabled = !state.isSaving && state.entries.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
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
                    Text("Save All Records",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SessionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun EntryRow(
    entry: BulkMilkEntry,
    qty: String,
    fat: String,
    snf: String,
    showQuality: Boolean,
    onQtyChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSnfChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.displayName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (entry.breed != null) {
                    Text(entry.breed, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(
                value = qty,
                onValueChange = onQtyChange,
                placeholder = { Text("0.0", color = MaterialTheme.colorScheme.outline) },
                trailingIcon = { Text(" L",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.width(100.dp).height(52.dp)
            )
        }
        if (showQuality) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fat,
                    onValueChange = onFatChange,
                    label = { Text("Fat %", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f).height(52.dp)
                )
                OutlinedTextField(
                    value = snf,
                    onValueChange = onSnfChange,
                    label = { Text("SNF %", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f).height(52.dp)
                )
            }
        }
    }
}
