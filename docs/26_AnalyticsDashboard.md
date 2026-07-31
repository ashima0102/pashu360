# Analytics Dashboard
## Smart Dairy Farm Management System

---

## Analytics Screens Overview

```
Analytics (accessible from Milk tab)
├── Milk Analytics Screen
│   ├── Monthly comparison chart
│   ├── Lactation curve per animal
│   ├── Top producers ranking
│   └── Average production trend
│
├── Herd Analytics Screen
│   ├── Status breakdown (pie chart)
│   ├── Breed distribution
│   ├── Age distribution
│   └── Calving intervals
│
├── Health Analytics Screen
│   ├── Disease frequency (top 5)
│   ├── Vaccination compliance %
│   ├── Average BCS trend
│   └── Sick days per animal
│
└── Financial Analytics Screen
    ├── Income vs Expense trend
    ├── Cost per litre produced
    ├── Top earner animals
    └── Monthly P&L trend
```

---

## Milk Analytics Screen

```kotlin
@Composable
fun MilkAnalyticsScreen(
    viewModel: MilkViewModel = hiltViewModel()
) {
    val uiState by viewModel.analyticsState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period selector
        item {
            SegmentedButton(
                options = listOf("7 Days", "30 Days", "3 Months", "6 Months"),
                selected = uiState.selectedPeriod,
                onSelected = viewModel::onPeriodChanged
            )
        }

        // Total summary
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Total This Month", "${uiState.monthTotal}L", Modifier.weight(1f))
                MetricCard("Daily Average", "${uiState.dailyAverage}L", Modifier.weight(1f))
                MetricCard("vs Last Month", "${uiState.vsLastMonth}%",
                    color = if (uiState.vsLastMonth >= 0) ColorActive else ColorSick,
                    modifier = Modifier.weight(1f))
            }
        }

        // Monthly production trend
        item {
            ChartCard(title = "Production Trend") {
                ProductionTrendChart(data = uiState.dailyTotals)
            }
        }

        // Morning vs Evening split
        item {
            ChartCard(title = "Morning vs Evening") {
                SessionSplitChart(
                    morningTotal = uiState.morningTotal,
                    eveningTotal = uiState.eveningTotal
                )
            }
        }

        // Top producers
        item {
            SectionHeader("Top Producers This Month")
        }
        items(uiState.topProducers) { producer ->
            TopProducerRow(
                rank = uiState.topProducers.indexOf(producer) + 1,
                animalName = producer.animalName,
                tagId = producer.tagId,
                total = producer.totalLitres,
                daily = producer.dailyAverage
            )
        }

        // Lactation curve for selected animal
        item {
            ChartCard(title = "Lactation Curve") {
                Text("Select an animal to view",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Animal selector → shows 305-day curve
                LactationCurveChart(data = uiState.lactationData)
            }
        }
    }
}
```

---

## Key Charts (Vico Configurations)

### Production Trend (Line Chart)
```kotlin
@Composable
fun ProductionTrendChart(data: List<DailyTotal>) {
    val lineSpec = lineSpec(
        line = lineComponent(color = MaterialTheme.colorScheme.primary, thickness = 2.dp),
        point = shapeComponent(shape = Shapes.pillShape, color = MaterialTheme.colorScheme.primary),
        pointSize = 6.dp
    )

    Chart(
        chart = lineChart(lines = listOf(lineSpec)),
        model = entryModelOf(*data.mapIndexed { i, d -> i.toFloat() entryOf d.total.toFloat() }.toTypedArray()),
        startAxis = rememberStartAxis(valueFormatter = { v, _ -> "${v.toInt()}L" }),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { v, _ -> data.getOrNull(v.toInt())?.recordDate?.dayOfMonth?.toString() ?: "" }
        ),
        modifier = Modifier.height(180.dp)
    )
}
```

### Session Split (Donut Chart — Compose Canvas)
```kotlin
@Composable
fun SessionSplitChart(morningTotal: Double, eveningTotal: Double) {
    val total = morningTotal + eveningTotal
    val morningFraction = (morningTotal / total).toFloat()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 20.dp.toPx()
            drawArc(color = primaryColor, startAngle = -90f,
                sweepAngle = 360f * morningFraction, useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(color = secondaryColor, startAngle = -90f + 360f * morningFraction,
                sweepAngle = 360f * (1 - morningFraction), useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${morningTotal.toInt()}L", style = MaterialTheme.typography.titleMedium)
            Text("Morning", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    // Legend
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(primaryColor, "Morning ${morningTotal.toInt()}L")
        LegendItem(secondaryColor, "Evening ${eveningTotal.toInt()}L")
    }
}
```

### Lactation Curve (305-Day)
```kotlin
@Composable
fun LactationCurveChart(actual: List<Pair<Int, Double>>, predicted: List<Pair<Int, Double>>) {
    // Shows actual (solid line) vs predicted (dashed line) lactation curve
    // Day 0 = calving, Day 305 = dry off
    // Peak production typically at Day 45-60

    val actualEntries = actual.map { (day, litres) -> day.toFloat() entryOf litres.toFloat() }
    val predictedEntries = predicted.map { (day, litres) -> day.toFloat() entryOf litres.toFloat() }

    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(color = MaterialTheme.colorScheme.primary),   // Actual
                lineSpec(color = MaterialTheme.colorScheme.secondary,  // Predicted
                    lineBackgroundShader = null)
            )
        ),
        model = entryModelOf(actualEntries, predictedEntries),
        startAxis = rememberStartAxis(valueFormatter = { v, _ -> "${v.toInt()}L" }),
        bottomAxis = rememberBottomAxis(
            valueFormatter = { v, _ -> "Day ${v.toInt()}" }
        ),
        modifier = Modifier.height(200.dp)
    )
}
```

---

## Vaccination Compliance Analytics

```kotlin
// Vaccination compliance %
// (vaccinations given on time / total vaccinations due) × 100

data class VaccinationComplianceStats(
    val compliancePercent: Double,
    val givenOnTime: Int,
    val overdue: Int,
    val upcoming: Int,
    val byVaccine: List<VaccineComplianceItem>
)

@Composable
fun VaccinationAnalyticsCard(stats: VaccinationComplianceStats) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Vaccination Compliance",
                        style = MaterialTheme.typography.titleMedium)
                    Text("Last 12 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Circular progress showing compliance %
                ComplianceCircle(percent = stats.compliancePercent)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ComplianceStat("✅ On Time", stats.givenOnTime.toString(),
                    ColorActive, Modifier.weight(1f))
                ComplianceStat("🔴 Overdue", stats.overdue.toString(),
                    ColorOverdue, Modifier.weight(1f))
                ComplianceStat("🕐 Upcoming", stats.upcoming.toString(),
                    ColorDueToday, Modifier.weight(1f))
            }
        }
    }
}
```

---

## KPI Cards (Key Performance Indicators)

```kotlin
// Farm-level KPIs shown in Analytics overview

data class FarmKPIs(
    val milkPerCowPerDay: Double,       // litres
    val conceptionRate: Double,          // %
    val vaccinationCompliance: Double,   // %
    val averageBCS: Double,              // 1-5
    val calvingInterval: Int,            // days
    val daysInMilk: Double,             // average
    val feedConversionRatio: Double      // kg feed / kg milk
)

@Composable
fun KpiRow(kpis: FarmKPIs) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { KpiCard("Milk/Cow/Day", "${kpis.milkPerCowPerDay}L", benchmark = "8-10L", Colors.Primary) }
        item { KpiCard("Conception Rate", "${kpis.conceptionRate}%", benchmark = ">60%", Colors.Secondary) }
        item { KpiCard("Vaccination", "${kpis.vaccinationCompliance}%", benchmark = ">90%", Colors.Green) }
        item { KpiCard("Avg BCS", "${kpis.averageBCS}", benchmark = "3.0-3.5", Colors.Orange) }
        item { KpiCard("Calving Interval", "${kpis.calvingInterval}d", benchmark = "<400d", Colors.Blue) }
    }
}
```

---

## Analytics Data Queries (Room)

```kotlin
@Dao
interface AnalyticsDao {
    // Monthly milk totals for comparison
    @Query("""
        SELECT 
            strftime('%Y-%m', record_date) as month,
            SUM(quantity_liters) as total,
            AVG(quantity_liters) as daily_avg
        FROM milk_records 
        WHERE farm_id = :farmId
        GROUP BY month
        ORDER BY month DESC
        LIMIT 12
    """)
    fun getMonthlyMilkTotals(farmId: String): Flow<List<MonthlyMilkStat>>

    // Top producers
    @Query("""
        SELECT a.id, a.name, a.tag_id,
               SUM(m.quantity_liters) as total_litres,
               AVG(m.quantity_liters) as daily_avg
        FROM animals a
        JOIN milk_records m ON m.animal_id = a.id
        WHERE a.farm_id = :farmId
        AND m.record_date BETWEEN :startDate AND :endDate
        GROUP BY a.id
        ORDER BY total_litres DESC
        LIMIT 10
    """)
    fun getTopProducers(farmId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<TopProducer>>

    // Disease frequency
    @Query("""
        SELECT 
            unnest(symptoms) as symptom,
            COUNT(*) as frequency
        FROM diseases
        WHERE animal_id IN (SELECT id FROM animals WHERE farm_id = :farmId)
        GROUP BY symptom
        ORDER BY frequency DESC
        LIMIT 10
    """)
    fun getTopSymptoms(farmId: String): Flow<List<SymptomFrequency>>
}
```
