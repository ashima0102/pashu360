package com.pashu360.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Pashu360App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_VACCINATION,
                    "Vaccination Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Alerts for upcoming vaccination due dates" },

                NotificationChannel(
                    CHANNEL_HEAT,
                    "Heat Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Heat detection and expected heat alerts" },

                NotificationChannel(
                    CHANNEL_CALVING,
                    "Calving Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Expected calving date reminders" },

                NotificationChannel(
                    CHANNEL_FEEDING,
                    "Feeding Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Feed reminders and low stock alerts" },

                NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General farm management alerts" }
            )

            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_VACCINATION = "vaccination_alerts"
        const val CHANNEL_HEAT = "heat_alerts"
        const val CHANNEL_CALVING = "calving_alerts"
        const val CHANNEL_FEEDING = "feeding_alerts"
        const val CHANNEL_GENERAL = "general_alerts"
    }
}
