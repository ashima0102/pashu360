# Entity Relationship Diagram
## Smart Dairy Farm Management System

---

## Core ER Diagram (ASCII)

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│    USERS    │1─────N│    FARMS     │1─────N│    BARNS    │
│─────────────│       │──────────────│       │─────────────│
│ id (PK)     │       │ id (PK)      │       │ id (PK)     │
│ phone       │       │ owner_id(FK) │       │ farm_id(FK) │
│ email       │       │ name         │       │ name        │
│ full_name   │       │ location     │       │ capacity    │
│ avatar_url  │       │ gps_lat/lng  │       │ barn_type   │
│ pref_lang   │       │ currency     │       └──────┬──────┘
└─────────────┘       └──────┬───────┘              │
                             │                      │N
                             │1                     │
                             │                ┌─────▼──────────────────────────┐
                             └───────────────N│           ANIMALS              │
                                             │────────────────────────────────│
                                             │ id (PK)                        │
                                             │ farm_id (FK) → farms           │
                                             │ barn_id (FK) → barns           │
                                             │ tag_id, rfid_tag, qr_code_data │
                                             │ name, breed, species            │
                                             │ dob, gender, color_marks       │
                                             │ weight_kg, purchase_date/price │
                                             │ sire_id (FK) → animals.id      │
                                             │ dam_id  (FK) → animals.id      │
                                             │ status, photo_url              │
                                             └──────────────┬─────────────────┘
                                                            │
                   ┌────────────────────────────────────────┼────────────────────────────────────────┐
                   │                 │                      │                │                │       │
                   │1               │1                    │1               │1              │1       │1
            ┌──────▼──────┐  ┌──────▼──────┐  ┌──────────▼────────┐ ┌────▼──────┐ ┌────▼──────┐ │
            │MILK_RECORDS │  │VACCINATIONS │  │  HEALTH_CHECKUPS  │ │  DISEASES │ │HEAT_RECORDS│ │
            │─────────────│  │─────────────│  │───────────────────│ │───────────│ │────────────│ │
            │ id (PK)     │  │ id (PK)     │  │ id (PK)           │ │ id (PK)   │ │ id (PK)    │ │
            │ animal_id   │  │ animal_id   │  │ animal_id         │ │ animal_id │ │ animal_id  │ │
            │ farm_id     │  │ vaccine_id  │  │ checkup_date      │ │ onset_date│ │ detect_date│ │
            │ record_date │  │ farm_id     │  │ temperature_c     │ │ symptoms[]│ │ symptoms[] │ │
            │ session     │  │ admin_date  │  │ pulse_bpm         │ │ diagnosis │ │ intensity  │ │
            │ qty_liters  │  │ next_due    │  │ bcs               │ │ is_active │ │ detected_by│ │
            │ fat_pct     │  │ batch_no    │  └───────────────────┘ └───────────┘ └────┬───────┘ │
            │ snf_pct     │  │ admin_by    │                                           │1        │
            └─────────────┘  └──────┬──────┘                                     ┌────▼──────────▼──┐
                                    │N                                            │BREEDING_RECORDS  │
                             ┌──────▼──────┐                                     │──────────────────│
                             │  VACCINE    │                                     │ id (PK)          │
                             │  CATALOGUE  │                                     │ animal_id        │
                             │─────────────│                                     │ heat_id          │
                             │ id (PK)     │                                     │ breeding_type    │
                             │ name        │                                     │ breeding_date    │
                             │ disease     │                                     │ bull_name        │
                             │ interval    │                                     │ semen_batch      │
                             │ dose_ml     │                                     │ conception_status│
                             └─────────────┘                                     └────────┬─────────┘
                                                                                          │1
                                                                                   ┌──────▼──────────┐
                                                                                   │PREGNANCY_RECORDS│
                                                                                   │─────────────────│
                                                                                   │ id (PK)         │
                                                                                   │ animal_id       │
                                                                                   │ breeding_id     │
                                                                                   │ expected_calving│
                                                                                   │ dry_period_start│
                                                                                   │ actual_calving  │
                                                                                   │ calf_id → animals│
                                                                                   └─────────────────┘
```

---

## Financial Module ER

```
┌──────────────┐          ┌──────────────────┐
│    FARMS     │1────────N│  INCOME_RECORDS   │
│──────────────│          │──────────────────│
│ id (PK)      │          │ id (PK)           │
│ name         │          │ farm_id (FK)      │
└──────┬───────┘          │ animal_id (FK)?   │
       │                  │ category          │
       │1                 │ quantity          │
       │                  │ unit_price        │
       │                  │ total_amount      │
       │                  └──────────────────┘
       │
       │          ┌──────────────────┐
       └─────────N│ EXPENSE_RECORDS  │
                  │──────────────────│
                  │ id (PK)          │
                  │ farm_id (FK)     │
                  │ animal_id (FK)?  │
                  │ category         │
                  │ amount           │
                  └──────────────────┘
```

---

## Feeding Module ER

```
┌──────────────┐      ┌──────────────┐      ┌────────────────┐
│    FARMS     │1────N│  FEED_TYPES  │1────N│ FEED_INVENTORY │
└──────┬───────┘      └──────┬───────┘      └────────────────┘
       │                     │
       │                     │N
       │              ┌──────▼──────┐
       │              │FEED_RECORDS │
       │1             │─────────────│
       └─────────────N│ id (PK)     │
                      │ animal_id?  │
                      │ farm_id     │
                      │ feed_type_id│
                      │ qty_kg      │
                      │ time_of_day │
                      └─────────────┘
```

---

## Alerts & Notifications ER

```
┌──────────────┐      ┌──────────────┐      ┌───────────────────┐
│    FARMS     │1────N│    ALERTS    │      │ NOTIFICATION_     │
└──────────────┘      │──────────────│      │     TOKENS        │
                      │ id (PK)      │      │───────────────────│
┌──────────────┐      │ farm_id      │      │ id (PK)           │
│   ANIMALS    │1────N│ animal_id?   │      │ user_id (FK)      │
└──────────────┘      │ alert_type   │      │ fcm_token         │
                      │ title        │      │ device_id         │
                      │ due_date     │      └───────────────────┘
                      │ is_resolved  │
                      └──────────────┘
```

---

## Relationship Summary Table

| From | To | Cardinality | Constraint |
|---|---|---|---|
| users | farms | 1 → N | ON DELETE CASCADE |
| farms | barns | 1 → N | ON DELETE CASCADE |
| farms | animals | 1 → N | ON DELETE CASCADE |
| barns | animals | 1 → N | SET NULL on delete |
| animals | animals (sire/dam) | N → 1 | self-referencing |
| animals | milk_records | 1 → N | ON DELETE CASCADE |
| animals | vaccinations | 1 → N | ON DELETE CASCADE |
| animals | health_checkups | 1 → N | ON DELETE CASCADE |
| animals | diseases | 1 → N | ON DELETE CASCADE |
| animals | medicines | 1 → N | ON DELETE CASCADE |
| animals | heat_records | 1 → N | ON DELETE CASCADE |
| animals | breeding_records | 1 → N | ON DELETE CASCADE |
| animals | pregnancy_records | 1 → N | ON DELETE CASCADE |
| animals | feed_records | 1 → N | ON DELETE CASCADE |
| animals | weight_records | 1 → N | ON DELETE CASCADE |
| vaccine_catalogue | vaccinations | 1 → N | RESTRICT |
| feed_types | feed_records | 1 → N | RESTRICT |
| farms | feed_inventory | 1 → N | ON DELETE CASCADE |
| farms | income_records | 1 → N | ON DELETE CASCADE |
| farms | expense_records | 1 → N | ON DELETE CASCADE |
| farms | alerts | 1 → N | ON DELETE CASCADE |
| breeding_records | pregnancy_records | 1 → 1 | optional |
| pregnancy_records | animals (calf) | 1 → 1 | optional |
| users | notification_tokens | 1 → N | ON DELETE CASCADE |

---

## Entity Count (MVP)

| Entity | Estimated Rows/Farm |
|---|---|
| animals | 5 – 200 |
| milk_records | 2 × animals/day × 365 = ~150,000/year |
| vaccinations | 5–10 per animal/year |
| feed_records | 2–3 per animal/day × 365 = ~100,000/year |
| health_checkups | ~50 per animal/year |
| alerts | ~20–50 per farm/month |

All tables are indexed for performant queries at 100k+ rows.
