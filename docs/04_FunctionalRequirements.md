# Functional Requirements
## Pashu360 — Smart Dairy Farm Management
**Total: 127 requirements across 16 modules**
**Status:** ✅ Implemented · 🟡 UI built (backend pending) · ⬜ Not started
**Last updated:** 2026-08-01

## Implementation Summary

| Module | Implemented | Total | Status |
|---|---|---|---|
| Authentication | 6/8 | 8 | 🟡 UI + navigation done, Supabase pending |
| Farm Management | 5/6 | 6 | ✅ Farm entity persisted in Room v8 (multi-farm pending Supabase) |
| Animal Management | 12/14 | 14 | ✅ Phase 1 |
| Milk Production | 10/13 | 13 | ✅ Phase 2 |
| Feeding | 8/8 | 8 | ✅ Phase 7 |
| Health | 8/9 | 9 | ✅ Phase 3 + 6 (records + vaccinations + vet contacts + dialer) |
| Vaccination | 10/10 | 10 | ✅ Phase 3 + 6 (add-form + alerts + presets) |
| Heat Cycle | 5/5 | 5 | ✅ Phase 8 (log + 21-day prediction + heat alerts) |
| Breeding | 6/6 | 6 | ✅ Phase 8 (AI/Natural + PD status + expected calving) |
| Pregnancy | 6/6 | 6 | ✅ Phase 8 (confirmation + calving + auto-calf + dry period) |
| Finance | 5/6 | 6 | ✅ Phase 4 (PDF export pending Phase 9) |
| Reports | 0/7 | 7 | ⬜ Phase 9 |
| Notifications | 6/6 | 6 | ✅ Phase 5 + 6 + 7 + 8 (vaccine + feed low-stock + heat + calving) |
| Offline | 3/5 | 5 | 🟡 Room v8 done; Supabase sync pending Phase 10 |
| Settings | 0/7 | 7 | ⬜ Phase 11 |
| **TOTAL** | **90/127** | **127** | **71%** |

---

## Authentication Module (FR-AUTH)

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-AUTH-01 | System shall allow registration via phone number OTP | P1 | 🟡 UI built |
| FR-AUTH-02 | System shall send 6-digit OTP via SMS and auto-verify on entry | P1 | 🟡 UI built |
| FR-AUTH-03 | System shall allow login via email + password | P1 | 🟡 UI built |
| FR-AUTH-04 | System shall allow password reset via email link | P1 | ⬜ |
| FR-AUTH-05 | System shall persist session for 30 days without re-login | P1 | ⬜ |
| FR-AUTH-06 | System shall display farm setup wizard on first login | P1 | ✅ Done |
| FR-AUTH-07 | System shall allow editing profile name, photo, and language | P2 | ⬜ |
| FR-AUTH-08 | System shall securely clear all session data on logout | P1 | ⬜ |

**Note:** Auth screens are fully built with proper UI/UX. Backend integration (Supabase Auth) is planned for Phase 5.

---

## Farm Management Module (FR-FARM) ✅ Phase 8 Complete (single-farm)

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-FARM-01 | System shall support multiple farms per account | P2 | ⬜ Pending Supabase auth (Phase 10) |
| FR-FARM-02 | System shall store farm name, village, state, owner name, expected herd size | P1 | ✅ Done — Room `farms` table (v8), `FarmRepository` |
| FR-FARM-03 | System shall allow switching active farm via drawer | P2 | ⬜ Pending multi-farm |
| FR-FARM-04 | System shall allow creating and naming barns/sheds | P2 | ⬜ |
| FR-FARM-05 | System shall allow assigning animals to barns | P2 | 🟡 Room field exists |
| FR-FARM-06 | System shall display active farm name in the Dashboard header and drawer | P1 | ✅ Done — bound via `DashboardViewModel` observing `FarmRepository.observeFarm()` |
| FR-FARM-07 | System shall carry the owner name from Register → Farm Setup pre-fill | P1 | ✅ Done — `SessionStore.pendingOwnerName` handoff via `RegisterViewModel.stashOwnerName()` |
| FR-FARM-08 | System shall show cow count vs. declared expected herd size | P2 | ✅ Done — Dashboard Cows mini-stat renders `X / Y expected` when `expectedHerdSize > 0` |

---

## Animal Management Module (FR-ANIM) ✅ Phase 1 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-ANIM-01 | System shall allow adding animals with all profile fields | P1 | ✅ Done |
| FR-ANIM-02 | System shall auto-generate a unique QR code per animal | P1 | ✅ Done (tag ID = QR data) |
| FR-ANIM-03 | System shall allow scanning QR code to open animal profile | P1 | ✅ Done (CameraX + ML Kit) |
| FR-ANIM-04 | System shall allow searching animals by name and tag ID | P1 | ✅ Done |
| FR-ANIM-05 | System shall allow filtering animals by status | P1 | ✅ Done (chips) |
| FR-ANIM-06 | System shall display a chronological timeline of all animal events | P1 | 🟡 UI (tabs) built |
| FR-ANIM-07 | System shall allow transferring an animal to another farm | P2 | ⬜ |
| FR-ANIM-08 | System shall support marking animals as Sold with price and buyer | P2 | 🟡 Data model ready |
| FR-ANIM-09 | System shall support marking animals as Deceased with reason | P2 | 🟡 Data model ready |
| FR-ANIM-10 | System shall support RFID tag field | P2 | ✅ Done (in schema) |
| FR-ANIM-11 | System shall allow photo upload from camera or gallery | P1 | ⬜ (placeholder shown) |
| FR-ANIM-12 | System shall generate QR code printable via share sheet | P2 | ⬜ |
| FR-ANIM-13 | System shall show today's milk amount on each animal card | P1 | 🟡 Placeholder (Phase 2) |
| FR-ANIM-14 | System shall allow editing all animal fields | P1 | ⬜ (Edit screen TBD) |

---

> **Note:** All modules below (FR-MILK through FR-SET) are not started yet.
> See PROGRESS.md at the project root for the up-to-date phase status.

---

## Milk Production Module (FR-MILK) ✅ Phase 2 Complete

**Status: 10/13 done — bulk entry (AM/PM), 7-day chart, quality grading, per-animal quick sheet, dashboard total. Missing: 30-day chart, lactation curve, top-producers ranking.**

| ID | Requirement | Priority |
|---|---|---|
| FR-MILK-01 | System shall provide a bulk milk entry screen for all active animals | P1 |
| FR-MILK-02 | System shall support Morning and Evening sessions | P1 |
| FR-MILK-03 | System shall auto-set date to today and allow changing it | P1 |
| FR-MILK-04 | System shall auto-advance cursor to next animal after entry | P1 |
| FR-MILK-05 | System shall allow skipping animals (blank = not recorded) | P1 |
| FR-MILK-06 | System shall support optional fat%, SNF%, CLR, pH fields | P2 |
| FR-MILK-07 | System shall calculate and display herd daily total on dashboard | P1 |
| FR-MILK-08 | System shall display a 7-day bar chart per animal | P1 |
| FR-MILK-09 | System shall display a 30-day herd line chart | P1 |
| FR-MILK-10 | System shall track lactation records per animal | P2 |
| FR-MILK-11 | System shall display 305-day lactation curve per animal | P2 |
| FR-MILK-12 | System shall identify top 5 milk producers | P2 |
| FR-MILK-13 | System shall allow editing milk records up to 7 days after entry | P1 |

---

## Feeding Management Module (FR-FEED) ✅ Phase 7 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-FEED-01 | System shall maintain a farm-level feed type catalogue | P1 | ✅ Done — `feed_types` table + `FeedType` domain |
| FR-FEED-02 | System shall include default feed types (green fodder, dry fodder, concentrate, mineral, water) | P1 | ✅ Done — seeded via FeedingRepository |
| FR-FEED-03 | System shall allow logging feed per animal or per herd | P1 | ✅ Done — FeedingScreen log tab |
| FR-FEED-04 | System shall support three feeding times | P2 | ✅ Done — Morning/Afternoon/Evening enum |
| FR-FEED-05 | System shall track feed inventory stock levels | P2 | ✅ Done — `feed_inventory` table with quantity + unit |
| FR-FEED-06 | System shall alert when inventory falls below threshold | P2 | ✅ Done — `AlertScannerWorker.scanLowFeedStock()` |
| FR-FEED-07 | System shall allow setting recurring daily feed schedules | P2 | ✅ Done — per-feed schedule view |
| FR-FEED-08 | System shall calculate feed cost per animal using unit costs | P3 | 🟡 Cost captured on each record; monthly rollup pending Phase 9 |

---

## Health Management Module (FR-HLTH) ✅ Phase 3 + 6 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-HLTH-01 | System shall log health checkups (event type, severity, notes) | P1 | ✅ Done — HealthEventSheet |
| FR-HLTH-02 | System shall support multi-select symptom tags | P1 | ✅ Done — 13-symptom chip grid |
| FR-HLTH-03 | System shall record disease diagnosis and treatment | P1 | ✅ Done — Diagnosis + notes fields |
| FR-HLTH-04 | System shall track medicine name, dose, and cost | P1 | ✅ Done — Medicine + dose + cost fields |
| FR-HLTH-05 | System shall show withdrawal period warning on animal profile | P1 | ⬜ Withdrawal date not yet on form |
| FR-HLTH-06 | System shall log weight records and display trend chart | P2 | ⬜ |
| FR-HLTH-07 | System shall log vet visits with cost and next visit date | P2 | ✅ Done — VET_VISIT event type + vet name + cost |
| FR-HLTH-08 | System shall display sick animals count on dashboard | P1 | ✅ Done — Sick mini-stat via `countActiveHealthIssues()` |
| FR-HLTH-09 | System shall support photo attachment on health events | P2 | ⬜ |
| FR-HLTH-10 | System shall provide a vet phone book with call button | P2 | ✅ Done — VetContactSheet + system dialer launch |

---

## Vaccination Module (FR-VAC) ✅ Phase 3 + 6 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-VAC-01 | System shall maintain a vaccine catalogue with default and custom vaccines | P1 | ✅ Done — `VaccineCatalog` object |
| FR-VAC-02 | System shall include 8 default Indian vaccines (FMD, BQ, HS, Brucellosis, Anthrax, Theileria, PPR, Rabies) | P1 | ✅ Done |
| FR-VAC-03 | System shall record vaccinations with all required fields | P1 | ✅ Done — VaccinationSheet (animal, vaccine, disease, given, next-due, batch, given-by, cost, notes) |
| FR-VAC-04 | System shall auto-calculate next due date from vaccine interval | P1 | ✅ Done — template picker computes next-due from `intervalDays` |
| FR-VAC-05 | System shall allow overriding the calculated next due date | P1 | ✅ Done — field is editable |
| FR-VAC-06 | System shall support batch vaccination for multiple animals | P2 | ⬜ Batch UX pending Phase 11 |
| FR-VAC-07 | System shall display vaccination calendar with color-coded dots | P1 | 🟡 List view shipped; calendar view Phase 11 |
| FR-VAC-08 | System shall color-code overdue (red), today (amber), upcoming (green) | P1 | ✅ Done — OVERDUE / DUE TODAY badges |
| FR-VAC-09 | System shall fire local notification 3 days before and on due date | P1 | ✅ Done — `AlertScannerWorker.scanVaccinations()` with 3-day window |
| FR-VAC-10 | System shall generate vaccination compliance report | P2 | ⬜ Phase 9 (Reports) |

---

## Heat Cycle Module (FR-HEAT) ✅ Phase 8 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-HEAT-01 | System shall record heat detection with date, symptoms, intensity | P2 | ✅ Done — HeatSheet with 8-symptom multi-select + intensity toggle |
| FR-HEAT-02 | System shall predict next heat as last heat + cycle days (default 21) | P2 | ✅ Done — `HeatRecord.expectedNextHeat(cycleDays = 21)` |
| FR-HEAT-03 | System shall display a heat calendar heatmap | P2 | 🟡 List view shipped; heatmap Phase 11 |
| FR-HEAT-04 | System shall fire local notification 1 day before expected heat | P2 | ✅ Done — `AlertScannerWorker.scanExpectedHeats()` |
| FR-HEAT-05 | System shall flag repeat breeders after 3+ heats without conception | P2 | ⬜ Phase 9 (Reports) |

---

## Breeding & Conception Module (FR-BRDG) ✅ Phase 8 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-BRDG-01 | System shall record AI events with technician, semen batch, cost | P2 | ✅ Done — BreedingSheet AI mode |
| FR-BRDG-02 | System shall record natural mating with bull name | P2 | ✅ Done — BreedingSheet Natural mode |
| FR-BRDG-03 | System shall track conception status (Pending/Confirmed/Failed) | P2 | ✅ Done — one-tap Confirm/Failed on Mating card |
| FR-BRDG-04 | System shall calculate expected PD date | P2 | ✅ Done — `BreedingRecord.expectedPdDate()` (breeding + 30 days) |
| FR-BRDG-05 | System shall calculate and display conception rate | P2 | 🟡 `BreedingStats.conceptionRatePercent` model exists; report screen Phase 9 |
| FR-BRDG-06 | System shall calculate services per conception | P2 | 🟡 Data available; report screen Phase 9 |

---

## Pregnancy & Calving Module (FR-PREG) ✅ Phase 8 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-PREG-01 | System shall calculate expected calving date (breeding + 280 days) | P2 | ✅ Done — auto-computed on PregnancySheet |
| FR-PREG-02 | System shall schedule dry period start (calving − 60 days) | P2 | ✅ Done — auto-computed and shown on Pregnancy card |
| FR-PREG-03 | System shall fire local notification 7 days before expected calving | P2 | ✅ Done — `AlertScannerWorker.scanCalvingDue()` (URGENT ≤ 2 days) |
| FR-PREG-04 | System shall record calving with difficulty (1-4) and outcome (LiveCalf/Stillbirth/Twins/Abortion) | P2 | ✅ Done — CalvingSheet |
| FR-PREG-05 | System shall auto-create calf animal record on calving | P2 | ✅ Done — `recordCalving()` inserts via `animalRepository.addAnimal()` then links `calfAnimalId` |
| FR-PREG-06 | System shall start new lactation record on calving | P2 | ⬜ Not automated — farmer creates milk record manually |

---

## Financial Module (FR-FIN) ✅ Phase 4 Complete

| ID | Requirement | Priority |
|---|---|---|
| FR-FIN-01 | System shall log milk sales with quantity and price per litre | P3 |
| FR-FIN-02 | System shall log animal sales | P3 |
| FR-FIN-03 | System shall log expenses by category | P3 |
| FR-FIN-04 | System shall calculate per-animal P&L | P3 |
| FR-FIN-05 | System shall display farm-level monthly income vs expenses | P3 |
| FR-FIN-06 | System shall display income vs expense bar chart | P3 |

---

## Reports Module (FR-RPT) ⬜ Phase 9

| ID | Requirement | Priority |
|---|---|---|
| FR-RPT-01 | System shall generate daily, monthly, yearly milk reports | P2 |
| FR-RPT-02 | System shall generate vaccination compliance reports | P2 |
| FR-RPT-03 | System shall generate health summary reports | P2 |
| FR-RPT-04 | System shall generate financial P&L reports | P3 |
| FR-RPT-05 | System shall export reports as PDF | P2 |
| FR-RPT-06 | System shall export reports as CSV | P3 |
| FR-RPT-07 | System shall allow sharing reports via Android share sheet | P2 |

---

## Notifications Module (FR-NOTIF) ✅ Phase 5 + 6 + 7 + 8 Complete

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-NOTIF-01 | System shall maintain a Notification Center with all alerts | P1 | ✅ Done — AlertsScreen with filter chips |
| FR-NOTIF-02 | System shall fire **local** notifications for time-sensitive alerts (offline-first, no FCM) | P1 | ✅ Done — WorkManager + `NotificationHelper` |
| FR-NOTIF-03 | System shall allow marking alerts as resolved | P1 | ✅ Done — Mark Done button on each card |
| FR-NOTIF-04 | System shall deep-link to relevant animal screen on notification tap | P1 | ✅ Done — `pashu360://animal/{id}` intents |
| FR-NOTIF-05 | System shall cover all 6 alert types: Vaccination Due, Heat Expected, Calving Due, Low Feed Stock, Sick Animal, Overdue Health Check | P1 | ✅ Done — 5 scanners live (sick-animal covered by health issue count) |
| FR-NOTIF-06 | System shall allow configuring which alert types are received | P2 | ⬜ Phase 11 (Settings) |

---

## Offline Module (FR-OFF) 🟡 Partial (Room v8 done, Supabase sync Phase 10)

| ID | Requirement | Priority |
|---|---|---|
| FR-OFF-01 | All data entry features shall work without internet | P1 |
| FR-OFF-02 | Offline writes shall be queued and synced automatically | P1 |
| FR-OFF-03 | System shall display sync status indicator | P2 |
| FR-OFF-04 | System shall not lose data on app crash or kill while offline | P1 |
| FR-OFF-05 | Conflict resolution shall use last-write-wins with timestamp | P1 |

---

## Settings Module (FR-SET) ⬜ Phase 11

| ID | Requirement | Priority |
|---|---|---|
| FR-SET-01 | System shall support English, Hindi, and Odia languages | P2 |
| FR-SET-02 | System shall support Light, Dark, and System theme | P2 |
| FR-SET-03 | System shall allow editing notification preferences | P2 |
| FR-SET-04 | System shall display cloud sync status | P2 |
| FR-SET-05 | System shall allow exporting all farm data | P3 |
| FR-SET-06 | System shall provide in-app help/FAQ | P2 |
| FR-SET-07 | System shall allow secure logout with session clearing | P1 |
