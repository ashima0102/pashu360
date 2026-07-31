package com.pashu360.app.core.presentation.navigation

import androidx.compose.runtime.Composable
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
import com.pashu360.app.feature.auth.presentation.SplashScreen
import com.pashu360.app.feature.dashboard.presentation.DashboardScreen

@Composable
fun Pashu360NavHost(
    navController: NavHostController = rememberNavController()
) {
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
            RegisterScreen(
                onRegistered = { navController.navigate(Screen.FarmSetup.route) },
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

        // ── DASHBOARD ────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
    }
}
