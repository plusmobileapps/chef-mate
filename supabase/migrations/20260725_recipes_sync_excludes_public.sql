-- Stop the account-sync pull from dragging in every public recipe.
--
-- Bug (#487): a brand-new account immediately shows a handful of recipes it never added. The
-- client sync does a blanket `SELECT * FROM recipes` (SupabaseRecipeRemoteDataSource
-- .fetchAccessibleRecipes) and relies on RLS to scope the rows to "owned + shared". That stopped
-- being true once public sharing landed: "Anyone can view public recipes" (20260713, widened to
-- anon in 20260715) is a permissive SELECT policy, and Postgres ORs permissive policies together,
-- so the blanket select also returns every row with is_public = true. A fresh account with no
-- recipes of its own therefore syncs down the entire public catalog.
--
-- We can't drop the public-read policy — the share-link flow (fetchPublicRecipe: select by id +
-- is_public) depends on it to let a non-owner read a shared recipe. Instead give the sync its own
-- explicit source of truth that means exactly "owned + shared", never "public".
--
-- get_accessible_recipes() is SECURITY DEFINER so it bypasses RLS on `recipes` entirely: its WHERE
-- clause is then the ONLY gate, and it re-scopes to the caller via auth.uid() + the existing
-- can_access_recipe() helper (which checks accepted book membership for auth.uid()). Public-but-
-- not-mine rows have neither owner_id = auth.uid() nor book access, so they're excluded — while a
-- recipe that is BOTH shared with me AND public is still returned (via can_access_recipe), which a
-- naive is_public = false filter would have wrongly dropped.
--
-- can_access_recipe() is itself SECURITY DEFINER (20260608 / 20260610), so nesting it here does not
-- re-enter recipes RLS and cannot reintroduce the 42P17 recursion fixed in 20260610.

CREATE OR REPLACE FUNCTION get_accessible_recipes()
RETURNS SETOF recipes
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT r.*
    FROM recipes r
    WHERE r.owner_id = auth.uid()
       OR can_access_recipe(r.id);
$$;

-- Real synced accounts are `authenticated`; anonymous share-link sessions never sync their library.
GRANT EXECUTE ON FUNCTION get_accessible_recipes() TO authenticated;
