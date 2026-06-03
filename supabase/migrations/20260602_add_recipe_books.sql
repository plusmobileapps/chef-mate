-- Adds the `recipe_books` table and links every recipe to a book.
--
-- A RecipeBook holds a collection of recipes. Every user gets a single default
-- "My Recipes" book (`is_default = true`); existing recipes are backfilled onto it.
-- Phase 1 is single-owner only — collaboration, invites, public visibility and
-- duplication land in later phases. RLS therefore mirrors the existing owner-scoped
-- model used by `recipes` and `categories`.
--
-- `client_id` is UNIQUE because the client-side upsert relies on
-- `onConflict = "client_id"` to dedup creates retried after a transient failure.
-- `recipe_book_id` on `recipes` is left NULLABLE so clients that haven't upgraded
-- yet can still insert recipes; the client backfills a missing book to the default.

CREATE TABLE IF NOT EXISTS recipe_books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    client_id TEXT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_recipe_books_owner ON recipe_books(owner_id);

-- At most one default book per owner.
CREATE UNIQUE INDEX IF NOT EXISTS idx_recipe_books_default_per_owner
    ON recipe_books(owner_id) WHERE is_default;

ALTER TABLE recipe_books ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own recipe books" ON recipe_books
    FOR SELECT USING (auth.uid() = owner_id);

CREATE POLICY "Users can insert own recipe books" ON recipe_books
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update own recipe books" ON recipe_books
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete own recipe books" ON recipe_books
    FOR DELETE USING (auth.uid() = owner_id);

-- Seed one default book per existing owner that has recipes.
INSERT INTO recipe_books (owner_id, name, is_default)
SELECT DISTINCT owner_id, 'My Recipes', true
FROM recipes
WHERE owner_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Add the nullable FK column to recipes, index it, then backfill onto each owner's default book.
ALTER TABLE recipes ADD COLUMN IF NOT EXISTS recipe_book_id UUID
    REFERENCES recipe_books(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_recipes_recipe_book ON recipes(recipe_book_id);

UPDATE recipes r
SET recipe_book_id = rb.id
FROM recipe_books rb
WHERE rb.owner_id = r.owner_id AND rb.is_default AND r.recipe_book_id IS NULL;
