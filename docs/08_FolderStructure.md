# Folder Structure
## Smart Dairy Farm Management System

---

## Complete Android Project Tree

```
SmartDairyFarm/                             ← Git repo root
│
├── README.md
├── docs/                                    ← All 30 documentation files
│
├── android/                                 ← Android project root
│   ├── gradle/
│   │   └── libs.versions.toml              ← Version catalog
│   ├── build.gradle.kts                    ← Project-level build
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   │
│   └── app/
│       ├── build.gradle.kts                ← App-level build + dependencies
│       ├── proguard-rules.pro
│       ├── google-services.json            ← Firebase (git-ignored)
│       │
│       └── src/
│           ├── main/
│           │   ├── AndroidManifest.xml
│           │   │
│           │   └── java/com/smartdairy/
│           │       ├── SmartDairyApp.kt
│           │       ├── MainActivity.kt
│           │       │
│           │       ├── core/
│           │       │   ├── data/
│           │       │   │   ├── local/
│           │       │   │   │   ├── AppDatabase.kt
│           │       │   │   │   ├── Converters.kt
│           │       │   │   │   └── dao/            (21 DAO files)
│           │       │   │   ├── remote/
│           │       │   │   │   ├── SupabaseClient.kt
│           │       │   │   │   └── SupabaseDataSource.kt
│           │       │   │   └── sync/
│           │       │   │       ├── SyncEngine.kt
│           │       │   │       ├── SyncWorker.kt
│           │       │   │       ├── SyncQueueEntity.kt
│           │       │   │       └── ConflictResolver.kt
│           │       │   ├── domain/
│           │       │   │   └── model/              (22 domain model files)
│           │       │   ├── presentation/
│           │       │   │   ├── theme/
│           │       │   │   │   ├── Color.kt
│           │       │   │   │   ├── Type.kt
│           │       │   │   │   ├── Shape.kt
│           │       │   │   │   └── Theme.kt
│           │       │   │   ├── navigation/
│           │       │   │   │   ├── NavGraph.kt
│           │       │   │   │   ├── AuthNavGraph.kt
│           │       │   │   │   ├── MainNavGraph.kt
│           │       │   │   │   └── Screen.kt
│           │       │   │   └── components/
│           │       │   │       ├── AnimalAvatar.kt
│           │       │   │       ├── StatCard.kt
│           │       │   │       ├── AlertCard.kt
│           │       │   │       ├── MilkEntryRow.kt
│           │       │   │       ├── SectionHeader.kt
│           │       │   │       ├── EmptyState.kt
│           │       │   │       ├── LoadingIndicator.kt
│           │       │   │       ├── ErrorState.kt
│           │       │   │       ├── DatePickerField.kt
│           │       │   │       ├── ConfirmDialog.kt
│           │       │   │       ├── StatusBadge.kt
│           │       │   │       ├── QuickActionButton.kt
│           │       │   │       ├── SyncStatusBar.kt
│           │       │   │       └── PhotoPicker.kt
│           │       │   └── util/
│           │       │       ├── DateUtils.kt
│           │       │       ├── Extensions.kt
│           │       │       ├── Resource.kt
│           │       │       ├── UiText.kt
│           │       │       ├── QrCodeGenerator.kt
│           │       │       └── PdfExporter.kt
│           │       │
│           │       ├── di/
│           │       │   ├── AppModule.kt
│           │       │   ├── DatabaseModule.kt
│           │       │   ├── RepositoryModule.kt
│           │       │   └── WorkManagerModule.kt
│           │       │
│           │       └── feature/
│           │           ├── auth/
│           │           │   ├── data/
│           │           │   │   ├── local/UserPreferencesDataStore.kt
│           │           │   │   ├── remote/AuthRemoteSource.kt
│           │           │   │   └── repository/AuthRepositoryImpl.kt
│           │           │   ├── domain/
│           │           │   │   ├── repository/AuthRepository.kt
│           │           │   │   └── usecase/
│           │           │   │       ├── LoginWithPhoneUseCase.kt
│           │           │   │       ├── VerifyOtpUseCase.kt
│           │           │   │       ├── LoginWithEmailUseCase.kt
│           │           │   │       ├── RegisterUseCase.kt
│           │           │   │       └── LogoutUseCase.kt
│           │           │   └── presentation/
│           │           │       ├── SplashScreen.kt
│           │           │       ├── LoginScreen.kt
│           │           │       ├── OtpVerificationScreen.kt
│           │           │       ├── RegisterScreen.kt
│           │           │       ├── FarmSetupScreen.kt
│           │           │       └── AuthViewModel.kt
│           │           │
│           │           ├── dashboard/
│           │           │   └── presentation/
│           │           │       ├── DashboardScreen.kt
│           │           │       └── DashboardViewModel.kt
│           │           │
│           │           ├── farm/
│           │           │   └── presentation/
│           │           │       ├── FarmDetailScreen.kt
│           │           │       ├── EditFarmScreen.kt
│           │           │       ├── BarnListScreen.kt
│           │           │       └── FarmViewModel.kt
│           │           │
│           │           ├── animal/                (Full data/domain/presentation)
│           │           ├── milk/                  (Full data/domain/presentation)
│           │           ├── feeding/               (Full data/domain/presentation)
│           │           ├── health/                (Full data/domain/presentation)
│           │           ├── vaccination/           (Full data/domain/presentation)
│           │           ├── heat/                  (Full data/domain/presentation)
│           │           ├── breeding/              (Full data/domain/presentation)
│           │           ├── finance/               (Full data/domain/presentation)
│           │           ├── reports/               (Full data/domain/presentation)
│           │           ├── notifications/
│           │           │   ├── service/
│           │           │   │   └── SmartDairyFirebaseMessagingService.kt
│           │           │   └── presentation/
│           │           │       ├── NotificationCenterScreen.kt
│           │           │       └── NotificationViewModel.kt
│           │           └── settings/
│           │               └── presentation/
│           │                   ├── SettingsScreen.kt
│           │                   ├── ProfileScreen.kt
│           │                   └── SettingsViewModel.kt
│           │
│           └── res/
│               ├── drawable/               ← Icons (SVG/VectorDrawable)
│               ├── font/                   ← poppins_*.ttf, nunito_*.ttf
│               ├── raw/                    ← Lottie JSON animations
│               ├── values/strings.xml      ← English
│               ├── values-hi/strings.xml   ← Hindi
│               └── values-or/strings.xml   ← Odia
│
├── supabase/
│   ├── config.toml
│   ├── migrations/
│   │   ├── 20260730_001_initial_schema.sql
│   │   ├── 20260730_002_rls_policies.sql
│   │   ├── 20260730_003_triggers.sql
│   │   ├── 20260730_004_default_data.sql
│   │   └── 20260730_005_indexes.sql
│   └── functions/
│       └── send-daily-alerts/
│           ├── index.ts
│           └── deno.json
│
└── .github/
    └── workflows/
        ├── android.yml                 ← CI/CD pipeline
        └── supabase-migration.yml      ← DB migration pipeline
```

---

## Feature Module Structure (Repeated Pattern)

Every feature follows this exact same 3-layer structure:

```
feature/{name}/
├── data/
│   ├── local/
│   │   ├── entity/{Name}Entity.kt      ← Room entity (has @Entity, @ColumnInfo)
│   │   └── mapper/{Name}Mapper.kt      ← Entity ↔ Domain model converter
│   ├── remote/
│   │   └── {Name}RemoteSource.kt       ← Supabase calls
│   └── repository/
│       └── {Name}RepositoryImpl.kt     ← Implements domain interface
│
├── domain/
│   ├── model/{Name}.kt                 ← Pure Kotlin data class (no annotations)
│   ├── repository/{Name}Repository.kt  ← Interface (no implementation)
│   └── usecase/
│       ├── Get{Name}sUseCase.kt
│       ├── Add{Name}UseCase.kt
│       ├── Update{Name}UseCase.kt
│       └── Delete{Name}UseCase.kt
│
└── presentation/
    ├── {Name}ListScreen.kt             ← Compose screen
    ├── {Name}DetailScreen.kt           ← Compose screen
    ├── Add{Name}Screen.kt              ← Compose screen
    └── {Name}ViewModel.kt              ← ViewModel with UiState
```

---

## Naming Conventions

| Type | Convention | Example |
|---|---|---|
| Kotlin classes | PascalCase | `AnimalEntity`, `MilkRecord` |
| Functions | camelCase | `getActiveAnimals()`, `saveMilkRecord()` |
| Constants | UPPER_SNAKE_CASE | `DATABASE_NAME`, `WORK_NAME` |
| Composable functions | PascalCase | `AnimalListScreen`, `StatCard` |
| XML resources | snake_case | `ic_cow.xml`, `strings.xml` |
| Database tables | snake_case | `milk_records`, `animal_id` |
| Route strings | snake_case | `"animal_detail/{animalId}"` |
| Packages | lowercase | `com.smartdairy.feature.animal` |

---

## Package Structure

```
com.smartdairy
├── core.data.local       ← Room DAOs and entities
├── core.data.remote      ← Supabase client
├── core.data.sync        ← SyncEngine and WorkManager
├── core.domain.model     ← Pure domain models
├── core.presentation.theme      ← Material 3 theme
├── core.presentation.navigation ← Routes and NavGraph
├── core.presentation.components ← Shared UI components
├── core.util             ← Extensions, helpers
├── di                    ← Hilt modules
└── feature.{name}        ← Feature modules
    ├── data
    ├── domain
    └── presentation
```
