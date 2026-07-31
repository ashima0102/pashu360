# Deployment Plan
## Smart Dairy Farm Management System

---

## Environments

| Environment | Purpose | Supabase Project | Firebase App |
|---|---|---|---|
| Development | Local dev, testing | smart-dairy-dev | smart-dairy-dev |
| Staging | Pre-release testing, beta | smart-dairy-staging | smart-dairy-staging |
| Production | Live users | smart-dairy-prod | smart-dairy-prod |

---

## Build Variants

```kotlin
// build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("String", "SUPABASE_URL", "\"$devSupabaseUrl\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"$devAnonKey\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "SUPABASE_URL", "\"$prodSupabaseUrl\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"$prodAnonKey\"")
            signingConfig = signingConfigs.getByName("release")
        }
        staging {
            initWith(buildTypes.getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            buildConfigField("String", "SUPABASE_URL", "\"$stagingSupabaseUrl\"")
        }
    }
}
```

---

## CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/android.yml
name: Android CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with: { java-version: '17', distribution: 'temurin' }
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest
      - name: Run lint
        run: ./gradlew lintDebug

  build-staging:
    needs: test
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with: { java-version: '17', distribution: 'temurin' }
      - name: Build staging APK
        run: ./gradlew assembleStagingRelease
      - name: Upload to Firebase App Distribution
        uses: wzieba/Firebase-Distribution-Github-Action@v1
        with:
          appId: ${{ secrets.FIREBASE_APP_ID_STAGING }}
          serviceCredentialsFileContent: ${{ secrets.FIREBASE_CREDENTIALS }}
          groups: beta-testers
          file: app/build/outputs/apk/staging/release/app-staging-release.apk

  build-production:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with: { java-version: '17', distribution: 'temurin' }
      - name: Build production AAB
        env:
          KEYSTORE_FILE: ${{ secrets.KEYSTORE_FILE }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew bundleRelease
      - name: Upload to Play Store (Internal Testing)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT }}
          packageName: com.smartdairy.farm
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          status: completed
```

---

## Play Store Setup

### App Listing
- **Package name:** `com.smartdairy.farm`
- **App name:** Smart Dairy Farm Manager
- **Category:** Business → Farm Management
- **Content rating:** Everyone
- **Target countries:** India (primary), Bangladesh, Sri Lanka, Nepal

### Store Listing Assets Required
```
Graphics:
├── App icon: 512×512 PNG
├── Feature graphic: 1024×500 PNG
├── Phone screenshots: 6 screenshots (1080×1920)
├── Tablet screenshots: 3 screenshots (optional)
└── Promo video: 30-second demo (optional)

Text:
├── Short description: 80 chars max
├── Full description: 4000 chars max
│   "Complete dairy farm management — track milk, vaccinations,
│    feeding, health, and finances for your entire herd.
│    Works offline. Hindi & Odia supported."
└── Keywords: dairy farm, cow management, milk tracking, vaccination reminder
```

### Release Track Strategy
| Track | Users | Purpose |
|---|---|---|
| Internal Testing | 5–10 team members | Final smoke tests |
| Closed Testing (Alpha) | 20–50 pilot farmers | Real-world feedback |
| Open Testing (Beta) | 500+ farmers | Broad feedback before launch |
| Production | All users | Full launch |

---

## Database Migration Strategy

```sql
-- Supabase migrations are tracked in supabase/migrations/
-- Each migration is a timestamped SQL file

-- Example: supabase/migrations/20260730_initial_schema.sql
-- Contains all CREATE TABLE statements

-- Subsequent changes:
-- supabase/migrations/20260815_add_lactation_no.sql
ALTER TABLE lactation_records ADD COLUMN lactation_no INTEGER DEFAULT 1;
```

```bash
# Apply migrations
supabase db push --db-url $PRODUCTION_DB_URL

# Room database migrations (Android)
# Each schema change increments version in AppDatabase
# Add migration path in Room.databaseBuilder()
.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
```

---

## App Size Optimization

| Technique | Implementation |
|---|---|
| R8 / ProGuard | Enabled for release — strips unused code |
| Resource shrinking | `isShrinkResources = true` |
| App Bundle (.aab) | Use AAB instead of APK — Google Play delivers only needed resources |
| Image compression | Use WebP for all image assets |
| Lottie | Use Lottie JSON over PNG sequences |
| Font subset | Only include characters needed for EN/HI/OR |

**Target app download size: < 20MB (from Play Store)**

---

## Monitoring & Crash Reporting

```kotlin
// Firebase Crashlytics — crash reporting
// Add to build.gradle.kts
implementation(libs.firebase.crashlytics.ktx)
implementation(libs.firebase.analytics.ktx)

// Usage
try {
    // operation
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    // handle gracefully
}

// Custom keys for debugging
FirebaseCrashlytics.getInstance().setCustomKey("farm_id", activeFarmId)
FirebaseCrashlytics.getInstance().setCustomKey("sync_queue_size", pendingCount)
```

---

## Rollout Strategy

### Week 15 (Pre-Launch)
- Deploy to Internal Testing on Play Store
- QA team tests all 32 screens
- Fix critical bugs
- Supabase production environment ready

### Week 16 (Beta Launch)
- Closed Testing — 50 pilot farmers
- In-person onboarding for 10 farmers
- Collect WhatsApp feedback
- Monitor Crashlytics for crashes

### Week 18 (Soft Launch)
- Open Testing — 500+ farmers
- Social media announcement (farming groups, YouTube)
- Monitor DAU/MAU

### Month 3 (Full Launch)
- Production release (100% rollout)
- Play Store optimization (A/B test store listing)
- Press release to agricultural media

---

## Supabase Production Checklist

- [ ] Enable Point-in-Time Recovery (PITR) for database backup
- [ ] Set up database connection pooling (PgBouncer) — Supabase Pro
- [ ] Configure custom domain for API (api.smartdairy.in)
- [ ] Enable audit logging for security events
- [ ] Set up Supabase alerts for API error rate > 1%
- [ ] Configure database read replicas for analytics queries (Pro)
- [ ] Review and lock down all RLS policies
- [ ] Enable pg_cron for Edge Function scheduling
- [ ] Set SMTP for transactional emails (reset password, welcome)
- [ ] Test backup restore procedure
