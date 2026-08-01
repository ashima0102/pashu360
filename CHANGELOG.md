# Changelog

All notable changes to Pashu360 are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased] — Phase 9 (Next)

### Planned
- Reports module (milk / vaccination / financial)
- PDF & CSV export via iTextPDF
- Multi-farm switching

---

## [0.9.0] — 2026-08-02 · Phase 8 — Breeding + Pregnancy + Calving ✅

Full reproduction lifecycle. Farmer can now trace an animal from first heat
signs through mating, pregnancy confirmation, dry period, and calving —
with the newborn calf auto-registered as a herd animal.

### Added
- Domain models: `HeatRecord`, `BreedingRecord`, `PregnancyRecord` + 5 enums
  (`HeatIntensity`, `BreedingType` AI/NATURAL, `ConceptionStatus`
  PENDING/CONFIRMED/FAILED, `PdMethod`, `CalvingOutcome`) with emojis + displayName
- `HeatSymptomCatalog` (8 symptoms), `HeatRecord.expectedNextHeat(cycleDays = 21)`,
  `BreedingRecord.expectedPdDate() / expectedCalvingDate()`
- Denormalized detail types: `HeatRecordDetail`, `BreedingRecordDetail`, `PregnancyDetail`
- Room v6 — 3 new entities (heat_records, breeding_records, pregnancy_records) with
  CASCADE FK to animals, pipe-delimited symptom serialization
- 3 non-suspend DAOs (KSP2-safe): observe/insert/update/delete + scanner helpers
  (`getLatestPerAnimal`, `getPendingBreedings`, `getCalvingsInWindow`, `getById`)
- `BreedingRepository` interface (18 methods) + Impl using `combine()` with
  `animalsMap` flow for denormalized reads. `recordCalving()` writes calf via
  `animalRepository.addAnimal()` first, then patches the pregnancy row with `calfAnimalId`
- `BreedingViewModel` — 4 form states (Heat / Mating / Pregnancy / Calving),
  `combine(groupA, groupB)` split for UI state (5+3 flows), auto-computed
  defaults (`expectedCalvingDate = today + 280 days`, `dryPeriodStart = calving - 60 days`)
- `BreedingScreen` — green-gradient header + 3 summary chips + 4 tabs +
  contextual `ExtendedFloatingActionButton` per tab
- 4 bottom-sheet forms:
  - HeatSheet — animal picker, symptom chip multi-select, intensity toggle
  - BreedingSheet — AI/Natural toggle with contextual fields, bull/batch/technician/cost
  - PregnancySheet — PD method radio list, calculated expected calving preview
  - CalvingSheet — outcome chips, difficulty 1–4, auto-create calf toggle with tag/name/gender
- `AlertScannerWorker` extended:
  - `scanExpectedHeats()` — 21-day cycle prediction, alerts 1 day early, dedup by `heat:{id}`
  - `scanCalvingDue()` — 7-day window, URGENT ≤ 2 days, dedup by `calving:{id}`
- Wired into `MainScaffold` for both `Screen.Breeding.route` and `Screen.Pregnancy.route`

### Changed
- Room DB bumped to v6, `DatabaseModule` provides 3 new DAOs
- `RepositoryModule` binds `BreedingRepository`
- Removed unused `PregnancyRecord.daysToCalving` computed property (fixed
  Int?/Long? mismatch from newer kotlinx-datetime `toEpochDays()` return type)

---

## [0.6.0] — 2026-08-02 · Phase 5 — Alerts + WorkManager ✅

Full offline alerts system. Background WorkManager job scans vaccinations,
generates Alert entities in Room, and fires system notifications. Header
bell badge live across all top-level screens.

### Added
- `Alert` domain model with urgency computation (Overdue / Today / Soon / Upcoming)
- `AlertType` enum with 6 types, each mapped to a notification channel
- Room v5 — `AlertEntity` + `AlertDao` with dedupe helpers + auto-cleanup
- `AlertRepository` with 8 methods including `insertOrIgnore`
- `AlertScannerWorker` (@HiltWorker) — periodic every 6 hours + one-shot
- `NotificationHelper` — builds NotificationCompat with deep-links (`pashu360://animal/{id}`)
- `Pashu360App` now implements `Configuration.Provider` + schedules periodic worker
- Manifest disables default `WorkManagerInitializer`
- `AlertsScreen` — filter chips, urgency-colored cards, Mark Done button
- `AlertBadgeViewModel` — always-on VM for live bell badge count on all tabs

Merged via PR #7 — commit `f320038`

---

## [0.5.0] — 2026-08-01 · Phase 4 — Finance Management ✅

Complete offline P&L tracking with income/expense logging, monthly trend chart,
category breakdown, and full transaction list.

### Added
- `FinancialRecord` + 11 categories with emojis
- `MonthlyPnL` + `CategoryBreakdown` aggregation models
- Room v4 — `FinancialRecordEntity` + indexed queries
- SQL `GROUP BY substr(record_date, ...)` for monthly buckets
- `FinanceRepository` with 7 methods
- `FinanceScreen`: Net Profit hero, monthly bar chart, expense breakdown, transactions
- Add Transaction bottom sheet with Income/Expense toggle, category chips
- ₹ Indian comma formatting (1,25,000)

Merged via PR #7 — commit `4bfd50c`

---

## [0.4.0] — 2026-08-01 · Phase 3 — Health Management ✅

Fully offline health tracking with three sub-modules: Health Records, Vaccinations
(with overdue detection), and Vet Contacts. Read side complete; add-forms in Phase 6.

### Added
- `HealthRecord` with event type / severity / symptoms
- `Vaccination` with `isOverdue()` / `isDueSoon()` helpers
- `VetContact` (name, phone, specialty, clinic)
- `SymptomCatalog` (13 Indian symptoms) + `VaccineCatalog` (8 default vaccines)
- Room v3 — 3 new entities with pipe-delimited symptoms + CASCADE FKs
- `HealthRepository` with 15 methods
- `HealthScreen` with green header + 3 summary chips (Overdue / Due Soon / Active Issues)
- TabRow: Records / Vaccinations / Vet Contacts
- Cards with severity color strip + medicine info
- Vaccination cards with OVERDUE / DUE TODAY badges
- Vet contact cards with call button
- Contextual FAB label per tab
- Friendly empty states

Merged via PR #7 — commit `b6e6f05`

---

## [0.3.1] — 2026-08-01 · Milk fixes

### Fixed
- `MilkRepository.buildBulkEntry` was one-shot — new animals didn't appear in the
  Milk sheet after being added. Replaced with `observeBulkEntry` returning a Flow
  that combines active-animals + records reactively. `touchedAnimals` set preserves
  in-progress typed values across re-emissions.

### Added
- `AnimalDetailViewModel` extended with milk sheet state (session, quantity, fat, snf)
- Log Milk header quick-action on Animal Detail now opens a per-animal bottom sheet
- Toast confirmation "✓ Logged X.X L" on save

Merged via PR #7 — commits `e0df5c0` + `58f09c6`

---

## [0.3.0] — 2026-08-01 · Phase 2 — Milk Production ✅

Full offline-first milk tracking module. Farmers can now log Morning + Evening
milk per animal, see today's total, view a 7-day bar chart, and grade milk quality.

### Added
- `MilkRecord` with quality grading (A+ / A / B / Below / Ungraded)
- `MilkSession` (Morning / Evening) + `BulkMilkEntry` + `DailyMilkTotal`
- Room v2 — `MilkRecordEntity` with unique `(animal, date, session)` and CASCADE FK
- `MilkRecordDao` with reactive Flow + weekly aggregation SQL
- `MilkRepository` with `observeBulkEntry`, `observeWeeklyTotals`, `saveBulkEntry`
- `MilkScreen` with green gradient header, today's KPI, 7-day bar chart, session breakdown
- `ModalBottomSheet` for bulk entry: session toggle, per-animal row, optional fat/SNF
- Live total across all typed quantities
- Wired into MainScaffold

Merged via PR #7 — commit `430d6e6`

---

## [0.2.1] — 2026-08-01 · Nav restructure + hamburger drawer

### Changed
- Bottom nav redesigned: Home / Animals / Milk / **Health** / **Finance**
  (was: Home / Animals / Milk / Alerts / More)

### Added
- Reusable `PashuAppBar` with hamburger + title + bell badge + profile
- `PashuDrawer` with 8 destinations + Logout
- `MainScaffold` wraps in `ModalNavigationDrawer` (gestures only on top-level tabs)
- All new destinations wired to Coming Soon placeholders
- Live bell badge from `AlertBadgeViewModel` (added later in PR #6)

Merged via PR #2 — commit `08a702d`

---

## [0.2.0] — 2026-08-01 · CONTRIBUTING workflow

### Added
- CONTRIBUTING.md — branch conventions, PR conventions, architecture rules
- `main` branch protection enabled on GitHub (PR-only)

Merged via PR #1 — commit `7e36668`

---

## [0.2.0] — 2026-08-01 · Phase 1 — Animal Management ✅

Complete offline-first animal management module. Farmers can now add, view, search, and manage their entire herd with data persisted locally in Room.

### Added
**Domain**
- `Animal` model with age calculation from DOB
- `Gender`, `AnimalStatus`, `AnimalFilter` enums
- `BreedCatalog` with 13 Indian dairy breeds (HF, Jersey, Sahiwal, Gir, Red Sindhi, Tharparkar, Rathi, Kankrej, Ongole, Hariana, Deoni, Cross Breed, Other)

**Data Layer (Room)**
- `AnimalEntity` with indices for `farm_id`, `barn_id`, `(farm_id, status)`, `(farm_id, tag_id)` unique
- `AnimalDao` with reactive Flow queries + search
- `AppDatabase` (v1) with schema export
- `AnimalRepositoryImpl` for entity ↔ domain mapping

**Use Cases**
- `GetAnimalsUseCase` (with filter)
- `GetAnimalByIdUseCase`
- `SearchAnimalsUseCase`
- `AddAnimalUseCase` (validates tag uniqueness + weight 1-2000 kg)
- `UpdateAnimalUseCase`
- `GetAnimalByQrUseCase`
- `GetAnimalStatsUseCase`

**Dependency Injection**
- `DatabaseModule` — Room + DAO providers
- `RepositoryModule` — @Binds AnimalRepository
- `SessionStore` — active farm ID (demo farm for now)

**Screens**
- **Animal List (Screen 7)** — Search, filter chips, animal cards, empty state, FAB
- **QR Scanner (Screen 8)** — CameraX + ML Kit barcode detection with runtime permission
- **Add Animal (Screen 9)** — Full form with breed dropdown, gender toggle, date picker
- **Animal Detail (Screen 10)** — Header with quick actions + 6 tabs (Overview built, others placeholder)

**Navigation**
- `MainScaffold` with 5-tab bottom nav (Home, Animals, Milk, Alerts, More)
- Nested NavHost for detail/add/qr screens
- `ComingSoonScreen` component for Milk/Alerts/More tabs

### Statistics
- 21 new Kotlin files
- 2,358 lines added
- Commit: `3c55558`

---

## [0.1.1] — 2026-08-01 · UI/UX Redesign

### Changed
- Complete visual redesign of Splash, Login, OTP, Register, Farm Setup, and Dashboard screens
- New color palette:
  - Primary: Rich forest green `#1F8B3E`
  - Accent: Warm sunflower amber `#F59E0B`
- Added brand gradient constants (`PashuGreen`, `PashuGreenDark`, `PashuGreenLight`, `PashuAmber`, etc.)
- Disabled dynamic color (was overriding brand palette)

### Added
- Green vertical gradient backgrounds on all auth screens
- Floating white card with rounded top corners (topStart/topEnd = 32.dp)
- Hero header with logo circle + branding on each auth screen
- 🇮🇳 +91 country prefix built into phone input
- Chunky 60dp rounded buttons with icon suffixes
- 6-box OTP input with focus highlighting and auto-verify
- Celebratory "🎉 Welcome to the family!" badge on Farm Setup
- 3-dot progress indicator on Farm Setup
- Redesigned Dashboard with:
  - Gradient header
  - Prominent Today's Milk KPI card with trend %
  - Mini stat cards (cows, vaccines, sick)
  - Quick action grid
  - Alert cards with urgency badges

Commit: `cb2fbae`

---

## [0.1.0] — 2026-07-31 · Phase 0 — Foundation ✅

Initial Android project setup with core architecture, dependencies, theme, navigation shell, and 6 base screens.

### Added
**Project Setup**
- Android Studio project with Kotlin + Jetpack Compose
- Gradle 9.5.0 with Kotlin DSL and version catalog
- Kotlin 2.4.10, AGP 9.3.1, KSP 2.3.10
- Min SDK 26 (Android 8.0), Target SDK 37

**Dependencies (30+)**
- Jetpack Compose BOM + Material 3
- Hilt DI 2.60.1
- Room Database 2.6.1
- WorkManager 2.10.0
- Kotlin Coroutines + Flow
- Kotlin Serialization + Datetime
- Supabase Kotlin SDK 3.7.0 + Ktor
- Coil (images), Vico (charts)
- CameraX + ML Kit Barcode 17.3.0
- Compose Navigation 2.8.4
- DataStore + EncryptedSharedPreferences
- Lottie animations

**Architecture**
- Clean Architecture folder structure (`core/`, `di/`, `feature/`)
- MVVM pattern with StateFlow + ViewModel
- Application class `Pashu360App.kt` with FCM notification channels
- AndroidManifest with permissions (INTERNET, CAMERA, POST_NOTIFICATIONS, ACCESS_NETWORK_STATE)
- Deep linking scheme: `pashu360://`

**Material 3 Theme**
- Light + Dark color schemes
- Custom typography (system font stack)
- Custom shapes (rounded corners)
- Semantic status colors (Active, Pregnant, Sick, Dry, Sold, Overdue, DueToday)

**Screens**
- **Splash Screen** — animated logo, green gradient, tagline
- **Login Screen** — phone OTP + email toggle
- **OTP Verification** — 6-digit input + resend timer
- **Register Screen** — name/phone/email/password
- **Farm Setup Screen** — farm name, location, animal count
- **Dashboard Screen** — placeholder with quick actions

**Navigation**
- `Screen` sealed class with all routes defined
- `Pashu360NavHost` — root nav host with auth graph
- Compose Navigation with typed arguments (phone, animalId, session)

Commits:
- `ee1a6f5` — Initial Android project setup

---

## [0.0.5] — 2026-07-31 · Build fixes

Multiple compatibility fixes for Kotlin/AGP/KSP version alignment.

### Fixed
- KSP version `2.2.10-2.0.4` didn't exist → downgraded to `2.2.10-2.0.2` (`3a69d5d`)
- Duplicate Kotlin plugin conflict — AGP 9 auto-applies it; removed explicit declaration (`d39c3e0`)
- KSP + AGP 9 built-in Kotlin conflict — added `android.disallowKotlinSourceSets=false` (`42d58a5`)
- Kotlin stdlib 2.4.0 pulled by Supabase 3.7 was incompatible with Kotlin 2.2 compiler:
  - First tried resolution strategy to force stdlib 2.2.10 (`da0bf62`)
  - Then properly upgraded Kotlin toolchain to 2.4.10 + KSP 2.3.10 (`8478c39`)

### Added
- Compose previews for Splash and Login screens (light + dark) (`30fdcbc`)

---

## [0.0.1] — 2026-07-30 · Documentation Phase

Initial 30 planning documents pushed to GitHub before any code.

### Added
- 30 planning documents in `docs/`:
  - PRD, SRS, User Stories (80+ stories)
  - Functional Requirements (127), Non-Functional Requirements
  - Full PostgreSQL schema + ER diagram
  - Kotlin project structure, MVVM architecture
  - Supabase RLS policies
  - Authentication, Navigation, Screen flow (all 32 screens)
  - UI Wireframes, Material 3 design system
  - Room + Sync engine design
  - Notification flow (FCM + Edge Functions)
  - Business rules, Security rules
  - API/Data Layer, State Management, Offline-First architecture
  - Dashboard, Analytics, Reports, AI module designs
  - Deployment plan + Future roadmap
- README.md project overview
- .gitignore for Android + Supabase secrets

Repository created: [github.com/ashima0102/pashu360](https://github.com/ashima0102/pashu360)

---

## Repository

- **GitHub:** https://github.com/ashima0102/pashu360
- **Package:** `com.pashu360.app`
- **Min Android:** 8.0 (API 26)
- **Language:** Kotlin 2.4.10
- **UI:** Jetpack Compose + Material 3
