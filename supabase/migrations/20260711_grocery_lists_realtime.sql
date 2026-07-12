-- Adds `grocery_lists` to the realtime publication so list-level changes
-- (rename / create / delete) propagate live across a user's devices, matching
-- the item-level realtime already enabled for `grocery_items` and
-- `grocery_list_members` in 20260425_add_collaboration.sql.
--
-- Row-level security still governs which rows each subscriber receives, so this
-- only exposes changes to lists the user can already access. Idempotent.

DO $$ BEGIN
  ALTER PUBLICATION supabase_realtime ADD TABLE grocery_lists;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
