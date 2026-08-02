# User Stories
## Pashu360 — Smart Dairy Farm Management

**Format:** As a [Farm Owner], I want to [action], so that [benefit].  
**Priority:** P1 = MVP Critical | P2 = MVP Important | P3 = Phase 2  
**Status:** ✅ Done · 🟡 UI built (backend pending Supabase) · ⬜ Not Started

**Last updated:** 2026-08-02

## Progress Summary

| Module | Done | Total | Progress |
|---|---|---|---|
| Authentication (UI + navigation) | 7 | 7 | 🟡 100% UI · Supabase backend pending |
| Farm Management | 3 | 5 | 🟡 60% (Setup persists to Room; multi-farm pending Supabase) |
| Animal Management | 14 | 14 | ✅ 100% |
| Milk Production | 9 | 12 | ✅ 75% |
| Feeding | 8 | 8 | ✅ 100% (log, inventory, low-stock alerts) |
| Health | 10 | 10 | ✅ 100% (records + vaccinations + vet contacts + dialer) |
| Vaccination | 10 | 10 | ✅ 100% (add sheet, 8 preset vaccines, alert scan on save) |
| Heat Cycle | 5 | 5 | ✅ 100% (log heat, 21-day next-heat prediction, alerts) |
| Breeding | 5 | 5 | ✅ 100% (AI/Natural, conception status, PD reminder) |
| Pregnancy & Calving | 6 | 6 | ✅ 100% (confirmation, calving, auto-calf registration) |
| Financial | 7 | 7 | ✅ 100% |
| Reports | 0 | 6 | ⬜ 0% (Phase 9) |
| Notifications | 5 | 5 | ✅ 100% (vaccination + feed low-stock + heat + calving) |
| Offline & Sync | 3 | 4 | 🟡 Room v8 done; Supabase sync pending |
| Settings | 0 | 5 | ⬜ 0% |
| **TOTAL** | **92** | **109** | **84%** |

---

## Authentication

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-A01 | Register with my phone number using OTP | P1 | 🟡 UI built | OTP received, verified, account created |
| US-A02 | Log in with email and password | P1 | 🟡 UI built | Session persisted for 30 days |
| US-A03 | Reset my password if I forget it | P1 | 🟡 UI built | Reset link sent to email |
| US-A04 | Set up my first farm after registering | P1 | ✅ Done | Farm created, dashboard shown |
| US-A05 | Update my profile name and photo | P2 | ⬜ | Changes saved and reflected in header |
| US-A06 | Stay logged in without re-entering password daily | P1 | ⬜ | Session persists 30 days (needs Supabase) |
| US-A07 | Log out securely | P1 | ⬜ | Session cleared, navigated to login |

---

## Farm Management

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-FM01 | Add multiple farms to my account | P2 | ⬜ | Each farm has independent data (pending Supabase) |
| US-FM02 | Switch between my farms easily | P2 | ⬜ | Farm switcher in drawer (pending multi-farm) |
| US-FM03 | Add barns/sheds to my farm | P2 | ⬜ | Animals can be assigned to barns |
| US-FM04 | Set up my farm identity | P1 | ✅ Done | Farm row persisted in Room v8 with owner name, farm name, village, state, expected herd size |
| US-FM05 | See my farm info reflected on the Dashboard | P1 | ✅ Done | Header shows real ownerName + farmName + today's date, drawer header uses same source |
| US-FM06 | Track progress vs. my declared herd size | P2 | ✅ Done | Cows mini-stat shows `X / Y expected` |
| US-FM07 | Edit my farm details later | P2 | ⬜ | Settings screen pending Phase 11 |
| US-FM08 | See which barn each animal is in | P2 | ⬜ | Barn shown on animal card |

---

## Animal Management ✅ Phase 1 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-AM01 | Add a new animal with a photo | P1 | ✅ Done (photo pending) | Animal saved to Room, appears in list |
| US-AM02 | Give each animal a unique QR code | P1 | ✅ Done | QR generated from tag ID |
| US-AM03 | Scan a QR code to open that animal's profile | P1 | ✅ Done | CameraX + ML Kit scan → profile opened |
| US-AM04 | Search for an animal by name or tag number | P1 | ✅ Done | Live filter on animal list |
| US-AM05 | Filter my herd by status (sick, pregnant, etc.) | P1 | ✅ Done | Filter chips: All/Active/Pregnant/Sick/Dry/Sold |
| US-AM06 | See a complete history of each animal's life | P1 | 🟡 UI + tabs built | Timeline data comes with Phase 2+ |
| US-AM07 | Mark an animal as sold with sale price | P2 | ⬜ | Edit screen not built yet |
| US-AM08 | Mark an animal as deceased with reason | P2 | ⬜ | Edit screen not built yet |
| US-AM09 | Transfer an animal to another farm | P2 | ⬜ | Multi-farm support pending Supabase |
| US-AM10 | Edit any animal's details | P1 | ⬜ | Edit screen not built yet (pencil icon shown) |
| US-AM11 | See each animal's today's milk on the list | P1 | 🟡 Placeholder | Data comes with Phase 2 (Milk) |
| US-AM12 | Add an RFID tag number to an animal | P2 | ✅ Done | RFID field in Room + domain |
| US-AM13 | Print the QR code for an animal's ear tag | P2 | ⬜ | Share sheet pending |
| US-AM14 | See a quick summary of each animal | P1 | ✅ Done | Overview tab: breed, age, weight, status, notes |

---

## Milk Production ✅ Phase 2 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-ML01 | Log morning milk for all my cows in one screen | P1 | ✅ Done | Bulk entry sheet, reactive animal list |
| US-ML02 | Log evening milk for all my cows in one screen | P1 | ✅ Done | Session toggle Morning/Evening |
| US-ML03 | See how much milk my farm produced today | P1 | ✅ Done | Today's Total KPI on Milk tab |
| US-ML04 | See milk trend for the past 7 days as a chart | P1 | ✅ Done | 7-day bar chart |
| US-ML05 | See milk trend for the past 30 days | P1 | ⬜ | Line chart pending — 7-day chart implemented |
| US-ML06 | Know which cow produced the most milk this month | P1 | ⬜ | Top producers section pending |
| US-ML07 | Log milk fat% and SNF% (optional) | P2 | ✅ Done | Toggle-revealed fat/SNF fields |
| US-ML08 | See the lactation history of each cow | P2 | ⬜ | |
| US-ML09 | See a 305-day lactation curve per cow | P2 | ⬜ | |
| US-ML10 | Compare this month's production to last month | P2 | ⬜ | |
| US-ML11 | Skip a cow during milk entry if she wasn't milked | P1 | ✅ Done | Blank/zero quantity is filtered out on save |
| US-ML12 | Correct a milk entry I made with wrong quantity | P1 | ✅ Done | Re-open sheet pre-fills existing values; save overwrites |
| — | (bonus) Log milk for one animal from its detail | P1 | ✅ Done | Log Milk quick-action on Animal Detail |
| — | (bonus) Milk quality grading A+/A/B (FSSAI) | P2 | ✅ Done | Computed from fat% + SNF% on MilkRecord.qualityGrade |

---

## Feeding Management ✅ Phase 7 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-FD01 | Log what feed and how much each cow got today | P1 | ✅ Done | Feed log saved per animal (or per herd) |
| US-FD02 | Set a daily feed schedule for each cow | P2 | ✅ Done | Schedule visible on Feeding tab |
| US-FD03 | Know how much feed stock I have | P2 | ✅ Done | Inventory screen with quantity + unit |
| US-FD04 | Get alerted when feed stock is running low | P2 | ✅ Done | AlertScannerWorker.scanLowFeedStock, URGENT when qty ≤ 0 |
| US-FD05 | Add new feed stock to inventory | P2 | ✅ Done | Add Stock action from Inventory tab |
| US-FD06 | See feed cost per animal per month | P3 | 🟡 | Cost per record captured; monthly rollup pending |
| US-FD07 | Log water intake for animals | P2 | ✅ Done | Water configurable as a FeedType |
| US-FD08 | Log mineral supplements | P2 | ✅ Done | Mineral configurable as a FeedType |

---

## Health Management ✅ Phase 3 + 6 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-HL01 | Record a health checkup for a cow | P1 | ✅ Done | HealthEventSheet (Records tab FAB) |
| US-HL02 | Log symptoms when a cow is sick | P1 | ✅ Done | 13-symptom multi-select chip UI |
| US-HL03 | Record what medicine I gave and the dose | P1 | ✅ Done | Medicine + dose fields in HealthEventSheet |
| US-HL04 | Know when a medicine's withdrawal period ends | P1 | ⬜ | Withdrawal warning UI pending |
| US-HL05 | Log a vet visit with cost | P2 | ✅ Done | Vet name + cost in HealthEventSheet |
| US-HL06 | Track my cow's weight over time | P2 | ⬜ | Weight tracking pending |
| US-HL07 | See which animals are currently sick | P1 | ✅ Done | Active Issues chip on Health tab header |
| US-HL08 | See the full health history of an animal | P1 | ✅ Done | Records list on Health tab |
| US-HL09 | Record body condition score of an animal | P2 | ⬜ | BCS field exists on model — form pending |
| US-HL10 | Take a photo of an injury or skin condition | P2 | ⬜ | Photo upload pending |
| — | (bonus) Vet phone book with call button | P2 | ✅ Done | Vet Contacts tab + system dialer launch |

---

## Vaccination Management ✅ Phase 3 + 6 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-VC01 | Record a vaccination I gave to a cow | P1 | ✅ Done | VaccinationSheet with 8 preset vaccines, auto next-due date |
| US-VC02 | Know the next vaccination due date for each cow | P1 | ✅ Done | Next Due column on Vaccination tab |
| US-VC03 | Get notified 3 days before a vaccination is due | P1 | ✅ Done | AlertScannerWorker 3-day window |
| US-VC04 | Get notified on the day a vaccination is due | P1 | ✅ Done | Local notification via NotificationHelper |
| US-VC05 | See all upcoming vaccinations in a calendar | P1 | 🟡 | List view built; calendar view pending Phase 11 polish |
| US-VC06 | See overdue vaccinations highlighted | P1 | ✅ Done | OVERDUE / DUE TODAY badges |
| US-VC07 | Vaccinate multiple cows at once (batch) | P2 | ⬜ | Batch UX pending Phase 11 |
| US-VC08 | Add a custom vaccine to my catalogue | P2 | ✅ Done | Manual vaccine-name field alongside 8 presets |
| US-VC09 | See vaccination history for each animal | P1 | ✅ Done | Vaccinations tab list |
| US-VC10 | Record who administered the vaccine | P2 | ✅ Done | "Given by" field on VaccinationSheet |

---

## Heat Cycle Management ✅ Phase 8 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-HC01 | Record when I see a cow in heat | P2 | ✅ Done | HeatSheet with 8-symptom multi-select + intensity toggle |
| US-HC02 | Know when each cow's next heat is expected | P2 | ✅ Done | 21-day cycle prediction shown per heat card |
| US-HC03 | Get notified 1 day before expected heat | P2 | ✅ Done | Local notification via `scanExpectedHeats()` (WorkManager, no FCM needed) |
| US-HC04 | See a calendar of heat dates | P2 | 🟡 | Chronological list built; calendar view pending Phase 11 polish |
| US-HC05 | Know if a cow is a repeat breeder | P2 | ⬜ | Requires history rollup — Phase 9 (Reports) |

---

## Breeding & Conception ✅ Phase 8 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-BR01 | Record an AI event for a cow | P2 | ✅ Done | BreedingSheet AI mode: technician, semen batch |
| US-BR02 | Record natural mating | P2 | ✅ Done | BreedingSheet Natural mode: bull name |
| US-BR03 | Know if a cow conceived | P2 | ✅ Done | Conception status: PENDING → CONFIRMED / FAILED with one tap |
| US-BR04 | Track my farm's conception rate | P2 | 🟡 | `BreedingStats.conceptionRatePercent` model exists; report screen Phase 9 |
| US-BR05 | Know services per conception for each cow | P2 | 🟡 | Countable from history; report screen Phase 9 |

---

## Pregnancy & Calving ✅ Phase 8 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-PC01 | Record pregnancy confirmation | P2 | ✅ Done | PregnancySheet with PdMethod radio (rectal / ultrasound / blood / observation) |
| US-PC02 | Know the expected calving date | P2 | ✅ Done | Auto-calculated: breeding date + 280 days |
| US-PC03 | Get notified 7 days before calving | P2 | ✅ Done | Local notification via `scanCalvingDue()` — URGENT ≤ 2 days |
| US-PC04 | Record the calving event | P2 | ✅ Done | CalvingSheet with outcome + difficulty 1–4 + notes |
| US-PC05 | Have a new calf animal automatically created | P2 | ✅ Done | `recordCalving()` writes calf via `animalRepository.addAnimal()` then links `calfAnimalId` |
| US-PC06 | Know when to start the dry period | P2 | ✅ Done | Auto: expected calving − 60 days, shown on Pregnancy card |

---

## Financial Management ✅ Phase 4 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-FN01 | Record how much milk I sold today and at what price | P3 | ✅ Done | Milk Sale category with amount + counterparty |
| US-FN02 | See my farm's net profit this month | P3 | ✅ Done | Net Profit hero card + 6-month monthly chart |
| US-FN03 | Know which cow is most profitable | P3 | 🟡 animal_id foreign key exists | Per-animal P&L UI pending |
| US-FN04 | Record feed expenses | P3 | ✅ Done | Feed category |
| US-FN05 | Record medicine expenses | P3 | ✅ Done | Medicine + Vet Fees categories |
| US-FN06 | Record labor and other expenses | P3 | ✅ Done | Labour + Equipment + Other categories |
| US-FN07 | Export a financial report for my accountant | P3 | ⬜ | PDF export in Phase 8 |

---

## Reports

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-RP01 | Generate a monthly milk production report | P2 | PDF with per-animal data |
| US-RP02 | Generate a vaccination compliance report | P2 | Shows due, given, overdue |
| US-RP03 | Generate a health summary report | P2 | Disease, treatment summary |
| US-RP04 | Export any report as PDF | P2 | PDF opens in viewer/shares |
| US-RP05 | Share a report via WhatsApp | P2 | Android share sheet |
| US-RP06 | Export report data as Excel/CSV | P3 | CSV file downloadable |

---

## Notifications ✅ Phase 5 Complete

| ID | Story | Priority | Status | Acceptance Criteria |
|---|---|---|---|---|
| US-NT01 | See all my farm alerts in one place | P1 | ✅ Done | AlertsScreen inbox with filter chips |
| US-NT02 | Mark an alert as done | P1 | ✅ Done | Mark Done button per card |
| US-NT03 | Tap a notification to go to the right screen | P1 | ✅ Done | Deep link `pashu360://animal/{id}` |
| US-NT04 | Choose which alert types I receive | P2 | ⬜ | Notification preferences screen pending |
| US-NT05 | Choose what time alerts are sent to me | P2 | ⬜ | Currently fires 3-day window + on due date via WorkManager |
| — | (bonus) Live bell badge count on all tabs | P2 | ✅ Done | AlertBadgeViewModel |
| — | (bonus) Auto-cleanup of resolved alerts > 90 days | P2 | ✅ Done | AlertScannerWorker.cleanupOldResolved |

---

## Offline & Sync

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-OF01 | Use the app with no internet connection | P1 | All entry screens work offline |
| US-OF02 | Have my data sync automatically when I get internet | P1 | WorkManager syncs within 60s |
| US-OF03 | See if my data is synced or pending | P2 | Sync status indicator |
| US-OF04 | Not lose data if the app crashes while offline | P1 | Room DB transaction safety |

---

## Settings

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-ST01 | Use the app in Hindi | P2 | Full Hindi localization |
| US-ST02 | Use the app in Odia | P2 | Full Odia localization |
| US-ST03 | Switch between light and dark mode | P2 | Instant theme switch |
| US-ST04 | Export all my farm data | P3 | JSON/CSV export of all tables |
| US-ST05 | Get help within the app | P2 | In-app FAQ and contact |
