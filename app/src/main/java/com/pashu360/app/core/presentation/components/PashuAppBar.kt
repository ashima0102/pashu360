package com.pashu360.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable app header used on the top-level tabs.
 *  - Left: hamburger menu (opens drawer with Feeding, Breeding, Reports, etc.)
 *  - Center: screen title
 *  - Right: bell (alerts inbox) + profile avatar
 *
 * Callers pass the alert count so the bell badge stays in sync with the notifications repo.
 */
@Composable
fun PashuAppBar(
    title: String,
    alertCount: Int,
    onMenuClick: () -> Unit,
    onBellClick: () -> Unit,
    onProfileClick: () -> Unit,
    titleColor: Color = Color.White,
    iconTint: Color = Color.White,
    iconBackground: Color = Color.White.copy(alpha = 0.2f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger
        HeaderIconButton(
            background = iconBackground,
            onClick = onMenuClick
        ) {
            Icon(Icons.Filled.Menu, "Open menu", tint = iconTint)
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        // Bell with badge
        HeaderIconButton(
            background = iconBackground,
            onClick = onBellClick
        ) {
            BadgedBox(
                badge = {
                    if (alertCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        ) {
                            Text(if (alertCount > 9) "9+" else "$alertCount")
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Notifications, "Alerts", tint = iconTint)
            }
        }

        Spacer(Modifier.width(8.dp))

        // Profile avatar
        HeaderIconButton(
            background = iconBackground,
            onClick = onProfileClick
        ) {
            Icon(Icons.Filled.Person, "Profile", tint = iconTint)
        }
    }
}

@Composable
private fun HeaderIconButton(
    background: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) { content() }
    }
}
