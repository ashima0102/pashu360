# Changelog

All notable changes to Pashu360 are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased] — Phase 2 (In Progress)

### Planned
- Bulk milk entry screen (morning/evening for all animals)
- Milk history + charts per animal
- Herd-level analytics
- Dashboard "Today's Milk" wired to real data

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
