-- Fix infinite RLS recursion on `recipes` (Postgres error 42P17) introduced by the Phase 2a
-- collaboration policies.
--
-- The cycle:
--   • recipes."Members can view recipes in shared books"  → SELECT FROM recipe_book_recipes
--   • recipe_book_recipes."Users can view own recipe book links" (20260602) → SELECT FROM recipes
-- Each table's RLS re-enters the other's, so every read/write on `recipes` aborts with
-- "infinite recursion detected in policy for relation recipes".
--
-- Fix: evaluate the cross-table membership check inside SECURITY DEFINER helper functions, which
-- bypass RLS. Evaluating `recipes` RLS then no longer re-enters `recipe_book_recipes` RLS, so the
-- loop is broken. The existing recipe_book_recipes → recipes policy terminates because the recipes
-- side now resolves through the helper instead of re-querying recipe_book_recipes under RLS.

CREATE OR REPLACE FUNCTION can_access_recipe(p_recipe_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_book_recipes rbr
        WHERE rbr.recipe_id = p_recipe_id
          AND can_access_recipe_book(rbr.recipe_book_id)
    );
$$;

CREATE OR REPLACE FUNCTION can_edit_recipe(p_recipe_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_book_recipes rbr
        WHERE rbr.recipe_id = p_recipe_id
          AND can_edit_recipe_book(rbr.recipe_book_id)
    );
$$;

-- Recreate the shared-recipe policies to go through the helpers (no direct recipe_book_recipes ref).
DROP POLICY IF EXISTS "Members can view recipes in shared books" ON recipes;
CREATE POLICY "Members can view recipes in shared books" ON recipes
    FOR SELECT USING (can_access_recipe(id));

DROP POLICY IF EXISTS "Editors can update recipes in shared books" ON recipes;
CREATE POLICY "Editors can update recipes in shared books" ON recipes
    FOR UPDATE USING (can_edit_recipe(id));

-- The shared recipe-category view policy referenced recipe_book_recipes directly too; route it
-- through the same helper so it can't re-enter recipe RLS.
DROP POLICY IF EXISTS "Members can view shared recipe categories" ON recipe_categories;
CREATE POLICY "Members can view shared recipe categories" ON recipe_categories
    FOR SELECT USING (can_access_recipe(recipe_id));
