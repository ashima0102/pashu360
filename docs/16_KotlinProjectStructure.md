# Kotlin Project Structure
## Smart Dairy Farm Management System

---

## Project-Level Files

```
SmartDairyFarm/
├── build.gradle.kts                    # Project-level build config
├── settings.gradle.kts                 # Module inclusion
├── gradle.properties                   # Gradle properties
└── app/
    ├── build.gradle.kts                # App-level dependencies
    ├── google-services.json            # Firebase config (git-ignored)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   └── java/com/smartdairy/
        └── test/
```

---

## `build.gradle.kts` (App Level)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.smartdairy"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.smartdairy.farm"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY")}\"")
    }
    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Coil (images)
    implementation(libs.coil.compose)

    // Vico (charts)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // CameraX + ML Kit (QR)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Firebase
    implementation(libs.firebase.messaging.ktx)

    // iTextPDF
    implementation(libs.itext7.core)

    // DataStore (preferences)
    implementation(libs.androidx.datastore.preferences)

    // Lottie (animations)
    implementation(libs.lottie.compose)
}
```

---

## Complete Source Tree

```
java/com/smartdairy/
│
├── SmartDairyApp.kt                    # Application class — Hilt init
│
├── MainActivity.kt                     # Single activity, Compose host
│
├── core/
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt          # Room database definition
│   │   │   ├── Converters.kt           # Type converters (List<String>, etc.)
│   │   │   └── dao/
│   │   │       ├── AnimalDao.kt
│   │   │       ├── MilkRecordDao.kt
│   │   │       ├── VaccinationDao.kt
│   │   │       ├── FeedRecordDao.kt
│   │   │       ├── HealthCheckupDao.kt
│   │   │       ├── DiseaseDao.kt
│   │   │       ├── MedicineDao.kt
│   │   │       ├── WeightRecordDao.kt
│   │   │       ├── HeatRecordDao.kt
│   │   │       ├── BreedingRecordDao.kt
│   │   │       ├── PregnancyRecordDao.kt
│   │   │       ├── LactationRecordDao.kt
│   │   │       ├── FeedTypeDao.kt
│   │   │       ├── FeedInventoryDao.kt
│   │   │       ├── VaccineCatalogueDao.kt
│   │   │       ├── AlertDao.kt
│   │   │       ├── FarmDao.kt
│   │   │       ├── BarnDao.kt
│   │   │       ├── IncomeRecordDao.kt
│   │   │       ├── ExpenseRecordDao.kt
│   │   │       └── SyncQueueDao.kt
│   │   ├── remote/
│   │   │   ├── SupabaseClient.kt       # Supabase client singleton
│   │   │   ├── SupabaseAuthSource.kt
│   │   │   └── SupabaseDataSource.kt   # Generic CRUD wrapper
│   │   └── sync/
│   │       ├── SyncEngine.kt           # Core sync logic
│   │       ├── SyncWorker.kt           # WorkManager worker
│   │       ├── SyncQueueEntity.kt      # Room entity for pending ops
│   │       └── ConflictResolver.kt     # Last-write-wins logic
│   │
│   ├── domain/
│   │   └── model/
│   │       ├── Animal.kt               # Pure Kotlin domain model
│   │       ├── Farm.kt
│   │       ├── Barn.kt
│   │       ├── MilkRecord.kt
│   │       ├── VaccinationRecord.kt
│   │       ├── VaccineCatalogue.kt
│   │       ├── FeedRecord.kt
│   │       ├── FeedType.kt
│   │       ├── FeedInventory.kt
│   │       ├── HealthCheckup.kt
│   │       ├── Disease.kt
│   │       ├── Medicine.kt
│   │       ├── WeightRecord.kt
│   │       ├── VetVisit.kt
│   │       ├── HeatRecord.kt
│   │       ├── BreedingRecord.kt
│   │       ├── PregnancyRecord.kt
│   │       ├── LactationRecord.kt
│   │       ├── IncomeRecord.kt
│   │       ├── ExpenseRecord.kt
│   │       ├── Alert.kt
│   │       └── User.kt
│   │
│   ├── presentation/
│   │   ├── theme/
│   │   │   ├── Color.kt                # Material 3 color scheme
│   │   │   ├── Type.kt                 # Typography
│   │   │   ├── Shape.kt                # Rounded corners
│   │   │   └── Theme.kt                # SmartDairyTheme composable
│   │   ├── navigation/
│   │   │   ├── NavGraph.kt             # Root NavHost
│   │   │   ├── AuthNavGraph.kt         # Auth nested graph
│   │   │   ├── MainNavGraph.kt         # Main app nested graph
│   │   │   └── Screen.kt              # sealed class Screen(route)
│   │   └── components/
│   │       ├── AnimalAvatar.kt         # Circular photo composable
│   │       ├── StatCard.kt             # Dashboard summary card
│   │       ├── AlertCard.kt            # Alert list item
│   │       ├── MilkEntryRow.kt         # Row in bulk milk entry
│   │       ├── SectionHeader.kt        # Styled section heading
│   │       ├── EmptyState.kt           # Empty list placeholder
│   │       ├── LoadingIndicator.kt     # Full-screen loading
│   │       ├── ErrorState.kt           # Error with retry
│   │       ├── DatePickerField.kt      # Material 3 date picker
│   │       ├── ConfirmDialog.kt        # Delete/action confirmation
│   │       ├── StatusBadge.kt          # Color-coded status chip
│   │       ├── QuickActionButton.kt    # Dashboard FAB-style button
│   │       ├── SyncStatusBar.kt        # Offline/syncing indicator
│   │       └── PhotoPicker.kt          # Camera + gallery picker
│   │
│   └── util/
│       ├── DateUtils.kt                # Date formatting helpers
│       ├── Extensions.kt              # Kotlin extension functions
│       ├── Resource.kt                # sealed class (Loading|Success|Error)
│       ├── UiText.kt                  # String/StringRes wrapper
│       ├── QrCodeGenerator.kt         # QR bitmap generator
│       └── PdfExporter.kt             # iTextPDF report generator
│
├── di/
│   ├── AppModule.kt                    # Supabase, DataStore bindings
│   ├── DatabaseModule.kt               # Room DB + DAO providers
│   ├── RepositoryModule.kt             # Repo implementations
│   └── WorkManagerModule.kt            # WorkManager config
│
└── feature/
    │
    ├── auth/
    │   ├── data/
    │   │   ├── local/UserPreferencesDataStore.kt
    │   │   ├── remote/AuthRemoteSource.kt
    │   │   └── repository/AuthRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/AuthRepository.kt
    │   │   └── usecase/
    │   │       ├── LoginWithPhoneUseCase.kt
    │   │       ├── VerifyOtpUseCase.kt
    │   │       ├── LoginWithEmailUseCase.kt
    │   │       ├── RegisterUseCase.kt
    │   │       ├── LogoutUseCase.kt
    │   │       └── GetCurrentUserUseCase.kt
    │   └── presentation/
    │       ├── SplashScreen.kt
    │       ├── LoginScreen.kt
    │       ├── OtpVerificationScreen.kt
    │       ├── RegisterScreen.kt
    │       ├── FarmSetupScreen.kt
    │       └── AuthViewModel.kt
    │
    ├── dashboard/
    │   ├── data/
    │   │   └── repository/DashboardRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── model/DashboardStats.kt
    │   │   ├── repository/DashboardRepository.kt
    │   │   └── usecase/GetDashboardStatsUseCase.kt
    │   └── presentation/
    │       ├── DashboardScreen.kt
    │       └── DashboardViewModel.kt
    │
    ├── farm/
    │   ├── data/
    │   │   ├── local/entity/FarmEntity.kt
    │   │   ├── local/entity/BarnEntity.kt
    │   │   ├── local/mapper/FarmMapper.kt
    │   │   ├── remote/FarmRemoteSource.kt
    │   │   └── repository/FarmRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/FarmRepository.kt
    │   │   └── usecase/
    │   │       ├── GetFarmsUseCase.kt
    │   │       ├── CreateFarmUseCase.kt
    │   │       ├── UpdateFarmUseCase.kt
    │   │       ├── GetBarnsUseCase.kt
    │   │       └── CreateBarnUseCase.kt
    │   └── presentation/
    │       ├── FarmDetailScreen.kt
    │       ├── EditFarmScreen.kt
    │       ├── BarnListScreen.kt
    │       ├── AddBarnScreen.kt
    │       └── FarmViewModel.kt
    │
    ├── animal/
    │   ├── data/
    │   │   ├── local/entity/AnimalEntity.kt
    │   │   ├── local/mapper/AnimalMapper.kt
    │   │   ├── remote/AnimalRemoteSource.kt
    │   │   └── repository/AnimalRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/AnimalRepository.kt
    │   │   └── usecase/
    │   │       ├── GetAnimalsUseCase.kt
    │   │       ├── GetAnimalByIdUseCase.kt
    │   │       ├── GetAnimalByTagUseCase.kt
    │   │       ├── AddAnimalUseCase.kt
    │   │       ├── UpdateAnimalUseCase.kt
    │   │       ├── SellAnimalUseCase.kt
    │   │       ├── DeceasedAnimalUseCase.kt
    │   │       ├── GenerateQrCodeUseCase.kt
    │   │       └── SearchAnimalsUseCase.kt
    │   └── presentation/
    │       ├── AnimalListScreen.kt
    │       ├── AnimalDetailScreen.kt
    │       ├── AddAnimalScreen.kt
    │       ├── EditAnimalScreen.kt
    │       ├── QrScannerScreen.kt
    │       ├── AnimalTimelineScreen.kt
    │       ├── AnimalListViewModel.kt
    │       └── AnimalDetailViewModel.kt
    │
    ├── milk/
    │   ├── data/
    │   │   ├── local/entity/MilkRecordEntity.kt
    │   │   ├── local/entity/LactationRecordEntity.kt
    │   │   ├── local/mapper/MilkMapper.kt
    │   │   ├── remote/MilkRemoteSource.kt
    │   │   └── repository/MilkRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/MilkRepository.kt
    │   │   └── usecase/
    │   │       ├── GetBulkMilkEntryUseCase.kt
    │   │       ├── SaveBulkMilkEntryUseCase.kt
    │   │       ├── GetMilkHistoryUseCase.kt
    │   │       ├── GetDailyTotalUseCase.kt
    │   │       └── GetLactationCurveUseCase.kt
    │   └── presentation/
    │       ├── BulkMilkEntryScreen.kt
    │       ├── MilkHistoryScreen.kt
    │       ├── MilkAnalyticsScreen.kt
    │       └── MilkViewModel.kt
    │
    ├── feeding/
    │   ├── data/
    │   │   ├── local/entity/FeedRecordEntity.kt
    │   │   ├── local/entity/FeedTypeEntity.kt
    │   │   ├── local/entity/FeedInventoryEntity.kt
    │   │   ├── local/mapper/FeedMapper.kt
    │   │   ├── remote/FeedRemoteSource.kt
    │   │   └── repository/FeedRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/FeedRepository.kt
    │   │   └── usecase/
    │   │       ├── GetFeedTypesUseCase.kt
    │   │       ├── LogFeedUseCase.kt
    │   │       ├── GetFeedInventoryUseCase.kt
    │   │       └── UpdateInventoryUseCase.kt
    │   └── presentation/
    │       ├── FeedingOverviewScreen.kt
    │       ├── LogFeedScreen.kt
    │       ├── FeedInventoryScreen.kt
    │       ├── FeedScheduleScreen.kt
    │       └── FeedingViewModel.kt
    │
    ├── health/
    │   ├── data/
    │   │   ├── local/entity/HealthCheckupEntity.kt
    │   │   ├── local/entity/DiseaseEntity.kt
    │   │   ├── local/entity/MedicineEntity.kt
    │   │   ├── local/entity/WeightRecordEntity.kt
    │   │   ├── local/entity/VetVisitEntity.kt
    │   │   ├── local/mapper/HealthMapper.kt
    │   │   ├── remote/HealthRemoteSource.kt
    │   │   └── repository/HealthRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/HealthRepository.kt
    │   │   └── usecase/
    │   │       ├── LogHealthCheckupUseCase.kt
    │   │       ├── LogDiseaseUseCase.kt
    │   │       ├── LogMedicineUseCase.kt
    │   │       ├── LogWeightUseCase.kt
    │   │       ├── GetHealthHistoryUseCase.kt
    │   │       └── GetSickAnimalsUseCase.kt
    │   └── presentation/
    │       ├── HealthOverviewScreen.kt
    │       ├── LogHealthEventScreen.kt
    │       ├── HealthHistoryScreen.kt
    │       ├── WeightChartScreen.kt
    │       └── HealthViewModel.kt
    │
    ├── vaccination/
    │   ├── data/
    │   │   ├── local/entity/VaccinationEntity.kt
    │   │   ├── local/entity/VaccineCatalogueEntity.kt
    │   │   ├── local/mapper/VaccinationMapper.kt
    │   │   ├── remote/VaccinationRemoteSource.kt
    │   │   └── repository/VaccinationRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/VaccinationRepository.kt
    │   │   └── usecase/
    │   │       ├── GetVaccinationScheduleUseCase.kt
    │   │       ├── AddVaccinationUseCase.kt
    │   │       ├── GetUpcomingVaccinationsUseCase.kt
    │   │       └── GetVaccineCatalogueUseCase.kt
    │   └── presentation/
    │       ├── VaccinationScheduleScreen.kt
    │       ├── AddVaccinationScreen.kt
    │       ├── VaccineCatalogueScreen.kt
    │       └── VaccinationViewModel.kt
    │
    ├── heat/
    │   ├── data/
    │   │   ├── local/entity/HeatRecordEntity.kt
    │   │   ├── local/mapper/HeatMapper.kt
    │   │   ├── remote/HeatRemoteSource.kt
    │   │   └── repository/HeatRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/HeatRepository.kt
    │   │   └── usecase/
    │   │       ├── LogHeatUseCase.kt
    │   │       ├── GetHeatHistoryUseCase.kt
    │   │       └── PredictNextHeatUseCase.kt
    │   └── presentation/
    │       ├── HeatCalendarScreen.kt
    │       ├── LogHeatScreen.kt
    │       └── HeatViewModel.kt
    │
    ├── breeding/
    │   ├── data/
    │   │   ├── local/entity/BreedingRecordEntity.kt
    │   │   ├── local/entity/PregnancyRecordEntity.kt
    │   │   ├── local/mapper/BreedingMapper.kt
    │   │   ├── remote/BreedingRemoteSource.kt
    │   │   └── repository/BreedingRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/BreedingRepository.kt
    │   │   └── usecase/
    │   │       ├── RecordAIUseCase.kt
    │   │       ├── RecordNaturalMatingUseCase.kt
    │   │       ├── UpdateConceptionStatusUseCase.kt
    │   │       ├── RecordPregnancyUseCase.kt
    │   │       └── RecordCalvingUseCase.kt
    │   └── presentation/
    │       ├── BreedingScreen.kt
    │       ├── AddBreedingScreen.kt
    │       ├── PregnancyTrackingScreen.kt
    │       ├── RecordCalvingScreen.kt
    │       └── BreedingViewModel.kt
    │
    ├── finance/
    │   ├── data/
    │   │   ├── local/entity/IncomeRecordEntity.kt
    │   │   ├── local/entity/ExpenseRecordEntity.kt
    │   │   ├── local/mapper/FinanceMapper.kt
    │   │   ├── remote/FinanceRemoteSource.kt
    │   │   └── repository/FinanceRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── model/ProfitLoss.kt
    │   │   ├── repository/FinanceRepository.kt
    │   │   └── usecase/
    │   │       ├── LogIncomeUseCase.kt
    │   │       ├── LogExpenseUseCase.kt
    │   │       ├── GetProfitLossUseCase.kt
    │   │       └── GetAnimalPnLUseCase.kt
    │   └── presentation/
    │       ├── FinanceDashboardScreen.kt
    │       ├── LogIncomeScreen.kt
    │       ├── LogExpenseScreen.kt
    │       ├── AnimalPnLScreen.kt
    │       └── FinanceViewModel.kt
    │
    ├── reports/
    │   ├── domain/
    │   │   └── usecase/
    │   │       ├── GenerateMilkReportUseCase.kt
    │   │       ├── GenerateVaccinationReportUseCase.kt
    │   │       ├── GenerateHealthReportUseCase.kt
    │   │       └── GenerateFinancialReportUseCase.kt
    │   └── presentation/
    │       ├── ReportsScreen.kt
    │       └── ReportsViewModel.kt
    │
    ├── notifications/
    │   ├── data/
    │   │   ├── local/entity/AlertEntity.kt
    │   │   ├── local/mapper/AlertMapper.kt
    │   │   ├── remote/AlertRemoteSource.kt
    │   │   └── repository/AlertRepositoryImpl.kt
    │   ├── domain/
    │   │   ├── repository/AlertRepository.kt
    │   │   └── usecase/
    │   │       ├── GetAlertsUseCase.kt
    │   │       └── ResolveAlertUseCase.kt
    │   ├── service/
    │   │   └── SmartDairyFirebaseMessagingService.kt
    │   └── presentation/
    │       ├── NotificationCenterScreen.kt
    │       └── NotificationViewModel.kt
    │
    └── settings/
        ├── data/
        │   └── UserPreferencesRepository.kt
        ├── domain/
        │   └── usecase/
        │       ├── GetUserPrefsUseCase.kt
        │       └── UpdateUserPrefsUseCase.kt
        └── presentation/
            ├── SettingsScreen.kt
            ├── ProfileScreen.kt
            ├── NotificationSettingsScreen.kt
            └── SettingsViewModel.kt
```

---

## Resource Files

```
res/
├── drawable/
│   ├── ic_launcher_foreground.xml
│   ├── ic_launcher_background.xml
│   ├── ic_cow.xml
│   ├── ic_milk.xml
│   ├── ic_vaccine.xml
│   ├── ic_feed.xml
│   ├── ic_health.xml
│   ├── ic_finance.xml
│   └── ic_reports.xml
├── values/
│   ├── strings.xml          # English
│   ├── colors.xml
│   └── themes.xml
├── values-hi/
│   └── strings.xml          # Hindi
├── values-or/
│   └── strings.xml          # Odia
├── raw/
│   ├── lottie_splash.json   # Splash animation
│   └── lottie_empty.json    # Empty state animation
└── font/
    ├── poppins_regular.ttf
    ├── poppins_medium.ttf
    └── poppins_bold.ttf
```
