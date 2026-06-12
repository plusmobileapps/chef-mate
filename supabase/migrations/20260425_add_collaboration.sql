-- ============================================================
-- Grocery List Collaboration
-- Run this migration in Supabase SQL Editor. Safe to re-run.
-- ============================================================

-- 1. Grocery list members (sharing/collaboration)
CREATE TABLE IF NOT EXISTS grocery_list_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  list_id UUID NOT NULL REFERENCES grocery_lists(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  role TEXT NOT NULL DEFAULT 'editor' CHECK (role IN ('owner', 'editor', 'viewer')),
  invited_by UUID REFERENCES auth.users(id),
  invited_email TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected')),
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(list_id, invited_email)
);

CREATE INDEX IF NOT EXISTS idx_glm_list_id ON grocery_list_members(list_id);
CREATE INDEX IF NOT EXISTS idx_glm_user_id ON grocery_list_members(user_id);
CREATE INDEX IF NOT EXISTS idx_glm_invited_email ON grocery_list_members(invited_email);

-- ============================================================
-- Triggers
-- ============================================================

-- Auto-insert owner row into grocery_list_members on list creation
CREATE OR REPLACE FUNCTION auto_add_grocery_list_owner()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO grocery_list_members (list_id, user_id, role, invited_email, status)
  SELECT NEW.id, NEW.owner_id, 'owner',
    COALESCE((SELECT email FROM auth.users WHERE id = NEW.owner_id), ''),
    'accepted'
  WHERE NEW.owner_id IS NOT NULL;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_auto_add_grocery_list_owner ON grocery_lists;
CREATE TRIGGER trg_auto_add_grocery_list_owner
  AFTER INSERT ON grocery_lists
  FOR EACH ROW EXECUTE FUNCTION auto_add_grocery_list_owner();

-- Migrate pending grocery-list invitations when a new user signs up
CREATE OR REPLACE FUNCTION migrate_pending_grocery_invitations()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE grocery_list_members
  SET user_id = NEW.id
  WHERE invited_email = NEW.email AND user_id IS NULL;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_migrate_pending_grocery_invitations ON auth.users;
CREATE TRIGGER trg_migrate_pending_grocery_invitations
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION migrate_pending_grocery_invitations();

-- ============================================================
-- RLS helper functions
-- SECURITY DEFINER so they bypass RLS and can't recurse — otherwise the
-- grocery_lists policies (which check membership) and the
-- grocery_list_members policies (which check list ownership) would reference
-- each other's RLS-protected tables and loop forever (Postgres 42P17).
-- ============================================================
CREATE OR REPLACE FUNCTION can_access_grocery_list(p_list_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT EXISTS (
    SELECT 1 FROM grocery_lists l
    WHERE l.id = p_list_id AND l.owner_id = auth.uid()
  ) OR EXISTS (
    SELECT 1 FROM grocery_list_members m
    WHERE m.list_id = p_list_id
      AND m.user_id = auth.uid()
      AND m.status = 'accepted'
  );
$$;

CREATE OR REPLACE FUNCTION can_edit_grocery_list(p_list_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT EXISTS (
    SELECT 1 FROM grocery_lists l
    WHERE l.id = p_list_id AND l.owner_id = auth.uid()
  ) OR EXISTS (
    SELECT 1 FROM grocery_list_members m
    WHERE m.list_id = p_list_id
      AND m.user_id = auth.uid()
      AND m.status = 'accepted'
      AND m.role IN ('owner', 'editor')
  );
$$;

CREATE OR REPLACE FUNCTION is_grocery_list_owner(p_list_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT EXISTS (
    SELECT 1 FROM grocery_lists l
    WHERE l.id = p_list_id AND l.owner_id = auth.uid()
  );
$$;

-- The caller's email, looked up from auth.users by auth.uid(). Used to match
-- email-keyed invites for users who already have an account. SECURITY DEFINER
-- because the authenticated role can't read auth.users directly.
CREATE OR REPLACE FUNCTION current_user_email()
RETURNS text LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT email FROM auth.users WHERE id = auth.uid();
$$;

-- ============================================================
-- RLS Policies
-- ============================================================

ALTER TABLE grocery_list_members ENABLE ROW LEVEL SECURITY;

-- === grocery_lists ===
DROP POLICY IF EXISTS "grocery_lists_select" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_insert" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_update" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_delete" ON grocery_lists;

CREATE POLICY "grocery_lists_select" ON grocery_lists FOR SELECT
  USING (can_access_grocery_list(id));

CREATE POLICY "grocery_lists_insert" ON grocery_lists FOR INSERT
  WITH CHECK (owner_id = auth.uid());

CREATE POLICY "grocery_lists_update" ON grocery_lists FOR UPDATE
  USING (can_edit_grocery_list(id));

CREATE POLICY "grocery_lists_delete" ON grocery_lists FOR DELETE
  USING (owner_id = auth.uid());

-- === grocery_items ===
DROP POLICY IF EXISTS "grocery_items_select" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_insert" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_update" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_delete" ON grocery_items;

CREATE POLICY "grocery_items_select" ON grocery_items FOR SELECT
  USING (can_access_grocery_list(list_id));

CREATE POLICY "grocery_items_insert" ON grocery_items FOR INSERT
  WITH CHECK (can_edit_grocery_list(list_id));

CREATE POLICY "grocery_items_update" ON grocery_items FOR UPDATE
  USING (can_edit_grocery_list(list_id));

CREATE POLICY "grocery_items_delete" ON grocery_items FOR DELETE
  USING (can_edit_grocery_list(list_id));

-- === grocery_list_members ===
DROP POLICY IF EXISTS "glm_select" ON grocery_list_members;
DROP POLICY IF EXISTS "glm_insert" ON grocery_list_members;
DROP POLICY IF EXISTS "glm_update" ON grocery_list_members;
DROP POLICY IF EXISTS "glm_delete" ON grocery_list_members;

-- See member rows for lists you can access (to list collaborators) and invites addressed to you.
CREATE POLICY "glm_select" ON grocery_list_members FOR SELECT USING (
  can_access_grocery_list(list_id)
  OR user_id = auth.uid()
  OR lower(invited_email) = lower(current_user_email())
);

-- Only the list owner can invite collaborators.
CREATE POLICY "glm_insert" ON grocery_list_members FOR INSERT WITH CHECK (
  is_grocery_list_owner(list_id)
);

-- Owner can change roles; the invitee can accept/reject their own invite.
CREATE POLICY "glm_update" ON grocery_list_members FOR UPDATE USING (
  is_grocery_list_owner(list_id)
  OR user_id = auth.uid()
  OR lower(invited_email) = lower(current_user_email())
);

-- Owner can remove anyone; a member can leave / decline.
CREATE POLICY "glm_delete" ON grocery_list_members FOR DELETE USING (
  is_grocery_list_owner(list_id)
  OR user_id = auth.uid()
  OR lower(invited_email) = lower(current_user_email())
);

-- ============================================================
-- Enable Realtime on tables for collaboration (idempotent)
-- ============================================================
DO $$ BEGIN
  ALTER PUBLICATION supabase_realtime ADD TABLE grocery_items;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
  ALTER PUBLICATION supabase_realtime ADD TABLE grocery_list_members;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;
