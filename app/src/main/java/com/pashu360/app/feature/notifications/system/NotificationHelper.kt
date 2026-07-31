package com.pashu360.app.feature.notifications.system

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pashu360.app.MainActivity
import com.pashu360.app.R
import com.pashu360.app.core.domain.model.Alert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns an Alert domain object into an Android system notification.
 * Channels are already created in Pashu360App.onCreate().
 * Deep-link URI matches the intent-filter in AndroidManifest.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun show(alert: Alert) {
        val nm = NotificationManagerCompat.from(context)

        // Build deep link URI
        val uri = when {
            alert.animalId != null -> "pashu360://animal/${alert.animalId}"
            else -> "pashu360://alerts"
        }
        val deepIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            deepIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = buildString {
            append(alert.message ?: "")
            alert.animalName?.let {
                if (isNotEmpty()) append(" · ")
                append(it)
            } ?: alert.animalTag?.let {
                if (isNotEmpty()) append(" · ")
                append("Tag #$it")
            }
        }.ifBlank { alert.title }

        val builder = NotificationCompat.Builder(context, alert.alertType.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${alert.alertType.emoji} ${alert.title}")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)

        // Notification ID is stable per alert so re-firing won't duplicate
        val notificationId = alert.id.hashCode()

        // POST_NOTIFICATIONS permission check on Android 13+
        try {
            nm.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Notification permission not granted — silently skip
        }
    }
}
