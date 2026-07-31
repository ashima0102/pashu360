package com.pashu360.app.core.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.pashu360.app.feature.animal.presentation.AddAnimalScreen
import com.pashu360.app.feature.animal.presentation.AnimalDetailScreen
import com.pashu360.app.feature.animal.presentation.AnimalListScreen
import com.pashu360.app.feature.animal.presentation.QrScannerScreen
import com.pashu360.app.feature.dashboard.presentation.DashboardScreen

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScaffold(
    rootNavController: NavController,
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, Icons.Filled.Home, "Home"),
        BottomNavItem(Screen.Animals, Icons.Filled.Pets, "Animals"),
        BottomNavItem(Screen.Milk, Icons.Filled.LocalDrink, "Milk"),
        BottomNavItem(Screen.Alerts, Icons.Filled.Notifications, "Alerts"),
        BottomNavItem(Screen.More, Icons.Filled.Menu, "More"),
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Only show bottom nav on top-level routes
    val showBottomNav = currentRoute in bottomNavItems.map { it.screen.route }

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
            // Bottom-tab destinations
            composable(Screen.Dashboard.route) { DashboardScreen() }

            composable(Screen.Animals.route) {
                AnimalListScreen(
                    onAnimalClick = { id -> navController.navigate(Screen.AnimalDetail.createRoute(id)) },
                    onAddAnimalClick = { navController.navigate(Screen.AddAnimal.route) },
                    onScanQrClick = { navController.navigate(Screen.QrScanner.route) }
                )
            }

            composable(Screen.Milk.route) {
                ComingSoonScreen(
                    title = "Milk Production",
                    subtitle = "Log morning + evening milk for your entire herd, track daily/monthly production, and see lactation trends.",
                    icon = Icons.Filled.LocalDrink
                )
            }

            composable(Screen.Alerts.route) {
                ComingSoonScreen(
                    title = "Alerts & Reminders",
                    subtitle = "Never miss a vaccination, heat cycle, or calving. All your farm alerts in one place.",
                    icon = Icons.Filled.Notifications
                )
            }

            composable(Screen.More.route) {
                ComingSoonScreen(
                    title = "More Features",
                    subtitle = "Health, feeding, breeding, finance, reports and settings.",
                    icon = Icons.Filled.Settings
                )
            }

            // Detail / sub-screens (hide bottom nav)
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
