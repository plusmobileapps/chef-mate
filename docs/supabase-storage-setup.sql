-- Supabase Storage setup for recipe photo uploads.
-- Paste this into the Supabase SQL editor for each environment (dev, prod, ...).
-- Notes:
--   * storage.objects already has RLS enabled by default.
--   * `create policy` has no `if not exists`; drop a policy first if you need to re-run.
--   * Every app session has a Supabase auth.uid() — anonymous sign-in is bootstrapped on
--     app start (SupabaseAuthenticationRepository.kt), so the client always uploads under
--     "<userId>/<uuid>.<ext>". There is no shared anonymous/ folder.
--   * IMPORTANT: enable Anonymous Sign-ins in the Supabase dashboard under
--     Authentication → Providers → Anonymous Sign-ins for the bootstrap to succeed.

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

-- 3. Authenticated users (anon-signed-in OR upgraded) can only write inside their own folder.
create policy "recipe_photos_owner_insert"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'recipe-photos'
  and (storage.foldername(name))[1] = auth.uid()::text
);

-- 3a. Previous setups installed a policy that let role 'anon' write into a shared
-- 'anonymous/' folder. We no longer use it (every session is its own anon Supabase
-- user with a real auth.uid()) — drop it so the bucket is no longer write-open.
drop policy if exists "recipe_photos_anon_insert" on storage.objects;

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

-- 4. Photo cleanup is handled client-side via the Storage REST API.
-- Earlier revisions of this file installed AFTER DELETE / AFTER UPDATE triggers on
-- public.recipes that ran `delete from storage.objects ...` inside `security definer`
-- functions. Current Supabase rejects direct deletes from storage tables with
-- "Direct deletion from storage tables is not allowed. Use the Storage API instead."
-- (PostgrestRestException code 42501), which aborts the parent transaction — so the
-- recipe row's own UPDATE/DELETE silently rolls back. The block below drops those
-- triggers and functions on existing projects.
--
-- RecipeRepositoryImpl already calls RecipePhotoStorage.deletePhoto() during
-- updateRecipe (photo swap) and deleteRecipe (recipe gone), which goes through the
-- Storage REST API, so client-driven cleanup keeps working. Direct dashboard edits
-- to image_url won't clean up the old object — accept that or re-introduce cleanup
-- via supabase_functions.http_request once the project's http extension is enabled.

drop trigger if exists recipes_update_photo on public.recipes;
drop trigger if exists recipes_delete_photo on public.recipes;
drop function if exists public.delete_replaced_recipe_photo();
drop function if exists public.delete_recipe_photo();
