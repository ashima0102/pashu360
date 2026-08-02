package com.pashu360.app.feature.feeding.presentation

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
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
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
import com.pashu360.app.core.domain.model.FeedCategory
import com.pashu360.app.core.domain.model.FeedRecordWithType
import com.pashu360.app.core.domain.model.FeedType
import com.pashu360.app.core.domain.model.InventoryWithType
import com.pashu360.app.core.domain.model.StockLevel
import com.pashu360.app.core.domain.model.TimeOfDay
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel = hiltViewModel(),
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val logForm by viewModel.logForm.collectAsStateWithLifecycle()
    val inventoryForm by viewModel.inventoryForm.collectAsStateWithLifecycle()
    val feedTypeForm by viewModel.feedTypeForm.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today's Log", "Inventory", "Feed Types")

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedingEvent.Saved -> Toast.makeText(
                    context, event.message, Toast.LENGTH_SHORT
                ).show()
                is FeedingEvent.ShowError -> Toast.makeText(
                    context, event.message, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── HEADER ─────────────────────
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
                        title = "Feeding",
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
                            label = "Today Fed",
                            value = "%.0f kg".format(state.todayTotalKg),
                            color = Color.White.copy(alpha = 0.95f),
                            fgOnLight = PashuGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryChip(
                            label = "Low Stock",
                            value = "${state.lowStockCount}",
                            color = if (state.lowStockCount > 0) ColorOverdue else PashuGreenDark,
                            fgOnLight = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryChip(
                            label = "Feed Types",
                            value = "${state.feedTypes.size}",
                            color = Color.White.copy(alpha = 0.95f),
                            fgOnLight = PashuGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── TABS ──────────────────────
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

            // ── CONTENT ────────────────────
            when (selectedTab) {
                0 -> TodayLogTab(state.todayRecords)
                1 -> InventoryTab(
                    state.inventory,
                    onAdjustClick = { inv ->
                        viewModel.openInventoryForm(
                            inv.inventory.feedTypeId,
                            inv.feedTypeName,
                            inv.feedTypeUnit
                        )
                    }
                )
                2 -> FeedTypesTab(state.feedTypes)
            }
        }

        // ── FAB (contextual per tab) ────
        ExtendedFloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> viewModel.openLogFeedForm()
                    1 -> {
                        val first = state.feedTypes.firstOrNull()
                        if (first != null) viewModel.openInventoryForm(first.id, first.name, first.unit)
                        else Toast.makeText(context,
                            "Add a feed type first", Toast.LENGTH_SHORT).show()
                    }
                    2 -> viewModel.openFeedTypeForm()
                }
            },
            containerColor = PashuGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, null) },
            text = {
                Text(
                    when (selectedTab) {
                        0 -> "Log Feed"
                        1 -> "Add Stock"
                        else -> "New Type"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    if (logForm.show) {
        LogFeedSheet(
            state = logForm,
            feedTypes = state.feedTypes,
            animals = state.animals,
            onDismiss = viewModel::closeLogFeedForm,
            onFeedTypeChange = viewModel::onLogFeedTypeChanged,
            onAnimalChange = viewModel::onLogAnimalChanged,
            onHerdToggle = viewModel::onLogHerdToggled,
            onTimeChange = viewModel::onLogTimeChanged,
            onQuantityChange = viewModel::onLogQuantityChanged,
            onNotesChange = viewModel::onLogNotesChanged,
            onSave = viewModel::saveFeedLog
        )
    }

    if (inventoryForm.show) {
        InventoryAdjustSheet(
            state = inventoryForm,
            onDismiss = viewModel::closeInventoryForm,
            onAddChange = viewModel::onInventoryAddChanged,
            onThresholdChange = viewModel::onInventoryThresholdChanged,
            onSave = viewModel::saveInventoryAdjustment
        )
    }

    if (feedTypeForm.show) {
        AddFeedTypeSheet(
            state = feedTypeForm,
            onDismiss = viewModel::closeFeedTypeForm,
            onNameChange = viewModel::onFeedTypeNameChanged,
            onCategoryChange = viewModel::onFeedTypeCategoryChanged,
            onUnitChange = viewModel::onFeedTypeUnitChanged,
            onCostChange = viewModel::onFeedTypeCostChanged,
            onSave = viewModel::saveFeedType
        )
    }
}

// ─────────────────────────────────────────────────────────
// TODAY'S LOG TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun TodayLogTab(records: List<FeedRecordWithType>) {
    if (records.isEmpty()) {
        EmptyPane(
            emoji = "🌿",
            title = "No feed logged today",
            subtitle = "Tap 'Log Feed' to record what you've fed the herd"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(records, key = { it.record.id }) { row ->
            RecordCard(row)
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun RecordCard(row: FeedRecordWithType) {
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
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(PashuGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(row.feedTypeCategory.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(row.feedTypeName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    buildString {
                        append(row.record.timeOfDay.emoji)
                        append(" ${row.record.timeOfDay.displayName}")
                        if (row.record.isHerdFeeding) append(" • Herd feed")
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                row.estimatedCost?.let {
                    Text("₹%.0f cost".format(it),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "%.1f ${row.feedTypeUnit}".format(row.record.quantity),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PashuGreenDark
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// INVENTORY TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun InventoryTab(
    inventory: List<InventoryWithType>,
    onAdjustClick: (InventoryWithType) -> Unit
) {
    if (inventory.isEmpty()) {
        EmptyPane(
            emoji = "📦",
            title = "No feed stock yet",
            subtitle = "Add stock to track inventory and get low-stock alerts"
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(inventory, key = { it.inventory.id }) { row ->
            InventoryCard(row, onAdjustClick = { onAdjustClick(row) })
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun InventoryCard(row: InventoryWithType, onAdjustClick: () -> Unit) {
    val level = row.inventory.stockLevel
    val (color, badge) = when (level) {
        StockLevel.OUT -> ColorOverdue to "OUT"
        StockLevel.LOW -> PashuAmber to "LOW"
        StockLevel.OK -> PashuGreen to "OK"
        StockLevel.HIGH -> PashuGreenDark to "GOOD"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onAdjustClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(4.dp, 48.dp).clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Text(row.feedTypeCategory.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(row.feedTypeName, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
                        Text(badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Text("Threshold: %.0f ${row.feedTypeUnit}".format(row.inventory.lowStockThreshold),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("%.1f".format(row.inventory.quantity),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                Text(row.feedTypeUnit,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// FEED TYPES TAB
// ─────────────────────────────────────────────────────────
@Composable
private fun FeedTypesTab(feedTypes: List<FeedType>) {
    if (feedTypes.isEmpty()) {
        EmptyPane(
            emoji = "📖",
            title = "No feed types yet",
            subtitle = "Feed types define what you can log — like Green Fodder, Concentrate, etc."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(feedTypes, key = { it.id }) { ft ->
            FeedTypeCard(ft)
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun FeedTypeCard(ft: FeedType) {
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
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(PashuGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(ft.category.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ft.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${ft.category.displayName} • per ${ft.unit}",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
            }
            ft.costPerUnit?.let {
                Text("₹%.0f/${ft.unit}".format(it),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = PashuGreenDark)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// SUMMARY CHIP + EMPTY PANE
// ─────────────────────────────────────────────────────────
@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    fgOnLight: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(color = color, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            val isLight = color == Color.White || (color.alpha > 0.9f && color.red > 0.9f)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = if (isLight) fgOnLight else Color.White)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = if (isLight) fgOnLight.copy(alpha = 0.7f)
                        else Color.White.copy(alpha = 0.85f))
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

// ─────────────────────────────────────────────────────────
// SHEETS
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogFeedSheet(
    state: LogFeedFormState,
    feedTypes: List<FeedType>,
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onFeedTypeChange: (String) -> Unit,
    onAnimalChange: (String?) -> Unit,
    onHerdToggle: (Boolean) -> Unit,
    onTimeChange: (TimeOfDay) -> Unit,
    onQuantityChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
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
            Text("🌿 Log Feed", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Feed type chips
            Text("Feed Type", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                feedTypes.forEach { ft ->
                    val selected = state.feedTypeId == ft.id
                    Surface(
                        onClick = { onFeedTypeChange(ft.id) },
                        color = if (selected) PashuGreen else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selected) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ft.category.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(ft.name,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White
                                        else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Herd toggle
            Surface(
                color = if (state.isHerdFeeding) PashuGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (state.isHerdFeeding) PashuGreen
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onHerdToggle(!state.isHerdFeeding) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.isHerdFeeding,
                        onCheckedChange = onHerdToggle,
                        colors = CheckboxDefaults.colors(checkedColor = PashuGreen)
                    )
                    Column {
                        Text("Herd feeding", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Applies to all active animals",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (!state.isHerdFeeding) {
                Spacer(Modifier.height(12.dp))
                AnimalPickerRow(
                    animals = animals,
                    selectedId = state.animalId,
                    onSelect = onAnimalChange
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Time of day", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeOfDay.entries.forEach { t ->
                    val selected = state.timeOfDay == t
                    Surface(
                        onClick = { onTimeChange(t) },
                        color = if (selected) PashuGreen else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selected) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("${t.emoji} ${t.displayName}",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White
                                    else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity") },
                    trailingIcon = {
                        val unit = feedTypes.find { it.id == state.feedTypeId }?.unit ?: "kg"
                        Text(unit, modifier = Modifier.padding(end = 12.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f).height(60.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                maxLines = 2,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryAdjustSheet(
    state: InventoryAdjustFormState,
    onDismiss: () -> Unit,
    onAddChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
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
            Text("📦 Adjust Inventory", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(state.feedTypeName,
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.addQuantity,
                onValueChange = onAddChange,
                label = { Text("Add stock") },
                placeholder = { Text("Positive to add, negative to correct") },
                trailingIcon = { Text(state.unit,
                    modifier = Modifier.padding(end = 12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.newThreshold,
                onValueChange = onThresholdChange,
                label = { Text("Low stock threshold (optional)") },
                placeholder = { Text("Alert when stock falls below this") },
                trailingIcon = { Text(state.unit,
                    modifier = Modifier.padding(end = 12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth().height(60.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFeedTypeSheet(
    state: FeedTypeFormState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (FeedCategory) -> Unit,
    onUnitChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
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
            Text("📖 New Feed Type", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Name *") },
                placeholder = { Text("e.g. Maize Silage") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedCategory.entries.forEach { cat ->
                    val selected = state.category == cat
                    Surface(
                        onClick = { onCategoryChange(cat) },
                        color = if (selected) PashuGreen else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selected) PashuGreen
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(cat.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(cat.displayName,
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White
                                        else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.unit,
                    onValueChange = onUnitChange,
                    label = { Text("Unit") },
                    placeholder = { Text("kg / g / L") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.costPerUnit,
                    onValueChange = onCostChange,
                    label = { Text("Cost ₹/unit") },
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
// SHARED COMPONENTS
// ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalPickerRow(
    animals: List<Animal>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = animals.find { it.id == selectedId }

    Column {
        Text("Animal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected?.displayName ?: "Select animal",
                onValueChange = {},
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PashuGreen),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                animals.forEach { a ->
                    DropdownMenuItem(
                        text = { Text("${a.displayName} • #${a.tagId}") },
                        onClick = {
                            onSelect(a.id)
                            expanded = false
                        }
                    )
                }
                if (animals.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No active animals — add in Animals tab") },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
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
            Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

