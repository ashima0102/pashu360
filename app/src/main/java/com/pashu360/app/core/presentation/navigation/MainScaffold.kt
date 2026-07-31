package com.pashu360.app.core.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pashu360.app.feature.notifications.presentation.AlertBadgeViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pashu360.app.core.presentation.components.ComingSoonScreen
import com.pashu360.app.core.presentation.components.PashuDrawer
import com.pashu360.app.feature.animal.presentation.AddAnimalScreen
import com.pashu360.app.feature.animal.presentation.AnimalDetailScreen
import com.pashu360.app.feature.animal.presentation.AnimalListScreen
import com.pashu360.app.feature.animal.presentation.QrScannerScreen
import com.pashu360.app.feature.dashboard.presentation.DashboardScreen
import com.pashu360.app.feature.finance.presentation.FinanceScreen
import com.pashu360.app.feature.health.presentation.HealthScreen
import com.pashu360.app.feature.milk.presentation.MilkScreen
import com.pashu360.app.feature.notifications.presentation.AlertsScreen
import kotlinx.coroutines.launch

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScaffold(
    rootNavController: NavController,
    navController: NavHostController = rememberNavController(),
    badgeViewModel: AlertBadgeViewModel = hiltViewModel()
) {
    val alertCount by badgeViewModel.unresolvedCount.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, Icons.Filled.Home, "Home"),
        BottomNavItem(Screen.Animals, Icons.Filled.Pets, "Animals"),
        BottomNavItem(Screen.Milk, Icons.Filled.LocalDrink, "Milk"),
        BottomNavItem(Screen.Health, Icons.Filled.Favorite, "Health"),
        BottomNavItem(Screen.Finance, Icons.Filled.AccountBalance, "Finance"),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavItems.map { it.screen.route }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PashuDrawer(
                userName = "Ramesh Sharma",       // TODO: from SessionStore
                farmName = "Sharma Dairy Farm",   // TODO: from SessionStore
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    if (route == "dashboard") {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(route)
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    rootNavController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        gesturesEnabled = showBottomNav   // Drawer only opens from top-level screens
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding)
            ) {
                // ── BOTTOM TAB DESTINATIONS ─────────────────────
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        alertCount = alertCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBellClick = { navController.navigate(Screen.Alerts.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Animals.route) {
                    AnimalListScreen(
                        onAnimalClick = { id -> navController.navigate(Screen.AnimalDetail.createRoute(id)) },
                        onAddAnimalClick = { navController.navigate(Screen.AddAnimal.route) },
                        onScanQrClick = { navController.navigate(Screen.QrScanner.route) },
                        alertCount = alertCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBellClick = { navController.navigate(Screen.Alerts.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Milk.route) {
                    MilkScreen(
                        alertCount = alertCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBellClick = { navController.navigate(Screen.Alerts.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Health.route) {
                    HealthScreen(
                        alertCount = alertCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBellClick = { navController.navigate(Screen.Alerts.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Finance.route) {
                    FinanceScreen(
                        alertCount = alertCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBellClick = { navController.navigate(Screen.Alerts.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                // ── HEADER ACTIONS ──────────────────────────────
                composable(Screen.Alerts.route) {
                    AlertsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenAnimal = { id ->
                            navController.navigate(Screen.AnimalDetail.createRoute(id))
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ComingSoonScreen(
                        title = "Profile",
                        subtitle = "Edit your name, phone, photo. Manage account preferences.",
                        icon = Icons.Filled.Settings
                    )
                }

                // ── DRAWER DESTINATIONS ─────────────────────────
                composable(Screen.FarmInfo.route) {
                    ComingSoonScreen(
                        title = "Farm Info",
                        subtitle = "Edit your farm details: name, location, GPS, barns.",
                        icon = Icons.Filled.Home
                    )
                }
                composable(Screen.Feeding.route) {
                    ComingSoonScreen(
                        title = "Feeding",
                        subtitle = "Log feed schedules, inventory, and per-animal feed cost.",
                        icon = Icons.Filled.Grass
                    )
                }
                composable(Screen.Breeding.route) {
                    ComingSoonScreen(
                        title = "Breeding",
                        subtitle = "Heat cycles, AI records, and conception tracking.",
                        icon = Icons.Filled.Favorite
                    )
                }
                composable(Screen.Pregnancy.route) {
                    ComingSoonScreen(
                        title = "Pregnancy",
                        subtitle = "Track pregnancies, dry period, and expected calving dates.",
                        icon = Icons.Filled.PregnantWoman
                    )
                }
                composable(Screen.Reports.route) {
                    ComingSoonScreen(
                        title = "Reports",
                        subtitle = "Milk, health, vaccination, financial reports. Export as PDF/CSV.",
                        icon = Icons.Filled.Home
                    )
                }
                composable(Screen.Settings.route) {
                    ComingSoonScreen(
                        title = "Settings",
                        subtitle = "Language, theme, notifications, backup and data export.",
                        icon = Icons.Filled.Settings
                    )
                }
                composable(Screen.Help.route) {
                    ComingSoonScreen(
                        title = "Help & Support",
                        subtitle = "FAQs, contact support, and user guides.",
                        icon = Icons.Filled.Settings
                    )
                }

                // ── ANIMAL SUB-SCREENS ──────────────────────────
                composable(
                    route = Screen.AnimalDetail.route,
                    arguments = listOf(navArgument("animalId") { type = NavType.StringType })
                ) { entry ->
                    val animalId = entry.arguments?.getString("animalId") ?: return@composable
                    AnimalDetailScreen(
                        animalId = animalId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AddAnimal.route) {
                    AddAnimalScreen(
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
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
            }
        }
    }
}
