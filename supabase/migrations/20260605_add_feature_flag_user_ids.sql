-- Adds the `user_ids` allowlist column to `feature_flags`. It holds an array of
-- authenticated Supabase user ids. Listed users get the flag's `value` regardless
-- of the rollout bucket (additive on top of `rollout_percent`), but still subject to
-- `enabled`, `platforms`, and the version bounds. NULL / empty means "no allowlist —
-- rollout_percent alone decides". Only takes effect for signed-in users, since
-- logged-out clients bucket by a device UUID that is never on the list.

ALTER TABLE public.feature_flags ADD COLUMN IF NOT EXISTS user_ids TEXT[];
