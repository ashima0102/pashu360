# Product Requirements Document (PRD)
## Smart Dairy Farm Management System
**Version:** 1.0  **Date:** 2026-07-30  **Status:** Approved

---

## 1. Executive Summary

The Smart Dairy Farm Management System is a production-grade Android application designed to digitize and streamline dairy farm operations in India. It enables farm owners to manage their entire herd digitally — tracking animal records, daily milk production, vaccinations, feeding schedules, health events, breeding cycles, and financials — from a single mobile app that works offline in low-connectivity rural environments.

The product competes with global solutions like Herdwatch, DairyComp 305, and Uniform Agri, but is purpose-built for the Indian dairy market with local language support, local pricing models (₹/litre), and an offline-first architecture suited to rural connectivity conditions.

---

## 2. Problem Statement

Indian dairy farmers manage herds of 5–200+ animals with:
- Paper registers that are lost, damaged, or illegible
- No reminders for vaccinations → animal deaths from preventable diseases
- No milk production tracking → no insight into declining producers
- No financial records → no understanding of per-animal profitability
- No breeding/pregnancy tracking → missed heat cycles, poor conception rates
- No health history → vets repeat the same diagnostics every visit

**Result:** Significant financial losses, poor herd productivity, and reactive (not preventive) farm management.

---

## 3. Product Vision

**"Every dairy farmer, regardless of herd size, should have access to enterprise-grade farm management tools on the phone in their pocket."**

---

## 4. Target Users

### Primary: Farm Owner (MVP)
- Manages 5–200 animals on 1–3 farms
- Uses Android smartphone
- May have limited English literacy → Hindi/Odia support required
- Daily tasks: milk logging (2x/day), vaccination checks, feeding oversight
- Pain points: forgotten vaccinations, declining milk yield, poor financial tracking

### Future Users (V2)
- **Farm Manager** — supervised access
- **Veterinarian** — read/write health records only
- **Dairy Cooperative** — aggregate data from multiple farms

---

## 5. Goals & Success Metrics

| Goal | Metric | Target |
|---|---|---|
| Daily Active Use | DAU / MAU ratio | > 60% |
| Milk Logging Adoption | % of animals logged daily | > 80% |
| Vaccination Compliance | % vaccinations given on time | > 90% (vs ~40% today) |
| Time to Log Milk | Seconds for 10 animals | < 90 seconds |
| User Retention | 30-day retention | > 70% |
| App Store Rating | Google Play rating | > 4.4 stars |
| Offline Reliability | % of actions successful offline | 100% |

---

## 6. Feature Scope

### V1 — MVP (Months 1–4)

#### 6.1 Authentication
- Phone OTP login via Supabase Auth
- Email + password login
- Forgot password (email reset link)
- User profile management
- Farm onboarding wizard (first login)

#### 6.2 Dashboard
- Real-time farm overview: total animals, today's milk, vaccinations due, sick animals, calving due
- Quick action buttons (Log Milk, Add Vaccine, Add Animal, Log Health)
- Today's alerts and reminders
- Recent activity feed

#### 6.3 Farm Management
- Multiple farm support
- Farm profile: name, location, GPS, registration number
- Barn/Shed management
- Farm settings

#### 6.4 Animal Management
- Add animal with full profile (tag ID, breed, DOB, gender, weight, photo)
- QR code generation per animal (printable ear tag)
- RFID tag field
- QR/barcode scanner to open animal profile
- Transfer/sell/mark deceased
- Complete animal timeline (all events in chronological order)
- Search by name, tag ID, breed
- Filter: Active | Pregnant | Sick | Dry | Sold

#### 6.5 Milk Production
- Bulk morning and evening entry (all animals in one screen)
- Optional milk quality fields: fat%, SNF%, CLR, pH
- Daily and monthly production charts (Vico)
- Lactation history per animal
- 305-day lactation curve
- Herd-level analytics

#### 6.6 Feeding Management
- Feed type catalogue (green fodder, dry fodder, concentrate, minerals, water)
- Daily feed log per animal or herd
- Feed schedule (recurring daily plan)
- Feed inventory with low-stock alerts
- Feed cost tracking

#### 6.7 Health Management
- Daily health checkup log (temperature, pulse, respiration, BCS)
- Disease records with symptoms, diagnosis, treatment
- Medicine records with withdrawal period tracking
- Veterinary visit log
- Weight monitoring with trend chart
- Sick animal list

#### 6.8 Vaccination Management
- Vaccine catalogue with configurable intervals
- Vaccination history per animal
- Auto-calculate next due date
- Calendar and list views
- Batch vaccination (multiple animals, one entry)
- Push notifications: 3 days before + day of

#### 6.9 Heat Cycle Management
- Log heat detection events
- Auto-predict next heat (21-day cycle, configurable)
- Heat calendar heatmap
- Push alerts 1 day before expected heat

#### 6.10 Notification Center
- All alerts in one inbox: vaccination, heat, pregnancy, calving, feeding, medicine
- Mark resolved, snooze
- Deep-link to relevant animal screen

#### 6.11 Settings
- Profile, farm info
- Language: English, Hindi, Odia
- Theme: Light, Dark, System
- Notification preferences
- Backup & sync status
- Help & Support

### V1 Phase 2 (Months 3–4)

#### 6.12 Breeding & Conception
- AI (Artificial Insemination) records
- Natural mating records
- Bull/Semen catalogue
- Conception status tracking
- Conception rate analytics

#### 6.13 Pregnancy & Calving
- Pregnancy confirmation
- Gestation tracking
- Dry period scheduling
- Calving records
- Newborn calf auto-registration

#### 6.14 Financial Management
- Milk sales income logging
- Animal sales income
- Feed, medicine, labour, vet, equipment expenses
- Per-animal P&L calculation
- Farm-level income vs expense

#### 6.15 Reports & Analytics
- Milk production reports (daily/monthly/yearly)
- Health and vaccination compliance reports
- Heat, breeding, conception reports
- Financial reports
- Export to PDF and CSV

### V2 — AI Smart Assistant (Month 5+)
- Disease prediction from symptoms
- Heat detection from milk drop pattern
- Milk yield prediction
- Feed recommendations
- AI chatbot (veterinary Q&A)
- Image-based disease detection
- Animal face recognition

---

## 7. Non-Goals (Out of Scope for V1)

- Web dashboard (Phase 3)
- iOS app (Phase 3)
- Dairy cooperative multi-farm aggregation (V2)
- IoT/Bluetooth milk meter integration (V2)
- Government Pashu Aadhaar API integration (V2)
- Marketplace for animal/feed buying/selling (V3)

---

## 8. Design Principles

1. **Offline First** — Every core action works without internet. Data syncs when connectivity is restored.
2. **3-Tap Rule** — Any daily recurring action (milk log, feed log, vaccination) completable in ≤ 3 taps from dashboard.
3. **Farmer-Friendly Language** — No technical jargon. Use familiar terms: "Milk Log" not "Lactation Record Entry."
4. **Large Tap Targets** — Minimum 48dp for all interactive elements. Built for calloused hands.
5. **Progressive Disclosure** — Show only essential fields by default. Optional fields (fat%, SNF%) hidden behind a toggle.
6. **Visual First** — Photos, icons, color coding (red = overdue, green = OK) over text-heavy interfaces.

---

## 9. Constraints

| Constraint | Detail |
|---|---|
| Android only | API level 26+ (Android 8.0) to cover 95%+ of Indian market |
| Offline support | Full offline operation required — rural areas have 2G/3G/no internet |
| Language | English (primary), Hindi, Odia (V1), Tamil, Marathi (V2) |
| Device | Works on low-end devices (2GB RAM, Snapdragon 450 class) |
| Storage | App size < 50MB (download on limited data plans) |
| Performance | Cold start < 2 seconds, screen transitions < 300ms |

---

## 10. Dependencies

| Dependency | Purpose | Risk |
|---|---|---|
| Supabase | Backend, auth, database, storage | Medium — vendor dependency |
| Firebase FCM | Push notifications | Low — Google infrastructure |
| CameraX + ML Kit | QR scanning | Low — Google library |
| Vico | Charts | Low — open source, maintained |
| Room | Offline database | Low — Google Jetpack |
| WorkManager | Background sync | Low — Google Jetpack |

---

## 11. Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Farmers don't adopt app | Medium | High | Onboarding wizard, tutorial, Hindi UI |
| Poor connectivity causes sync issues | High | Medium | Offline-first + WorkManager retry |
| Data loss on device reset | Low | High | Daily cloud backup + export |
| Supabase outage | Low | High | Room DB cache keeps app functional |
| QR tag not scannable (mud, damage) | Medium | Low | Manual tag ID entry fallback |
| App size too large for low-end phones | Medium | Medium | Optimize assets, ProGuard, R8 |

---

## 12. Monetization Strategy

| Tier | Price | Features |
|---|---|---|
| Free | ₹0/month | Up to 10 animals, basic milk/vaccination |
| Farmer | ₹299/month | Unlimited animals, all modules, reports |
| Pro | ₹599/month | Multi-farm, AI features (V2), priority support |
| Cooperative | Custom | Aggregate reporting, bulk farm management |

---

## 13. Timeline

| Milestone | Target Date |
|---|---|
| Documentation Complete | Week 1 |
| Auth + Shell (Phase 0) | Week 2 |
| Animal Management (Phase 1) | Week 4 |
| Milk + Vaccination (Phases 2–3) | Week 7 |
| Health + Feeding (Phase 4) | Week 9 |
| Offline Sync (Phase 5) | Week 10 |
| Finance + Reports (Phase 6) | Week 12 |
| Polish + Localization (Phase 7) | Week 14 |
| Beta Launch | Week 15 |
| Play Store Launch | Week 16 |
| V2 AI Features | Month 6 |

---

## 14. Stakeholders

| Role | Name | Responsibility |
|---|---|---|
| Product Owner | Farm Owner (user) | Requirement validation |
| Lead Developer | TBD | Android + Backend implementation |
| UI/UX Designer | TBD | Figma designs |
| QA Engineer | TBD | Testing and bug verification |
