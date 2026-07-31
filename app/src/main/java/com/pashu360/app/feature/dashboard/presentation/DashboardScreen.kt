package com.pashu360.app.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.ColorSick
import com.pashu360.app.core.presentation.theme.Pashu360Theme
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@Composable
fun DashboardScreen() {
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
            // ── HEADER (green gradient) ──────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PashuGreenLight, PashuGreen)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🌅 Good Morning",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium)
                            Text("Ramesh",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White)
                            Spacer(Modifier.height(4.dp))
                            Text("Sharma Dairy Farm • Wed, 1 Aug",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Highlight KPI card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(PashuGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.LocalDrink,
                                    contentDescription = null,
                                    tint = PashuGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.size(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Today's Milk",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("42.5",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PashuGreenDark)
                                    Text(" L",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PashuGreenDark,
                                        modifier = Modifier.padding(bottom = 4.dp))
                                }
                                Text("↑ 8% vs yesterday",
                                    fontSize = 12.sp,
                                    color = PashuGreen,
                                    fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { /* log milk */ },
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

            // ── STAT ROW ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStat("25", "Cows", Icons.Filled.Pets, PashuGreen, Modifier.weight(1f))
                MiniStat("3", "Vaccines", Icons.Filled.Vaccines, PashuAmber, Modifier.weight(1f))
                MiniStat("1", "Sick", Icons.Filled.Favorite, ColorSick, Modifier.weight(1f))
            }

            // ── QUICK ACTIONS ────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionHeader("Quick Actions")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Log Milk", Icons.Filled.LocalDrink, PashuGreen, Modifier.weight(1f))
                    QuickAction("Vaccine", Icons.Filled.Vaccines, PashuAmber, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Add Cow", Icons.Filled.Add, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    QuickAction("Feed", Icons.Filled.Grass, Color(0xFF7B9E4A), Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── ALERTS ───────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Alerts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f))
                    Surface(
                        color = ColorOverdue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("2 urgent",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = ColorOverdue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                AlertItem("FMD Vaccine Due Today", "Gouri (Tag #4)", "💉", ColorOverdue)
                Spacer(Modifier.height(10.dp))
                AlertItem("Expected Heat Tomorrow", "Rani (Tag #7)", "♨️", PashuAmber)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
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
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
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

@Composable
private fun AlertItem(title: String, subtitle: String, emoji: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.size(12.dp))
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
            }
            FilledTonalButton(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
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
