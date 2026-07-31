package com.pashu360.app.feature.animal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast
import com.pashu360.app.core.domain.model.MilkSession
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalStatus
import com.pashu360.app.core.presentation.theme.ColorPregnant
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetailScreen(
    animalId: String,
    viewModel: AnimalDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val animal by viewModel.animal.collectAsStateWithLifecycle()
    val milkSheet by viewModel.milkSheet.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AnimalDetailEvent.MilkSaved -> {
                    Toast.makeText(context,
                        "✓ Logged %.1f L".format(event.litres),
                        Toast.LENGTH_SHORT).show()
                }
                is AnimalDetailEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (animal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PashuGreen)
        }
        return
    }

    val a = animal!!
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Milk", "Vaccination", "Health", "Feeding", "Breeding")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── HEADER ──────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(colors = listOf(PashuGreenLight, PashuGreen, PashuGreenDark))
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { /* TODO edit */ }) {
                            Icon(Icons.Filled.Edit, null, tint = Color.White)
                        }
                        IconButton(onClick = { /* TODO qr */ }) {
                            Icon(Icons.Filled.QrCode2, null, tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐄", fontSize = 56.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(a.displayName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White)

                        StatusBadgeLarge(a.status)

                        Spacer(Modifier.height(6.dp))

                        Text(
                            buildString {
                                append("#${a.tagId}")
                                a.breed?.let { append(" • $it") }
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(20.dp))

                        // Quick action buttons row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeaderAction("Log Milk", Icons.Filled.LocalDrink) {
                                viewModel.openMilkSheet()
                            }
                            HeaderAction("Vaccine", Icons.Filled.Vaccines) { /* TODO PR #4 */ }
                            HeaderAction("Health", Icons.Filled.Favorite) { /* TODO PR #4 */ }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            // ── TABS ────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PashuGreen,
                divider = { HorizontalDivider() }
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

            // ── TAB CONTENT ─────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(a)
                    1 -> TabPlaceholder("Milk", "Milk history and analytics for ${a.displayName}",
                        Icons.Filled.LocalDrink)
                    2 -> TabPlaceholder("Vaccination", "Vaccination schedule and history",
                        Icons.Filled.Vaccines)
                    3 -> TabPlaceholder("Health", "Health records and vet visits",
                        Icons.Filled.Favorite)
                    4 -> TabPlaceholder("Feeding", "Feeding schedule and logs",
                        Icons.Filled.Grass)
                    5 -> TabPlaceholder("Breeding", "Heat cycles, AI records, and pregnancy",
                        Icons.Filled.Pets)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── QUICK MILK ENTRY BOTTOM SHEET ─────────────────
    if (milkSheet.show) {
        QuickMilkEntrySheet(
            animalName = a.displayName,
            state = milkSheet,
            onDismiss = viewModel::closeMilkSheet,
            onSessionChange = viewModel::onSessionChanged,
            onQuantityChange = viewModel::onQuantityChanged,
            onFatChange = viewModel::onFatChanged,
            onSnfChange = viewModel::onSnfChanged,
            onToggleQuality = viewModel::onToggleQualityFields,
            onSave = viewModel::onSaveMilk
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickMilkEntrySheet(
    animalName: String,
    state: QuickMilkSheetState,
    onDismiss: () -> Unit,
    onSessionChange: (MilkSession) -> Unit,
    onQuantityChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSnfChange: (String) -> Unit,
    onToggleQuality: () -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text("Log Milk", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("for $animalName",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))

            // Session toggle
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MilkSession.entries.forEach { session ->
                    SessionToggle(
                        label = "${session.emoji} ${session.displayName}",
                        selected = state.session == session,
                        onClick = { onSessionChange(session) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Quantity",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.quantity,
                onValueChange = onQuantityChange,
                placeholder = { Text("Litres") },
                trailingIcon = { Text(" L", modifier = Modifier.padding(end = 12.dp),
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onToggleQuality) {
                Icon(Icons.Filled.Tune, null, tint = PashuGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (state.showQualityFields) "Hide quality" else "Add fat/SNF (optional)",
                    color = PashuGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
            }

            if (state.showQualityFields) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.fat,
                        onValueChange = onFatChange,
                        label = { Text("Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.weight(1f).height(60.dp)
                    )
                    OutlinedTextField(
                        value = state.snf,
                        onValueChange = onSnfChange,
                        label = { Text("SNF %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                        modifier = Modifier.weight(1f).height(60.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSave,
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
                    Text("Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SessionToggle(
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun OverviewTab(a: Animal) {
    // Details card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            DetailRow("Tag ID", "#${a.tagId}")
            a.name?.let { DetailRow("Name", it) }
            a.breed?.let { DetailRow("Breed", it) }
            DetailRow("Gender", a.gender.displayName)
            DetailRow("Age", a.ageString)
            a.weightKg?.let { DetailRow("Weight", "${it.toInt()} kg") }
            a.dob?.let { DetailRow("Date of Birth", it.toString()) }
            a.purchasePrice?.let { DetailRow("Purchase Price", "₹${it.toInt()}") }
            a.notes?.let {
                Spacer(Modifier.height(8.dp))
                Text("Notes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Quick stats card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PashuGreen.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("This Month",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PashuGreen)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                MiniStatDetail("Milk", "—")
                MiniStatDetail("Feed cost", "—")
                MiniStatDetail("Last Vet", "—")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium)
        Text(value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MiniStatDetail(label: String, value: String) {
    Column {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PashuGreenDark)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusBadgeLarge(status: AnimalStatus) {
    val bg = Color.White.copy(alpha = 0.25f)
    Surface(color = bg, shape = RoundedCornerShape(12.dp)) {
        Text(
            status.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun HeaderAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PashuGreen, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = PashuGreenDark)
        }
    }
}

@Composable
private fun TabPlaceholder(title: String, subtitle: String, icon: ImageVector) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PashuGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PashuGreen, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = PashuAmber.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Coming in next phase",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PashuAmber)
        }
    }
}
