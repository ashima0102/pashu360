package com.pashu360.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenDark
import com.pashu360.app.core.presentation.theme.PashuGreenLight

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun PashuDrawer(
    userName: String,
    farmName: String,
    onItemClick: (route: String) -> Unit,
    onLogout: () -> Unit
) {
    val topItems = listOf(
        DrawerItem("Farm Info", Icons.Filled.Agriculture, "farm_info"),
        DrawerItem("Feeding", Icons.Filled.Grass, "feeding"),
        DrawerItem("Breeding", Icons.Filled.Favorite, "breeding"),
        DrawerItem("Pregnancy", Icons.Filled.PregnantWoman, "pregnancy"),
        DrawerItem("Reports", Icons.Filled.BarChart, "reports"),
    )
    val bottomItems = listOf(
        DrawerItem("Settings", Icons.Filled.Settings, "settings"),
        DrawerItem("Help & Support", Icons.AutoMirrored.Filled.Help, "help"),
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Profile header on green gradient ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(PashuGreenLight, PashuGreen, PashuGreenDark)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Pets, null, tint = PashuGreen,
                            modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userName, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text(farmName, fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Home shortcut ──────────────────────────────
            DrawerRow(
                label = "Dashboard",
                icon = Icons.Filled.Home,
                onClick = { onItemClick("dashboard") }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Farm modules ────────────────────────────────
            topItems.forEach { item ->
                DrawerRow(
                    label = item.label,
                    icon = item.icon,
                    onClick = { onItemClick(item.route) }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Settings / Help ─────────────────────────────
            bottomItems.forEach { item ->
                DrawerRow(
                    label = item.label,
                    icon = item.icon,
                    onClick = { onItemClick(item.route) }
                )
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Logout ─────────────────────────────────────
            DrawerRow(
                label = "Logout",
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = onLogout,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = tint)
    }
}
