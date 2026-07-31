# Security Rules
## Smart Dairy Farm Management System

---

## 1. Authentication Security

| Control | Implementation |
|---|---|
| Session tokens | Stored in EncryptedSharedPreferences (AES-256-GCM) |
| OTP expiry | 60 seconds; invalid after first use |
| OTP rate limit | Max 5 requests per phone per hour (Supabase Auth) |
| Password requirements | Min 8 chars, 1 uppercase, 1 number |
| Session duration | JWT: 1 hour; Refresh token: 30 days |
| Refresh token rotation | Enabled — each refresh invalidates old token |
| Biometric (V2) | Android BiometricPrompt for re-auth |

---

## 2. Supabase Row Level Security (Complete Policies)

### All core tables use farm-ownership isolation:

```sql
-- Template applied to all tables with farm_id:
-- animals, milk_records, feed_records, health_checkups,
-- diseases, medicines, vet_visits, vaccinations, heat_records,
-- breeding_records, pregnancy_records, income_records, expense_records,
-- alerts, barns

CREATE POLICY "{table}_farm_isolation" ON {table}
    FOR ALL
    USING (
        farm_id IN (
            SELECT id FROM farms WHERE owner_id = auth.uid()
        )
    )
    WITH CHECK (
        farm_id IN (
            SELECT id FROM farms WHERE owner_id = auth.uid()
        )
    );

-- farms table — direct ownership
CREATE POLICY "farms_owner_only" ON farms
    FOR ALL USING (owner_id = auth.uid())
    WITH CHECK (owner_id = auth.uid());

-- users table — each user sees only themselves
CREATE POLICY "users_self_only" ON users
    FOR ALL USING (id = auth.uid())
    WITH CHECK (id = auth.uid());

-- notification_tokens — user-scoped
CREATE POLICY "tokens_self_only" ON notification_tokens
    FOR ALL USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());
```

### What RLS prevents:
- User A cannot read, create, update, or delete any data belonging to User B's farms
- Applies at the database level — cannot be bypassed from the client SDK
- Even if someone steals an anon key, they cannot read other users' data

---

## 3. API Key Security

```
Supabase Anon Key:
  ├── Used in Android app (client-side)
  ├── Safe to expose in app — RLS enforces access control
  └── Stored in BuildConfig (not in code/git)

Supabase Service Role Key:
  ├── Used ONLY in Edge Functions (server-side)
  ├── NEVER included in Android app
  └── Stored in Supabase secrets (not in code/git)

Firebase Config:
  ├── google-services.json stored locally only
  └── Git-ignored via .gitignore
```

---

## 4. Android App Security

```kotlin
// local.properties (git-ignored)
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGc...

// .gitignore must include:
local.properties
google-services.json
*.jks
*.keystore
```

| Control | Implementation |
|---|---|
| ProGuard/R8 | Enabled for release builds — obfuscates code |
| Certificate pinning | Optional V2 — pin Supabase certificate |
| Screenshot prevention | `FLAG_SECURE` on financial screens |
| Root detection | Optional V2 — warn if device is rooted |
| Cleartext traffic | `android:usesCleartextTraffic="false"` — HTTPS only |
| Exported activities | `android:exported="false"` on all except MainActivity |

---

## 5. Data Security

| Control | Implementation |
|---|---|
| Data in transit | TLS 1.3 for all Supabase and FCM communication |
| Data at rest (local) | Room DB on internal storage (not world-readable) |
| Data at rest (cloud) | Supabase PostgreSQL encrypted at rest (AES-256) |
| Photo storage | Private Supabase Storage bucket (signed URLs, 1-hour expiry) |
| Signed URL generation | Server-side via Edge Function, not client-side |
| Sensitive fields | No passwords stored in Room — auth via Supabase tokens only |

---

## 6. Supabase Storage Security

```sql
-- Animal photos: only farm owner can access
CREATE POLICY "animal_photos_owner" ON storage.objects
    FOR ALL USING (
        bucket_id = 'animal-photos'
        AND (storage.foldername(name))[1] IN (
            SELECT id::text FROM farms WHERE owner_id = auth.uid()
        )
    );

-- Health photos: same pattern
CREATE POLICY "health_photos_owner" ON storage.objects
    FOR ALL USING (
        bucket_id = 'health-photos'
        AND (storage.foldername(name))[1] IN (
            SELECT id::text FROM farms WHERE owner_id = auth.uid()
        )
    );
```

---

## 7. Edge Function Security

```typescript
// Validate that caller is authorized (Edge Functions use service key)
// Cron-triggered Edge Functions are not publicly callable via HTTP
// If HTTP callable, add Authorization header check:

serve(async (req) => {
  const authHeader = req.headers.get('Authorization')
  if (authHeader !== `Bearer ${Deno.env.get('CRON_SECRET')}`) {
    return new Response('Unauthorized', { status: 401 })
  }
  // ... proceed
})
```

---

## 8. Input Validation

| Field | Validation |
|---|---|
| Phone number | Must be 10 digits (Indian format); strip non-numeric |
| Email | Standard email format validation |
| Tag ID | Alphanumeric, 1–20 chars, no special characters except hyphen |
| Milk quantity | Positive decimal; max 99.9 L |
| Temperature | Range 30.0–45.0°C |
| Date fields | Not in future for historical records; valid calendar date |
| Free text | Sanitize HTML/script tags; max length enforced |
| Photo | Max 5MB; only jpeg/png/webp; validate mime type |

```kotlin
// Example validation in UseCase
class AddAnimalUseCase @Inject constructor(private val repo: AnimalRepository) {
    suspend operator fun invoke(animal: Animal): Result<Unit> {
        require(animal.tagId.isNotBlank()) { "Tag ID is required" }
        require(animal.tagId.length <= 20) { "Tag ID too long (max 20 chars)" }
        require(animal.tagId.matches(Regex("[a-zA-Z0-9-]+"))) { "Invalid Tag ID format" }
        require(animal.gender in listOf("female", "male")) { "Invalid gender" }
        animal.weightKg?.let { require(it in 1.0..2000.0) { "Invalid weight" } }
        return repo.addAnimal(animal)
    }
}
```

---

## 9. Privacy

| Control | Detail |
|---|---|
| Data minimization | Only collect data necessary for farm management |
| Third-party sharing | No data shared with third parties without consent |
| GDPR compliance | Account deletion removes all data from Supabase |
| Local data clearing | Logout clears Room DB and EncryptedSharedPreferences |
| Analytics | Firebase Crashlytics only (crash reports) — no behavioral analytics without consent |

---

## 10. Account Deletion Flow

```kotlin
// Complete data deletion on account close
suspend fun deleteAccount() {
    // 1. Delete all farm data from Supabase (cascades to all tables via FK)
    val farms = supabase.from("farms").select().decodeList<Farm>()
    farms.forEach { farm ->
        // Delete storage objects for this farm
        supabase.storage["animal-photos"].list(farm.id).forEach { photo ->
            supabase.storage["animal-photos"].delete(listOf("${farm.id}/${photo.name}"))
        }
        // Delete farm (cascades to all tables)
        supabase.from("farms").delete { filter { eq("id", farm.id) } }
    }

    // 2. Delete user account
    supabase.auth.admin.deleteUser(supabase.auth.currentUserOrNull()!!.id)

    // 3. Clear local Room database
    AppDatabase.clearAllTables()

    // 4. Clear preferences
    userPrefs.clearAll()
}
```
