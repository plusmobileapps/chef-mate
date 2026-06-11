-- TEMPORARY DIAGNOSTIC — safe to drop once the invite banner works.
-- Returns the server's view of the caller plus RLS-bypassing row counts, so we can tell apart:
--   * an anonymous request  (uid null / role 'anon'), vs.
--   * a project mismatch     (total_member_rows = 0 in the project the app actually talks to), vs.
--   * an email-data mismatch (my_pending = 0 while total_member_rows > 0).
-- SECURITY DEFINER so the counts ignore RLS; auth.uid()/role still reflect the real caller.
CREATE OR REPLACE FUNCTION debug_auth_context()
RETURNS jsonb LANGUAGE sql SECURITY DEFINER STABLE SET search_path = public AS $$
    SELECT jsonb_build_object(
        'uid', auth.uid(),
        'role', auth.role(),
        'jwt_email', auth.jwt() ->> 'email',
        'my_email', current_user_email(),
        'total_member_rows', (SELECT count(*) FROM recipe_book_members),
        'my_pending', (
            SELECT count(*) FROM recipe_book_members
            WHERE status = 'pending'
              AND lower(invited_email) = lower(current_user_email())
        ),
        -- The deployed SELECT policy in THIS project, so we can see if the email clause is actually
        -- live where the app queries (vs. whatever project the SQL editor is pointed at).
        'select_policy_count', (
            SELECT count(*) FROM pg_policy
            WHERE polrelid = 'recipe_book_members'::regclass AND polcmd = 'r'
        ),
        'select_policy', (
            SELECT pg_get_expr(polqual, polrelid) FROM pg_policy
            WHERE polrelid = 'recipe_book_members'::regclass AND polcmd = 'r'
            LIMIT 1
        )
    );
$$;
