-- Let pending grocery-list invitees read the invited list row before accepting.
-- This mirrors recipe-book collaboration: item access still requires accepted membership,
-- but the app needs the list name for the invite banner.

DROP POLICY IF EXISTS "grocery_lists_invitees_select" ON grocery_lists;
CREATE POLICY "grocery_lists_invitees_select" ON grocery_lists FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM grocery_list_members m
      WHERE m.list_id = grocery_lists.id
        AND m.status = 'pending'
      AND lower(m.invited_email) = lower(current_user_email())
    )
  );

-- Pending grocery-list invites addressed to the current user, including the list name for the
-- invite banner. SECURITY DEFINER avoids relying on an embed/select across RLS-protected tables.
DROP FUNCTION IF EXISTS grocery_list_pending_invites();
CREATE OR REPLACE FUNCTION grocery_list_pending_invites()
RETURNS TABLE(member_id uuid, list_id uuid, list_name text, role text, status text)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT m.id, m.list_id, l.name::text, m.role::text, m.status::text
  FROM grocery_list_members m
  JOIN grocery_lists l ON l.id = m.list_id
  WHERE m.status = 'pending'
    AND lower(m.invited_email) = lower(current_user_email())
  ORDER BY l.name ASC, m.created_at ASC;
$$;

-- Full collaborator list for a grocery list: synthesized owner plus invited members. This mirrors
-- recipe_book_collaborators and avoids depending on owner rows existing in grocery_list_members.
DROP FUNCTION IF EXISTS grocery_list_collaborators(uuid);
CREATE OR REPLACE FUNCTION grocery_list_collaborators(p_list_id uuid)
RETURNS TABLE(
  member_id uuid, email text, name text, role text, status text, is_owner boolean, avatar_url text
)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
  SELECT c.member_id, c.email, c.name, c.role, c.status, c.is_owner, c.avatar_url
  FROM (
    SELECT NULL::uuid AS member_id, u.email::text AS email,
           (u.raw_user_meta_data ->> 'name')::text AS name,
           'owner'::text AS role, 'accepted'::text AS status, true AS is_owner,
           (u.raw_user_meta_data ->> 'avatar_url')::text AS avatar_url
    FROM grocery_lists l
    JOIN auth.users u ON u.id = l.owner_id
    WHERE l.id = p_list_id AND can_access_grocery_list(p_list_id)
    UNION ALL
    SELECT m.id, m.invited_email, (mu.raw_user_meta_data ->> 'name')::text,
           m.role::text, m.status::text, false,
           (mu.raw_user_meta_data ->> 'avatar_url')::text
    FROM grocery_list_members m
    LEFT JOIN auth.users mu ON mu.id = m.user_id
    JOIN grocery_lists l ON l.id = m.list_id
    WHERE m.list_id = p_list_id
      AND can_access_grocery_list(p_list_id)
      AND NOT (m.role = 'owner' AND m.user_id = l.owner_id)
  ) c
  ORDER BY c.is_owner DESC, (c.status = 'accepted') DESC, c.email ASC;
$$;
