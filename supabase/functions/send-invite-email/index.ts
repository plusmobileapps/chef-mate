// Supabase Edge Function: send-invite-email
//
// Emails a collaboration invitee when they're invited to a shared grocery list or recipe book.
// Invites are plain INSERTs into `grocery_list_members` / `recipe_book_members` with
// status='pending', done directly by every client (iOS/Android/Desktop/Web). A database trigger
// (see supabase/migrations/20260707_invite_email_notification.sql) fires `pg_net` at this function
// on each new pending invite, so the notification is client-agnostic and needs no app-side code.
//
// Auth: callers must present `Authorization: Bearer <INVITE_HOOK_SECRET>`. Only the DB trigger
// should reach this — it must NOT be invokable with an ordinary user JWT. Set INVITE_HOOK_SECRET
// to a long random value and store the same value in Vault for the trigger (see the migration).
//
// Body (sent by the trigger):
//   { kind: "grocery" | "recipe_book", memberId, parentId, invitedEmail, invitedBy, role }
// `invitedBy` is only present for grocery invites; recipe-book invites fall back to the book owner.
//
// Deploy:  supabase functions deploy send-invite-email
// Secrets: supabase secrets set RESEND_API_KEY=<key> INVITE_HOOK_SECRET=<random>
//          (SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY are injected automatically by Supabase.)
//          RESEND_FROM and APP_INVITE_URL are optional overrides for the sender / call-to-action.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const DEFAULT_FROM = "Chef Mate <noreply@plusmobileapps.com>";
const DEFAULT_APP_URL = "https://chefmate.plusmobileapps.com";

interface InvitePayload {
  kind: "grocery" | "recipe_book";
  memberId: string;
  parentId: string;
  invitedEmail: string;
  invitedBy?: string | null;
  role?: string | null;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const hookSecret = Deno.env.get("INVITE_HOOK_SECRET");
    if (!hookSecret) {
      return json({ error: "INVITE_HOOK_SECRET is not configured" }, 500);
    }
    const authHeader = req.headers.get("Authorization");
    if (authHeader !== `Bearer ${hookSecret}`) {
      return json({ error: "Unauthorized" }, 401);
    }

    const resendApiKey = Deno.env.get("RESEND_API_KEY");
    if (!resendApiKey) {
      return json({ error: "RESEND_API_KEY is not configured" }, 500);
    }

    const payload = (await req.json()) as InvitePayload;
    if (!payload?.kind || !payload?.parentId || !payload?.invitedEmail) {
      return json({ error: "Missing kind, parentId, or invitedEmail" }, 400);
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const admin = createClient(supabaseUrl, serviceRoleKey);

    // Resolve the parent list/book: its display name and owner (the inviter fallback).
    const table = payload.kind === "grocery" ? "grocery_lists" : "recipe_books";
    const { data: parent, error: parentError } = await admin
      .from(table)
      .select("name, owner_id")
      .eq("id", payload.parentId)
      .single();

    if (parentError || !parent) {
      return json({ error: `Could not load ${table}: ${parentError?.message ?? "not found"}` }, 404);
    }

    const listName = (parent.name as string) || (payload.kind === "grocery" ? "a grocery list" : "a recipe book");
    const kindLabel = payload.kind === "grocery" ? "grocery list" : "recipe book";

    // Inviter: `invited_by` when present (grocery), else the parent owner (recipe books).
    const inviterId = payload.invitedBy ?? (parent.owner_id as string | null);
    const inviterName = await resolveInviterName(admin, inviterId);

    const appUrl = Deno.env.get("APP_INVITE_URL") ?? DEFAULT_APP_URL;
    const from = Deno.env.get("RESEND_FROM") ?? DEFAULT_FROM;
    const subject = `${inviterName} invited you to “${listName}” on Chef Mate`;

    const resendResponse = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${resendApiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        from,
        to: payload.invitedEmail,
        subject,
        html: htmlBody(inviterName, listName, kindLabel, appUrl),
        text: textBody(inviterName, listName, kindLabel, appUrl),
      }),
    });

    if (!resendResponse.ok) {
      const detail = await resendResponse.text();
      return json({ error: `Resend failed (${resendResponse.status}): ${detail}` }, 502);
    }

    return new Response(null, { status: 204, headers: corsHeaders });
  } catch (e) {
    return json({ error: String(e) }, 500);
  }
});

// Inviter's display name from auth.users metadata, falling back to their email, then "Someone".
async function resolveInviterName(
  admin: ReturnType<typeof createClient>,
  inviterId: string | null,
): Promise<string> {
  if (!inviterId) return "Someone";
  try {
    const { data, error } = await admin.auth.admin.getUserById(inviterId);
    if (error || !data?.user) return "Someone";
    const name = data.user.user_metadata?.name;
    if (typeof name === "string" && name.trim().length > 0) return name.trim();
    return data.user.email ?? "Someone";
  } catch (_) {
    return "Someone";
  }
}

function htmlBody(inviter: string, listName: string, kindLabel: string, appUrl: string): string {
  return `
    <div style="font-family: -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif; max-width: 480px; margin: 0 auto; color: #1a1a1a;">
      <h2 style="margin-bottom: 8px;">You’ve been invited to collaborate</h2>
      <p><strong>${escapeHtml(inviter)}</strong> invited you to the ${kindLabel}
        <strong>“${escapeHtml(listName)}”</strong> on Chef Mate.</p>
      <p>Open Chef Mate with this email address to accept the invite.</p>
      <p style="margin: 24px 0;">
        <a href="${appUrl}" style="background: #2e7d32; color: #fff; padding: 12px 20px; border-radius: 8px; text-decoration: none; display: inline-block;">Open Chef Mate</a>
      </p>
      <p style="color: #777; font-size: 13px;">If you weren’t expecting this, you can safely ignore this email.</p>
    </div>`;
}

function textBody(inviter: string, listName: string, kindLabel: string, appUrl: string): string {
  return [
    `${inviter} invited you to the ${kindLabel} “${listName}” on Chef Mate.`,
    ``,
    `Open Chef Mate with this email address to accept the invite: ${appUrl}`,
    ``,
    `If you weren’t expecting this, you can safely ignore this email.`,
  ].join("\n");
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
