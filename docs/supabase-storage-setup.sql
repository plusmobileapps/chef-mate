-- Supabase Storage setup for recipe photo uploads.
-- Paste this into the Supabase SQL editor for each environment (dev, prod, ...).
-- Notes:
--   * storage.objects already has RLS enabled by default.
--   * `create policy` has no `if not exists`; drop a policy first if you need to re-run.
--   * The client uploads under "<userId>/<uuid>.<ext>" (see SupabaseRecipePhotoStorage.kt).

-- 1. Create the bucket (public, 5 MB cap, image MIME types only).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'recipe-photos',
  'recipe-photos',
  true,
  5242880, -- 5 MB
  array['image/jpeg', 'image/png', 'image/webp', 'image/heic']
)
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

-- 2. Public read so Coil can load images via bucket.publicUrl(path).
create policy "recipe_photos_public_read"
on storage.objects for select
to public
using (bucket_id = 'recipe-photos');

-- 3. Authenticated users can only write inside their own "<userId>/" folder.
create policy "recipe_photos_owner_insert"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = auth.uid()::text
);

-- 3a. Signed-out users can write into the shared "anonymous/" folder.
-- Note: this lets anyone with the anon key upload up to 5 MB at a time.
-- The bucket's file_size_limit + allowed_mime_types are the only abuse
-- guardrails; tighten or remove this policy if quota abuse becomes an issue.
create policy "recipe_photos_anon_insert"
on storage.objects for insert
to anon
with check (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = 'anonymous'
);

create policy "recipe_photos_owner_update"
on storage.objects for update
to authenticated
using (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = auth.uid()::text
);

create policy "recipe_photos_owner_delete"
on storage.objects for delete
to authenticated
using (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = auth.uid()::text
);
