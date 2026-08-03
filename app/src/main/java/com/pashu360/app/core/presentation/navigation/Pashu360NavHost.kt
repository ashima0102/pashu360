package com.pashu360.app.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pashu360.app.feature.auth.presentation.FarmSetupScreen
import com.pashu360.app.feature.auth.presentation.LoginScreen
import com.pashu360.app.feature.auth.presentation.OtpVerificationScreen
import com.pashu360.app.feature.auth.presentation.RegisterScreen
import com.pashu360.app.feature.auth.presentation.RegisterViewModel
import com.pashu360.app.feature.auth.presentation.SplashScreen
import com.pashu360.app.DeepLink

@Composable
fun Pashu360NavHost(
    navController: NavHostController = rememberNavController(),
    pendingDeepLink: DeepLink? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    // When a notification-tap wakes the app with pashu360://animal/{id},
    // jump past Splash to Dashboard immediately; MainScaffold's nested
    // NavHost consumes the DeepLink and navigates to AnimalDetail.
    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink is DeepLink.Animal) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── SPLASH ─────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigate = { isAuthenticated, isFirstLogin ->
                    val destination = when {
                        !isAuthenticated -> Screen.Login.route
                        isFirstLogin -> Screen.FarmSetup.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── LOGIN ──────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToOtp = { phone ->
                    navController.navigate(Screen.OtpVerify.createRoute(phone))
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── OTP VERIFY ────────────────────────────────────
        composable(
            route = Screen.OtpVerify.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OtpVerificationScreen(
                phone = phone,
                onVerified = { isFirstLogin ->
                    val destination = if (isFirstLogin) Screen.FarmSetup.route
                                      else Screen.Dashboard.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── REGISTER ──────────────────────────────────────
        composable(Screen.Register.route) {
            val vm: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                onRegistered = { fullName ->
                    vm.stashOwnerName(fullName)
                    navController.navigate(Screen.FarmSetup.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── FARM SETUP ───────────────────────────────────
        composable(Screen.FarmSetup.route) {
            FarmSetupScreen(
                onFarmCreated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.FarmSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // ── MAIN APP (bottom nav) ────────────────────────
        composable(Screen.Dashboard.route) {
            MainScaffold(
                rootNavController = navController,
                pendingDeepLink = pendingDeepLink,
                onDeepLinkConsumed = onDeepLinkConsumed
            )
        }
    }
}
