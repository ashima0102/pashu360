package com.pashu360.app.core.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Graph
    object Splash : Screen("splash")
    object Login : Screen("login")
    object OtpVerify : Screen("otp_verify/{phone}") {
        fun createRoute(phone: String) = "otp_verify/$phone"
    }
    object Register : Screen("register")
    object FarmSetup : Screen("farm_setup")

    // Main Graph — Bottom Nav (5 tabs)
    object Dashboard : Screen("dashboard")
    object Animals : Screen("animals")
    object Milk : Screen("milk")
    object Health : Screen("health")
    object Finance : Screen("finance")

    // Header actions
    object Alerts : Screen("alerts")
    object Profile : Screen("profile")

    // Drawer destinations
    object FarmInfo : Screen("farm_info")
    object Feeding : Screen("feeding")
    object Breeding : Screen("breeding")
    object Pregnancy : Screen("pregnancy")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Help : Screen("help")

    // Animal sub-screens
    object AnimalDetail : Screen("animal_detail/{animalId}") {
        fun createRoute(animalId: String) = "animal_detail/$animalId"
    }
    object AddAnimal : Screen("add_animal")
    object QrScanner : Screen("qr_scanner")

    // Milk sub-screens
    object BulkMilkEntry : Screen("bulk_milk_entry/{session}") {
        fun createRoute(session: String) = "bulk_milk_entry/$session"
    }
    object MilkHistory : Screen("milk_history")

    // Vaccination
    object VaccinationSchedule : Screen("vaccination_schedule")
    object AddVaccination : Screen("add_vaccination")

    // Health
    object LogHealthEvent : Screen("log_health_event")

    // Feeding
    object LogFeed : Screen("log_feed")
}
