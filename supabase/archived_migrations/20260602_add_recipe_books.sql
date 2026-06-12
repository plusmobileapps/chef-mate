-- Adds the `recipe_books` table and the `recipe_book_recipes` many-to-many join: a
-- recipe can belong to several books.
--
-- A RecipeBook holds a collection of recipes. Every user gets a single default
-- "My Recipes" book (`is_default = true`); existing recipes are linked to it via the
-- join table. Phase 1 is single-owner only — collaboration, invites, public visibility
-- and duplication land in later phases. RLS therefore mirrors the existing owner-scoped
-- model used by `recipes` and `recipe_categories`.
--
-- `client_id` is UNIQUE because the client-side upsert relies on
-- `onConflict = "client_id"` to dedup creates retried after a transient failure.

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

-- Many-to-many join between recipes and recipe books. Mirrors recipe_categories.
CREATE TABLE IF NOT EXISTS recipe_book_recipes (
    recipe_book_id UUID NOT NULL REFERENCES recipe_books(id) ON DELETE CASCADE,
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (recipe_book_id, recipe_id)
);

CREATE INDEX IF NOT EXISTS idx_recipe_book_recipes_recipe ON recipe_book_recipes(recipe_id);

ALTER TABLE recipe_book_recipes ENABLE ROW LEVEL SECURITY;

-- Recipe ownership gates the join rows: insert/delete requires owning both the recipe AND
-- the book, which keeps users from filing another user's recipe (or into another user's book).
CREATE POLICY "Users can view own recipe book links" ON recipe_book_recipes
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_book_recipes.recipe_id
              AND recipes.owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert own recipe book links" ON recipe_book_recipes
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_book_recipes.recipe_id
              AND recipes.owner_id = auth.uid()
        )
        AND EXISTS (
            SELECT 1 FROM recipe_books
            WHERE recipe_books.id = recipe_book_recipes.recipe_book_id
              AND recipe_books.owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete own recipe book links" ON recipe_book_recipes
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_book_recipes.recipe_id
              AND recipes.owner_id = auth.uid()
        )
    );

-- Link every existing recipe to its owner's default book.
INSERT INTO recipe_book_recipes (recipe_book_id, recipe_id)
SELECT rb.id, r.id
FROM recipes r
JOIN recipe_books rb ON rb.owner_id = r.owner_id AND rb.is_default
ON CONFLICT DO NOTHING;
