-- Full collaborator list for a recipe book — owner plus every invited member — readable by anyone
-- who can access the book (owner or accepted member), so collaborators can see who they share with
-- and each person's role. SECURITY DEFINER so it can resolve the owner's email from auth.users
-- (not readable cross-user) and read member rows without tripping per-row RLS; the can_access guard
-- keeps the list scoped to people actually on the book.
CREATE OR REPLACE FUNCTION recipe_book_collaborators(p_book_id uuid)
RETURNS TABLE(member_id uuid, email text, role text, status text, is_owner boolean)
LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT NULL::uuid AS member_id, u.email::text, 'owner'::text, 'accepted'::text, true AS is_owner
    FROM recipe_books b
    JOIN auth.users u ON u.id = b.owner_id
    WHERE b.id = p_book_id AND can_access_recipe_book(p_book_id)
    UNION ALL
    SELECT m.id, m.invited_email, m.role::text, m.status, false
    FROM recipe_book_members m
    WHERE m.recipe_book_id = p_book_id AND can_access_recipe_book(p_book_id)
    ORDER BY is_owner DESC, (status = 'accepted') DESC, email ASC;
$$;
