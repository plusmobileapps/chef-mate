-- Under RLS, Supabase Realtime evaluates the access policy against the row's
-- columns. For UPDATE/DELETE the *old* tuple is used, and with the default
-- replica identity that tuple only carries the primary key — so the policy
-- (which checks `list_id` membership) can't be evaluated and the change event
-- is dropped. Setting REPLICA IDENTITY FULL includes every column in the old
-- tuple so UPDATE (e.g. checking off an item) and DELETE events are delivered
-- to the other subscribed devices. Idempotent.

ALTER TABLE grocery_items REPLICA IDENTITY FULL;
