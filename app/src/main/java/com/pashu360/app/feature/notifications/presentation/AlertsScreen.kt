package com.pashu360.app.feature.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.pashu360.app.core.domain.model.Alert
import com.pashu360.app.core.domain.model.AlertFilter
import com.pashu360.app.core.domain.model.AlertUrgency
import com.pashu360.app.core.presentation.theme.ColorOverdue
import com.pashu360.app.core.presentation.theme.PashuAmber
import com.pashu360.app.core.presentation.theme.PashuGreen
import com.pashu360.app.core.presentation.theme.PashuGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenAnimal: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AlertsEvent.ShowMessage ->
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── HEADER ──────────────────────
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Text(
                            "Alerts",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        val unresolvedCount = state.alerts.count { !it.isResolved }
                        Text(
                            if (unresolvedCount == 0) "All caught up 🌱"
                            else "$unresolvedCount unresolved",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // ── FILTER CHIPS ────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
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

            // ── LIST ────────────────────────
            if (state.filteredAlerts.isEmpty()) {
                EmptyAlertsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredAlerts, key = { it.id }) { alert ->
                        AlertCard(
                            alert = alert,
                            today = state.today,
                            onResolve = { viewModel.onMarkResolved(alert.id) },
                            onClick = { alert.animalId?.let(onOpenAnimal) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    today: kotlinx.datetime.LocalDate,
    onResolve: () -> Unit,
    onClick: () -> Unit
) {
    val urgency = alert.urgency(today)
    val (label, color) = when (urgency) {
        AlertUrgency.OVERDUE -> "OVERDUE" to ColorOverdue
        AlertUrgency.TODAY -> "TODAY" to PashuAmber
        AlertUrgency.SOON -> "SOON" to PashuGreen
        AlertUrgency.UPCOMING -> "" to PashuGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isResolved)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Color strip
            Box(
                modifier = Modifier.size(4.dp, 60.dp).clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Text(alert.alertType.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        alert.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (label.isNotEmpty()) {
                        Surface(color = color, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        alert.animalName?.let { append("$it ") }
                            ?: alert.animalTag?.let { append("Tag #$it ") }
                        append("· due ${alert.dueDate}")
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                alert.message?.let {
                    Text(
                        it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2
                    )
                }
                if (!alert.isResolved) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = onResolve,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mark Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlertsState() {
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
            Icon(Icons.Filled.NotificationsOff, null,
                tint = PashuGreen, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("All caught up!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "New alerts will appear here when vaccinations, heat cycles, " +
                "or calving events are coming up",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
