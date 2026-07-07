-- ============================================================
-- Email notification on collaboration invite (#355)
--
-- Grocery-list and recipe-book invites are plain INSERTs into `grocery_list_members` /
-- `recipe_book_members` (status='pending'), performed directly by every client. This migration
-- notifies the invitee by email: an AFTER INSERT trigger fires `pg_net` at the `send-invite-email`
-- edge function, which sends the mail via Resend. Being a DB trigger, it's client-agnostic
-- (iOS/Android/Desktop/Web) and needs no app-side change.
--
-- Safe to re-run (idempotent). Run in the Supabase SQL editor / via `supabase db push`.
--
-- OPERATOR SETUP — before this does anything, store two Vault secrets the trigger reads (the
-- edge function itself reads RESEND_API_KEY / INVITE_HOOK_SECRET from function secrets separately):
--
--   select vault.create_secret('https://<project-ref>.supabase.co', 'project_url');
--   select vault.create_secret('<same value as INVITE_HOOK_SECRET>', 'invite_hook_secret');
--
-- To rotate, delete and recreate the named secret. `project_url` is the base URL; the trigger
-- appends `/functions/v1/send-invite-email`.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pg_net;

-- Reads a Vault secret by name, or NULL if unset. SECURITY DEFINER: only the definer (superuser
-- during migration) can read vault.decrypted_secrets, not the authenticated role.
CREATE OR REPLACE FUNCTION invite_email_config(p_name text)
RETURNS text LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public, vault AS $$
  SELECT decrypted_secret FROM vault.decrypted_secrets WHERE name = p_name LIMIT 1;
$$;

-- Trigger fn: on a new pending invite, POST the invite details to the send-invite-email function.
-- TG_ARGV[0] is the kind ('grocery' | 'recipe_book'); the parent id column differs per table, so
-- read it dynamically from the NEW row. grocery_list_members carries invited_by; recipe_book_members
-- does not, so we default it to NULL (the function falls back to the parent owner).
CREATE OR REPLACE FUNCTION notify_invite_email()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
  v_kind text := TG_ARGV[0];
  v_base_url text := invite_email_config('project_url');
  v_secret text := invite_email_config('invite_hook_secret');
  v_parent_id uuid;
  v_invited_by uuid;
BEGIN
  -- Only a genuine new invite: pending, not yet linked to a user (skips the accepted owner
  -- auto-row and any pre-linked self-add).
  IF NEW.status <> 'pending' OR NEW.user_id IS NOT NULL THEN
    RETURN NEW;
  END IF;

  -- Missing config: don't fail the invite insert — just skip the email. (Comment in the header
  -- explains the required vault.create_secret calls.)
  IF v_base_url IS NULL OR v_secret IS NULL THEN
    RETURN NEW;
  END IF;

  IF v_kind = 'grocery' THEN
    v_parent_id := NEW.list_id;
    v_invited_by := NEW.invited_by;
  ELSE
    v_parent_id := NEW.recipe_book_id;
    v_invited_by := NULL;
  END IF;

  -- Async: net.http_post queues into net.http_request_queue and returns immediately, so a slow or
  -- failing email never blocks or rolls back the invite insert. Delivery/errors land in
  -- net._http_response.
  PERFORM net.http_post(
    url := rtrim(v_base_url, '/') || '/functions/v1/send-invite-email',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', 'Bearer ' || v_secret
    ),
    body := jsonb_build_object(
      'kind', v_kind,
      'memberId', NEW.id,
      'parentId', v_parent_id,
      'invitedEmail', NEW.invited_email,
      'invitedBy', v_invited_by,
      'role', NEW.role
    )
  );

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_notify_grocery_invite_email ON grocery_list_members;
CREATE TRIGGER trg_notify_grocery_invite_email
  AFTER INSERT ON grocery_list_members
  FOR EACH ROW EXECUTE FUNCTION notify_invite_email('grocery');

DROP TRIGGER IF EXISTS trg_notify_recipe_book_invite_email ON recipe_book_members;
CREATE TRIGGER trg_notify_recipe_book_invite_email
  AFTER INSERT ON recipe_book_members
  FOR EACH ROW EXECUTE FUNCTION notify_invite_email('recipe_book');
