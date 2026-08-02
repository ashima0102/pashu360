# Pashu360 — Progress Tracker

> Live tracking of what's built vs what's planned.
> Updated: **2026-08-02**

---

## Overall Status

| Phase | Status | Progress |
|---|---|---|
| **Phase 0** — Foundation & Auth Shell | ✅ Done | 100% |
| **Phase 1** — Animal Management | ✅ Done | 100% |
| **Phase 2** — Milk Production | ✅ Done | 100% |
| **Phase 3** — Health Management (records + vaccinations + vet contacts) | ✅ Done | 100% |
| **Phase 4** — Finance Management | ✅ Done | 100% |
| **Phase 5** — Alerts + Local Notifications (WorkManager) | ✅ Done | 100% |
| **Phase 6** — Health add-forms + alert scheduler triggers | ✅ Done | 100% |
| **Phase 7** — Feeding management | ✅ Done | 100% |
| **Phase 8** — Breeding + Pregnancy + Calving | ✅ Done | 100% |
| **Phase 9** — Reports + PDF/CSV export | ⬜ Not started | 0% |
| **Phase 10** — Offline sync with Supabase | ⬜ Not started | 0% |
| **Phase 11** — Localization + polish + Play Store | ⬜ Not started | 0% |

**8 of 11 phases complete (~73%)** — reproduction lifecycle fully closed: heat → mating → pregnancy → calving → calf becomes an animal.

---

## Phase 0 — Foundation ✅

**Completed:** 2026-07-31

- Android Studio project with Kotlin + Jetpack Compose
- Gradle 9.5, Kotlin 2.4.10, KSP 2.3.10, AGP 9.3.1
- Clean Architecture folder tree, MVVM + Hilt DI
- Material 3 theme (forest green / amber)
- Root NavHost, 5-tab bottom nav shell
- 6 auth screens (Splash, Login, OTP, Register, Farm Setup) + Dashboard shell

Notable early fixes: KSP2 codegen bug workaround (non-suspend DAO methods),
Kotlin 2.4 stdlib migration (`kotlin.time.Clock`), Room 2.6 API fixes.

---

## Phase 1 — Animal Management ✅

**Completed:** 2026-08-01

- Animal domain + Room entity + DAO (Room v1)
- AnimalRepository with Flow reads + IO-dispatched writes
- AnimalListScreen with search / filter chips / cards / FAB
- AddAnimalScreen with breed dropdown, gender toggle, DOB picker
- AnimalDetailScreen with 6 tabs (Overview built; others placeholder)
- QrScannerScreen using CameraX + ML Kit
- MainScaffold bottom nav shell

---

## Phase 2 — Milk Production ✅

**Completed:** 2026-08-01 · merged 2026-08-02 (PR #7)

### Domain
- `MilkRecord`, `MilkSession` (Morning/Evening), `MilkGrade` (A+/A/B), `BulkMilkEntry`, `DailyMilkTotal`
- Quality grading from Indian FSSAI fat% + SNF% thresholds

### Data (Room v2)
- MilkRecordEntity with unique `(animal, date, session)` and CASCADE FK to Animals
- MilkRecordDao with reactive Flow + weekly aggregation SQL

### Presentation
- MilkScreen with today's KPI, 7-day bar chart, session breakdown
- Bulk milk entry bottom sheet (reactively re-emits when animals are added)
- Per-animal milk entry sheet from Animal Detail (Log Milk header button)

---

## Phase 3 — Health Management ✅ (read side)

**Completed:** 2026-08-01 · merged 2026-08-02 (PR #7)

### Domain
- `HealthRecord` (checkup / disease / injury / vet visit), severity, symptoms
- `Vaccination` with `isOverdue()` / `isDueSoon()` helpers
- `VetContact` (name, phone, specialty, clinic)
- `SymptomCatalog` (13 Indian symptoms) + `VaccineCatalog` (8 default vaccines)

### Data (Room v3)
- 3 new entities with CASCADE FK to Animals
- Pipe-delimited symptom serialization
- Overdue + due-soon count queries

### Presentation
- HealthScreen with 3 tabs: **Records | Vaccinations | Vet Contacts**
- 3 summary chips in header (Overdue / Due Soon / Active Issues)
- Cards with severity color strip, overdue badges
- Empty states + contextual FAB per tab

### ⏳ Follow-up (Phase 6)
- Add-record / Add-vaccination / Add-contact bottom sheets still show a "coming next" toast

---

## Phase 4 — Finance Management ✅

**Completed:** 2026-08-01 · merged 2026-08-02 (PR #7)

### Domain
- `FinancialRecord`, 11 categories (Milk Sale, Feed, Medicine, etc.)
- `MonthlyPnL`, `CategoryBreakdown`

### Data (Room v4)
- FinancialRecordEntity + indexed queries
- SQL `GROUP BY substr(record_date, ...)` for monthly buckets

### Presentation
- FinanceScreen with Net Profit hero, monthly bar chart, expense breakdown, transactions list
- Add Transaction bottom sheet with Income/Expense toggle, category chips
- ₹ Indian comma formatting

---

## Phase 5 — Alerts + Local Notifications ✅

**Completed:** 2026-08-02 · merged 2026-08-02 (PR #7)

### Domain
- `Alert` with type / priority / urgency helpers
- `AlertType` — 6 types with per-type notification channels
- `AlertFilter` for tab filtering

### Data (Room v5)
- AlertEntity with indexes + dedupe helpers (`getBySourceId`, `getByAnimalTypeAndDate`)
- Atomic `getPendingNotifications + markNotified`
- Auto-cleanup of resolved alerts > 90 days

### WorkManager
- `AlertScannerWorker` (@HiltWorker) periodic every 6 hours + one-shot
- `Pashu360App` implements `Configuration.Provider` with HiltWorkerFactory
- Manifest disables default `WorkManagerInitializer`
- Scans vaccinations with `next_due_date` within 3-day window and creates alerts

### Notifications
- `NotificationHelper` builds NotificationCompat.Builder with deep links (`pashu360://animal/{id}`)
- Uses per-type channels already registered in Pashu360App

### UI
- AlertsScreen replaces placeholder: filter chips, urgency-colored cards, Mark Done
- Live bell badge count via `AlertBadgeViewModel` on all 5 top-level tabs

### ⏳ Follow-up (Phase 6)
- Add-vaccination sheet needs to call `AlertScheduler.scanNow()` after saving
- Heat + calving scanners still to be written

---

## Phase 6 — Add-forms wiring + alert scheduler triggers ✅

**Completed:** 2026-08-02 (PR #9)

- Add Vaccination bottom sheet on Health screen (calls `AlertScheduler.scanNow()` after save)
- Add Health Event sheet with symptom multi-select
- Add Vet Contact form
- Overdue chip on Health screen driven by live data
- Bell badge auto-refreshes on due-soon vaccinations

---

## Phase 7 — Feeding Management ✅

**Completed:** 2026-08-02 (PR #10)

- FeedType / FeedRecord / FeedInventory domain
- Room v6 branch (feeding): 3 entities + DAOs + repository
- Feeding screen with per-animal daily log, per-feed schedule, and inventory view
- Low-stock alerts fire when kg-remaining < threshold

---

## Phase 8 — Breeding + Pregnancy + Calving ✅

**Completed & merged:** 2026-08-02 (PR #11)

### Reproduction lifecycle domain
- `HeatRecord`, `BreedingRecord`, `PregnancyRecord` + 5 enums (HeatIntensity,
  BreedingType AI/NATURAL, ConceptionStatus PENDING/CONFIRMED/FAILED, PdMethod, CalvingOutcome)
- 3 Room entities + 3 DAOs (non-suspend, KSP2-safe), FK CASCADE to animals
- `BreedingRepository` (18 methods) + Impl using `combine()` with `animalsMap` for
  denormalized detail views. `recordCalving()` auto-creates calf Animal when requested.
- `BreedingViewModel` with 4 form states (Heat / Mating / Pregnancy / Calving)
- `BreedingScreen` — green-gradient header + 3 summary chips + 4 tabs + contextual FAB
- 4 bottom-sheet forms with animal picker, symptom multi-select, AI/Natural toggle,
  PD-method radio, calving outcome + difficulty + auto-calf creation
- `AlertScannerWorker` extended: `scanExpectedHeats()` (21-day cycle, 1 day early)
  + `scanCalvingDue()` (7-day window, URGENT ≤ 2 days)

### Farm persistence
- `Farm` domain + Room `FarmEntity` / `FarmDao` / `FarmRepository`
- `FarmSetupViewModel` persists Owner Name + Farm Name + Village + State + Expected herd
- `RegisterViewModel.stashOwnerName()` handoff so Register's Full Name pre-fills
  the setup wizard's Owner Name field

### Dashboard live wiring
- `DashboardViewModel` combines animal count, vaccines overdue+due-soon (7-day window),
  active health issues, Farm data, and today's date/time-based greeting
- Home header shows real farm name, first name (fallback "Farmer"), and formatted
  today's date (e.g. "Sat, 2 Aug"). Greeting picks morning/afternoon/evening/night from clock
- Cows mini-stat shows `X / Y expected` when farmer specified an expected herd size
- Drawer header binds to the same live data
- Dashboard quick-action cards (Log Milk / Vaccine / Add Cow / Feed) now navigate
  to their destinations (previously non-clickable decorative cards)

---

## Phase 9 — Reports + PDF/CSV export ⬜

Planned:
- Milk production report (per-animal + herd)
- Vaccination compliance report
- Financial P&L PDF
- CSV export for accountants

---

## Phase 10 — Supabase cloud sync ⬜

Planned:
- Supabase project + RLS policies
- Real auth (replace SessionStore's demo farm)
- Sync engine writing local Room mutations to remote
- Realtime pull

---

## Phase 11 — Localization + polish + Play Store ⬜

Planned:
- Hindi + Odia strings
- Onboarding tutorial
- Compose animation polish
- Play Store beta

---

## Statistics

| Metric | Count |
|---|---|
| Total Kotlin files | ~100+ |
| Total lines of code | ~15,000 |
| Screens built (functional) | 22+ of 32 (~69%) |
| Modules complete | 8 of 11 (~73%) |
| Room DB version | 8 |
| Merged PRs | 11 |

---

## Repository

- **GitHub:** https://github.com/ashima0102/pashu360
- **Default branch:** `main` (branch-protected, PR-only)
- **Latest commit:** `88fd34b` — Phase 8: Breeding, Pregnancy & Calving (PR #11)
