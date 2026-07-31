# Supabase Table Design
## Smart Dairy Farm Management System

---

## Project Setup

```
Supabase Project: smart-dairy-farm
Region: ap-south-1 (Mumbai) — closest to Indian users
Plan: Pro ($25/month) for production
```

---

## Row Level Security (RLS) — Master Policy

Every table has RLS enabled. The core rule: a user can only see/modify data belonging to farms they own.

```sql
-- Enable RLS on all tables
ALTER TABLE farms              ENABLE ROW LEVEL SECURITY;
ALTER TABLE barns              ENABLE ROW LEVEL SECURITY;
ALTER TABLE animals            ENABLE ROW LEVEL SECURITY;
ALTER TABLE milk_records       ENABLE ROW LEVEL SECURITY;
ALTER TABLE feed_records       ENABLE ROW LEVEL SECURITY;
ALTER TABLE feed_types         ENABLE ROW LEVEL SECURITY;
ALTER TABLE feed_inventory     ENABLE ROW LEVEL SECURITY;
ALTER TABLE health_checkups    ENABLE ROW LEVEL SECURITY;
ALTER TABLE weight_records     ENABLE ROW LEVEL SECURITY;
ALTER TABLE diseases           ENABLE ROW LEVEL SECURITY;
ALTER TABLE medicines          ENABLE ROW LEVEL SECURITY;
ALTER TABLE vet_visits         ENABLE ROW LEVEL SECURITY;
ALTER TABLE vaccine_catalogue  ENABLE ROW LEVEL SECURITY;
ALTER TABLE vaccinations       ENABLE ROW LEVEL SECURITY;
ALTER TABLE heat_records       ENABLE ROW LEVEL SECURITY;
ALTER TABLE breeding_records   ENABLE ROW LEVEL SECURITY;
ALTER TABLE pregnancy_records  ENABLE ROW LEVEL SECURITY;
ALTER TABLE lactation_records  ENABLE ROW LEVEL SECURITY;
ALTER TABLE income_records     ENABLE ROW LEVEL SECURITY;
ALTER TABLE expense_records    ENABLE ROW LEVEL SECURITY;
ALTER TABLE alerts             ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_tokens ENABLE ROW LEVEL SECURITY;
```

---

## RLS Policies

### farms table — Owner-only
```sql
-- Only the farm owner can see their farms
CREATE POLICY "farms_owner_select" ON farms
    FOR SELECT USING (owner_id = auth.uid());

CREATE POLICY "farms_owner_insert" ON farms
    FOR INSERT WITH CHECK (owner_id = auth.uid());

CREATE POLICY "farms_owner_update" ON farms
    FOR UPDATE USING (owner_id = auth.uid());

CREATE POLICY "farms_owner_delete" ON farms
    FOR DELETE USING (owner_id = auth.uid());
```

### Helper function — get user's farm IDs
```sql
CREATE OR REPLACE FUNCTION get_user_farm_ids()
RETURNS TABLE(farm_id UUID) AS $$
    SELECT id FROM farms WHERE owner_id = auth.uid();
$$ LANGUAGE sql SECURITY DEFINER;
```

### animals table — Farm-scoped
```sql
CREATE POLICY "animals_farm_select" ON animals
    FOR SELECT USING (farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid()));

CREATE POLICY "animals_farm_insert" ON animals
    FOR INSERT WITH CHECK (farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid()));

CREATE POLICY "animals_farm_update" ON animals
    FOR UPDATE USING (farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid()));

CREATE POLICY "animals_farm_delete" ON animals
    FOR DELETE USING (farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid()));
```

### All farm-scoped tables use the same pattern
```sql
-- Template for: milk_records, feed_records, health_checkups, vaccinations,
--               heat_records, breeding_records, alerts, income_records, expense_records

-- Replace 'milk_records' with each table name:
CREATE POLICY "milk_records_farm_access" ON milk_records
    FOR ALL USING (farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid()));
```

### vaccine_catalogue — System defaults + farm-specific
```sql
CREATE POLICY "vaccine_catalogue_read" ON vaccine_catalogue
    FOR SELECT USING (
        is_system_default = TRUE   -- Everyone sees system defaults
        OR farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid())  -- Own custom vaccines
    );

CREATE POLICY "vaccine_catalogue_write" ON vaccine_catalogue
    FOR INSERT WITH CHECK (
        farm_id IN (SELECT id FROM farms WHERE owner_id = auth.uid())
        AND is_system_default = FALSE
    );
```

### notification_tokens — User-scoped
```sql
CREATE POLICY "notification_tokens_user" ON notification_tokens
    FOR ALL USING (user_id = auth.uid());
```

---

## Supabase Realtime Configuration

Enable realtime on tables that need live sync:
```sql
-- Enable realtime publication
ALTER PUBLICATION supabase_realtime ADD TABLE animals;
ALTER PUBLICATION supabase_realtime ADD TABLE milk_records;
ALTER PUBLICATION supabase_realtime ADD TABLE vaccinations;
ALTER PUBLICATION supabase_realtime ADD TABLE alerts;
ALTER PUBLICATION supabase_realtime ADD TABLE feed_inventory;
```

---

## Database Triggers

```sql
-- Auto-update updated_at for animals table
CREATE TRIGGER animals_updated_at_trigger
    BEFORE UPDATE ON animals
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Auto-update for farms
CREATE TRIGGER farms_updated_at_trigger
    BEFORE UPDATE ON farms
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function definition
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Auto-generate alert when vaccination next_due_date is set
CREATE OR REPLACE FUNCTION create_vaccination_alert()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.next_due_date IS NOT NULL THEN
        INSERT INTO alerts (farm_id, animal_id, alert_type, title, message, due_date)
        SELECT 
            a.farm_id,
            NEW.animal_id,
            'vaccination_due',
            'Vaccination Due: ' || vc.name,
            'Animal ' || a.tag_id || COALESCE(' (' || a.name || ')', '') || 
            ' is due for ' || vc.name || ' on ' || NEW.next_due_date::text,
            NEW.next_due_date
        FROM animals a
        JOIN vaccine_catalogue vc ON vc.id = NEW.vaccine_id
        WHERE a.id = NEW.animal_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER vaccination_alert_trigger
    AFTER INSERT ON vaccinations
    FOR EACH ROW
    EXECUTE FUNCTION create_vaccination_alert();

-- Auto-generate alert when heat record is added (predict next heat)
CREATE OR REPLACE FUNCTION create_heat_alert()
RETURNS TRIGGER AS $$
DECLARE
    next_heat_date DATE;
    animal_rec RECORD;
BEGIN
    next_heat_date := NEW.detection_date + INTERVAL '21 days';
    SELECT * INTO animal_rec FROM animals WHERE id = NEW.animal_id;
    
    INSERT INTO alerts (farm_id, animal_id, alert_type, title, message, due_date)
    VALUES (
        animal_rec.farm_id,
        NEW.animal_id,
        'heat_expected',
        'Expected Heat: ' || COALESCE(animal_rec.name, animal_rec.tag_id),
        COALESCE(animal_rec.name, 'Animal ' || animal_rec.tag_id) || 
        ' is expected to be in heat around ' || next_heat_date::text,
        next_heat_date - INTERVAL '1 day'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER heat_alert_trigger
    AFTER INSERT ON heat_records
    FOR EACH ROW
    EXECUTE FUNCTION create_heat_alert();
```

---

## Supabase Storage Configuration

```
Buckets:
├── animal-photos/
│   ├── Access: Private (signed URLs, 1 hour expiry)
│   ├── Max file size: 5MB
│   ├── Allowed types: image/jpeg, image/png, image/webp
│   └── Path pattern: {farm_id}/{animal_id}/{filename}
│
├── health-photos/
│   ├── Access: Private
│   ├── Max file size: 5MB
│   └── Path pattern: {farm_id}/{animal_id}/health/{filename}
│
└── expense-receipts/
    ├── Access: Private
    ├── Max file size: 5MB
    └── Path pattern: {farm_id}/receipts/{filename}
```

```sql
-- Storage RLS
CREATE POLICY "animal_photos_access" ON storage.objects
    FOR ALL USING (
        bucket_id = 'animal-photos' AND
        (storage.foldername(name))[1] IN (
            SELECT id::text FROM farms WHERE owner_id = auth.uid()
        )
    );
```

---

## Supabase Edge Functions

### Function 1: `send-daily-alerts` (daily cron)

```typescript
// supabase/functions/send-daily-alerts/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const supabase = createClient(
  Deno.env.get("SUPABASE_URL")!,
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
)

serve(async (_req) => {
  const today = new Date().toISOString().split('T')[0]
  const threeDaysAhead = new Date(Date.now() + 3 * 86400000).toISOString().split('T')[0]
  const sevenDaysAhead = new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0]

  // 1. Find upcoming vaccinations (today or in next 3 days)
  const { data: vaccinations } = await supabase
    .from('vaccinations')
    .select(`
      id, next_due_date, animal_id,
      animals!inner(name, tag_id, farm_id,
        farms!inner(owner_id)),
      vaccine_catalogue!inner(name)
    `)
    .gte('next_due_date', today)
    .lte('next_due_date', threeDaysAhead)
    .eq('notification_sent', false)

  // 2. Find upcoming calving (in next 7 days)
  const { data: pregnancies } = await supabase
    .from('pregnancy_records')
    .select(`
      id, expected_calving_date, animal_id,
      animals!inner(name, tag_id, farm_id,
        farms!inner(owner_id))
    `)
    .gte('expected_calving_date', today)
    .lte('expected_calving_date', sevenDaysAhead)
    .is('actual_calving_date', null)

  // 3. Find low feed inventory
  const { data: lowStock } = await supabase
    .from('feed_inventory')
    .select(`*, feed_types!inner(name), farms!inner(owner_id)`)
    .filter('quantity_kg', 'lt', 'low_stock_threshold')

  // 4. Get FCM tokens for affected farm owners
  // 5. Send FCM notifications via HTTP v1 API
  // 6. Mark notification_sent = true

  return new Response(JSON.stringify({ success: true }), {
    headers: { "Content-Type": "application/json" }
  })
})
```

### Cron Schedule (pg_cron)
```sql
-- Run daily at 6:00 AM IST (00:30 UTC)
SELECT cron.schedule(
    'daily-farm-alerts',
    '30 0 * * *',
    $$
    SELECT net.http_post(
        url := 'https://your-project.supabase.co/functions/v1/send-daily-alerts',
        headers := '{"Authorization": "Bearer SERVICE_ROLE_KEY"}'::jsonb
    );
    $$
);
```

---

## Supabase Auth Configuration

```
Auth Providers:
├── Email/Password: ✅ Enabled
├── Phone OTP: ✅ Enabled (Twilio SMS provider)
│   ├── OTP expiry: 60 seconds
│   └── OTP length: 6 digits
├── Google OAuth: Planned (V2)
└── Apple OAuth: Not planned

Security:
├── JWT expiry: 3600 seconds (1 hour)
├── Refresh token rotation: Enabled
├── Refresh token expiry: 30 days
└── Rate limiting: 5 OTP requests/hour per phone
```

---

## Database Indexes (Complete List)

```sql
-- Performance indexes beyond FK indexes
CREATE INDEX idx_milk_farm_date     ON milk_records(farm_id, record_date DESC);
CREATE INDEX idx_vacc_next_due      ON vaccinations(next_due_date) WHERE next_due_date IS NOT NULL;
CREATE INDEX idx_preg_calving       ON pregnancy_records(expected_calving_date) WHERE actual_calving_date IS NULL;
CREATE INDEX idx_animals_status     ON animals(farm_id, status);
CREATE INDEX idx_alerts_unresolved  ON alerts(farm_id, due_date) WHERE is_resolved = FALSE;
CREATE INDEX idx_diseases_active    ON diseases(animal_id) WHERE is_active = TRUE;
CREATE INDEX idx_heat_latest        ON heat_records(animal_id, detection_date DESC);
```

---

## Environment Variables

```bash
# .env (never commit to git)
SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
FCM_SERVER_KEY=AAAA...
TWILIO_ACCOUNT_SID=ACxxx
TWILIO_AUTH_TOKEN=xxx
```
