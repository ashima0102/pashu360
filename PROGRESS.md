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
| **Phase 3** — Health Management (records + vaccinations + vet contacts) | ✅ Done (read side) | 90% |
| **Phase 4** — Finance Management | ✅ Done | 100% |
| **Phase 5** — Alerts + Local Notifications (WorkManager) | ✅ Done | 100% |
| **Phase 6** — Add-forms wiring + alert scheduler triggers | ⬜ Not started | 0% |
| **Phase 7** — Feeding + Breeding + Pregnancy | ⬜ Not started | 0% |
| **Phase 8** — Reports + PDF/CSV export | ⬜ Not started | 0% |
| **Phase 9** — Offline sync with Supabase | ⬜ Not started | 0% |
| **Phase 10** — Localization + polish + Play Store | ⬜ Not started | 0% |

**5 of 10 phases complete (~50%)** — the whole core-tracking loop (animals → milk → health → finance → alerts) is live locally.

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

## Phase 6 — Add-forms wiring + alert scheduler triggers ⬜ (Next)

Planned:
- Add Vaccination bottom sheet in Health screen (calls `AlertScheduler.scanNow()` after save)
- Add Health Event bottom sheet with symptom multi-select
- Add Vet Contact form
- Verify overdue chip on Health screen updates from real data
- Verify bell badge lights up after adding a due-soon vaccine

---

## Phase 7 — Feeding + Breeding + Pregnancy ⬜

Planned (drawer destinations):
- Feed log + inventory + low-stock alerts
- Heat calendar + AI record + conception status
- Pregnancy timeline + expected calving alerts

---

## Phase 8 — Reports + PDF/CSV export ⬜

Planned:
- Milk production report (per-animal + herd)
- Vaccination compliance report
- Financial P&L PDF
- CSV export for accountants

---

## Phase 9 — Supabase cloud sync ⬜

Planned:
- Supabase project + RLS policies
- Real auth (replace SessionStore's demo farm)
- Sync engine writing local Room mutations to remote
- Realtime pull

---

## Phase 10 — Localization + polish + Play Store ⬜

Planned:
- Hindi + Odia strings
- Onboarding tutorial
- Compose animation polish
- Play Store beta

---

## Statistics

| Metric | Count |
|---|---|
| Total Kotlin files | ~80 |
| Total lines of code | ~10,000 |
| Screens built (functional) | 15+ of 32 (~47%) |
| Modules complete | 5 of 10 (50%) |
| Room DB version | 5 |
| Git commits on main | 40+ |
| Merged PRs | 7 |

---

## Repository

- **GitHub:** https://github.com/ashima0102/pashu360
- **Default branch:** `main` (branch-protected, PR-only)
- **Latest commit:** `d59adca` — Merge PR #7 (all remaining phases)
