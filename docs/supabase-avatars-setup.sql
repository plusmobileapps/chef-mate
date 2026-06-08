-- Supabase Storage setup for profile photo (avatar) uploads.
-- Paste this into the Supabase SQL editor for each environment (dev, prod, ...).
-- Idempotent: every `create policy` is preceded by `drop policy if exists`, so this
-- file is safe to re-run when the setup changes.
-- Notes:
--   * storage.objects already has RLS enabled by default.
--   * Avatars are written by SupabaseProfilePhotoStorage.kt under "<userId>/<uuid>.<ext>",
--     mirroring the recipe-photos bucket. Only real (non-anonymous) signed-in users reach
--     the Manage Profile screen, so every upload has a real auth.uid().

-- 1. Create the bucket (public, 5 MB cap, image MIME types only).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'avatars',
  'avatars',
  true,
  5242880, -- 5 MB
  array['image/jpeg', 'image/png', 'image/webp', 'image/heic']
)
on conflict (id) do update set
  public = excluded.public,
  file_size_limit = excluded.file_size_limit,
  allowed_mime_types = excluded.allowed_mime_types;

-- 2. Public read so Coil can load avatars via bucket.publicUrl(path).
drop policy if exists "avatars_public_read" on storage.objects;
create policy "avatars_public_read"
on storage.objects for select
to public
using (bucket_id = 'avatars');

-- 3. Authenticated users can only write inside their own folder.
drop policy if exists "avatars_owner_insert" on storage.objects;
create policy "avatars_owner_insert"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'avatars'
  and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "avatars_owner_update" on storage.objects;
create policy "avatars_owner_update"
on storage.objects for update
to authenticated
using (
  bucket_id = 'avatars'
  and (storage.foldername(name))[1] = auth.uid()::text
)
with check (
  bucket_id = 'avatars'
  and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "avatars_owner_delete" on storage.objects;
create policy "avatars_owner_delete"
on storage.objects for delete
to authenticated
using (
  bucket_id = 'avatars'
  and (storage.foldername(name))[1] = auth.uid()::text
);
