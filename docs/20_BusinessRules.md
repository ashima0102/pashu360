# Business Rules
## Smart Dairy Farm Management System

---

## BR-001 — Animal Identification

| Rule | Detail |
|---|---|
| BR-001.1 | Every animal must have a unique Tag ID within a farm |
| BR-001.2 | Tag ID can be alphanumeric (max 20 chars) |
| BR-001.3 | QR Code is auto-generated from Tag ID + Farm ID — it is unique globally |
| BR-001.4 | RFID tag number is optional but unique if provided |
| BR-001.5 | An animal's Tag ID cannot be changed once milk records exist for it |

## BR-002 — Animal Status

| Status | Rules |
|---|---|
| Active | Default status; animal is healthy and milking or being managed |
| Dry | Female; not currently milking; milk entry is disabled |
| Pregnant | Female; confirmed pregnancy; milk entry allowed unless also dry |
| Sick | Currently under treatment; appears on Sick Animals dashboard card |
| Sold | Animal no longer on farm; excluded from all active lists; records preserved |
| Deceased | Animal died; excluded from active lists; records preserved |

**BR-002.1:** Sold and Deceased animals cannot be made Active again.  
**BR-002.2:** Dry period automatically set to 60 days before expected calving. System suggests marking animal Dry.  
**BR-002.3:** When a calving record is added, animal status resets to Active, new lactation begins.

## BR-003 — Milk Production

| Rule | Detail |
|---|---|
| BR-003.1 | Only animals with status Active or Pregnant can have milk records |
| BR-003.2 | Exactly two sessions per day: Morning and Evening |
| BR-003.3 | Minimum milk quantity: 0.1 litres; Maximum: 99.9 litres per session |
| BR-003.4 | Blank entry means animal was not milked that session — NOT zero |
| BR-003.5 | A session entry can be edited up to 7 days after the entry date |
| BR-003.6 | Fat% range: 1.0–10.0; SNF% range: 6.0–15.0; CLR: 16–40 |
| BR-003.7 | Dashboard "Today's Milk" = sum of all records for today (both sessions) |
| BR-003.8 | Lactation ends when animal enters Dry status; a new one begins on calving |

## BR-004 — Vaccination

| Rule | Detail |
|---|---|
| BR-004.1 | Next due date = Administered date + vaccine's default interval (days) |
| BR-004.2 | Default interval is configurable per vaccine (override system default) |
| BR-004.3 | An overdue vaccination is one where next_due_date < today |
| BR-004.4 | Alert fires 3 days before AND on the due date |
| BR-004.5 | A batch vaccination creates one record per selected animal |
| BR-004.6 | FMD vaccine interval: 6 months (180 days) — mandatory government requirement |
| BR-004.7 | Vaccination history is never deleted; status changes are tracked |

## BR-005 — Heat & Breeding

| Rule | Detail |
|---|---|
| BR-005.1 | Heat cycle default: 21 days (configurable 18–24 days per animal) |
| BR-005.2 | Next heat prediction = last heat date + cycle days |
| BR-005.3 | If 3+ consecutive heats recorded without conception → animal flagged as Repeat Breeder |
| BR-005.4 | AI record must reference a heat record (or be entered without one) |
| BR-005.5 | Conception status options: Pending (default), Confirmed, Failed |
| BR-005.6 | Only female animals can have heat/breeding records |
| BR-005.7 | Conception rate = (confirmed conceptions / total breeding attempts) × 100 |

## BR-006 — Pregnancy & Calving

| Rule | Detail |
|---|---|
| BR-006.1 | Expected calving date = insemination date + 280 days (configurable 275–285) |
| BR-006.2 | Dry period start = expected calving date − 60 days |
| BR-006.3 | Calving alert sent 7 days before expected date |
| BR-006.4 | Calving difficulty score: 1=Normal, 2=Slight difficulty, 3=Needed assistance, 4=Vet required |
| BR-006.5 | On calving: auto-create calf animal record linked to dam (and sire if known) |
| BR-006.6 | On calving: start new lactation record for dam |
| BR-006.7 | Lactation number increments by 1 with each calving |
| BR-006.8 | If calving outcome = Abortion/Stillbirth, no calf record is created |

## BR-007 — Feeding

| Rule | Detail |
|---|---|
| BR-007.1 | Minimum 2 feed types per farm: Green Fodder and Concentrate |
| BR-007.2 | Feed quantity: minimum 0.1 kg, maximum 999.9 kg per record |
| BR-007.3 | Low-stock threshold is configurable per feed type (default: 50 kg) |
| BR-007.4 | Low-stock alert sent once per day when inventory is below threshold |
| BR-007.5 | Herd feeding record distributes equally across all active animals for reporting |

## BR-008 — Health & Medicine

| Rule | Detail |
|---|---|
| BR-008.1 | Body Condition Score: 1.0 (emaciated) to 5.0 (obese), step 0.5 |
| BR-008.2 | Normal temperature: 38.0°C – 39.5°C; alert if > 40.5°C |
| BR-008.3 | Withdrawal period: milk from treated animals should not be sold until withdrawal_end_date |
| BR-008.4 | System shows a warning banner on the animal profile during withdrawal period |
| BR-008.5 | Disease is marked Active until explicitly marked resolved |
| BR-008.6 | Multiple active diseases can exist simultaneously for one animal |
| BR-008.7 | Sick status should be set when is_active disease count ≥ 1 |

## BR-009 — Finance

| Rule | Detail |
|---|---|
| BR-009.1 | Currency default: INR (₹) — configurable per farm |
| BR-009.2 | Per-animal P&L = Milk income − Feed cost − Medicine cost − Vet cost |
| BR-009.3 | Farm P&L = All income − All expenses (all categories) |
| BR-009.4 | Income from milk sale = quantity × price_per_litre |
| BR-009.5 | Feed cost per animal = sum of feed_record.quantity × feed_type.cost_per_unit |
| BR-009.6 | Expense records linked to a specific animal appear in that animal's P&L |

## BR-010 — Alerts & Notifications

| Rule | Detail |
|---|---|
| BR-010.1 | Alerts are generated by Supabase Edge Function daily at 6:00 AM IST |
| BR-010.2 | An alert is not regenerated if one already exists for same animal + type + due_date |
| BR-010.3 | Resolved alerts are hidden from the Notification Center (kept in DB for 90 days) |
| BR-010.4 | User can configure which alert types they receive |
| BR-010.5 | User can configure the daily alert notification time |
| BR-010.6 | Maximum 1 push notification per alert per day (no spam) |

## BR-011 — Data Integrity

| Rule | Detail |
|---|---|
| BR-011.1 | An animal record cannot be hard-deleted if it has any milk, health, or vaccination records |
| BR-011.2 | Selling or marking deceased is a soft status change — all records preserved |
| BR-011.3 | Farm deletion requires explicit confirmation and deletes all associated data |
| BR-011.4 | QR codes cannot be re-used (tied to the specific animal ID permanently) |
| BR-011.5 | Dates cannot be set in the future for milk records, health checkups, or vaccinations administered |

## BR-012 — Multi-Farm

| Rule | Detail |
|---|---|
| BR-012.1 | One user account can have unlimited farms |
| BR-012.2 | Animals cannot be shared between farms (transfer creates a new record) |
| BR-012.3 | Analytics and reports are scoped per farm |
| BR-012.4 | Active farm context is stored in EncryptedSharedPreferences |
