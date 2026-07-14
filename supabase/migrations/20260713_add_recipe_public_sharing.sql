-- Adds public recipe sharing: an `is_public` flag on `recipes` plus an additive RLS
-- SELECT policy so anyone signed in can read a recipe once its owner marks it public.
--
-- Sharing model: a recipe's global identity is its `id` (UUID). A share link embeds that
-- UUID (https://chefmate.plusmobileapps.com/recipe/<uuid>). Because UUIDs are unguessable,
-- "anyone with the link" is effectively "anyone who knows the UUID" — a capability URL. The
-- read policy is scoped to the `authenticated` role for now: the apps require sign-in and
-- there is no public web recipe renderer yet. Widen to `anon` later if a logged-out web
-- fallback page is built.
--
-- This policy is ADDITIVE to the existing owner and shared-book-member SELECT policies
-- (Postgres ORs permissive policies together), so it only ever grants extra read access to
-- rows explicitly flagged public. It references no other table, so it cannot reintroduce the
-- RLS recursion fixed in 20260610_fix_recipe_rls_recursion.sql.
--
-- The `recipes` table itself was created out-of-band (its base DDL is not in this migrations
-- folder), so this uses `ADD COLUMN IF NOT EXISTS` defensively and must be verified against the
-- local Supabase stack before being applied to prod.

ALTER TABLE recipes ADD COLUMN IF NOT EXISTS is_public BOOLEAN NOT NULL DEFAULT false;

-- Only public rows are exposed; private recipes remain owner/collaborator scoped.
DROP POLICY IF EXISTS "Anyone can view public recipes" ON recipes;
CREATE POLICY "Anyone can view public recipes" ON recipes
    FOR SELECT TO authenticated USING (is_public = true);

-- Partial index to keep the public-by-id lookup cheap.
CREATE INDEX IF NOT EXISTS idx_recipes_public ON recipes(id) WHERE is_public;
