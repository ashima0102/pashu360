# Notification Flow
## Smart Dairy Farm Management System

---

## Complete Flow Diagram

```
Supabase Edge Function (Daily Cron — 6:00 AM IST)
    │
    ├── Query: Vaccinations due in next 3 days
    ├── Query: Expected heats tomorrow
    ├── Query: Expected calvings in next 7 days
    ├── Query: Overdue vaccines (already past due)
    ├── Query: Feed inventory below threshold
    └── Query: Medicines with withdrawal period ending
    │
    ├── INSERT rows into `alerts` table
    │         └── Triggers Supabase Realtime → Device updates Notification Center
    │
    └── Call FCM HTTP v1 API
              │
              └── FCM delivers to device
                        │
                        └── SmartDairyFirebaseMessagingService.kt
                                  │
                                  ├── [App in FOREGROUND]
                                  │     └── Show in-app notification banner
                                  │
                                  └── [App in BACKGROUND / KILLED]
                                        └── System tray notification
                                              │
                                              └── User taps
                                                    └── Deep-link → relevant screen
```

---

## Edge Function (TypeScript/Deno)

```typescript
// supabase/functions/send-daily-alerts/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const supabaseAdmin = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
)

const FCM_SERVER_KEY = Deno.env.get("FCM_SERVER_KEY")!
const FCM_URL = "https://fcm.googleapis.com/v1/projects/smart-dairy/messages:send"

serve(async (_req) => {
  const today = new Date()
  const todayStr = today.toISOString().split('T')[0]
  const in3Days = new Date(today.getTime() + 3 * 86400000).toISOString().split('T')[0]
  const in7Days = new Date(today.getTime() + 7 * 86400000).toISOString().split('T')[0]
  const tomorrow = new Date(today.getTime() + 86400000).toISOString().split('T')[0]

  let totalNotifications = 0

  // ── 1. VACCINATION ALERTS ──────────────────────────────────
  const { data: vaccinations } = await supabaseAdmin
    .from('vaccinations')
    .select(`
      id, next_due_date, animal_id,
      animals!inner(id, name, tag_id, farm_id,
        farms!inner(owner_id)),
      vaccine_catalogue!inner(name, disease_target)
    `)
    .gte('next_due_date', todayStr)
    .lte('next_due_date', in3Days)
    .eq('notification_sent', false)

  if (vaccinations) {
    for (const vacc of vaccinations) {
      const animal = vacc.animals as any
      const farm = animal.farms as any
      const vaccine = vacc.vaccine_catalogue as any

      const daysUntil = Math.ceil(
        (new Date(vacc.next_due_date).getTime() - today.getTime()) / 86400000
      )
      const urgency = daysUntil === 0 ? 'TODAY' : `in ${daysUntil} day${daysUntil > 1 ? 's' : ''}`

      await sendNotification({
        ownerId: farm.owner_id,
        title: `💉 Vaccination Due ${urgency}`,
        body: `${animal.name || 'Tag #' + animal.tag_id} — ${vaccine.name} (${vaccine.disease_target})`,
        data: {
          type: 'vaccination_due',
          animal_id: animal.id,
          deep_link: `smartdairy://animal/${animal.id}`
        }
      })

      // Mark as notified
      await supabaseAdmin
        .from('vaccinations')
        .update({ notification_sent: true })
        .eq('id', vacc.id)

      totalNotifications++
    }
  }

  // ── 2. HEAT ALERTS ────────────────────────────────────────
  const { data: heatAlerts } = await supabaseAdmin
    .from('alerts')
    .select(`
      id, animal_id, title, message, due_date,
      animals!inner(id, name, tag_id,
        farms!inner(owner_id))
    `)
    .eq('alert_type', 'heat_expected')
    .eq('due_date', tomorrow)
    .eq('notification_sent', false)
    .eq('is_resolved', false)

  if (heatAlerts) {
    for (const alert of heatAlerts) {
      const animal = alert.animals as any
      const farm = animal.farms as any

      await sendNotification({
        ownerId: farm.owner_id,
        title: '♨️ Heat Expected Tomorrow',
        body: `${animal.name || 'Tag #' + animal.tag_id} is expected to be in heat tomorrow. Plan AI/mating.`,
        data: {
          type: 'heat_expected',
          animal_id: animal.id,
          deep_link: `smartdairy://animal/${animal.id}`
        }
      })

      await supabaseAdmin
        .from('alerts')
        .update({ notification_sent: true })
        .eq('id', alert.id)

      totalNotifications++
    }
  }

  // ── 3. CALVING ALERTS ────────────────────────────────────
  const { data: calvingDue } = await supabaseAdmin
    .from('pregnancy_records')
    .select(`
      id, expected_calving_date, animal_id,
      animals!inner(id, name, tag_id,
        farms!inner(owner_id))
    `)
    .gte('expected_calving_date', todayStr)
    .lte('expected_calving_date', in7Days)
    .is('actual_calving_date', null)
    .is('calving_notification_sent', null)

  if (calvingDue) {
    for (const preg of calvingDue) {
      const animal = preg.animals as any
      const farm = animal.farms as any
      const daysUntil = Math.ceil(
        (new Date(preg.expected_calving_date).getTime() - today.getTime()) / 86400000
      )

      await sendNotification({
        ownerId: farm.owner_id,
        title: `🤱 Calving Due in ${daysUntil} Days`,
        body: `${animal.name || 'Tag #' + animal.tag_id} — Expected: ${preg.expected_calving_date}. Prepare calving area.`,
        data: {
          type: 'calving_due',
          animal_id: animal.id,
          deep_link: `smartdairy://animal/${animal.id}`
        }
      })
      totalNotifications++
    }
  }

  // ── 4. LOW FEED STOCK ALERTS ─────────────────────────────
  const { data: lowStock } = await supabaseAdmin
    .from('feed_inventory')
    .select(`
      id, quantity_kg, low_stock_threshold,
      feed_types!inner(name),
      farms!inner(id, owner_id)
    `)
    .lt('quantity_kg', 'low_stock_threshold')

  if (lowStock) {
    // Group by farm to send one notification per farm
    const byFarm = lowStock.reduce((acc: any, item: any) => {
      const farmId = item.farms.id
      if (!acc[farmId]) acc[farmId] = { ownerId: item.farms.owner_id, items: [] }
      acc[farmId].items.push(item.feed_types.name)
      return acc
    }, {})

    for (const [farmId, data] of Object.entries(byFarm) as any[]) {
      await sendNotification({
        ownerId: data.ownerId,
        title: '🌿 Low Feed Stock Alert',
        body: `Running low on: ${data.items.join(', ')}. Reorder soon.`,
        data: { type: 'low_feed_stock', deep_link: 'smartdairy://feeding/inventory' }
      })
      totalNotifications++
    }
  }

  return new Response(
    JSON.stringify({ success: true, notificationsSent: totalNotifications }),
    { headers: { "Content-Type": "application/json" } }
  )
})

async function sendNotification({
  ownerId,
  title,
  body,
  data
}: {
  ownerId: string
  title: string
  body: string
  data: Record<string, string>
}) {
  // Get FCM tokens for this user
  const { data: tokens } = await supabaseAdmin
    .from('notification_tokens')
    .select('fcm_token')
    .eq('user_id', ownerId)

  if (!tokens || tokens.length === 0) return

  // Send to each device
  for (const { fcm_token } of tokens) {
    await fetch(FCM_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${FCM_SERVER_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: {
          token: fcm_token,
          notification: { title, body },
          data,
          android: {
            priority: 'HIGH',
            notification: {
              channel_id: 'farm_alerts',
              click_action: 'OPEN_ACTIVITY_1'
            }
          }
        }
      })
    })
  }
}
```

---

## FCM Service (Android)

```kotlin
// SmartDairyFirebaseMessagingService.kt
@AndroidEntryPoint
class SmartDairyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationRepository: AlertRepository
    @Inject lateinit var userPrefs: UserPreferencesDataStore

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val alertType = data["type"] ?: return
        val deepLink = data["deep_link"]
        val animalId = data["animal_id"]

        // Show system notification
        showSystemNotification(
            title = remoteMessage.notification?.title ?: return,
            body = remoteMessage.notification?.body ?: return,
            deepLink = deepLink,
            alertType = alertType
        )

        // Save to local alerts table for Notification Center
        CoroutineScope(Dispatchers.IO).launch {
            val farmId = userPrefs.getActiveFarmId() ?: return@launch
            notificationRepository.insertLocalAlert(
                AlertEntity(
                    farmId = farmId,
                    animalId = animalId,
                    alertType = alertType,
                    title = remoteMessage.notification?.title ?: "",
                    message = remoteMessage.notification?.body
                )
            )
        }
    }

    override fun onNewToken(token: String) {
        // Upload new FCM token to Supabase
        CoroutineScope(Dispatchers.IO).launch {
            uploadFcmToken(token)
        }
    }

    private fun showSystemNotification(
        title: String,
        body: String,
        deepLink: String?,
        alertType: String
    ) {
        val channelId = getChannelForAlertType(alertType)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink ?: "smartdairy://alerts"))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_cow)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getChannelForAlertType(type: String) = when (type) {
        "vaccination_due" -> CHANNEL_VACCINATION
        "heat_expected"   -> CHANNEL_HEAT
        "calving_due"     -> CHANNEL_CALVING
        "low_feed_stock"  -> CHANNEL_FEEDING
        else              -> CHANNEL_GENERAL
    }

    companion object {
        const val CHANNEL_VACCINATION = "vaccination_alerts"
        const val CHANNEL_HEAT        = "heat_alerts"
        const val CHANNEL_CALVING     = "calving_alerts"
        const val CHANNEL_FEEDING     = "feeding_alerts"
        const val CHANNEL_GENERAL     = "general_alerts"
    }
}
```

---

## Notification Channels Setup

```kotlin
// In Application.onCreate()
private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_VACCINATION, "Vaccination Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alerts for upcoming vaccination due dates" },

            NotificationChannel(
                CHANNEL_HEAT, "Heat Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Heat detection and expected heat alerts" },

            NotificationChannel(
                CHANNEL_CALVING, "Calving Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Expected calving date reminders" },

            NotificationChannel(
                CHANNEL_FEEDING, "Feeding Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Feed reminders and low stock alerts" },

            NotificationChannel(
                CHANNEL_GENERAL, "General Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General farm management alerts" }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }
}
```

---

## FCM Token Management

```kotlin
// Upload token to Supabase when user logs in or token refreshes
suspend fun uploadFcmToken(token: String) {
    val userId = supabase.auth.currentUserOrNull()?.id ?: return
    val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    supabase.from("notification_tokens").upsert(
        mapOf(
            "user_id"  to userId,
            "fcm_token" to token,
            "device_id" to deviceId,
            "platform"  to "android",
            "updated_at" to Clock.System.now().toString()
        )
    )
}
```

---

## Alert Types & Deep Links

| Alert Type | Trigger | Deep Link |
|---|---|---|
| `vaccination_due` | 3 days before + day of due date | `smartdairy://animal/{id}` |
| `heat_expected` | 1 day before predicted heat | `smartdairy://animal/{id}` |
| `calving_due` | 7 days before expected calving | `smartdairy://animal/{id}` |
| `pregnancy_check` | 30 days after AI (check conception) | `smartdairy://animal/{id}` |
| `medicine_reminder` | Daily until end_date | `smartdairy://animal/{id}` |
| `low_feed_stock` | When inventory < threshold | `smartdairy://feeding/inventory` |
| `withdrawal_period` | When withdrawal date is today | `smartdairy://animal/{id}` |

---

## Cron Schedule

```sql
-- Runs daily at 6:00 AM IST = 00:30 UTC
SELECT cron.schedule(
    'daily-farm-alerts',
    '30 0 * * *',
    $$ SELECT net.http_post(...) $$
);
```
