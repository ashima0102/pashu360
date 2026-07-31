# Software Requirements Specification (SRS)
## Smart Dairy Farm Management System
**Version:** 1.0  **Date:** 2026-07-30

---

## 1. Introduction

### 1.1 Purpose
This SRS defines the software requirements for the Smart Dairy Farm Management System Android application. It serves as the authoritative reference for design, development, and testing teams.

### 1.2 Scope
The system is a native Android application backed by Supabase (PostgreSQL) that enables dairy farm owners to manage their entire herd, production, health, feeding, and financial records from a mobile device with full offline capability.

### 1.3 Definitions
| Term | Definition |
|---|---|
| Animal | Any bovine in the farm (cow, heifer, bull, calf) |
| Lactation | Active milk-producing period of a cow |
| BCS | Body Condition Score (1-5 scale) |
| AI | Artificial Insemination |
| FCM | Firebase Cloud Messaging |
| RLS | Row Level Security (Supabase/PostgreSQL) |
| Room | Android local SQLite ORM database |
| Sync Queue | Local table tracking unsynced operations |

### 1.4 References
- Android Developer Documentation
- Supabase Documentation
- Material Design 3 Guidelines
- Jetpack Compose Documentation
- Clean Architecture (Robert C. Martin)

---

## 2. System Overview

### 2.1 System Architecture
```
┌─────────────────────────────────────────────────────┐
│                  Android Application                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │Presentation │  │   Domain    │  │    Data     │  │
│  │  (Compose)  │→ │ (UseCases)  │→ │(Repository) │  │
│  └─────────────┘  └─────────────┘  └──────┬──────┘  │
│                                           │         │
│                              ┌────────────┴───────┐ │
│                              │     Room DB        │ │
│                              │  (Offline Cache)   │ │
│                              └────────────┬───────┘ │
└───────────────────────────────────────────┼─────────┘
                                            │ WorkManager Sync
                               ┌────────────▼──────────┐
                               │      Supabase          │
                               │  PostgreSQL + Auth     │
                               │  Storage + Realtime    │
                               │  Edge Functions        │
                               └───────────────────────┘
```

### 2.2 Key Interfaces
- **User Interface:** Jetpack Compose screens (Material 3)
- **Local Storage:** Room Database (SQLite)
- **Remote Storage:** Supabase PostgreSQL
- **Authentication:** Supabase Auth (Phone OTP + Email)
- **Push Notifications:** Firebase Cloud Messaging
- **File Storage:** Supabase Storage (animal photos)

---

## 3. Functional Requirements

### 3.1 Authentication System

**FR-AUTH-01:** The system shall allow users to register with a phone number and receive an OTP via SMS.  
**FR-AUTH-02:** The system shall allow users to log in with email and password.  
**FR-AUTH-03:** The system shall persist authentication sessions for 30 days without requiring re-login.  
**FR-AUTH-04:** The system shall allow users to reset their password via email.  
**FR-AUTH-05:** The system shall present a farm onboarding wizard on first login.  
**FR-AUTH-06:** The system shall allow users to update their profile photo, name, and contact details.  
**FR-AUTH-07:** The system shall securely log out and clear local session data on explicit logout.

### 3.2 Farm Management

**FR-FARM-01:** The system shall allow creation of multiple farms per user account.  
**FR-FARM-02:** The system shall store farm name, location, GPS coordinates, state, and registration number.  
**FR-FARM-03:** The system shall allow creation of named barns/sheds with capacity.  
**FR-FARM-04:** The system shall allow switching between farms via a drawer.  
**FR-FARM-05:** The system shall display active farm name in the app header.

### 3.3 Animal Management

**FR-ANIM-01:** The system shall allow adding animals with: tag ID, name, breed, DOB, gender, weight, purchase date, purchase price, source, barn assignment, and photo.  
**FR-ANIM-02:** The system shall auto-generate a unique QR code for each animal on creation.  
**FR-ANIM-03:** The system shall support RFID tag field for each animal.  
**FR-ANIM-04:** The system shall allow scanning a QR code via camera to navigate directly to the animal's profile.  
**FR-ANIM-05:** The system shall display an animal profile with tabs: Overview, Milk, Vaccination, Feeding, Health, Breeding.  
**FR-ANIM-06:** The system shall allow marking animals as Active, Dry, Pregnant, Sick, Sold, or Deceased.  
**FR-ANIM-07:** The system shall track an animal's complete lifetime history in a chronological timeline.  
**FR-ANIM-08:** The system shall allow searching animals by name and tag ID.  
**FR-ANIM-09:** The system shall allow filtering the animal list by status (Active, Pregnant, Sick, Dry, Sold).  
**FR-ANIM-10:** The system shall record transfer or sale of an animal with date, buyer details, and price.  
**FR-ANIM-11:** The system shall support animal photo upload from camera or gallery.  
**FR-ANIM-12:** The system shall allow printing/sharing the QR code for physical ear tag attachment.

### 3.4 Milk Production

**FR-MILK-01:** The system shall provide a bulk milk entry screen showing all active animals in a scrollable list.  
**FR-MILK-02:** The system shall support two daily sessions: Morning and Evening.  
**FR-MILK-03:** The system shall auto-set the date to today and allow changing it.  
**FR-MILK-04:** The system shall auto-advance cursor to the next animal field on entry confirmation.  
**FR-MILK-05:** The system shall allow skipping an animal (leave blank = not recorded).  
**FR-MILK-06:** The system shall support optional milk quality fields: fat%, SNF%, CLR, pH (hidden by default, shown on toggle).  
**FR-MILK-07:** The system shall calculate and display herd daily total on the dashboard.  
**FR-MILK-08:** The system shall display a 7-day milk bar chart per animal.  
**FR-MILK-09:** The system shall display a 30-day herd milk line chart.  
**FR-MILK-10:** The system shall track lactation history: start date, end date, total yield, peak yield.  
**FR-MILK-11:** The system shall display a 305-day lactation curve per animal.  
**FR-MILK-12:** The system shall show the top 5 producers of the current month.

### 3.5 Feeding Management

**FR-FEED-01:** The system shall maintain a farm-level catalogue of feed types.  
**FR-FEED-02:** Default feed types: Green Fodder, Dry Fodder, Concentrate, Mineral Mix, Water.  
**FR-FEED-03:** The system shall allow logging feed per animal or per herd.  
**FR-FEED-04:** The system shall support three feeding times: Morning, Afternoon, Evening.  
**FR-FEED-05:** The system shall track feed inventory (stock levels per feed type).  
**FR-FEED-06:** The system shall trigger a low-stock alert when inventory falls below a configurable threshold.  
**FR-FEED-07:** The system shall allow setting a recurring daily feed schedule per animal.  
**FR-FEED-08:** The system shall calculate daily feed cost per animal based on feed type unit cost.

### 3.6 Health Management

**FR-HLTH-01:** The system shall log health checkups with: temperature, pulse, respiration, BCS (1–5 slider).  
**FR-HLTH-02:** The system shall support multi-select symptom tags: fever, diarrhea, limping, not eating, bloat, eye discharge, skin lesion, nasal discharge.  
**FR-HLTH-03:** The system shall record disease diagnosis and treatment notes.  
**FR-HLTH-04:** The system shall log medicine given: drug name, dose, route, frequency, start date, end date, withdrawal period end date.  
**FR-HLTH-05:** The system shall track weight records over time with a trend chart.  
**FR-HLTH-06:** The system shall log veterinary visits with vet name, purpose, cost, and next visit date.  
**FR-HLTH-07:** The system shall maintain a list of currently sick animals on the dashboard.  
**FR-HLTH-08:** The system shall alert if a medicine withdrawal period has not elapsed before sale/milk use.

### 3.7 Vaccination Management

**FR-VAC-01:** The system shall maintain a farm-level vaccine catalogue.  
**FR-VAC-02:** Default vaccines: FMD, BQ, HS, Brucellosis, Theileriosis, PPR, Anthrax, Rabies.  
**FR-VAC-03:** The system shall record vaccinations: animal, vaccine, date, next due date, batch number, administered by, cost.  
**FR-VAC-04:** The system shall auto-calculate next due date from the vaccine's default interval (editable).  
**FR-VAC-05:** The system shall support batch vaccination — one entry covering multiple selected animals.  
**FR-VAC-06:** The system shall display vaccinations in a calendar view with color-coded dots.  
**FR-VAC-07:** The system shall show overdue vaccinations in red, today's in orange, upcoming in green.  
**FR-VAC-08:** The system shall send a push notification 3 days before and on the day of a vaccination due date.  
**FR-VAC-09:** The system shall generate a vaccination compliance report per animal and per herd.

### 3.8 Heat Cycle Management

**FR-HEAT-01:** The system shall record heat detection events: date, time, symptoms, detected by.  
**FR-HEAT-02:** The system shall auto-predict next heat date as last heat date + 21 days (configurable 18–24 days).  
**FR-HEAT-03:** The system shall display a heat calendar heatmap.  
**FR-HEAT-04:** The system shall send a push notification 1 day before expected heat date.  
**FR-HEAT-05:** The system shall flag repeat breeders (3+ consecutive heats without conception).

### 3.9 Breeding & Conception

**FR-BRDG-01:** The system shall record AI events: date, bull/semen name, batch, technician name, cost.  
**FR-BRDG-02:** The system shall record natural mating: date, bull tag, cost.  
**FR-BRDG-03:** The system shall track conception status: Pending, Confirmed, Failed.  
**FR-BRDG-04:** The system shall record pregnancy diagnosis: date, method (PD/ultrasound), result.  
**FR-BRDG-05:** The system shall calculate conception rate: conceptions / total services × 100.  
**FR-BRDG-06:** The system shall calculate services per conception.

### 3.10 Pregnancy & Calving

**FR-PREG-01:** The system shall track expected calving date = insemination date + 280 days (configurable).  
**FR-PREG-02:** The system shall schedule dry period start = expected calving date − 60 days.  
**FR-PREG-03:** The system shall send a push notification 7 days before expected calving date.  
**FR-PREG-04:** The system shall record calving: date, time, difficulty score (1–4), assistance type, outcome.  
**FR-PREG-05:** The system shall auto-create a calf animal record linked to dam and sire.  
**FR-PREG-06:** The system shall start a new lactation record on calving.

### 3.11 Financial Management

**FR-FIN-01:** The system shall log milk sales: date, quantity (litres), price per litre, total, buyer.  
**FR-FIN-02:** The system shall log animal sales: date, animal, buyer, sale price.  
**FR-FIN-03:** The system shall log expenses by category: feed, medicine, labour, vet, equipment, other.  
**FR-FIN-04:** The system shall calculate per-animal P&L: milk income − feed cost − medicine cost.  
**FR-FIN-05:** The system shall calculate farm-level monthly income vs expense.  
**FR-FIN-06:** The system shall display an income vs expense bar chart.

### 3.12 Reports & Export

**FR-RPT-01:** The system shall generate daily, monthly, and yearly milk production reports.  
**FR-RPT-02:** The system shall generate vaccination compliance reports.  
**FR-RPT-03:** The system shall generate health summary reports.  
**FR-RPT-04:** The system shall generate financial P&L reports.  
**FR-RPT-05:** The system shall export all reports as PDF.  
**FR-RPT-06:** The system shall export tabular reports as CSV/Excel.  
**FR-RPT-07:** The system shall allow sharing reports via Android share sheet (WhatsApp, email, etc.).

### 3.13 Notification System

**FR-NOTIF-01:** The system shall maintain a Notification Center screen with all alerts.  
**FR-NOTIF-02:** Alert types: Vaccination Due, Heat Alert, Calving Alert, Pregnancy Check, Medicine Reminder, Low Feed Stock.  
**FR-NOTIF-03:** The system shall send FCM push notifications for all time-sensitive alerts.  
**FR-NOTIF-04:** The system shall allow resolving/snoozing alerts.  
**FR-NOTIF-05:** Push notification tap shall deep-link to the relevant animal screen.  
**FR-NOTIF-06:** The user shall be able to configure which alert types they receive and at what time of day.

### 3.14 Offline Mode

**FR-OFF-01:** All data entry features shall work without internet connectivity.  
**FR-OFF-02:** Offline writes shall be queued in a local sync queue.  
**FR-OFF-03:** When connectivity is restored, the system shall automatically sync queued items to Supabase.  
**FR-OFF-04:** Conflict resolution shall use last-write-wins with server timestamp comparison.  
**FR-OFF-05:** The system shall display a sync status indicator (synced / pending / offline).

---

## 4. Non-Functional Requirements

### 4.1 Performance
- Cold app start: < 2 seconds on mid-range device
- Screen transition: < 300ms
- Bulk milk entry for 50 animals: < 5 minutes total
- Report generation (1 month): < 3 seconds
- Sync after reconnect: < 30 seconds for < 1000 pending records

### 4.2 Scalability
- Single farm: up to 1,000 animals
- Multiple farms: up to 10 per account
- Supabase backend: designed for 100,000+ concurrent users
- Database: properly indexed for fast queries at scale

### 4.3 Reliability
- Offline operation: 100% of write operations available offline
- Data integrity: ACID transactions via Room and PostgreSQL
- Sync reliability: WorkManager exponential backoff retry

### 4.4 Security
- Supabase RLS: farmers cannot access other farms' data
- Auth token: stored in EncryptedSharedPreferences
- API keys: stored in BuildConfig, not in version control
- Media: Supabase Storage with signed URLs (not public)

### 4.5 Usability
- Support Android API 26+ (Android 8.0)
- Minimum supported RAM: 2 GB
- Works on screen sizes 5"–7" (phones and small tablets)
- WCAG AA color contrast ratios for accessibility

### 4.6 Localization
- English (default)
- Hindi (hi-IN)
- Odia (or-IN)
- Currency: Indian Rupee (₹) default
- Date format: DD/MM/YYYY
- Weight: kg

---

## 5. System Interfaces

### 5.1 External Interfaces
| Interface | Protocol | Purpose |
|---|---|---|
| Supabase REST API | HTTPS/REST | CRUD operations |
| Supabase Realtime | WebSocket | Live data updates |
| Firebase FCM | HTTPS | Push notifications |
| Supabase Storage | HTTPS | Photo upload/download |
| ML Kit Barcode | On-device | QR code scanning |

### 5.2 Hardware Interfaces
| Hardware | Requirement |
|---|---|
| Camera | Required for QR scanning and photo capture |
| Internet | Required for initial setup and sync (not for daily use) |
| Storage | Minimum 200MB free for local database and photos |
| GPS | Optional — for farm location during setup |

---

## 6. Data Requirements

### 6.1 Data Retention
- Active farm data: indefinitely
- Deleted animal records: soft-deleted, retained 90 days
- Sync queue items: cleared after successful sync
- Notification history: retained 90 days

### 6.2 Data Backup
- Automatic cloud sync via Supabase
- User can trigger manual backup from Settings
- Export full data as JSON or CSV from Settings

### 6.3 Data Privacy
- Farm data is private to the farm owner
- No data sharing with third parties without consent
- GDPR-compliant data deletion on account closure
