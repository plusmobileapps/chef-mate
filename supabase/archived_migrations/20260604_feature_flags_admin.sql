-- Brings the `feature_flags` table under source control and adds the columns +
-- access policies the admin dashboard needs.
--
-- The table was originally created by hand in the Supabase console (the client
-- reads it via SupabaseFeatureFlagRemoteDataSource). `CREATE TABLE IF NOT EXISTS`
-- captures that existing shape so the schema is finally versioned. On the live
-- project it is a no-op, and the `ALTER ... ADD COLUMN IF NOT EXISTS` statements
-- below add only what's new.
--
-- New columns:
--   archived     - soft-retire a flag. Clients never see archived rows (see the
--                  SELECT policy plus the client-side archived filter). The admin
--                  tool keeps the row so history isn't lost.
--   description  - optional admin-authored note about what the flag does. The
--                  shipping client doesn't read it (descriptions live in the
--                  FeatureFlagRegistry in code) - it's purely documentation here.
--   created_at / updated_at - audit timestamps. updated_at is bumped by trigger.
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

-- Seed yourself as an admin AFTER this migration runs, in a separate query:
-- find your uid in the Supabase console (Authentication, Users) and insert a row
-- into admins. See the project README / PR for the exact statement.

-- Membership check used by every policy below. SECURITY DEFINER makes it run as
-- the function owner (the table owner), which bypasses RLS on `admins` - without
-- this, a policy on `admins` that selects from `admins` recurses infinitely
-- (Postgres 42P17). `stable` + a pinned search_path keep it safe and cacheable.
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE
AS $$
    SELECT EXISTS (SELECT 1 FROM admins WHERE user_id = auth.uid());
$$;

-- Only admins may read the admin allow-list (prevents enumerating who's an admin).
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Admins can view admins" ON admins;
CREATE POLICY "Admins can view admins" ON admins
    FOR SELECT USING (public.is_admin());

ALTER TABLE feature_flags ENABLE ROW LEVEL SECURITY;

-- Everyone (incl. the anon client) can read active flags. Admins also see archived.
DROP POLICY IF EXISTS "Anyone can read active flags" ON feature_flags;
CREATE POLICY "Anyone can read active flags" ON feature_flags
    FOR SELECT USING (archived = false OR public.is_admin());

-- Writes are admin-only.
DROP POLICY IF EXISTS "Admins can insert flags" ON feature_flags;
CREATE POLICY "Admins can insert flags" ON feature_flags
    FOR INSERT WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Admins can update flags" ON feature_flags;
CREATE POLICY "Admins can update flags" ON feature_flags
    FOR UPDATE USING (public.is_admin());

DROP POLICY IF EXISTS "Admins can delete flags" ON feature_flags;
CREATE POLICY "Admins can delete flags" ON feature_flags
    FOR DELETE USING (public.is_admin());
