package com.pashu360.app.feature.animal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pashu360.app.core.domain.model.Animal
import com.pashu360.app.core.domain.model.AnimalFilter
import com.pashu360.app.core.domain.model.AnimalStatus
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.ColorPregnant
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalListScreen(
    viewModel: AnimalListViewModel = hiltViewModel(),
    onAnimalClick: (String) -> Unit,
    onAddAnimalClick: () -> Unit,
    onScanQrClick: () -> Unit,
    alertCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── HEADER ────────────────────────────
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
                    // Reusable app bar
                    PashuAppBar(
                        title = "My Herd",
                        alertCount = alertCount,
                        onMenuClick = onMenuClick,
                        onBellClick = onBellClick,
                        onProfileClick = onProfileClick
                    )

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("${uiState.animals.size} animals total",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium)

                        Spacer(Modifier.height(12.dp))

                        // Search bar + QR button
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = viewModel::onSearchQueryChanged,
                                placeholder = { Text("Search by name or tag...",
                                    color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.White) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = onScanQrClick) {
                                    Icon(
                                        Icons.Filled.QrCodeScanner,
                                        contentDescription = "Scan QR",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── FILTER CHIPS ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimalFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.onFilterChanged(filter) },
                        label = { Text(filter.displayName, fontWeight = FontWeight.Medium) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PashuGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // ── LIST ─────────────────────────────
            if (uiState.filteredAnimals.isEmpty() && !uiState.isLoading) {
                EmptyAnimalsState(onAddClick = onAddAnimalClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredAnimals, key = { it.id }) { animal ->
                        AnimalCard(animal = animal, onClick = { onAnimalClick(animal.id) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }

        // ── FAB ──────────────────────────────
        ExtendedFloatingActionButton(
            onClick = onAddAnimalClick,
            containerColor = PashuGreen,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("Add Animal", fontWeight = FontWeight.SemiBold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun AnimalCard(animal: Animal, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PashuGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🐄", fontSize = 28.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        animal.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(status = animal.status)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append("#${animal.tagId}")
                        animal.breed?.let { append(" • $it") }
                        append(" • ${animal.ageString}")
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                animal.weightKg?.let {
                    Text(
                        text = "⚖️ ${it.toInt()} kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Placeholder for today's milk / next action
            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Filled.LocalDrink, null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp))
                Text(
                    "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: AnimalStatus) {
    val (bg, fg) = when (status) {
        AnimalStatus.ACTIVE -> PashuGreen.copy(alpha = 0.15f) to PashuGreen
        AnimalStatus.PREGNANT -> ColorPregnant.copy(alpha = 0.15f) to ColorPregnant
        AnimalStatus.SICK -> ColorSick.copy(alpha = 0.15f) to ColorSick
        AnimalStatus.DRY -> PashuAmber.copy(alpha = 0.15f) to PashuAmber
        AnimalStatus.SOLD, AnimalStatus.DECEASED ->
            Color.Gray.copy(alpha = 0.15f) to Color.Gray
    }
    Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun EmptyAnimalsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(PashuGreen.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🐄", fontSize = 72.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "No animals yet",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add your first cow to start tracking milk, health, and vaccinations",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PashuGreen),
            modifier = Modifier.height(52.dp).padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Filled.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Add First Animal",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)
        }
    }
}
