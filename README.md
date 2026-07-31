# Pashu360 — Smart Dairy Farm Management

> Production-ready Android application for dairy farmers to manage their herd, milk production, vaccinations, health records, and more.

**Pashu (पशु) = animal · 360 = complete, all-round care**

---

## Project Status

📊 **[View live progress in PROGRESS.md →](./PROGRESS.md)**
📝 **[View changelog in CHANGELOG.md →](./CHANGELOG.md)**

**Current phase:** Phase 1 complete · Ready to start Phase 2

| Phase | Status |
|---|---|
| Phase 0 — Foundation & Auth | ✅ Done |
| Phase 1 — Animal Management | ✅ Done |
| Phase 2 — Milk Production | ⬜ Next |
| Phase 3–7 | ⬜ Planned |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt (Dagger) |
| Local DB | Room |
| Networking | Ktor (via Supabase Kotlin SDK) |
| Backend | Supabase (PostgreSQL + Auth + Storage) |
| Async | Kotlin Coroutines + Flow |
| Charts | Vico |
| QR Scan | CameraX + ML Kit |
| Push | Firebase Cloud Messaging |
| Images | Coil |

---

## Project Structure

```
Pashu360/
├── app/
│   └── src/main/java/com/pashu360/app/
│       ├── MainActivity.kt
│       ├── Pashu360App.kt              # Hilt Application class
│       ├── core/
│       │   ├── data/local/dao/         # Room DAOs
│       │   ├── data/remote/            # Supabase client
│       │   ├── data/sync/              # WorkManager sync engine
│       │   ├── domain/model/           # Domain models
│       │   ├── presentation/
│       │   │   ├── theme/              # Material 3 theme
│       │   │   ├── navigation/         # NavGraph + routes
│       │   │   └── components/         # Shared UI components
│       │   └── util/                   # Utilities
│       ├── di/                         # Hilt modules
│       └── feature/
│           ├── auth/                   # Login, OTP, Register
│           ├── dashboard/              # Home dashboard
│           ├── animal/                 # Animal management
│           ├── milk/                   # Milk production
│           ├── vaccination/            # Vaccinations
│           ├── health/                 # Health records
│           ├── feeding/                # Feeding management
│           ├── farm/                   # Farm management
│           ├── notifications/          # Alerts
│           └── settings/               # Settings
├── docs/                               # All 30 planning documents
├── supabase/                           # Database migrations
├── build.gradle.kts
└── gradle/libs.versions.toml           # Version catalog
```

---

## Documentation

See the [docs/](./docs/) folder for complete documentation (30 documents):

- [PRD](docs/01_PRD.md) — Product Requirements
- [SRS](docs/02_SRS.md) — Software Requirements
- [Database Design](docs/06_DatabaseDesign.md)
- [Kotlin Project Structure](docs/16_KotlinProjectStructure.md)
- [UI Wireframes](docs/14_UIWireframes.md) — All 32 screens
- [MVVM Architecture](docs/09_MVVMArchitecture.md)
- ...and 24 more

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 26+ (Android 8.0)

### Setup
1. Clone the repo
   ```bash
   git clone https://github.com/ashima0102/pashu360.git
   cd pashu360
   ```

2. Open in Android Studio → wait for Gradle sync

3. Add your Supabase credentials to `local.properties`:
   ```
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```

4. Run on emulator or device (min Android 8.0)

---

## Build Phases

| Phase | Status | Timeline |
|---|---|---|
| 0 — Auth + Shell | 🟡 In Progress | Week 1–2 |
| 1 — Animal Management | ⏳ | Week 3–4 |
| 2 — Milk Production | ⏳ | Week 5 |
| 3 — Vaccination + Alerts | ⏳ | Week 6–7 |
| 4 — Health + Feeding | ⏳ | Week 8–9 |
| 5 — Offline Sync | ⏳ | Week 10 |
| 6 — Finance + Reports | ⏳ | Week 11–12 |
| 7 — Polish + Localization | ⏳ | Week 13–14 |

---

## Package
`com.pashu360.app`

## Version
1.0.0 (MVP — Pre-release)
