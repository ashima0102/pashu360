# Navigation Graph
## Smart Dairy Farm Management System

---

## Route Sealed Class

```kotlin
sealed class Screen(val route: String) {
    // Auth Graph
    object Splash       : Screen("splash")
    object Login        : Screen("login")
    object OtpVerify    : Screen("otp_verify/{phone}") {
        fun createRoute(phone: String) = "otp_verify/$phone"
    }
    object Register     : Screen("register")
    object FarmSetup    : Screen("farm_setup")

    // Main Graph — Bottom Nav
    object Dashboard    : Screen("dashboard")
    object Animals      : Screen("animals")
    object Milk         : Screen("milk")
    object Alerts       : Screen("alerts")
    object More         : Screen("more")

    // Animal Sub-screens
    object AnimalDetail : Screen("animal_detail/{animalId}") {
        fun createRoute(animalId: String) = "animal_detail/$animalId"
    }
    object AddAnimal    : Screen("add_animal")
    object EditAnimal   : Screen("edit_animal/{animalId}") {
        fun createRoute(animalId: String) = "edit_animal/$animalId"
    }
    object QrScanner    : Screen("qr_scanner")

    // Milk Sub-screens
    object BulkMilkEntry   : Screen("bulk_milk_entry/{session}") {
        fun createRoute(session: String) = "bulk_milk_entry/$session"
    }
    object MilkHistory     : Screen("milk_history")
    object MilkAnalytics   : Screen("milk_analytics")

    // Vaccination
    object VaccinationSchedule : Screen("vaccination_schedule")
    object AddVaccination      : Screen("add_vaccination/{animalId}") {
        fun createRoute(animalId: String?) = "add_vaccination/${animalId ?: "all"}"
    }
    object VaccineCatalogue    : Screen("vaccine_catalogue")

    // Health
    object HealthOverview   : Screen("health_overview")
    object LogHealthEvent   : Screen("log_health/{animalId}") {
        fun createRoute(animalId: String) = "log_health/$animalId"
    }
    object HealthHistory    : Screen("health_history/{animalId}") {
        fun createRoute(animalId: String) = "health_history/$animalId"
    }

    // Feeding
    object FeedingOverview  : Screen("feeding_overview")
    object LogFeed          : Screen("log_feed")
    object FeedInventory    : Screen("feed_inventory")
    object FeedSchedule     : Screen("feed_schedule")

    // Heat & Breeding
    object HeatCalendar     : Screen("heat_calendar")
    object LogHeat          : Screen("log_heat/{animalId}") {
        fun createRoute(animalId: String) = "log_heat/$animalId"
    }
    object BreedingScreen   : Screen("breeding/{animalId}") {
        fun createRoute(animalId: String) = "breeding/$animalId"
    }
    object PregnancyTracking : Screen("pregnancy_tracking")
    object RecordCalving     : Screen("record_calving/{animalId}") {
        fun createRoute(animalId: String) = "record_calving/$animalId"
    }

    // Finance
    object FinanceDashboard : Screen("finance_dashboard")
    object LogIncome        : Screen("log_income")
    object LogExpense       : Screen("log_expense")
    object AnimalPnL        : Screen("animal_pnl/{animalId}") {
        fun createRoute(animalId: String) = "animal_pnl/$animalId"
    }

    // Reports
    object Reports          : Screen("reports")

    // Farm
    object FarmDetail       : Screen("farm_detail")
    object EditFarm         : Screen("edit_farm")
    object BarnList         : Screen("barn_list")

    // Settings
    object Settings         : Screen("settings")
    object Profile          : Screen("profile")
    object NotificationSettings : Screen("notification_settings")
}
```

---

## Root NavHost

```kotlin
@Composable
fun SmartDairyNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Auth graph
        authNavGraph(navController)

        // Main app with bottom navigation
        composable(Screen.Dashboard.route) { MainScaffold(navController) }
    }
}
```

---

## Auth Nav Graph

```kotlin
fun NavGraphBuilder.authNavGraph(navController: NavController) {
    composable(Screen.Splash.route) {
        SplashScreen(
            onNavigate = { destination ->
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.Login.route) {
        LoginScreen(
            onNavigateToOtp = { phone ->
                navController.navigate(Screen.OtpVerify.createRoute(phone))
            },
            onNavigateToRegister = {
                navController.navigate(Screen.Register.route)
            },
            onLoginSuccess = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = Screen.OtpVerify.route,
        arguments = listOf(navArgument("phone") { type = NavType.StringType })
    ) { backStackEntry ->
        OtpVerificationScreen(
            phone = backStackEntry.arguments?.getString("phone") ?: "",
            onVerified = { isFirstLogin ->
                val destination = if (isFirstLogin) Screen.FarmSetup.route
                                 else Screen.Dashboard.route
                navController.navigate(destination) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        )
    }

    composable(Screen.Register.route) {
        RegisterScreen(
            onRegistered = { navController.navigate(Screen.FarmSetup.route) },
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.FarmSetup.route) {
        FarmSetupScreen(
            onFarmCreated = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.FarmSetup.route) { inclusive = true }
                }
            }
        )
    }
}
```

---

## Main Nav Graph (Bottom Navigation)

```kotlin
@Composable
fun MainScaffold(rootNavController: NavController) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, Icons.Default.Dashboard, "Home"),
        BottomNavItem(Screen.Animals,   Icons.Default.Pets,      "Animals"),
        BottomNavItem(Screen.Milk,      Icons.Default.LocalDrink,"Milk"),
        BottomNavItem(Screen.Alerts,    Icons.Default.Notifications, "Alerts"),
        BottomNavItem(Screen.More,      Icons.Default.Menu,       "More"),
    )

    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute?.destination?.route == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onLogMilk = { navController.navigate(Screen.BulkMilkEntry.createRoute("morning")) },
                    onAddAnimal = { navController.navigate(Screen.AddAnimal.route) },
                    onAddVaccine = { navController.navigate(Screen.AddVaccination.createRoute(null)) },
                    onLogHealth = { navController.navigate(Screen.HealthOverview.route) },
                    onAlertClick = { animalId, tab -> navController.navigate(Screen.AnimalDetail.createRoute(animalId)) }
                )
            }

            // ── ANIMALS ──────────────────────────────────────────────
            composable(Screen.Animals.route) {
                AnimalListScreen(
                    onAnimalClick = { id -> navController.navigate(Screen.AnimalDetail.createRoute(id)) },
                    onAddAnimal = { navController.navigate(Screen.AddAnimal.route) },
                    onScanQr = { navController.navigate(Screen.QrScanner.route) }
                )
            }
            composable(Screen.QrScanner.route) {
                QrScannerScreen(
                    onAnimalFound = { id ->
                        navController.navigate(Screen.AnimalDetail.createRoute(id)) {
                            popUpTo(Screen.QrScanner.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddAnimal.route) {
                AddAnimalScreen(
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AnimalDetail.route,
                arguments = listOf(navArgument("animalId") { type = NavType.StringType })
            ) { backStack ->
                AnimalDetailScreen(
                    animalId = backStack.arguments?.getString("animalId")!!,
                    onLogMilk = { id -> navController.navigate(Screen.BulkMilkEntry.createRoute("morning")) },
                    onAddVaccine = { id -> navController.navigate(Screen.AddVaccination.createRoute(id)) },
                    onLogHealth = { id -> navController.navigate(Screen.LogHealthEvent.createRoute(id)) },
                    onEdit = { id -> navController.navigate(Screen.EditAnimal.createRoute(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── MILK ─────────────────────────────────────────────────
            composable(Screen.Milk.route) {
                MilkScreen(
                    onLogMorning = { navController.navigate(Screen.BulkMilkEntry.createRoute("morning")) },
                    onLogEvening = { navController.navigate(Screen.BulkMilkEntry.createRoute("evening")) },
                    onViewHistory = { navController.navigate(Screen.MilkHistory.route) },
                    onViewAnalytics = { navController.navigate(Screen.MilkAnalytics.route) }
                )
            }
            composable(
                route = Screen.BulkMilkEntry.route,
                arguments = listOf(navArgument("session") { type = NavType.StringType })
            ) { backStack ->
                BulkMilkEntryScreen(
                    session = backStack.arguments?.getString("session") ?: "morning",
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Screen.MilkHistory.route) { MilkHistoryScreen() }
            composable(Screen.MilkAnalytics.route) { MilkAnalyticsScreen() }

            // ── ALERTS ───────────────────────────────────────────────
            composable(Screen.Alerts.route) {
                NotificationCenterScreen(
                    onAlertClick = { alert ->
                        // Deep-link to relevant screen based on alert type
                        when (alert.alertType) {
                            "vaccination_due" -> navController.navigate(
                                Screen.AnimalDetail.createRoute(alert.animalId ?: return@NotificationCenterScreen))
                            "heat_expected" -> navController.navigate(Screen.HeatCalendar.route)
                            "calving_due" -> navController.navigate(Screen.PregnancyTracking.route)
                            else -> {}
                        }
                    }
                )
            }

            // ── MORE ─────────────────────────────────────────────────
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.VaccinationSchedule.route) {
                VaccinationScheduleScreen(
                    onAddVaccination = { navController.navigate(Screen.AddVaccination.createRoute(null)) },
                    onViewCatalogue = { navController.navigate(Screen.VaccineCatalogue.route) }
                )
            }
            composable(Screen.FeedingOverview.route) { FeedingOverviewScreen(navController) }
            composable(Screen.LogFeed.route) { LogFeedScreen(onSaved = { navController.popBackStack() }) }
            composable(Screen.FeedInventory.route) { FeedInventoryScreen() }
            composable(Screen.HealthOverview.route) { HealthOverviewScreen(navController) }
            composable(Screen.FinanceDashboard.route) { FinanceDashboardScreen(navController) }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
```

---

## Navigation Map (Visual)

```
Splash
 └── Login / OtpVerify / Register / FarmSetup
       └── Main (BottomNav)
             ├── Dashboard
             │     ├── → BulkMilkEntry
             │     ├── → AddAnimal
             │     ├── → AddVaccination
             │     └── → LogHealth
             ├── Animals
             │     ├── AnimalList
             │     ├── → QrScanner
             │     ├── → AddAnimal
             │     └── → AnimalDetail
             │           ├── (Overview tab)
             │           ├── (Milk tab) → BulkMilkEntry
             │           ├── (Vaccination tab) → AddVaccination
             │           ├── (Feeding tab) → LogFeed
             │           ├── (Health tab) → LogHealthEvent
             │           └── (Breeding tab) → AddBreeding
             ├── Milk
             │     ├── → BulkMilkEntry (morning)
             │     ├── → BulkMilkEntry (evening)
             │     ├── → MilkHistory
             │     └── → MilkAnalytics
             ├── Alerts
             │     └── → deep-link any animal screen
             └── More
                   ├── → VaccinationSchedule → AddVaccination, VaccineCatalogue
                   ├── → FeedingOverview → LogFeed, FeedInventory, FeedSchedule
                   ├── → HealthOverview → LogHealthEvent, HealthHistory
                   ├── → HeatCalendar → LogHeat
                   ├── → PregnancyTracking → RecordCalving
                   ├── → FinanceDashboard → LogIncome, LogExpense, AnimalPnL
                   ├── → Reports
                   ├── → FarmDetail → EditFarm, BarnList
                   └── → Settings → Profile, NotificationSettings
```

---

## Deep Link Configuration (AndroidManifest.xml)

```xml
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="smartdairy" android:host="animal"/>
    </intent-filter>
    <!-- FCM notification tap → deep link -->
    <!-- smartdairy://animal/{animalId} → AnimalDetailScreen -->
    <!-- smartdairy://alerts → NotificationCenterScreen -->
    <!-- smartdairy://vaccination → VaccinationScheduleScreen -->
</activity>
```
