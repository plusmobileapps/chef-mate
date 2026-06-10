-- Phase 2a — recipe-book collaboration.
--
-- Adds `recipe_book_members` (collaborators + email invites) and widens RLS so an accepted
-- member of a book can see/edit its recipes, mirroring the owner. Invites are keyed by email:
-- the owner inserts a row with invited_email + role and status='pending'; the invitee (whose JWT
-- carries that email) accepts by stamping their user_id and status='accepted'.
--
-- RLS strategy: every new policy is ADDITIVE (PostgreSQL ORs permissive policies), so we never
-- touch the existing owner-scoped policies on recipes/recipe_books/etc. — we just grant the extra
-- collaborator access on top. Access checks go through SECURITY DEFINER helper functions so the
-- policies don't recurse through each other's RLS.

CREATE TYPE recipe_book_role AS ENUM ('owner', 'editor', 'viewer');

CREATE TABLE IF NOT EXISTS recipe_book_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipe_book_id UUID NOT NULL REFERENCES recipe_books(id) ON DELETE CASCADE,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    invited_email TEXT NOT NULL,
    role recipe_book_role NOT NULL DEFAULT 'editor',
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One invite per email per book.
CREATE UNIQUE INDEX IF NOT EXISTS idx_recipe_book_members_book_email
    ON recipe_book_members(recipe_book_id, lower(invited_email));
CREATE INDEX IF NOT EXISTS idx_recipe_book_members_user ON recipe_book_members(user_id);
CREATE INDEX IF NOT EXISTS idx_recipe_book_members_email ON recipe_book_members(lower(invited_email));

-- ---------------------------------------------------------------------------
-- Helper functions (SECURITY DEFINER so they bypass RLS and can't recurse).
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION can_access_recipe_book(p_book_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_books b
        WHERE b.id = p_book_id AND b.owner_id = auth.uid()
    ) OR EXISTS (
        SELECT 1 FROM recipe_book_members m
        WHERE m.recipe_book_id = p_book_id
          AND m.user_id = auth.uid()
          AND m.status = 'accepted'
    );
$$;

CREATE OR REPLACE FUNCTION can_edit_recipe_book(p_book_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_books b
        WHERE b.id = p_book_id AND b.owner_id = auth.uid()
    ) OR EXISTS (
        SELECT 1 FROM recipe_book_members m
        WHERE m.recipe_book_id = p_book_id
          AND m.user_id = auth.uid()
          AND m.status = 'accepted'
          AND m.role IN ('owner', 'editor')
    );
$$;

CREATE OR REPLACE FUNCTION is_recipe_book_owner(p_book_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_books b
        WHERE b.id = p_book_id AND b.owner_id = auth.uid()
    );
$$;

-- Recipe-level access, resolved through the join table. SECURITY DEFINER so the join lookup
-- bypasses recipe_book_recipes' RLS — otherwise the recipes policy below and the existing
-- recipe_book_recipes → recipes policy would recurse infinitely (Postgres 42P17).
CREATE OR REPLACE FUNCTION can_access_recipe(p_recipe_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_book_recipes rbr
        WHERE rbr.recipe_id = p_recipe_id
          AND can_access_recipe_book(rbr.recipe_book_id)
    );
$$;

CREATE OR REPLACE FUNCTION can_edit_recipe(p_recipe_id uuid)
RETURNS boolean LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT EXISTS (
        SELECT 1 FROM recipe_book_recipes rbr
        WHERE rbr.recipe_id = p_recipe_id
          AND can_edit_recipe_book(rbr.recipe_book_id)
    );
$$;

-- ---------------------------------------------------------------------------
-- recipe_book_members RLS
-- ---------------------------------------------------------------------------
ALTER TABLE recipe_book_members ENABLE ROW LEVEL SECURITY;

-- See member rows for books you can access (to list collaborators) and invites addressed to you.
CREATE POLICY "View members of accessible books or own invites" ON recipe_book_members
    FOR SELECT USING (
        can_access_recipe_book(recipe_book_id)
        OR user_id = auth.uid()
        OR lower(invited_email) = lower(auth.jwt() ->> 'email')
    );

-- Only the book owner can invite collaborators.
CREATE POLICY "Owner can invite members" ON recipe_book_members
    FOR INSERT WITH CHECK (is_recipe_book_owner(recipe_book_id));

-- Owner can change roles; the invitee can accept their own invite (stamping their user_id).
CREATE POLICY "Owner or invitee can update membership" ON recipe_book_members
    FOR UPDATE USING (
        is_recipe_book_owner(recipe_book_id)
        OR lower(invited_email) = lower(auth.jwt() ->> 'email')
        OR user_id = auth.uid()
    );

-- Owner can remove a member; the invitee can decline / leave.
CREATE POLICY "Owner or invitee can delete membership" ON recipe_book_members
    FOR DELETE USING (
        is_recipe_book_owner(recipe_book_id)
        OR lower(invited_email) = lower(auth.jwt() ->> 'email')
        OR user_id = auth.uid()
    );

-- ---------------------------------------------------------------------------
-- Widen access on the existing tables (additive — OR's with owner policies).
-- ---------------------------------------------------------------------------
CREATE POLICY "Members can view shared recipe books" ON recipe_books
    FOR SELECT USING (can_access_recipe_book(id));

-- A pending invitee can read just the invited book's row (to show its name in the invite banner)
-- before accepting — content access (recipes) still requires an accepted membership.
CREATE POLICY "Invitees can view invited books" ON recipe_books
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM recipe_book_members m
            WHERE m.recipe_book_id = recipe_books.id
              AND lower(m.invited_email) = lower(auth.jwt() ->> 'email')
        )
    );

CREATE POLICY "Editors can update shared recipe books" ON recipe_books
    FOR UPDATE USING (can_edit_recipe_book(id));

-- A recipe is visible/editable to members of any book it belongs to. Go through the SECURITY
-- DEFINER helpers (not a direct recipe_book_recipes subquery) to avoid RLS recursion.
CREATE POLICY "Members can view recipes in shared books" ON recipes
    FOR SELECT USING (can_access_recipe(id));

CREATE POLICY "Editors can update recipes in shared books" ON recipes
    FOR UPDATE USING (can_edit_recipe(id));

-- Join rows: viewable to members; editable (file/unfile a recipe) to editors of the book.
CREATE POLICY "Members can view shared book links" ON recipe_book_recipes
    FOR SELECT USING (can_access_recipe_book(recipe_book_id));

CREATE POLICY "Editors can insert shared book links" ON recipe_book_recipes
    FOR INSERT WITH CHECK (can_edit_recipe_book(recipe_book_id));

CREATE POLICY "Editors can delete shared book links" ON recipe_book_recipes
    FOR DELETE USING (can_edit_recipe_book(recipe_book_id));

-- Category attachments of recipes that live in a shared book.
CREATE POLICY "Members can view shared recipe categories" ON recipe_categories
    FOR SELECT USING (can_access_recipe(recipe_id));
