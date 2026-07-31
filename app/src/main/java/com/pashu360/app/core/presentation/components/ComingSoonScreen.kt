package com.pashu360.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen

@Composable
fun ComingSoonScreen(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(PashuGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PashuGreen, modifier = Modifier.size(60.dp))
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            color = PashuAmber.copy(alpha = 0.15f),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                "🚧 Coming soon in Phase " + when (title) {
                    "Milk" -> "2"
                    "Alerts" -> "3"
                    else -> "4+"
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PashuAmber
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            subtitle,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
