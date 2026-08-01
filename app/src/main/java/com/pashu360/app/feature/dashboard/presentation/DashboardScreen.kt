package com.pashu360.app.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.presentation.components.PashuAppBar
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    alertCount: Int = 2,   // TODO: wire to Alerts repository in PR #6
    onMenuClick: () -> Unit = {},
    onBellClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogMilkClick: () -> Unit = {},
    onAddVaccineClick: () -> Unit = {},
    onAddAnimalClick: () -> Unit = {},
    onFeedClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── GREEN GRADIENT HEADER ──────────────────
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
                        title = "Pashu360",
                        alertCount = alertCount,
                        onMenuClick = onMenuClick,
                        onBellClick = onBellClick,
                        onProfileClick = onProfileClick
                    )

                    Spacer(Modifier.height(4.dp))

                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text("${state.greetingEmoji} ${state.greetingByTime}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium)
                        Text(
                            state.ownerName.takeIf { it.isNotBlank() }?.split(' ')?.first() ?: "Farmer",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        val farmLabel = state.farmName.takeIf { it.isNotBlank() } ?: "Your Farm"
                        Text("$farmLabel • ${state.todayFormatted}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f))

                        Spacer(Modifier.height(24.dp))

                        // Highlight KPI card
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
                                    Text("Today's Milk", fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("0.0", fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold, color = PashuGreenDark)
                                        Text(" L", fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PashuGreenDark,
                                            modifier = Modifier.padding(bottom = 4.dp))
                                    }
                                    Text("Log today's milk to get started",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium)
                                }
                                Button(
                                    onClick = { },
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

            // ── STAT ROW ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val cowsLabel = if (state.expectedHerdSize > 0)
                    "${state.cowCount} / ${state.expectedHerdSize}"
                else
                    "${state.cowCount}"
                MiniStat(cowsLabel, "Cows", Icons.Filled.Pets, PashuGreen, Modifier.weight(1f))
                MiniStat("${state.vaccinesDueCount}", "Vaccines", Icons.Filled.Vaccines, PashuAmber, Modifier.weight(1f))
                MiniStat("${state.sickCount}", "Sick", Icons.Filled.Favorite, ColorSick, Modifier.weight(1f))
            }

            // ── QUICK ACTIONS ────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Quick Actions", fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Log Milk", Icons.Filled.LocalDrink, PashuGreen,
                        onClick = onLogMilkClick, modifier = Modifier.weight(1f))
                    QuickAction("Vaccine", Icons.Filled.Vaccines, PashuAmber,
                        onClick = onAddVaccineClick, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Add Cow", Icons.Filled.Add, MaterialTheme.colorScheme.tertiary,
                        onClick = onAddAnimalClick, modifier = Modifier.weight(1f))
                    QuickAction("Feed", Icons.Filled.Grass, Color(0xFF7B9E4A),
                        onClick = onFeedClick, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── ALERTS PLACEHOLDER ───────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Alerts", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (alertCount > 0) {
                        Surface(
                            color = ColorOverdue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("$alertCount urgent",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = ColorOverdue, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Empty state — real alerts come with PR #6
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PashuGreen.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌱", fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("All caught up!",
                                fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Add animals to start tracking events",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MiniStat(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(12.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(showBackground = true, name = "Dashboard")
@Composable
private fun DashboardPreview() {
    Pashu360Theme(dynamicColor = false) {
        DashboardScreen()
    }
}
