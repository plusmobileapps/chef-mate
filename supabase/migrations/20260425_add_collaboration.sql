-- ============================================================
-- Grocery List & Recipe Collaboration
-- Run this migration in Supabase SQL Editor
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

-- 2. Recipe shares (sharing/collaboration)
CREATE TABLE IF NOT EXISTS recipe_shares (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  role TEXT NOT NULL DEFAULT 'viewer' CHECK (role IN ('owner', 'editor', 'viewer')),
  invited_by UUID REFERENCES auth.users(id),
  invited_email TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected')),
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(recipe_id, invited_email)
);

CREATE INDEX IF NOT EXISTS idx_rs_recipe_id ON recipe_shares(recipe_id);
CREATE INDEX IF NOT EXISTS idx_rs_user_id ON recipe_shares(user_id);
CREATE INDEX IF NOT EXISTS idx_rs_invited_email ON recipe_shares(invited_email);

-- 3. Add forked_from to recipes
ALTER TABLE recipes ADD COLUMN IF NOT EXISTS forked_from UUID REFERENCES recipes(id) ON DELETE SET NULL;

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

-- Auto-insert owner row into recipe_shares on recipe creation
CREATE OR REPLACE FUNCTION auto_add_recipe_owner()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO recipe_shares (recipe_id, user_id, role, invited_email, status)
  SELECT NEW.id, NEW.owner_id, 'owner',
    COALESCE((SELECT email FROM auth.users WHERE id = NEW.owner_id), ''),
    'accepted'
  WHERE NEW.owner_id IS NOT NULL;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_auto_add_recipe_owner ON recipes;
CREATE TRIGGER trg_auto_add_recipe_owner
  AFTER INSERT ON recipes
  FOR EACH ROW EXECUTE FUNCTION auto_add_recipe_owner();

-- Migrate pending invitations when a new user signs up
CREATE OR REPLACE FUNCTION migrate_pending_invitations()
RETURNS TRIGGER AS $$
BEGIN
  -- Grocery list invitations
  UPDATE grocery_list_members
  SET user_id = NEW.id
  WHERE invited_email = NEW.email AND user_id IS NULL;

  -- Recipe share invitations
  UPDATE recipe_shares
  SET user_id = NEW.id
  WHERE invited_email = NEW.email AND user_id IS NULL;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trg_migrate_pending_invitations ON auth.users;
CREATE TRIGGER trg_migrate_pending_invitations
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION migrate_pending_invitations();

-- ============================================================
-- RLS Policies
-- ============================================================

-- Enable RLS on new tables
ALTER TABLE grocery_list_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_shares ENABLE ROW LEVEL SECURITY;

-- === grocery_lists ===
-- Drop existing policies if any to avoid conflicts
DROP POLICY IF EXISTS "grocery_lists_select" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_insert" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_update" ON grocery_lists;
DROP POLICY IF EXISTS "grocery_lists_delete" ON grocery_lists;

CREATE POLICY "grocery_lists_select" ON grocery_lists FOR SELECT USING (
  owner_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM grocery_list_members
    WHERE grocery_list_members.list_id = grocery_lists.id
      AND grocery_list_members.user_id = auth.uid()
      AND grocery_list_members.status = 'accepted'
  )
);

CREATE POLICY "grocery_lists_insert" ON grocery_lists FOR INSERT
  WITH CHECK (owner_id = auth.uid());

CREATE POLICY "grocery_lists_update" ON grocery_lists FOR UPDATE USING (
  owner_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM grocery_list_members
    WHERE grocery_list_members.list_id = grocery_lists.id
      AND grocery_list_members.user_id = auth.uid()
      AND grocery_list_members.role IN ('owner', 'editor')
      AND grocery_list_members.status = 'accepted'
  )
);

CREATE POLICY "grocery_lists_delete" ON grocery_lists FOR DELETE USING (
  owner_id = auth.uid()
);

-- === grocery_items ===
DROP POLICY IF EXISTS "grocery_items_select" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_insert" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_update" ON grocery_items;
DROP POLICY IF EXISTS "grocery_items_delete" ON grocery_items;

CREATE POLICY "grocery_items_select" ON grocery_items FOR SELECT USING (
  EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_items.list_id
      AND (
        grocery_lists.owner_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM grocery_list_members
          WHERE grocery_list_members.list_id = grocery_lists.id
            AND grocery_list_members.user_id = auth.uid()
            AND grocery_list_members.status = 'accepted'
        )
      )
  )
);

CREATE POLICY "grocery_items_insert" ON grocery_items FOR INSERT WITH CHECK (
  EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_items.list_id
      AND (
        grocery_lists.owner_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM grocery_list_members
          WHERE grocery_list_members.list_id = grocery_lists.id
            AND grocery_list_members.user_id = auth.uid()
            AND grocery_list_members.role IN ('owner', 'editor')
            AND grocery_list_members.status = 'accepted'
        )
      )
  )
);

CREATE POLICY "grocery_items_update" ON grocery_items FOR UPDATE USING (
  EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_items.list_id
      AND (
        grocery_lists.owner_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM grocery_list_members
          WHERE grocery_list_members.list_id = grocery_lists.id
            AND grocery_list_members.user_id = auth.uid()
            AND grocery_list_members.role IN ('owner', 'editor')
            AND grocery_list_members.status = 'accepted'
        )
      )
  )
);

CREATE POLICY "grocery_items_delete" ON grocery_items FOR DELETE USING (
  EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_items.list_id
      AND (
        grocery_lists.owner_id = auth.uid()
        OR EXISTS (
          SELECT 1 FROM grocery_list_members
          WHERE grocery_list_members.list_id = grocery_lists.id
            AND grocery_list_members.user_id = auth.uid()
            AND grocery_list_members.role IN ('owner', 'editor')
            AND grocery_list_members.status = 'accepted'
        )
      )
  )
);

-- === grocery_list_members ===
CREATE POLICY "glm_select" ON grocery_list_members FOR SELECT USING (
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_list_members.list_id
      AND grocery_lists.owner_id = auth.uid()
  )
);

CREATE POLICY "glm_insert" ON grocery_list_members FOR INSERT WITH CHECK (
  EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_list_members.list_id
      AND grocery_lists.owner_id = auth.uid()
  )
);

CREATE POLICY "glm_update" ON grocery_list_members FOR UPDATE USING (
  -- Invited user can accept/reject
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_list_members.list_id
      AND grocery_lists.owner_id = auth.uid()
  )
);

CREATE POLICY "glm_delete" ON grocery_list_members FOR DELETE USING (
  -- Owner can remove anyone, member can leave
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM grocery_lists
    WHERE grocery_lists.id = grocery_list_members.list_id
      AND grocery_lists.owner_id = auth.uid()
  )
);

-- === recipes ===
DROP POLICY IF EXISTS "recipes_select" ON recipes;
DROP POLICY IF EXISTS "recipes_insert" ON recipes;
DROP POLICY IF EXISTS "recipes_update" ON recipes;
DROP POLICY IF EXISTS "recipes_delete" ON recipes;

CREATE POLICY "recipes_select" ON recipes FOR SELECT USING (
  owner_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM recipe_shares
    WHERE recipe_shares.recipe_id = recipes.id
      AND recipe_shares.user_id = auth.uid()
      AND recipe_shares.status = 'accepted'
  )
);

CREATE POLICY "recipes_insert" ON recipes FOR INSERT
  WITH CHECK (owner_id = auth.uid());

CREATE POLICY "recipes_update" ON recipes FOR UPDATE USING (
  owner_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM recipe_shares
    WHERE recipe_shares.recipe_id = recipes.id
      AND recipe_shares.user_id = auth.uid()
      AND recipe_shares.role IN ('owner', 'editor')
      AND recipe_shares.status = 'accepted'
  )
);

CREATE POLICY "recipes_delete" ON recipes FOR DELETE USING (
  owner_id = auth.uid()
);

-- === recipe_shares ===
CREATE POLICY "rs_select" ON recipe_shares FOR SELECT USING (
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM recipes
    WHERE recipes.id = recipe_shares.recipe_id
      AND recipes.owner_id = auth.uid()
  )
);

CREATE POLICY "rs_insert" ON recipe_shares FOR INSERT WITH CHECK (
  EXISTS (
    SELECT 1 FROM recipes
    WHERE recipes.id = recipe_shares.recipe_id
      AND recipes.owner_id = auth.uid()
  )
);

CREATE POLICY "rs_update" ON recipe_shares FOR UPDATE USING (
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM recipes
    WHERE recipes.id = recipe_shares.recipe_id
      AND recipes.owner_id = auth.uid()
  )
);

CREATE POLICY "rs_delete" ON recipe_shares FOR DELETE USING (
  user_id = auth.uid()
  OR EXISTS (
    SELECT 1 FROM recipes
    WHERE recipes.id = recipe_shares.recipe_id
      AND recipes.owner_id = auth.uid()
  )
);

-- ============================================================
-- Enable Realtime on tables for collaboration
-- ============================================================
ALTER PUBLICATION supabase_realtime ADD TABLE grocery_items;
ALTER PUBLICATION supabase_realtime ADD TABLE grocery_list_members;
ALTER PUBLICATION supabase_realtime ADD TABLE recipe_shares;
