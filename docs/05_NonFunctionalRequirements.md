# Non-Functional Requirements (NFR)
## Smart Dairy Farm Management System

---

## NFR-01 — Performance

| ID | Requirement | Target | Measurement |
|---|---|---|---|
| NFR-PERF-01 | App cold start time | < 2 seconds | Time from tap to first frame |
| NFR-PERF-02 | Screen-to-screen transition | < 300ms | Navigation transition duration |
| NFR-PERF-03 | Milk entry for 50 animals | < 5 minutes end-to-end | User task timing |
| NFR-PERF-04 | Animal list load (200 animals) | < 500ms | Room query + render |
| NFR-PERF-05 | QR code scan to profile open | < 2 seconds | Scan-to-navigate latency |
| NFR-PERF-06 | Report generation (1 month) | < 3 seconds | Query + PDF render |
| NFR-PERF-07 | Sync after reconnect (<1000 ops) | < 30 seconds | WorkManager queue drain |
| NFR-PERF-08 | Dashboard load | < 1 second | All stats visible |
| NFR-PERF-09 | Chart render (30-day) | < 500ms | Vico chart frame rate 60fps |

---

## NFR-02 — Scalability

| ID | Requirement | Target |
|---|---|---|
| NFR-SCAL-01 | Animals per farm | Up to 1,000 |
| NFR-SCAL-02 | Farms per account | Up to 10 |
| NFR-SCAL-03 | Milk records per farm per year | ~150,000 (no limit in DB) |
| NFR-SCAL-04 | Supabase concurrent users | 100,000+ (Supabase Pro scales horizontally) |
| NFR-SCAL-05 | Room DB size on device | < 500MB for 5 years of data |
| NFR-SCAL-06 | API response time at 10k concurrent | < 500ms (p95) |

---

## NFR-03 — Reliability

| ID | Requirement | Target |
|---|---|---|
| NFR-REL-01 | Offline operation coverage | 100% of write operations |
| NFR-REL-02 | Data integrity | ACID transactions (Room + PostgreSQL) |
| NFR-REL-03 | Sync reliability | WorkManager exponential backoff; max 5 retries |
| NFR-REL-04 | Supabase uptime SLA | 99.9% (Supabase Pro) |
| NFR-REL-05 | Push notification delivery | > 95% delivery (Firebase SLA) |
| NFR-REL-06 | No data loss on crash | Room transactions are atomic |
| NFR-REL-07 | App crash rate | < 0.1% sessions (Firebase Crashlytics target) |

---

## NFR-04 — Security

| ID | Requirement | Detail |
|---|---|---|
| NFR-SEC-01 | Auth token storage | EncryptedSharedPreferences (AES-256-GCM) |
| NFR-SEC-02 | Multi-tenant isolation | Supabase RLS on all tables |
| NFR-SEC-03 | Transport security | TLS 1.3 for all network calls |
| NFR-SEC-04 | API key protection | BuildConfig only; git-ignored |
| NFR-SEC-05 | Photo access | Private Storage bucket; signed URLs; 1-hour expiry |
| NFR-SEC-06 | HTTPS only | `android:usesCleartextTraffic="false"` |
| NFR-SEC-07 | Code obfuscation | R8 + ProGuard on release builds |
| NFR-SEC-08 | OTP rate limit | Max 5 OTP requests/hour per phone |

---

## NFR-05 — Usability

| ID | Requirement | Detail |
|---|---|---|
| NFR-USE-01 | Minimum tap target size | 48dp × 48dp (Material 3 standard) |
| NFR-USE-02 | Minimum body text size | 16sp |
| NFR-USE-03 | Daily milk entry speed | < 2 minutes for 10 animals |
| NFR-USE-04 | Onboarding completion rate | > 80% (users who install complete setup) |
| NFR-USE-05 | Error recovery | All error states have a retry action |
| NFR-USE-06 | Loading feedback | Skeleton or spinner within 100ms of any operation |
| NFR-USE-07 | Empty states | All empty lists show helpful prompt (not blank) |
| NFR-USE-08 | Offline indication | Sync status always visible in header |

---

## NFR-06 — Compatibility

| ID | Requirement | Detail |
|---|---|---|
| NFR-COMPAT-01 | Minimum Android version | API 26 (Android 8.0 Oreo) — covers 96%+ of Indian market |
| NFR-COMPAT-02 | Minimum RAM | 2 GB |
| NFR-COMPAT-03 | Screen sizes | 5" – 7" phones; basic tablet support |
| NFR-COMPAT-04 | Screen density | mdpi to xxxhdpi |
| NFR-COMPAT-05 | Orientation | Portrait primary; landscape for charts/reports |
| NFR-COMPAT-06 | Dark mode | Full support via Material 3 dynamic color |
| NFR-COMPAT-07 | Font scale | Tested at 200% font scale |
| NFR-COMPAT-08 | TalkBack | Accessibility labels on all interactive elements |

---

## NFR-07 — App Size & Resources

| ID | Requirement | Target |
|---|---|---|
| NFR-SIZE-01 | Play Store download size | < 20MB |
| NFR-SIZE-02 | Installed app size | < 50MB |
| NFR-SIZE-03 | Local DB growth per farm/year | < 50MB |
| NFR-SIZE-04 | RAM usage (foreground) | < 150MB |
| NFR-SIZE-05 | Battery impact | Background sync < 1% per day |

---

## NFR-08 — Localization

| ID | Requirement | Detail |
|---|---|---|
| NFR-L10N-01 | English | Default; all strings |
| NFR-L10N-02 | Hindi (hi-IN) | V1.3; all UI strings |
| NFR-L10N-03 | Odia (or-IN) | V1.3; all UI strings |
| NFR-L10N-04 | Date format | DD/MM/YYYY (Indian standard) |
| NFR-L10N-05 | Currency | INR (₹) default; configurable |
| NFR-L10N-06 | Number format | Indian numbering (1,00,000) |
| NFR-L10N-07 | Weight | kg (metric) |

---

## NFR-09 — Maintainability

| ID | Requirement | Detail |
|---|---|---|
| NFR-MAINT-01 | Architecture | Clean Architecture — domain independent of frameworks |
| NFR-MAINT-02 | Test coverage | ≥ 70% unit test coverage on UseCases and ViewModels |
| NFR-MAINT-03 | Code style | Kotlin coding conventions; ktlint enforced |
| NFR-MAINT-04 | CI/CD | All PRs must pass tests before merge |
| NFR-MAINT-05 | Database migrations | Room migrations tracked; no destructive fallback in production |
| NFR-MAINT-06 | Feature flags | New features behind flags for gradual rollout |
| NFR-MAINT-07 | Crash reporting | Firebase Crashlytics integrated from V1 |

---

## NFR-10 — Data & Privacy

| ID | Requirement | Detail |
|---|---|---|
| NFR-PRIV-01 | Data isolation | RLS ensures no cross-user data access |
| NFR-PRIV-02 | Data export | User can export all data as JSON/CSV |
| NFR-PRIV-03 | Account deletion | All data deleted from Supabase within 30 days |
| NFR-PRIV-04 | No third-party analytics | Only Firebase Crashlytics (crash data only) without consent |
| NFR-PRIV-05 | Offline data | Local DB on internal storage (Android sandbox) |
| NFR-PRIV-06 | GDPR compliance | Privacy policy, data deletion, consent management |
