# Archived migrations

These are the original incremental migration files that predate adopting the Supabase CLI.

They are **not** applied by the CLI (they live outside `supabase/migrations/`, so `supabase db
reset` / `db push` ignore them). They were superseded by the squashed baseline migration
(`supabase/migrations/<timestamp>_baseline.sql`), which is a full snapshot of the prod schema and
already includes every change these files made.

Why squashed: the prod schema was built largely by hand via the dashboard, so the base tables
(`recipes`, `profiles`, grocery, `meal_plans`) never had migration files — the oldest file here
already `REFERENCES recipes(id)`. Replaying these against a fresh DB therefore fails on ordering,
and three of them share the version `20260610` (a duplicate-version collision in the history
table). The baseline sidesteps both problems.

Kept here purely for historical reference. See [docs/supabase-local-development.md](../../docs/supabase-local-development.md).
