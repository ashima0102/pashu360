# Dashboard Design
## Smart Dairy Farm Management System

---

## Dashboard Layout

```
┌────────────────────────────────────────┐
│  HEADER                                │
│  Good Morning, Ramesh!                 │
│  Sharma Dairy Farm  •  Wed, 30 Jul     │
│  [☁️ Synced]                           │
├────────────────────────────────────────┤
│  STAT CARDS (horizontal scroll)        │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │  25  │ │ 42L  │ │  3   │ │  1   │  │
│  │Cows  │ │Milk  │ │Vacc  │ │Sick  │  │
│  │Total │ │Today │ │Due   │ │Now   │  │
│  └──────┘ └──────┘ └──────┘ └──────┘  │
│  ┌──────┐ ┌──────┐                    │
│  │  2   │ │  5   │                    │
│  │Preg- │ │Calv  │                    │
│  │nant  │ │Soon  │                    │
│  └──────┘ └──────┘                    │
├────────────────────────────────────────┤
│  QUICK ACTIONS                         │
│  ┌────────┐ ┌────────┐ ┌────────┐     │
│  │  🥛    │ │  💉    │ │  🐄    │     │
│  │Log     │ │Add     │ │Add     │     │
│  │Milk    │ │Vaccine │ │Animal  │     │
│  └────────┘ └────────┘ └────────┘     │
│  ┌────────┐ ┌────────┐               │
│  │  ❤️    │ │  🌿    │               │
│  │Log     │ │Log     │               │
│  │Health  │ │Feed    │               │
│  └────────┘ └────────┘               │
├────────────────────────────────────────┤
│  TODAY'S ALERTS (3)                    │
│  ┌──────────────────────────────────┐  │
│  │ 💉 FMD Vaccine — Gouri #4 —TODAY │  │
│  │                      [Mark Done] │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │ ♨️ Heat — Rani #7 — Tomorrow     │  │
│  │                      [Mark Done] │  │
│  └──────────────────────────────────┘  │
│  [View All 8 Alerts →]                │
├────────────────────────────────────────┤
│  THIS WEEK'S MILK                      │
│  ┌──────────────────────────────────┐  │
│  │  Bar chart: Mon–Sun              │  │
│  │  ████ ████ ████ ████ ████        │  │
│  │   38   40   41   42   35L        │  │
│  └──────────────────────────────────┘  │
├────────────────────────────────────────┤
│  RECENT ACTIVITY                       │
│  🥛 Morning milk — 42L — 7:20 AM      │
│  💉 FMD — Lakshmi — 29 Jul            │
│  🐄 Added Ganga (Tag #26) — 28 Jul    │
├────────────────────────────────────────┤
│  🏠Home │🐄Animals│🥛Milk│🔔 (3)│☰   │
└────────────────────────────────────────┘
```

---

## Dashboard Data Sources

| Widget | Data Query | Refresh |
|---|---|---|
| Total Animals | `COUNT(*) WHERE status IN (active, pregnant, dry, sick)` | On load |
| Today's Milk | `SUM(quantity_liters) WHERE date = today` | On load + after milk entry |
| Vaccinations Due | `COUNT(*) WHERE next_due_date <= today + 7` | On load |
| Sick Animals | `COUNT(*) WHERE is_active disease exists` | On load |
| Pregnant Animals | `COUNT(*) WHERE status = 'pregnant'` | On load |
| Calving Soon | `COUNT(*) WHERE expected_calving BETWEEN today AND today+14` | On load |
| Today's Alerts | `alerts WHERE is_resolved=false AND due_date <= today + 3` | Realtime |
| Weekly Milk Chart | `SUM daily for past 7 days` | On load |
| Recent Activity | Last 5 events across all tables | On load |

---

## DashboardViewModel.kt

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getAlertsUseCase: GetAlertsUseCase,
    private val getWeeklyMilkUseCase: GetWeeklyMilkUseCase,
    private val resolveAlertUseCase: ResolveAlertUseCase,
    private val activeFarmPrefs: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val farmId = activeFarmPrefs.getActiveFarmId() ?: return

        viewModelScope.launch {
            combine(
                getDashboardStatsUseCase(farmId),
                getAlertsUseCase(farmId, limit = 3),
                getWeeklyMilkUseCase(farmId)
            ) { stats, alerts, weeklyMilk ->
                _uiState.update { it.copy(
                    stats = stats,
                    todayAlerts = alerts,
                    weeklyMilkData = weeklyMilk,
                    isLoading = false
                )}
            }.collect()
        }
    }

    fun onAlertResolved(alertId: String) {
        viewModelScope.launch {
            resolveAlertUseCase(alertId)
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: DashboardStats = DashboardStats(),
    val todayAlerts: List<Alert> = emptyList(),
    val weeklyMilkData: List<DailyTotal> = emptyList(),
    val userName: String = "",
    val farmName: String = ""
)

data class DashboardStats(
    val totalAnimals: Int = 0,
    val todayMilkLitres: Double = 0.0,
    val vaccinationsDue: Int = 0,
    val sickAnimals: Int = 0,
    val pregnantAnimals: Int = 0,
    val calvingSoon: Int = 0
)
```

---

## DashboardScreen.kt

```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onLogMilk: () -> Unit,
    onAddAnimal: () -> Unit,
    onAddVaccine: () -> Unit,
    onLogHealth: () -> Unit,
    onLogFeed: () -> Unit,
    onViewAllAlerts: () -> Unit,
    onAlertClick: (Alert) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting = getGreeting()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header
        item {
            DashboardHeader(
                greeting = greeting,
                farmName = uiState.farmName,
                syncStatus = uiState.syncStatus
            )
        }

        // Stat cards
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { StatCard("Total Animals", "${uiState.stats.totalAnimals}", Icons.Default.Pets) }
                item { StatCard("Milk Today", "${uiState.stats.todayMilkLitres}L", Icons.Default.LocalDrink,
                    color = MaterialTheme.colorScheme.tertiary) }
                item { StatCard("Vaccines Due", "${uiState.stats.vaccinationsDue}", Icons.Default.Vaccines,
                    color = if (uiState.stats.vaccinationsDue > 0) ColorOverdue else ColorActive) }
                item { StatCard("Sick", "${uiState.stats.sickAnimals}", Icons.Default.Sick,
                    color = if (uiState.stats.sickAnimals > 0) ColorSick else ColorActive) }
                item { StatCard("Pregnant", "${uiState.stats.pregnantAnimals}", Icons.Default.PregnantWoman,
                    color = ColorPregnant) }
                item { StatCard("Calving Soon", "${uiState.stats.calvingSoon}", Icons.Default.ChildCare) }
            }
        }

        // Quick actions
        item {
            SectionHeader("Quick Actions", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(Icons.Default.LocalDrink, "Log Milk",
                    MaterialTheme.colorScheme.tertiary, onLogMilk)
                QuickActionButton(Icons.Default.Vaccines, "Add Vaccine",
                    MaterialTheme.colorScheme.secondary, onAddVaccine)
                QuickActionButton(Icons.Default.AddCircle, "Add Animal",
                    MaterialTheme.colorScheme.primary, onAddAnimal)
                QuickActionButton(Icons.Default.Favorite, "Log Health",
                    Color(0xFFD32F2F), onLogHealth)
                QuickActionButton(Icons.Default.Grass, "Log Feed",
                    Color(0xFF558B2F), onLogFeed)
            }
        }

        // Today's alerts
        if (uiState.todayAlerts.isNotEmpty()) {
            item {
                SectionHeader(
                    "Today's Alerts",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    action = { TextButton(onClick = onViewAllAlerts) { Text("View all") } }
                )
            }
            items(uiState.todayAlerts, key = { it.id }) { alert ->
                AlertCard(
                    alert = alert,
                    onMarkDone = { viewModel.onAlertResolved(alert.id) },
                    onClick = { onAlertClick(alert) }
                )
            }
        }

        // Weekly milk chart
        item {
            SectionHeader("This Week's Milk", Modifier.padding(16.dp))
            WeeklyMilkChart(data = uiState.weeklyMilkData)
        }
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11  -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else      -> "Good Evening"
    }
}
```

---

## Weekly Milk Chart (Vico)

```kotlin
@Composable
fun WeeklyMilkChart(data: List<DailyTotal>) {
    if (data.isEmpty()) return

    val entries = data.mapIndexed { index, total ->
        index.toFloat() entryOf total.total.toFloat()
    }

    val chartEntryModel = entryModelOf(*entries.toTypedArray())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(160.dp)
    ) {
        Chart(
            chart = columnChart(
                columns = listOf(
                    lineComponent(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 24.dp,
                        shape = MaterialTheme.shapes.small.toVicoShape()
                    )
                )
            ),
            model = chartEntryModel,
            startAxis = rememberStartAxis(
                valueFormatter = { value, _ -> "${value.toInt()}L" }
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { value, _ ->
                    val dayOfWeek = data.getOrNull(value.toInt())
                    dayOfWeek?.recordDate?.dayOfWeek?.name?.take(3) ?: ""
                }
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
```
