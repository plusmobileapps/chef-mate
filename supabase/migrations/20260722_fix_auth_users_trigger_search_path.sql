-- Fix sign up failing with "Database error saving new user" (unexpected_failure) on POST /auth/v1/signup.
--
-- `migrate_pending_grocery_invitations` runs as an AFTER INSERT trigger on auth.users, so it is
-- executed by the GoTrue auth server's own database session — not a PostgREST session. That session's
-- search_path does not include `public`, so the unqualified `grocery_list_members` reference failed to
-- resolve (42P01). The trigger error aborted the enclosing INSERT, and GoTrue surfaced it as the
-- generic "Database error saving new user". Every new account creation hit this, including the
-- anonymous sign-in used by the photo-upload path.
--
-- Fix: pin `search_path = public` and schema-qualify, matching every other SECURITY DEFINER function
-- in these migrations. These two were the only ones missing the pinned search_path.

CREATE OR REPLACE FUNCTION migrate_pending_grocery_invitations()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  UPDATE public.grocery_list_members
  SET user_id = NEW.id
  WHERE invited_email = NEW.email AND user_id IS NULL;

  RETURN NEW;
END;
$$;

-- Same latent defect, lower blast radius: this one fires on public.grocery_lists, where the caller's
-- search_path has so far always included `public`. Pin it too rather than leave it depending on that.
CREATE OR REPLACE FUNCTION auto_add_grocery_list_owner()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.grocery_list_members (list_id, user_id, role, invited_email, status)
  SELECT NEW.id, NEW.owner_id, 'owner',
    COALESCE((SELECT email FROM auth.users WHERE id = NEW.owner_id), ''),
    'accepted'
  WHERE NEW.owner_id IS NOT NULL;
  RETURN NEW;
END;
$$;
