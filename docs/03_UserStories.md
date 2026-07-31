# User Stories
## Pashu360 — Smart Dairy Farm Management

**Format:** As a [Farm Owner], I want to [action], so that [benefit].  
**Priority:** P1 = MVP Critical | P2 = MVP Important | P3 = Phase 2  
**Status:** ✅ Done · 🟡 UI built (backend pending Supabase) · ⬜ Not Started

**Last updated:** 2026-08-01

## Progress Summary

| Module | Done | Total | Progress |
|---|---|---|---|
| Authentication (UI + navigation) | 7 | 7 | 🟡 100% UI · Supabase backend pending |
| Farm Management | 1 | 5 | 🟡 20% (Farm Setup screen only) |
| Animal Management | 14 | 14 | ✅ 100% |
| Milk Production | 0 | 12 | ⬜ 0% |
| Feeding | 0 | 8 | ⬜ 0% |
| Health | 0 | 10 | ⬜ 0% |
| Vaccination | 0 | 10 | ⬜ 0% |
| Heat Cycle | 0 | 5 | ⬜ 0% |
| Breeding | 0 | 5 | ⬜ 0% |
| Pregnancy & Calving | 0 | 6 | ⬜ 0% |
| Financial | 0 | 7 | ⬜ 0% |
| Reports | 0 | 6 | ⬜ 0% |
| Notifications | 0 | 5 | ⬜ 0% |
| Offline & Sync | 0 | 4 | ⬜ 0% (Phase 5) |
| Settings | 0 | 5 | ⬜ 0% |
| **TOTAL** | **22** | **109** | **20%** |

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
| US-FM02 | Switch between my farms easily | P2 | ⬜ | Farm switcher in drawer |
| US-FM03 | Add barns/sheds to my farm | P2 | ⬜ | Animals can be assigned to barns |
| US-FM04 | Edit my farm name and location | P2 | 🟡 Farm Setup done | Changes saved to Supabase |
| US-FM05 | See which barn each animal is in | P2 | ⬜ | Barn shown on animal card |

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

## Milk Production

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-ML01 | Log morning milk for all my cows in one screen | P1 | Bulk entry, all cows visible |
| US-ML02 | Log evening milk for all my cows in one screen | P1 | Session toggle between AM/PM |
| US-ML03 | See how much milk my farm produced today | P1 | Dashboard total updated |
| US-ML04 | See milk trend for the past 7 days as a chart | P1 | Bar chart on milk history screen |
| US-ML05 | See milk trend for the past 30 days | P1 | Line chart on analytics screen |
| US-ML06 | Know which cow produced the most milk this month | P1 | Top producers section |
| US-ML07 | Log milk fat% and SNF% (optional) | P2 | Optional toggle fields |
| US-ML08 | See the lactation history of each cow | P2 | Lactation records on Milk tab |
| US-ML09 | See a 305-day lactation curve per cow | P2 | Chart on analytics |
| US-ML10 | Compare this month's production to last month | P2 | Monthly comparison chart |
| US-ML11 | Skip a cow during milk entry if she wasn't milked | P1 | Blank entry = no record |
| US-ML12 | Correct a milk entry I made with wrong quantity | P1 | Long-press or edit on history |

---

## Feeding Management

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-FD01 | Log what feed and how much each cow got today | P1 | Feed log saved per animal |
| US-FD02 | Set a daily feed schedule for each cow | P2 | Schedule visible on Feeding tab |
| US-FD03 | Know how much feed stock I have | P2 | Inventory screen |
| US-FD04 | Get alerted when feed stock is running low | P2 | Push notification + red indicator |
| US-FD05 | Add new feed stock to inventory | P2 | Add Stock button |
| US-FD06 | See feed cost per animal per month | P3 | Finance module integration |
| US-FD07 | Log water intake for animals | P2 | Water as a feed type |
| US-FD08 | Log mineral supplements | P2 | Mineral as a feed type |

---

## Health Management

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-HL01 | Record a health checkup for a cow | P1 | Temperature, pulse, BCS saved |
| US-HL02 | Log symptoms when a cow is sick | P1 | Multi-select symptom tags |
| US-HL03 | Record what medicine I gave and the dose | P1 | Medicine record with withdrawal date |
| US-HL04 | Know when a medicine's withdrawal period ends | P1 | Alert before selling milk from treated cow |
| US-HL05 | Log a vet visit with cost | P2 | Vet visit record |
| US-HL06 | Track my cow's weight over time | P2 | Weight chart on Health tab |
| US-HL07 | See which animals are currently sick | P1 | Sick animals on dashboard |
| US-HL08 | See the full health history of an animal | P1 | Timeline on Health tab |
| US-HL09 | Record body condition score of an animal | P2 | BCS slider (1–5) |
| US-HL10 | Take a photo of an injury or skin condition | P2 | Photo attachment on health event |

---

## Vaccination Management

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-VC01 | Record a vaccination I gave to a cow | P1 | Vaccination saved with next due date |
| US-VC02 | Know the next vaccination due date for each cow | P1 | Next due shown on Vaccination tab |
| US-VC03 | Get notified 3 days before a vaccination is due | P1 | FCM push notification sent |
| US-VC04 | Get notified on the day a vaccination is due | P1 | Day-of FCM notification |
| US-VC05 | See all upcoming vaccinations in a calendar | P1 | Calendar view with colored dots |
| US-VC06 | See overdue vaccinations highlighted | P1 | Red color for overdue |
| US-VC07 | Vaccinate multiple cows at once (batch) | P2 | Multi-select animals on add form |
| US-VC08 | Add a custom vaccine to my catalogue | P2 | Add to vaccine catalogue |
| US-VC09 | See vaccination history for each animal | P1 | List on Vaccination tab |
| US-VC10 | Record who administered the vaccine | P2 | Administered by field |

---

## Heat Cycle Management

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-HC01 | Record when I see a cow in heat | P2 | Heat record with date, symptoms |
| US-HC02 | Know when each cow's next heat is expected | P2 | Predicted date shown |
| US-HC03 | Get notified 1 day before expected heat | P2 | FCM push notification |
| US-HC04 | See a calendar of heat dates | P2 | Heat calendar heatmap |
| US-HC05 | Know if a cow is a repeat breeder | P2 | Flag after 3+ heats without conception |

---

## Breeding & Conception

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-BR01 | Record an AI event for a cow | P2 | AI record with technician, semen |
| US-BR02 | Record natural mating | P2 | Mating record with bull |
| US-BR03 | Know if a cow conceived | P2 | Conception status field |
| US-BR04 | Track my farm's conception rate | P2 | Analytics screen |
| US-BR05 | Know services per conception for each cow | P2 | Calculated metric |

---

## Pregnancy & Calving

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-PC01 | Record pregnancy confirmation | P2 | Pregnancy record created |
| US-PC02 | Know the expected calving date | P2 | Calculated from insemination date |
| US-PC03 | Get notified 7 days before calving | P2 | FCM push notification |
| US-PC04 | Record the calving event | P2 | Calving form with difficulty score |
| US-PC05 | Have a new calf animal automatically created | P2 | Calf linked to dam/sire |
| US-PC06 | Know when to start the dry period | P2 | Dry period date shown |

---

## Financial Management

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-FN01 | Record how much milk I sold today and at what price | P3 | Income record created |
| US-FN02 | See my farm's net profit this month | P3 | P&L dashboard |
| US-FN03 | Know which cow is most profitable | P3 | Per-animal P&L |
| US-FN04 | Record feed expenses | P3 | Expense record |
| US-FN05 | Record medicine expenses | P3 | Expense linked to health record |
| US-FN06 | Record labor and other expenses | P3 | Expense categories |
| US-FN07 | Export a financial report for my accountant | P3 | PDF export |

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

## Notifications

| ID | Story | Priority | Acceptance Criteria |
|---|---|---|---|
| US-NT01 | See all my farm alerts in one place | P1 | Notification Center screen |
| US-NT02 | Mark an alert as done | P1 | Alert removed from list |
| US-NT03 | Tap a notification to go to the right screen | P1 | Deep-link navigation |
| US-NT04 | Choose which alert types I receive | P2 | Notification settings |
| US-NT05 | Choose what time alerts are sent to me | P2 | Configurable notification time |

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
