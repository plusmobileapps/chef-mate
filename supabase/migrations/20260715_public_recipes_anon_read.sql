-- Widens the public-recipe SELECT policy to the `anon` role so a logged-out web visitor can read a
-- recipe that its owner marked public.
--
-- 20260713_add_recipe_public_sharing.sql deliberately scoped "Anyone can view public recipes" to
-- `authenticated`, and said: "Widen to `anon` later if a logged-out web fallback page is built."
-- That page now exists — `web/404.html` in the chefmate-site repo, served for
-- https://chefmate.plusmobileapps.com/recipe/<uuid> when the recipient doesn't have the app. It
-- queries PostgREST with the anon key, so until this lands the page can only ever render
-- "This recipe isn't available".
--
-- Why this exposes nothing new: the apps bootstrap a Supabase session with signInAnonymously()
-- (SupabaseAuthenticationRepository.kt), so Anonymous Sign-ins are enabled on the project and the
-- anon key is public by construction — anyone can already mint an `authenticated` session and read
-- exactly these rows. Adding `anon` removes a pointless sign-in round-trip rather than widening
-- the blast radius.
--
-- Scope is otherwise unchanged: still only rows explicitly flagged is_public, so private recipes
-- stay owner/collaborator scoped. The share link remains a capability URL — an unguessable UUID.
-- Still additive to the existing owner and shared-book-member SELECT policies (Postgres ORs
-- permissive policies together), and it still references no other table, so it cannot reintroduce
-- the RLS recursion fixed in 20260610_fix_recipe_rls_recursion.sql.

DROP POLICY IF EXISTS "Anyone can view public recipes" ON recipes;
CREATE POLICY "Anyone can view public recipes" ON recipes
    FOR SELECT TO anon, authenticated USING (is_public = true);

-- RLS filters rows for a role that already holds the table privilege; it does not grant that
-- privilege. Supabase's default privileges on `public` normally cover this, but `recipes` was
-- created out-of-band (see 20260713), so don't assume — GRANT is idempotent.
GRANT SELECT ON recipes TO anon;
