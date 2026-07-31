# Screen Flow & User Journeys
## Smart Dairy Farm Management System

---

## Journey 1 — New Farmer First Use

```
Day 1 First Open
│
Install App
    └── Splash Screen (1.5s)
            └── [No session] Login Screen
                    └── Tap "Register"
                            └── Register Screen
                                (Name + Phone + Password)
                                    └── OTP Verification
                                            └── [OTP verified]
                                                    └── Farm Setup Screen
                                                        (Farm name, location, state)
                                                            └── "Start Managing Farm →"
                                                                    └── Dashboard
                                                                        (Empty state — guided)
                                                                            └── "Add your first animal"
                                                                                    └── Add Animal Screen
```

---

## Journey 2 — Daily Morning Milk Log (Most Common Daily Flow)

```
Farmer opens app at 7 AM
│
Splash (< 0.5s auto-login)
    └── Dashboard
            └── Tap [🥛 Log Milk] Quick Action
                    └── Bulk Milk Entry Screen
                        Session: MORNING  Date: Today
                        │
                        [Scroll through all cows]
                        [Enter litres for each]
                        │
                        Tap [💾 Save All Records]
                            └── Records saved to Room (instant)
                                SyncQueue updated
                                WorkManager triggered
                                    └── Dashboard
                                        "Today's Milk: 42L" updated
```

**Time for 10 cows: ~90 seconds**

---

## Journey 3 — Scan QR and View Animal Profile

```
Farmer at cow pen
│
Animals Tab
    └── Tap [📷 QR Scan Icon] (top right)
            └── QR Scanner Screen
                (CameraX full-screen)
                    └── Point at ear tag QR
                            └── [Auto-detected < 2s]
                                    └── Animal Detail Screen
                                        Profile Tab open
                                        │
                                        ├── [Log Milk] ──→ Bulk Milk Entry (pre-filled)
                                        ├── [Add Vaccine] → Add Vaccination Screen
                                        └── [Log Health] → Log Health Event Screen
```

---

## Journey 4 — Add Vaccination + Set Alert

```
Vet visit day
│
More Tab → Vaccination Schedule
    └── [+ Add Vaccination] FAB
            └── Add Vaccination Screen
                │
                Select Animal: Gouri (Tag #4)
                Vaccine: FMD (from catalogue)
                Date: Today
                Next Due: [Auto-calculated: 6 months from today]
                Given By: Dr. Sharma
                Cost: ₹150
                │
                Tap [Save Vaccination]
                    └── Vaccination saved to Room
                        Alert created in alerts table
                        SyncQueue updated
                            └── Vaccination Schedule Screen
                                (Gouri's FMD now shows in calendar)
                                    └── [3 days before next due]
                                        FCM push notification arrives:
                                        "💉 FMD Vaccine Due in 3 days — Gouri"
```

---

## Journey 5 — Vaccination Alert to Treatment

```
Push notification arrives on phone
    │
    Tap notification
        └── Deep-link → Animal Detail Screen
            Vaccination Tab
                │
                Shows: "FMD Vaccine DUE TODAY"
                        │
                        Tap [+ Add Vaccination Record]
                            └── Add Vaccination Screen
                                (Animal pre-filled: Gouri)
                                (Vaccine pre-filled: FMD)
                                │
                                Confirm date, cost, batch
                                Tap [Save]
                                    └── Vaccination recorded
                                        Next due: +6 months auto-set
                                        Alert marked resolved
                                            └── Notification Center
                                                Alert removed ✅
```

---

## Journey 6 — Sick Animal → Diagnosis → Medicine

```
Farmer notices cow limping
│
Animals Tab → Tap Lakshmi (Tag #12)
    └── Animal Profile
            └── Tap [❤️ Log Health]
                    └── Log Health Event Screen
                        │
                        Event Type: [Disease]
                        Symptoms: [Limping] [Not Eating]
                        Temperature: 40.2°C
                        BCS: 2.5
                        Diagnosis: Foot rot
                        │
                        [+ Add Medicine]
                            Drug: Oxytetracycline
                            Dose: 10mL
                            Route: Injection IM
                            Frequency: Once daily × 5 days
                            Withdrawal: 5 days from last dose
                            │
                        Tap [Save Health Record]
                            └── Health record saved
                                Animal status → Sick
                                Dashboard "Sick Animals: 1" updated
                                    │
                                    [During withdrawal period]
                                    Animal profile shows:
                                    "⚠️ Withdrawal period active
                                     Milk safe after: 04-Aug-2026"
```

---

## Journey 7 — Calving Workflow

```
7 days before expected calving
│
FCM notification: "🤱 Calving Due in 7 days — Rani"
    │
    Tap → Animal Profile → Breeding Tab
        Shows: Expected calving: 06-Aug-2026
        │
    Calving day arrives
        └── Record Calving Screen
            Animal: Rani (Tag #7)
            Date: 06-Aug-2026
            Difficulty: 1 (Normal)
            Outcome: Live Calf
            │
            Tap [Save Calving]
                └── Rani status → Active
                    New lactation record created
                    New calf animal auto-created:
                        Tag: AUTO-027
                        Dam: Rani (Tag #7)
                        Gender: [select] Female
                        DOB: Today
                    │
                    Navigate → New Calf Profile
                        [Complete calf details]
```

---

## Journey 8 — Monthly Report → WhatsApp

```
End of month
│
More Tab → Reports
    └── Reports Screen
            └── Select: "This Month" preset
                    └── Tap [Milk Production Report]
                            └── Loading... (2-3s)
                                    └── PDF generated
                                            └── Android Share Sheet opens
                                                │
                                                Select: WhatsApp
                                                    └── Send to dairy co-op contact
```

---

## Journey 9 — Feed Inventory Low Stock

```
Feed inventory running low (below threshold)
│
[6:00 AM] Edge Function runs
    └── Query: feed_inventory WHERE quantity < threshold
        └── Alert created
            FCM sent: "🌿 Low Feed Stock — Green Fodder"
                │
                Tap notification → Feed Inventory Screen
                    Shows: Green Fodder: 45kg (threshold: 100kg) 🔴
                        │
                    Tap [+ Add Stock]
                        └── Log new stock purchase
                            Quantity: 500 kg
                            Cost: ₹8,000
                                └── Inventory updated
                                    Alert resolved ✅
                                    Expense record created (₹8,000 feed)
```

---

## Screen Transition Map

```
From Any Screen → Back → Previous Screen (standard back stack)

Dashboard
├── [Log Milk] → BulkMilkEntry → [Save] → Dashboard
├── [Add Vaccine] → AddVaccination → [Save] → Dashboard
├── [Add Animal] → AddAnimal → [Save] → AnimalList
├── [Log Health] → HealthOverview → LogHealthEvent
├── Alert tap → AnimalDetail (relevant tab)
└── [View All Alerts] → NotificationCenter

AnimalList
├── Animal card tap → AnimalDetail
├── [+] FAB → AddAnimal
├── [QR Scan] → QrScanner → AnimalDetail
└── Search → filtered list

AnimalDetail (tabbed)
├── Overview → [Edit] → EditAnimal
├── Milk Tab → [Add Record] → BulkMilkEntry
├── Vaccination Tab → [Add] → AddVaccination
├── Feeding Tab → [Log Feed] → LogFeed
├── Health Tab → [Add] → LogHealthEvent
└── Breeding Tab → [AI Record] → AddBreeding

BulkMilkEntry
└── [Save] → pops back (to Dashboard or AnimalList)

Notifications
└── Any alert → deep-link to AnimalDetail or relevant module screen
```

---

## 32 Screens Summary

| # | Screen | Module | Priority |
|---|---|---|---|
| 1 | Splash | Auth | P1 |
| 2 | Login | Auth | P1 |
| 3 | OTP Verification | Auth | P1 |
| 4 | Register | Auth | P1 |
| 5 | Farm Setup | Auth | P1 |
| 6 | Dashboard | Dashboard | P1 |
| 7 | Animal List | Animals | P1 |
| 8 | QR Scanner | Animals | P1 |
| 9 | Add Animal | Animals | P1 |
| 10 | Animal Profile (tabbed) | Animals | P1 |
| 11 | Bulk Milk Entry | Milk | P1 |
| 12 | Milk History | Milk | P1 |
| 13 | Milk Analytics | Milk | P2 |
| 14 | Notification Center | Alerts | P1 |
| 15 | More / Menu | Navigation | P1 |
| 16 | Feeding Overview | Feeding | P2 |
| 17 | Log Feed | Feeding | P2 |
| 18 | Feed Schedule | Feeding | P2 |
| 19 | Feed Inventory | Feeding | P2 |
| 20 | Health Overview | Health | P1 |
| 21 | Log Health Event | Health | P1 |
| 22 | Health History | Health | P1 |
| 23 | Vaccination Schedule | Vaccination | P1 |
| 24 | Add Vaccination | Vaccination | P1 |
| 25 | Vaccine Catalogue | Vaccination | P2 |
| 26 | Finance Dashboard | Finance | P3 |
| 27 | Log Income | Finance | P3 |
| 28 | Log Expense | Finance | P3 |
| 29 | Animal P&L | Finance | P3 |
| 30 | Reports | Reports | P2 |
| 31 | Farm Detail / Edit | Farm | P2 |
| 32 | Settings | Settings | P2 |
