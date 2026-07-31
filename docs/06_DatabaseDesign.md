# Database Design (PostgreSQL)
## Smart Dairy Farm Management System

---

## Schema: `public`

---

### Table: `users`
```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone           VARCHAR(15) UNIQUE,
    email           VARCHAR(255) UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    avatar_url      TEXT,
    preferred_lang  VARCHAR(5)  DEFAULT 'en',  -- 'en', 'hi', 'or'
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);
```

---

### Table: `farms`
```sql
CREATE TABLE farms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    location_address    TEXT,
    location_village    VARCHAR(100),
    location_district   VARCHAR(100),
    location_state      VARCHAR(100),
    gps_latitude        DECIMAL(10,8),
    gps_longitude       DECIMAL(11,8),
    registration_number VARCHAR(50),
    established_date    DATE,
    currency            VARCHAR(5)  DEFAULT 'INR',
    milk_price_per_litre DECIMAL(8,2),
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_farms_owner ON farms(owner_id);
```

---

### Table: `barns`
```sql
CREATE TABLE barns (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id     UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    barn_type   VARCHAR(50),  -- 'shed', 'open', 'tie_stall', 'loose_housing'
    capacity    INTEGER,
    notes       TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_barns_farm ON barns(farm_id);
```

---

### Table: `animals`
```sql
CREATE TABLE animals (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    barn_id         UUID REFERENCES barns(id),
    tag_id          VARCHAR(50) NOT NULL,
    rfid_tag        VARCHAR(100),
    qr_code_data    TEXT,                    -- JSON string for QR content
    name            VARCHAR(100),
    breed           VARCHAR(100),
    species         VARCHAR(50) DEFAULT 'cow',
    dob             DATE,
    gender          VARCHAR(10) NOT NULL,    -- 'female', 'male'
    color_marks     TEXT,
    weight_kg       DECIMAL(6,2),
    purchase_date   DATE,
    purchase_price  DECIMAL(10,2),
    source          VARCHAR(100),            -- 'born_on_farm', 'market', etc.
    sire_id         UUID REFERENCES animals(id),
    dam_id          UUID REFERENCES animals(id),
    status          VARCHAR(20) DEFAULT 'active',
    -- status options: active, dry, pregnant, sick, sold, deceased
    sold_date       DATE,
    sold_price      DECIMAL(10,2),
    sold_to         VARCHAR(200),
    deceased_date   DATE,
    deceased_reason TEXT,
    photo_url       TEXT,
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(farm_id, tag_id)
);

CREATE INDEX idx_animals_farm     ON animals(farm_id);
CREATE INDEX idx_animals_barn     ON animals(barn_id);
CREATE INDEX idx_animals_status   ON animals(farm_id, status);
CREATE INDEX idx_animals_tag      ON animals(farm_id, tag_id);
```

---

### Table: `milk_records`
```sql
CREATE TABLE milk_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id       UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    record_date     DATE NOT NULL,
    session         VARCHAR(10) NOT NULL,    -- 'morning', 'evening'
    quantity_liters DECIMAL(6,2) NOT NULL,
    fat_pct         DECIMAL(4,2),
    snf_pct         DECIMAL(4,2),
    clr             DECIMAL(5,2),
    ph              DECIMAL(4,2),
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(animal_id, record_date, session)
);

CREATE INDEX idx_milk_animal  ON milk_records(animal_id);
CREATE INDEX idx_milk_farm    ON milk_records(farm_id, record_date DESC);
CREATE INDEX idx_milk_date    ON milk_records(record_date DESC);
```

---

### Table: `lactation_records`
```sql
CREATE TABLE lactation_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id       UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    lactation_no    INTEGER NOT NULL,        -- 1st, 2nd, 3rd lactation
    start_date      DATE NOT NULL,
    end_date        DATE,
    total_yield_l   DECIMAL(8,2),
    peak_yield_l    DECIMAL(6,2),
    peak_date       DATE,
    dry_date        DATE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_lactation_animal ON lactation_records(animal_id);
```

---

### Table: `feed_types`
```sql
CREATE TABLE feed_types (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(50),   -- 'green_fodder', 'dry_fodder', 'concentrate', 'mineral', 'water'
    unit            VARCHAR(20) DEFAULT 'kg',
    cost_per_unit   DECIMAL(8,2),
    notes           TEXT,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_feedtypes_farm ON feed_types(farm_id);
```

---

### Table: `feed_records`
```sql
CREATE TABLE feed_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id       UUID REFERENCES animals(id) ON DELETE CASCADE,
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    feed_type_id    UUID NOT NULL REFERENCES feed_types(id),
    record_date     DATE NOT NULL,
    time_of_day     VARCHAR(15),             -- 'morning', 'afternoon', 'evening'
    quantity_kg     DECIMAL(7,2) NOT NULL,
    is_herd_feeding BOOLEAN DEFAULT FALSE,   -- true = applies to all animals
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_feed_animal ON feed_records(animal_id, record_date DESC);
CREATE INDEX idx_feed_farm   ON feed_records(farm_id, record_date DESC);
```

---

### Table: `feed_inventory`
```sql
CREATE TABLE feed_inventory (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id             UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    feed_type_id        UUID NOT NULL REFERENCES feed_types(id),
    quantity_kg         DECIMAL(10,2) DEFAULT 0,
    low_stock_threshold DECIMAL(10,2) DEFAULT 50,
    last_updated        TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(farm_id, feed_type_id)
);
```

---

### Table: `health_checkups`
```sql
CREATE TABLE health_checkups (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id           UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    checkup_date        DATE NOT NULL,
    temperature_c       DECIMAL(4,1),
    pulse_bpm           INTEGER,
    respiration_rpm     INTEGER,
    body_condition_score DECIMAL(3,1),      -- 1.0 to 5.0
    general_condition   VARCHAR(20),        -- 'good', 'fair', 'poor'
    notes               TEXT,
    recorded_by         UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_checkup_animal ON health_checkups(animal_id, checkup_date DESC);
```

---

### Table: `weight_records`
```sql
CREATE TABLE weight_records (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id   UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    record_date DATE NOT NULL,
    weight_kg   DECIMAL(6,2) NOT NULL,
    notes       TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_weight_animal ON weight_records(animal_id, record_date DESC);
```

---

### Table: `diseases`
```sql
CREATE TABLE diseases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id       UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    onset_date      DATE NOT NULL,
    symptoms        TEXT[],                  -- array of symptom tags
    diagnosis       TEXT,
    severity        VARCHAR(10),             -- 'mild', 'moderate', 'severe'
    treatment_notes TEXT,
    vet_id          UUID REFERENCES users(id),
    vet_name        VARCHAR(100),
    cost            DECIMAL(8,2),
    is_active       BOOLEAN DEFAULT TRUE,
    resolved_date   DATE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_disease_animal ON diseases(animal_id, onset_date DESC);
CREATE INDEX idx_disease_active ON diseases(animal_id, is_active);
```

---

### Table: `medicines`
```sql
CREATE TABLE medicines (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id           UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    disease_id          UUID REFERENCES diseases(id),
    drug_name           VARCHAR(200) NOT NULL,
    dose                VARCHAR(100),
    route               VARCHAR(50),         -- 'oral', 'injection_im', 'injection_iv', 'topical'
    frequency           VARCHAR(100),        -- 'once daily', 'twice daily', etc.
    start_date          DATE NOT NULL,
    end_date            DATE,
    withdrawal_end_date DATE,               -- milk/meat safe after this date
    cost                DECIMAL(8,2),
    notes               TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_medicine_animal ON medicines(animal_id);
```

---

### Table: `vet_visits`
```sql
CREATE TABLE vet_visits (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id       UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    visit_date      DATE NOT NULL,
    vet_name        VARCHAR(100),
    purpose         TEXT,
    findings        TEXT,
    treatment       TEXT,
    cost            DECIMAL(8,2),
    next_visit_date DATE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_vet_animal ON vet_visits(animal_id, visit_date DESC);
```

---

### Table: `vaccine_catalogue`
```sql
CREATE TABLE vaccine_catalogue (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id             UUID REFERENCES farms(id) ON DELETE CASCADE,
    -- NULL farm_id = system default vaccine
    name                VARCHAR(200) NOT NULL,
    disease_target      VARCHAR(200),
    default_interval_days INTEGER,
    dose_ml             DECIMAL(5,2),
    route               VARCHAR(50),
    manufacturer        VARCHAR(200),
    is_system_default   BOOLEAN DEFAULT FALSE,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

-- Insert default vaccines
INSERT INTO vaccine_catalogue (name, disease_target, default_interval_days, is_system_default)
VALUES
    ('FMD Vaccine',         'Foot & Mouth Disease',     180, TRUE),
    ('BQ Vaccine',          'Black Quarter',            365, TRUE),
    ('HS Vaccine',          'Haemorrhagic Septicaemia', 365, TRUE),
    ('Brucellosis Vaccine', 'Brucellosis',              NULL, TRUE),
    ('Anthrax Spore Vaccine','Anthrax',                 365, TRUE),
    ('Theileria Vaccine',   'Theileriosis',             NULL, TRUE),
    ('PPR Vaccine',         'Peste des Petits Ruminants',365, TRUE),
    ('Rabies Vaccine',      'Rabies',                   365, TRUE);
```

---

### Table: `vaccinations`
```sql
CREATE TABLE vaccinations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id           UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    vaccine_id          UUID NOT NULL REFERENCES vaccine_catalogue(id),
    farm_id             UUID NOT NULL REFERENCES farms(id),
    administered_date   DATE NOT NULL,
    next_due_date       DATE,
    batch_number        VARCHAR(100),
    administered_by     VARCHAR(100),
    cost                DECIMAL(8,2),
    notes               TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_vacc_animal   ON vaccinations(animal_id, administered_date DESC);
CREATE INDEX idx_vacc_due      ON vaccinations(next_due_date);
CREATE INDEX idx_vacc_farm     ON vaccinations(farm_id, next_due_date);
```

---

### Table: `heat_records`
```sql
CREATE TABLE heat_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id           UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    detection_date      DATE NOT NULL,
    detection_time      TIME,
    symptoms            TEXT[],              -- ['mounting', 'mucus_discharge', 'restless', etc.]
    intensity           VARCHAR(20),         -- 'weak', 'medium', 'strong'
    detected_by         VARCHAR(100),
    notes               TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_heat_animal ON heat_records(animal_id, detection_date DESC);
```

---

### Table: `breeding_records`
```sql
CREATE TABLE breeding_records (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id           UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    heat_id             UUID REFERENCES heat_records(id),
    breeding_type       VARCHAR(20) NOT NULL,    -- 'ai', 'natural'
    breeding_date       DATE NOT NULL,
    bull_name           VARCHAR(100),
    semen_batch         VARCHAR(100),
    ai_technician       VARCHAR(100),
    bull_animal_id      UUID REFERENCES animals(id),
    conception_status   VARCHAR(20) DEFAULT 'pending',
    -- 'pending', 'confirmed', 'failed'
    pd_date             DATE,                    -- pregnancy diagnosis date
    pd_method           VARCHAR(50),             -- 'rectal_palpation', 'ultrasound'
    cost                DECIMAL(8,2),
    notes               TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_breeding_animal ON breeding_records(animal_id, breeding_date DESC);
```

---

### Table: `pregnancy_records`
```sql
CREATE TABLE pregnancy_records (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id               UUID NOT NULL REFERENCES animals(id) ON DELETE CASCADE,
    breeding_id             UUID REFERENCES breeding_records(id),
    confirmation_date       DATE,
    confirmation_method     VARCHAR(50),
    expected_calving_date   DATE,
    dry_period_start        DATE,                -- expected_calving_date - 60 days
    actual_calving_date     DATE,
    calving_difficulty      INTEGER,             -- 1=normal, 2=slight, 3=needed_help, 4=vet_needed
    calving_assistance_type TEXT,
    calving_outcome         VARCHAR(50),         -- 'live_calf', 'stillbirth', 'twins', 'abortion'
    calf_id                 UUID REFERENCES animals(id),
    notes                   TEXT,
    created_at              TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_preg_animal ON pregnancy_records(animal_id);
CREATE INDEX idx_preg_calving ON pregnancy_records(expected_calving_date);
```

---

### Table: `income_records`
```sql
CREATE TABLE income_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    animal_id       UUID REFERENCES animals(id),
    record_date     DATE NOT NULL,
    category        VARCHAR(50) NOT NULL,    -- 'milk_sale', 'animal_sale', 'other'
    quantity        DECIMAL(10,2),
    unit            VARCHAR(20),
    unit_price      DECIMAL(8,2),
    total_amount    DECIMAL(10,2) NOT NULL,
    buyer_name      VARCHAR(100),
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_income_farm ON income_records(farm_id, record_date DESC);
```

---

### Table: `expense_records`
```sql
CREATE TABLE expense_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    animal_id       UUID REFERENCES animals(id),
    record_date     DATE NOT NULL,
    category        VARCHAR(50) NOT NULL,
    -- 'feed', 'medicine', 'labour', 'vet', 'equipment', 'vaccination', 'other'
    amount          DECIMAL(10,2) NOT NULL,
    description     TEXT,
    receipt_url     TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_expense_farm ON expense_records(farm_id, record_date DESC);
```

---

### Table: `alerts`
```sql
CREATE TABLE alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    animal_id       UUID REFERENCES animals(id),
    alert_type      VARCHAR(50) NOT NULL,
    -- 'vaccination_due', 'heat_expected', 'calving_due', 'pregnancy_check',
    -- 'medicine_reminder', 'low_feed_stock', 'withdrawal_period'
    title           VARCHAR(200) NOT NULL,
    message         TEXT,
    due_date        DATE,
    is_resolved     BOOLEAN DEFAULT FALSE,
    resolved_at     TIMESTAMPTZ,
    notification_sent BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_alerts_farm       ON alerts(farm_id, is_resolved, due_date);
CREATE INDEX idx_alerts_pending    ON alerts(is_resolved, due_date) WHERE NOT is_resolved;
```

---

### Table: `notification_tokens`
```sql
CREATE TABLE notification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token   TEXT NOT NULL,
    device_id   VARCHAR(200),
    platform    VARCHAR(20) DEFAULT 'android',
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, device_id)
);
```

---

### Table: `sync_log` (audit trail)
```sql
CREATE TABLE sync_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),
    table_name      VARCHAR(100),
    operation       VARCHAR(10),     -- 'insert', 'update', 'delete'
    record_id       UUID,
    synced_at       TIMESTAMPTZ DEFAULT NOW()
);
```

---

## Database Functions

```sql
-- Auto-update updated_at on any table
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to animals table
CREATE TRIGGER animals_updated_at
    BEFORE UPDATE ON animals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Apply to farms, users similarly

-- Function: calculate next heat date
CREATE OR REPLACE FUNCTION calculate_next_heat(last_heat DATE, cycle_days INTEGER DEFAULT 21)
RETURNS DATE AS $$
BEGIN
    RETURN last_heat + cycle_days;
END;
$$ LANGUAGE plpgsql;

-- Function: calculate expected calving date
CREATE OR REPLACE FUNCTION calculate_calving_date(insemination_date DATE, gestation_days INTEGER DEFAULT 280)
RETURNS DATE AS $$
BEGIN
    RETURN insemination_date + gestation_days;
END;
$$ LANGUAGE plpgsql;
```

---

## Indexes Summary

All foreign key columns are indexed. Additional indexes for:
- `milk_records(farm_id, record_date DESC)` — dashboard daily totals
- `vaccinations(next_due_date)` — alert cron queries
- `animals(farm_id, status)` — filtered list queries
- `alerts(is_resolved, due_date)` — notification center
