-- Brings the `feature_flags` table under source control and adds the columns +
-- access policies the admin dashboard needs.
--
-- The table was originally created by hand in the Supabase console (the client
-- reads it via SupabaseFeatureFlagRemoteDataSource). `CREATE TABLE IF NOT EXISTS`
-- captures that existing shape so the schema is finally versioned; on the live
-- project it is a no-op, and the `ALTER ... ADD COLUMN IF NOT EXISTS` statements
-- below add only what's new.
--
-- New columns:
--   archived     — soft-retire a flag. Clients never see archived rows (see the
--                  SELECT policy + the client-side `eq("archived", false)` filter);
--                  the admin tool keeps the row so history isn't lost.
--   description  — optional admin-authored note about what the flag does. The
--                  shipping client doesn't read it (descriptions live in the
--                  FeatureFlagRegistry in code); it's purely documentation here.
--   created_at / updated_at — audit timestamps; `updated_at` is bumped by trigger.
--
-- Writes are restricted to admins: membership in the `admins` table gates every
-- INSERT/UPDATE/DELETE, and admins additionally get to SELECT archived rows.

CREATE TABLE IF NOT EXISTS feature_flags (
    key TEXT PRIMARY KEY,
    value_type TEXT NOT NULL,
    value TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT false,
    rollout_percent INT NOT NULL DEFAULT 100,
    platforms TEXT[],
    min_version TEXT,
    max_version TEXT
);

ALTER TABLE feature_flags ADD COLUMN IF NOT EXISTS archived BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE feature_flags ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE feature_flags ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE feature_flags ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Keep `updated_at` current on every write.
CREATE OR REPLACE FUNCTION set_feature_flags_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS feature_flags_set_updated_at ON feature_flags;
CREATE TRIGGER feature_flags_set_updated_at
    BEFORE UPDATE ON feature_flags
    FOR EACH ROW
    EXECUTE FUNCTION set_feature_flags_updated_at();

-- Admin allow-list. A user is an admin iff their auth uid has a row here.
CREATE TABLE IF NOT EXISTS admins (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- TODO: seed yourself as an admin. Find your uid in the Supabase console under
-- Authentication → Users, then run (or uncomment with your uid):
-- INSERT INTO admins (user_id) VALUES ('00000000-0000-0000-0000-000000000000')
--     ON CONFLICT DO NOTHING;

-- Only admins may read the admin allow-list (prevents enumerating who's an admin).
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Admins can view admins" ON admins;
CREATE POLICY "Admins can view admins" ON admins
    FOR SELECT USING (auth.uid() IN (SELECT user_id FROM admins));

ALTER TABLE feature_flags ENABLE ROW LEVEL SECURITY;

-- Everyone (incl. the anon client) can read active flags; admins also see archived.
DROP POLICY IF EXISTS "Anyone can read active flags" ON feature_flags;
CREATE POLICY "Anyone can read active flags" ON feature_flags
    FOR SELECT USING (
        archived = false OR auth.uid() IN (SELECT user_id FROM admins)
    );

-- Writes are admin-only.
DROP POLICY IF EXISTS "Admins can insert flags" ON feature_flags;
CREATE POLICY "Admins can insert flags" ON feature_flags
    FOR INSERT WITH CHECK (auth.uid() IN (SELECT user_id FROM admins));

DROP POLICY IF EXISTS "Admins can update flags" ON feature_flags;
CREATE POLICY "Admins can update flags" ON feature_flags
    FOR UPDATE USING (auth.uid() IN (SELECT user_id FROM admins));

DROP POLICY IF EXISTS "Admins can delete flags" ON feature_flags;
CREATE POLICY "Admins can delete flags" ON feature_flags
    FOR DELETE USING (auth.uid() IN (SELECT user_id FROM admins));
