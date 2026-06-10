// Supabase Edge Function: cleanup-avatars
//
// Sweeps the `avatars` bucket for orphaned objects — files no longer referenced by any user's
// `avatar_url` metadata. The app deletes the previous avatar best-effort when a photo is replaced
// (see ManageProfileViewModel.save), and delete-account removes a user's folder on deletion, but
// neither is a hard guarantee: a failed client-side delete still leaks, and avatars orphaned before
// those safeguards shipped remain. This function is the backstop — run it once to clear the backlog
// and/or on a schedule (pg_cron + pg_net, or any external cron) to keep storage tidy.
//
// Direct DELETE on storage.objects is blocked by storage.protect_delete(), so deletion goes through
// the Storage API with the service-role client.
//
// Auth: callers must present `Authorization: Bearer <CLEANUP_SECRET>`. This is an admin sweep over
// every user's storage, so it must NOT be reachable with an ordinary user JWT. Set CLEANUP_SECRET to
// a long random value.
//
// Dry run: pass `{ "dryRun": true }` (or `?dryRun=true`) to list orphans without deleting — do this
// on the first run to eyeball the result before letting it delete anything.
//
// Deploy:  supabase functions deploy cleanup-avatars
// Secret:  supabase secrets set CLEANUP_SECRET=<random>   (SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY
//          are injected automatically by Supabase.)
//
// Example:
//   curl -X POST "$SUPABASE_URL/functions/v1/cleanup-avatars" \
//     -H "Authorization: Bearer $CLEANUP_SECRET" \
//     -H "Content-Type: application/json" \
//     -d '{"dryRun": true}'

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const BUCKET = "avatars";

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
    const cleanupSecret = Deno.env.get("CLEANUP_SECRET");
    if (!cleanupSecret) {
      return json({ error: "CLEANUP_SECRET is not configured" }, 500);
    }
    const authHeader = req.headers.get("Authorization");
    if (authHeader !== `Bearer ${cleanupSecret}`) {
      return json({ error: "Unauthorized" }, 401);
    }

    const dryRun = await wantsDryRun(req);

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const admin = createClient(supabaseUrl, serviceRoleKey);

    // 1. Collect every avatar path still referenced by a user's metadata.
    const referenced = new Set<string>();
    for (let page = 1; ; page++) {
      const { data, error } = await admin.auth.admin.listUsers({ page, perPage: 1000 });
      if (error) return json({ error: `listUsers failed: ${error.message}` }, 500);
      if (data.users.length === 0) break;
      for (const user of data.users) {
        const url = user.user_metadata?.avatar_url;
        if (typeof url === "string" && url.includes(`/${BUCKET}/`)) {
          // Public URL: <...>/storage/v1/object/public/avatars/<userId>/<uuid>.<ext>[?query]
          referenced.add(url.split(`/${BUCKET}/`)[1].split("?")[0]);
        }
      }
    }

    // 2. Walk the bucket (<userId>/ prefixes -> files) and flag anything unreferenced.
    const orphans: string[] = [];
    let scanned = 0;
    const { data: folders, error: foldersError } = await admin.storage
      .from(BUCKET)
      .list("", { limit: 100000 });
    if (foldersError) return json({ error: `list root failed: ${foldersError.message}` }, 500);

    // A null `id` marks a prefix (folder); real objects have an id.
    for (const folder of (folders ?? []).filter((entry) => entry.id === null)) {
      const { data: files, error: filesError } = await admin.storage
        .from(BUCKET)
        .list(folder.name, { limit: 100000 });
      if (filesError) return json({ error: `list ${folder.name} failed: ${filesError.message}` }, 500);
      for (const file of files ?? []) {
        scanned++;
        const path = `${folder.name}/${file.name}`;
        if (!referenced.has(path)) orphans.push(path);
      }
    }

    // 3. Delete the orphans (unless this is a dry run), in batches to stay within request limits.
    let deleted = 0;
    if (!dryRun && orphans.length > 0) {
      for (let i = 0; i < orphans.length; i += 100) {
        const batch = orphans.slice(i, i + 100);
        const { error } = await admin.storage.from(BUCKET).remove(batch);
        if (error) return json({ error: `remove failed: ${error.message}`, deleted }, 500);
        deleted += batch.length;
      }
    }

    return json({
      dryRun,
      scanned,
      referenced: referenced.size,
      orphans: orphans.length,
      deleted,
      orphanPaths: orphans,
    }, 200);
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});

async function wantsDryRun(req: Request): Promise<boolean> {
  if (new URL(req.url).searchParams.get("dryRun") === "true") return true;
  try {
    const body = await req.clone().json();
    return body?.dryRun === true;
  } catch (_) {
    return false;
  }
}

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
