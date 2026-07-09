-- Adds the `grocery_autocomplete_items` table so a user's custom grocery
-- autocomplete entries (their own staples) sync across devices. These augment
-- the built-in IngredientParser vocabulary on the grocery list.
--
-- Mirrors the `categories` sync scaffolding: `client_id` is UNIQUE so the
-- client-side upsert can rely on `onConflict = "client_id"` to dedup creates
-- that get retried after a transient network failure.
--
-- Case-insensitive name uniqueness is intentionally NOT enforced here (unlike
-- the client's local index). The remote upsert keys on `client_id`, so a
-- server-side name conflict would fail the best-effort push and strand the
-- row. Cross-device name dupes are instead collapsed locally on pull, where the
-- repository adopts a matching name onto the incoming remote row.

CREATE TABLE IF NOT EXISTS grocery_autocomplete_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    client_id TEXT UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_grocery_autocomplete_items_owner
    ON grocery_autocomplete_items(owner_id);

ALTER TABLE grocery_autocomplete_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own grocery autocomplete items" ON grocery_autocomplete_items
    FOR SELECT USING (auth.uid() = owner_id);

CREATE POLICY "Users can insert own grocery autocomplete items" ON grocery_autocomplete_items
    FOR INSERT WITH CHECK (auth.uid() = owner_id);

CREATE POLICY "Users can update own grocery autocomplete items" ON grocery_autocomplete_items
    FOR UPDATE USING (auth.uid() = owner_id);

CREATE POLICY "Users can delete own grocery autocomplete items" ON grocery_autocomplete_items
    FOR DELETE USING (auth.uid() = owner_id);
