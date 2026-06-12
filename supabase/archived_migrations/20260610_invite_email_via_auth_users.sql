-- Fix: invitees could never read / accept / decline their own pending invites, so the invite
-- banner was always empty.
--
-- The invitee-facing RLS clauses matched on `auth.jwt() ->> 'email'`. In this project (ES256
-- asymmetric JWT signing keys) that expression does not resolve to the user's email in the request
-- context, even though the access token carries it — so `lower(invited_email) = lower(auth.jwt()
-- ->> 'email')` was never true and the invitee's own member row was filtered out by RLS.
--
-- `auth.uid()` (the `sub` claim) IS reliably available — owner-scoped reads work — so match on the
-- caller's email looked up from auth.users by auth.uid() instead. The lookup goes through a
-- SECURITY DEFINER helper because a plain `select … from auth.users` inside a policy hits
-- permission-denied for the authenticated role.

CREATE OR REPLACE FUNCTION current_user_email()
RETURNS text LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT email FROM auth.users WHERE id = auth.uid();
$$;

-- recipe_book_members: read / accept / decline your own invite.
DROP POLICY IF EXISTS "View members of accessible books or own invites" ON recipe_book_members;
CREATE POLICY "View members of accessible books or own invites" ON recipe_book_members
    FOR SELECT USING (
        can_access_recipe_book(recipe_book_id)
        OR user_id = auth.uid()
        OR lower(invited_email) = lower(current_user_email())
    );

DROP POLICY IF EXISTS "Owner or invitee can update membership" ON recipe_book_members;
CREATE POLICY "Owner or invitee can update membership" ON recipe_book_members
    FOR UPDATE USING (
        is_recipe_book_owner(recipe_book_id)
        OR lower(invited_email) = lower(current_user_email())
        OR user_id = auth.uid()
    );

DROP POLICY IF EXISTS "Owner or invitee can delete membership" ON recipe_book_members;
CREATE POLICY "Owner or invitee can delete membership" ON recipe_book_members
    FOR DELETE USING (
        is_recipe_book_owner(recipe_book_id)
        OR lower(invited_email) = lower(current_user_email())
        OR user_id = auth.uid()
    );

-- recipe_books: a pending invitee can read the invited book's row (so the banner can name it).
DROP POLICY IF EXISTS "Invitees can view invited books" ON recipe_books;
CREATE POLICY "Invitees can view invited books" ON recipe_books
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM recipe_book_members m
            WHERE m.recipe_book_id = recipe_books.id
              AND lower(m.invited_email) = lower(current_user_email())
        )
    );
