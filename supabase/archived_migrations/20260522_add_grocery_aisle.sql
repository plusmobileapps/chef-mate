-- Adds the `aisle` column to `grocery_items` so the user's manual aisle
-- override (a GroceryCategory enum name, e.g. 'DAIRY') round-trips through
-- Supabase. NULL means "no manual override — fall back to the client-side
-- IngredientParser categorisation".

ALTER TABLE grocery_items ADD COLUMN IF NOT EXISTS aisle TEXT;
