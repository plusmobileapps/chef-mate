-- Adds the social layer: a public `profiles` table keyed by an immutable @handle, and a
-- `recipes.published_at` flag that lists a recipe on its owner's profile.
--
-- WHY A NEW TABLE. Until now every user-facing identity field (display name, avatar) lived in
-- `auth.users.raw_user_meta_data`. That table is not readable by `anon`/`authenticated` — by
-- design — so there is nothing a stranger can read. A public profile needs a publicly-readable
-- row, so identity is mirrored into `public.profiles`. The mirror deliberately carries NO email:
-- `auth.users.email` must never reach a table with a public SELECT policy.
--
-- WHY `published_at` IS NOT `is_public`. `is_public` (20260713, widened to anon in 20260715) means
-- "anyone holding the unguessable UUID share link can read this" — a capability URL, deliberately
-- UNLISTED. If profiles listed every `is_public` row, then on the day this ships every recipe every
-- user has ever shared by link would retroactively appear on their public profile. That is a
-- privacy regression, so listing gets its own explicit opt-in column. Publishing implies
-- link-readability (enforced by CHECK below), but not the reverse.
--
-- The `recipes` table was created out-of-band (its base DDL is not in this migrations folder), so
-- the ALTERs below are defensive and must be verified against the local Supabase stack before prod.

-- ---------------------------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    -- Lowercase-only by CHECK, so the plain UNIQUE index is effectively case-insensitive without
    -- pulling in the citext extension. 3-30 chars of [a-z0-9_].
    handle TEXT NOT NULL UNIQUE CONSTRAINT profiles_handle_format
        CHECK (handle ~ '^[a-z0-9_]{3,30}$'),
    display_name TEXT NOT NULL DEFAULT '',
    bio TEXT NOT NULL DEFAULT '' CONSTRAINT profiles_bio_length CHECK (char_length(bio) <= 500),
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Readable by everyone, including logged-out visitors, because that is the entire point of a
-- public profile — and because a future web renderer for /@handle queries PostgREST with the anon
-- key (same reasoning as 20260715).
--
-- Why a blanket `USING (true)` is NOT the enumeration hazard that the `TO authenticated` policy in
-- 20260713 was: having a row here is strictly OPT-IN. A user has no profile until they deliberately
-- claim a handle, so enumerating this table can only ever reveal people who chose to be public,
-- and only the fields they chose to publish. Contrast with `recipes`, where every row exists
-- whether or not the owner opted into anything — which is why THAT table needs a capability check
-- rather than a blanket policy.
DROP POLICY IF EXISTS "Profiles are publicly readable" ON profiles;
CREATE POLICY "Profiles are publicly readable" ON profiles
    FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Users can insert own profile" ON profiles;
CREATE POLICY "Users can insert own profile" ON profiles
    FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update own profile" ON profiles;
CREATE POLICY "Users can update own profile" ON profiles
    FOR UPDATE TO authenticated USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- No DELETE policy: a profile dies with its auth user via ON DELETE CASCADE. Letting a user drop
-- the row on its own would free the handle for someone else to claim and impersonate them with.

GRANT SELECT ON profiles TO anon;
GRANT SELECT, INSERT, UPDATE ON profiles TO authenticated;

-- ---------------------------------------------------------------------------------------------
-- Reserved handles
-- ---------------------------------------------------------------------------------------------

-- Names that must not be claimable by a user: routes we may want on the web host, and names that
-- would let an account pass itself off as us.
CREATE TABLE IF NOT EXISTS reserved_handles (handle TEXT PRIMARY KEY);

ALTER TABLE reserved_handles ENABLE ROW LEVEL SECURITY;
-- Deliberately no policies: RLS with zero policies denies every role. Belt and braces with an
-- explicit REVOKE, because Supabase projects typically set default privileges that GRANT new
-- public-schema tables to anon/authenticated — so RLS would otherwise be the only thing standing
-- between a client and the reserved list. Only the SECURITY DEFINER functions below read it.
REVOKE ALL ON reserved_handles FROM anon, authenticated;

INSERT INTO reserved_handles (handle) VALUES
    ('admin'), ('administrator'), ('support'), ('help'), ('chefmate'), ('chef_mate'),
    ('plusmobileapps'), ('api'), ('www'), ('app'), ('about'), ('privacy'), ('terms'),
    ('settings'), ('recipe'), ('recipes'), ('profile'), ('notifications'), ('signin'), ('signup')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------------------------------
-- Handle integrity: immutable, and never a reserved word
-- ---------------------------------------------------------------------------------------------

-- Handles are chosen ONCE. Allowing changes would break every link already shared to the old
-- handle and free that handle for someone else to claim — an impersonation vector. Enforced by
-- trigger rather than a policy because RLS can't compare OLD to NEW.
CREATE OR REPLACE FUNCTION enforce_profile_handle_immutable()
RETURNS TRIGGER LANGUAGE plpgsql SET search_path = public AS $$
BEGIN
    IF NEW.handle IS DISTINCT FROM OLD.handle THEN
        RAISE EXCEPTION 'handle is immutable'
            USING ERRCODE = 'check_violation';
    END IF;
    NEW.updated_at := now();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_profiles_handle_immutable ON profiles;
CREATE TRIGGER trg_profiles_handle_immutable
    BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION enforce_profile_handle_immutable();

-- Checked in a trigger, not in the insert path, so it holds for a direct PostgREST insert too.
CREATE OR REPLACE FUNCTION enforce_profile_handle_allowed()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM reserved_handles WHERE handle = NEW.handle) THEN
        RAISE EXCEPTION 'handle is reserved'
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_profiles_handle_allowed ON profiles;
CREATE TRIGGER trg_profiles_handle_allowed
    BEFORE INSERT ON profiles
    FOR EACH ROW EXECUTE FUNCTION enforce_profile_handle_allowed();

-- Deleting an account frees its handle, which would let a stranger claim @alice and inherit every
-- link, screenshot and mention that ever pointed at the real Alice — the same impersonation vector
-- the immutability rule above exists to close, just reached through account deletion instead of a
-- rename. So a handle is retired rather than recycled: it moves to the reserved list on delete.
--
-- The cost is that a user who deletes their account can never reclaim their own handle. That is the
-- deliberate trade — handles are cheap, and a hijacked identity is not. Drop this trigger if you'd
-- rather let handles be recycled.
CREATE OR REPLACE FUNCTION retire_profile_handle()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    INSERT INTO reserved_handles (handle) VALUES (OLD.handle) ON CONFLICT DO NOTHING;
    RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS trg_profiles_retire_handle ON profiles;
CREATE TRIGGER trg_profiles_retire_handle
    AFTER DELETE ON profiles
    FOR EACH ROW EXECUTE FUNCTION retire_profile_handle();

-- UX pre-check only, so the claim form can say "taken" before the user hits save. The UNIQUE
-- constraint remains the real source of truth — claiming is inherently racy, so the client must
-- still handle a 23505 unique violation on insert.
CREATE OR REPLACE FUNCTION is_handle_available(p_handle TEXT)
RETURNS BOOLEAN
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT p_handle ~ '^[a-z0-9_]{3,30}$'
       AND NOT EXISTS (SELECT 1 FROM reserved_handles WHERE handle = p_handle)
       AND NOT EXISTS (SELECT 1 FROM profiles WHERE handle = p_handle);
$$;

GRANT EXECUTE ON FUNCTION is_handle_available(TEXT) TO authenticated;

-- ---------------------------------------------------------------------------------------------
-- Publishing a recipe to a profile
-- ---------------------------------------------------------------------------------------------

ALTER TABLE recipes ADD COLUMN IF NOT EXISTS published_at TIMESTAMPTZ;

-- A published recipe must also be link-readable, otherwise it would be listed on a profile that
-- no one can open. This is the one direction the two flags are coupled in. `ADD CONSTRAINT` has no
-- IF NOT EXISTS form, hence the guard.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'recipes_published_implies_public'
    ) THEN
        ALTER TABLE recipes ADD CONSTRAINT recipes_published_implies_public
            CHECK (published_at IS NULL OR is_public);
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_recipes_published
    ON recipes(owner_id, published_at DESC) WHERE published_at IS NOT NULL;

-- The profile listing gets its own explicit source of truth rather than a filtered SELECT that
-- leans on the permissive "Anyone can view public recipes" policy to do the scoping. Depending on
-- policy composition for correctness is exactly what caused #487 (a blanket select silently
-- picking up every public row). SECURITY DEFINER bypasses RLS on `recipes` so this WHERE clause is
-- the ONLY gate: owner + published + public, newest first.
CREATE OR REPLACE FUNCTION get_published_recipes(
    p_profile_id UUID,
    p_limit INT DEFAULT 50,
    p_offset INT DEFAULT 0
)
RETURNS SETOF recipes
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT r.*
    FROM recipes r
    WHERE r.owner_id = p_profile_id
      AND r.published_at IS NOT NULL
      AND r.is_public
    ORDER BY r.published_at DESC
    LIMIT LEAST(GREATEST(p_limit, 0), 100)
    OFFSET GREATEST(p_offset, 0);
$$;

-- anon as well as authenticated: the app can be in an anonymous session when a /@handle link is
-- opened, and a logged-out web renderer would use the anon key (see 20260715).
GRANT EXECUTE ON FUNCTION get_published_recipes(UUID, INT, INT) TO anon, authenticated;

-- How many recipes a profile has published. Separate from get_published_recipes because that
-- result is paginated, so the header count can't be derived from it.
CREATE OR REPLACE FUNCTION profile_published_count(p_profile_id UUID)
RETURNS BIGINT
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT count(*)
    FROM recipes r
    WHERE r.owner_id = p_profile_id
      AND r.published_at IS NOT NULL
      AND r.is_public;
$$;

-- One round-trip for the profile header. Two entry points over the same shape: by handle for
-- someone else's profile (opened from a /@handle link), by id for your own (where the client knows
-- its user id but not necessarily its handle — a user who hasn't claimed one has no profile yet).
CREATE OR REPLACE FUNCTION get_profile_by_handle(p_handle TEXT)
RETURNS TABLE (
    id UUID,
    handle TEXT,
    display_name TEXT,
    bio TEXT,
    avatar_url TEXT,
    published_recipe_count BIGINT
)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT p.id, p.handle, p.display_name, p.bio, p.avatar_url,
           profile_published_count(p.id)
    FROM profiles p
    WHERE p.handle = lower(p_handle);
$$;

CREATE OR REPLACE FUNCTION get_profile_by_id(p_profile_id UUID)
RETURNS TABLE (
    id UUID,
    handle TEXT,
    display_name TEXT,
    bio TEXT,
    avatar_url TEXT,
    published_recipe_count BIGINT
)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT p.id, p.handle, p.display_name, p.bio, p.avatar_url,
           profile_published_count(p.id)
    FROM profiles p
    WHERE p.id = p_profile_id;
$$;

GRANT EXECUTE ON FUNCTION profile_published_count(UUID) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION get_profile_by_handle(TEXT) TO anon, authenticated;
GRANT EXECUTE ON FUNCTION get_profile_by_id(UUID) TO anon, authenticated;

-- get_accessible_recipes() (20260725) needs NO change: it scopes to owned + shared-book rows, so a
-- stranger's published recipe still never syncs into anyone else's library.
