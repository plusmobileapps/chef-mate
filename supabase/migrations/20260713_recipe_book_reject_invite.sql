-- Let a recipe-book invitee reject an invite instead of the invite silently disappearing.
--
-- Previously declining an invite DELETEd the member row, so the book owner had no way to tell the
-- difference between "never responded" and "declined". The client now stamps status = 'rejected'
-- (mirroring grocery-list collaboration), so the owner sees the declined invite in the collaborator
-- list. The row stays readable to the owner via recipe_book_collaborators (which already returns
-- every status), and the invitee loses content access because can_access_recipe_book requires
-- status = 'accepted'.
--
-- The original status CHECK only allowed ('pending', 'accepted'); widen it to include 'rejected'.
ALTER TABLE recipe_book_members
    DROP CONSTRAINT IF EXISTS recipe_book_members_status_check;

ALTER TABLE recipe_book_members
    ADD CONSTRAINT recipe_book_members_status_check
    CHECK (status IN ('pending', 'accepted', 'rejected'));
