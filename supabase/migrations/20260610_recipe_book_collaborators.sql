-- Full collaborator list for a recipe book — owner plus every invited member — readable by anyone
-- who can access the book (owner or accepted member), so collaborators can see who they share with
-- and each person's role. SECURITY DEFINER so it can resolve the owner's email from auth.users
-- (not readable cross-user) and read member rows without tripping per-row RLS; the can_access guard
-- keeps the list scoped to people actually on the book.
-- DROP first: CREATE OR REPLACE can't change a function's return type, and the RETURNS TABLE shape
-- has grown (avatar_url, then name) since earlier versions shipped.
DROP FUNCTION IF EXISTS recipe_book_collaborators(uuid);
CREATE OR REPLACE FUNCTION recipe_book_collaborators(p_book_id uuid)
RETURNS TABLE(
    member_id uuid, email text, name text, role text, status text, is_owner boolean, avatar_url text
)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    -- Sort outside the UNION: a set-operation ORDER BY can't use column expressions, only the
    -- union's output columns, so wrap it and order the combined result here. name + avatar_url come
    -- from each user's metadata; pending invites have no account yet so both are null.
    SELECT c.member_id, c.email, c.name, c.role, c.status, c.is_owner, c.avatar_url
    FROM (
        SELECT NULL::uuid AS member_id, u.email::text AS email,
               (u.raw_user_meta_data ->> 'name')::text AS name,
               'owner'::text AS role, 'accepted'::text AS status, true AS is_owner,
               (u.raw_user_meta_data ->> 'avatar_url')::text AS avatar_url
        FROM recipe_books b
        JOIN auth.users u ON u.id = b.owner_id
        WHERE b.id = p_book_id AND can_access_recipe_book(p_book_id)
        UNION ALL
        SELECT m.id, m.invited_email, (mu.raw_user_meta_data ->> 'name')::text,
               m.role::text, m.status, false,
               (mu.raw_user_meta_data ->> 'avatar_url')::text
        FROM recipe_book_members m
        LEFT JOIN auth.users mu ON mu.id = m.user_id
        WHERE m.recipe_book_id = p_book_id AND can_access_recipe_book(p_book_id)
    ) c
    ORDER BY c.is_owner DESC, (c.status = 'accepted') DESC, c.email ASC;
$$;
