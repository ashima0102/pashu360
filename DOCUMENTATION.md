# Pashu360 — Smart Dairy Farm Management

> Production-ready Android SaaS application for dairy farmers, farm managers, and dairy cooperatives.

**Pashu (पशु) = animal · 360 = complete, all-round care**

---

## Project Overview

**App Name:** Pashu360  
**Tagline:** Complete Dairy Farm Management, in Your Pocket  
**Platform:** Android (Kotlin + Jetpack Compose)  
**Backend:** Supabase (PostgreSQL + Auth + Storage + Edge Functions)  
**Architecture:** MVVM + Clean Architecture + Repository Pattern  
**Target Users:** Dairy Farmers (Farm Owner — full access)  
**Version:** 1.0.0 (MVP)  
**Package:** `com.pashu360.app`

---

## Documentation Index

| # | Document | Description |
|---|---|---|
| 01 | [PRD](docs/01_PRD.md) | Product Requirements Document |
| 02 | [SRS](docs/02_SRS.md) | Software Requirements Specification |
| 03 | [User Stories](docs/03_UserStories.md) | All user stories by module |
| 04 | [Functional Requirements](docs/04_FunctionalRequirements.md) | FR-001 to FR-150+ |
| 05 | [Non-Functional Requirements](docs/05_NonFunctionalRequirements.md) | Performance, security, scalability |
| 06 | [Database Design](docs/06_DatabaseDesign.md) | Full PostgreSQL schema with SQL |
| 07 | [ER Diagram](docs/07_ERDiagram.md) | Entity relationship diagram |
| 08 | [Folder Structure](docs/08_FolderStructure.md) | Android project tree |
| 09 | [MVVM Architecture](docs/09_MVVMArchitecture.md) | Architecture layers and patterns |
| 10 | [Supabase Table Design](docs/10_SupabaseTableDesign.md) | Tables, RLS, indexes, triggers |
| 11 | [Authentication Flow](docs/11_AuthenticationFlow.md) | OTP + email auth flow |
| 12 | [Navigation Graph](docs/12_NavigationGraph.md) | Complete nav graph |
| 13 | [Screen Flow](docs/13_ScreenFlow.md) | All 32 screens and user journeys |
| 14 | [UI Wireframes](docs/14_UIWireframes.md) | ASCII wireframes for all screens |
| 15 | [Material 3 UI Design](docs/15_Material3UIDesign.md) | Colors, typography, components |
| 16 | [Kotlin Project Structure](docs/16_KotlinProjectStructure.md) | Full Kotlin file listing |
| 17 | [Room Database Structure](docs/17_RoomDatabaseStructure.md) | Entities, DAOs, Database class |
| 18 | [Sync Engine Design](docs/18_SyncEngineDesign.md) | Offline sync and conflict resolution |
| 19 | [Notification Flow](docs/19_NotificationFlow.md) | FCM + Edge Functions cron |
| 20 | [Business Rules](docs/20_BusinessRules.md) | Domain logic and validation rules |
| 21 | [Security Rules](docs/21_SecurityRules.md) | RLS policies and data security |
| 22 | [API / Data Layer](docs/22_APIDataLayer.md) | Repository pattern and data sources |
| 23 | [State Management](docs/23_StateManagement.md) | StateFlow, ViewModel, UiState |
| 24 | [Offline-First Architecture](docs/24_OfflineFirstArchitecture.md) | Cache strategy and sync design |
| 25 | [Dashboard Design](docs/25_DashboardDesign.md) | Dashboard layout and data sources |
| 26 | [Analytics Dashboard](docs/26_AnalyticsDashboard.md) | Charts, KPIs, metrics |
| 27 | [Reports Module](docs/27_ReportsModule.md) | Report types, PDF/Excel export |
| 28 | [AI Module Architecture](docs/28_AIModuleArchitecture.md) | V2 AI features and integration |
| 29 | [Deployment Plan](docs/29_DeploymentPlan.md) | Play Store, CI/CD, environments |
| 30 | [Future Roadmap](docs/30_FutureRoadmap.md) | V2, V3, and long-term vision |

---

## Tech Stack Summary

```
Android App
├── Language:      Kotlin
├── UI:            Jetpack Compose + Material 3
├── Architecture:  Clean Architecture + MVVM
├── DI:            Hilt
├── Navigation:    Compose Navigation
├── Local DB:      Room Database (offline-first)
├── Sync:          WorkManager + Supabase Realtime
├── Async:         Kotlin Coroutines + Flow
├── Charts:        Vico
├── QR Scan:       CameraX + ML Kit
├── Images:        Coil
└── PDF Export:    iTextPDF

Backend (Supabase)
├── Database:      PostgreSQL
├── Auth:          Phone OTP + Email
├── Storage:       Animal photos, documents
├── Edge Fns:      TypeScript/Deno (daily alert cron)
├── Realtime:      Live sync to devices
└── RLS:           Row Level Security (multi-tenant)

Push Notifications: Firebase Cloud Messaging (FCM)
```

---

## Build Phases

| Phase | Scope | Timeline |
|---|---|---|
| 0 | Auth + Navigation Shell + Dashboard | Week 1–2 |
| 1 | Animal Management (CRUD + QR) | Week 3–4 |
| 2 | Milk Production Logging + Charts | Week 5 |
| 3 | Vaccination + Alert System + FCM | Week 6–7 |
| 4 | Health + Feeding Management | Week 8–9 |
| 5 | Offline Sync Engine | Week 10 |
| 6 | Finance + Reports + PDF Export | Week 11–12 |
| 7 | Dark Mode + i18n + Polish | Week 13–14 |
| V2 | AI Assistant + Breeding + Cooperative | Month 5–8 |

---

## Quick Start (Developer)

```bash
# Clone and open in Android Studio
git clone <repo>
cd SmartDairyFarm/android

# Add your keys to local.properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=your_anon_key
GOOGLE_SERVICES_JSON=<path to google-services.json>

# Build
./gradlew assembleDebug
```

---

*Last updated: 2026-07-30*  
*Status: Planning & Documentation Phase*
