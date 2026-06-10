// Supabase Edge Function: delete-account
//
// Permanently deletes the *calling* user's account. The client SDK can't delete auth users, so the
// app invokes this function (see SupabaseAuthenticationRepository.deleteAccount). It:
//   1. Reads the caller's JWT from the Authorization header.
//   2. Resolves the user with an anon-key client (RLS-scoped to that user).
//   3. Deletes the user with a service-role client (admin.deleteUser).
//
// App tables (recipes, groceries, meals, categories, …) must declare their owner FK to
// auth.users(id) with ON DELETE CASCADE so the user's rows are removed alongside the auth row.
// Storage objects under the user's folder in the `recipe-photos` / `avatars` buckets are best-effort
// removed here too.
//
// Deploy:  supabase functions deploy delete-account
// Secrets: SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY are injected automatically by Supabase.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return json({ error: "Missing Authorization header" }, 401);
    }

    // Resolve the caller from their JWT.
    const userClient = createClient(supabaseUrl, anonKey, {
      global: { headers: { Authorization: authHeader } },
    });
    const {
      data: { user },
      error: userError,
    } = await userClient.auth.getUser();

    if (userError || !user) {
      return json({ error: "Invalid or expired session" }, 401);
    }

    const admin = createClient(supabaseUrl, serviceRoleKey);

    // Best-effort: remove the user's storage objects before deleting the auth row. Failures here
    // don't block account deletion — orphaned objects can be swept later.
    for (const bucket of ["recipe-photos", "avatars"]) {
      try {
        const { data: files } = await admin.storage.from(bucket).list(user.id);
        if (files && files.length > 0) {
          await admin.storage
            .from(bucket)
            .remove(files.map((f) => `${user.id}/${f.name}`));
        }
      } catch (_) {
        // ignore storage cleanup failures
      }
    }

    // Delete the auth user. App-table rows cascade via ON DELETE CASCADE FKs to auth.users.
    const { error: deleteError } = await admin.auth.admin.deleteUser(user.id);
    if (deleteError) {
      return json({ error: deleteError.message }, 500);
    }

    return new Response(null, { status: 204, headers: corsHeaders });
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
