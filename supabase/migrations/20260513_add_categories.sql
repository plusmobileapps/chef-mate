-- Adds the `categories` table and `recipe_categories` join table to support
-- many-to-many recipe ↔ category relationships, including user-created
-- categories alongside built-in presets.
--
-- Built-in presets (BREAKFAST, LUNCH, etc.) live as constants in client code
-- and are materialized into a row here the first time a user attaches one to
-- a recipe. The `builtin_id` column carries the stable preset ID; user-created
-- categories leave it NULL. The partial unique index ensures each preset is
-- materialized at most once per owner; multiple user-created categories with
-- NULL builtin_id are allowed.
--
-- `client_id` is UNIQUE because the client-side upsert relies on
-- `onConflict = "client_id"` to dedup creates that get retried after a
-- transient network failure.

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    builtin_id TEXT,
    client_id TEXT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_categories_owner ON categories(owner_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_builtin_per_owner
    ON categories(owner_id, builtin_id) WHERE builtin_id IS NOT NULL;

ALTER TABLE categories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own categories" ON categories
    FOR SELECT USING (auth.uid() = owner_id);

CREATE POLICY "Users can insert own categories" ON categories
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update own categories" ON categories
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete own categories" ON categories
    FOR DELETE USING (auth.uid() = owner_id);

CREATE TABLE IF NOT EXISTS recipe_categories (
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (recipe_id, category_id)
);

CREATE INDEX IF NOT EXISTS idx_recipe_categories_category ON recipe_categories(category_id);

ALTER TABLE recipe_categories ENABLE ROW LEVEL SECURITY;

-- Recipe ownership gates access to the join rows. Inserting/deleting requires
-- ownership of both the recipe AND the category, which keeps users from
-- attaching another user's category (or attaching to another user's recipe).
CREATE POLICY "Users can view own recipe categories" ON recipe_categories
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_categories.recipe_id
              AND recipes.owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert own recipe categories" ON recipe_categories
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_categories.recipe_id
              AND recipes.owner_id = auth.uid()
        )
        AND EXISTS (
            SELECT 1 FROM categories
            WHERE categories.id = recipe_categories.category_id
              AND categories.owner_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete own recipe categories" ON recipe_categories
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM recipes
            WHERE recipes.id = recipe_categories.recipe_id
              AND recipes.owner_id = auth.uid()
        )
    );
