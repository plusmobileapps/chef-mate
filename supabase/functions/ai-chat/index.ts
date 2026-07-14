// Supabase Edge Function: ai-chat
//
// Server-side proxy for the in-app AI chat (Google Gemini). The Gemini API key used to ship in the
// client build (BuildConfig.GEMINI_API_KEY), which meant anyone could pull it out of the binary.
// Moving the key into this function keeps it server-only: the client sends the chat history, this
// function forwards it to Gemini with the secret key and streams the reply back.
//
// The app talks to this function for two things, distinguished by the `stream` flag in the body:
//   - stream = true  → streaming chat reply (Gemini `:streamGenerateContent?alt=sse`). The upstream
//                      SSE body is piped straight back so the client's existing SSE parsing is
//                      unchanged (each event's `data:` is a Gemini `GenerateContentResponse`).
//   - stream = false → one-shot structured-output extraction (Gemini `:generateContent`). Used by
//                      the "Add recipe" pill and the photo-scan flow. `generationConfig` carries the
//                      responseSchema. The Gemini JSON is returned verbatim (same `candidates` shape).
//
// Only `contents` and `generationConfig` are forwarded — the model id and API key are fixed here so
// a client can't override them.
//
// Auth: callers must present a Supabase JWT (the app is always signed in, anonymously at minimum).
// Edge Functions verify the JWT at the gateway by default; we additionally reject a missing header.
//
// Deploy:  supabase functions deploy ai-chat
// Secrets: supabase secrets set GEMINI_API_KEY=<key>
//          (SUPABASE_URL / SUPABASE_ANON_KEY are injected automatically by Supabase.)

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const MODEL_ID = "gemini-2.5-flash";
const GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/models";

interface ChatRequest {
  stream?: boolean;
  contents?: unknown;
  generationConfig?: unknown;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return json({ error: "Method not allowed" }, 405);
  }

  // Gate access to signed-in callers. Supabase verifies the JWT at the gateway; this is a
  // defence-in-depth check so the function never runs unauthenticated even if that changes.
  if (!req.headers.get("Authorization")) {
    return json({ error: "Missing Authorization header" }, 401);
  }

  const apiKey = Deno.env.get("GEMINI_API_KEY");
  if (!apiKey) {
    // Surfaced by the client as the "AI features not configured" state.
    return json({ error: "MISSING_API_KEY" }, 503);
  }

  let payload: ChatRequest;
  try {
    payload = (await req.json()) as ChatRequest;
  } catch (_) {
    return json({ error: "Invalid JSON body" }, 400);
  }

  if (!Array.isArray(payload.contents) || payload.contents.length === 0) {
    return json({ error: "Missing contents" }, 400);
  }

  // Only forward the two fields we control; the model id and key are fixed above.
  const geminiBody: Record<string, unknown> = { contents: payload.contents };
  if (payload.generationConfig != null) {
    geminiBody.generationConfig = payload.generationConfig;
  }

  const stream = payload.stream === true;
  const upstreamUrl = stream
    ? `${GEMINI_BASE}/${MODEL_ID}:streamGenerateContent?alt=sse&key=${apiKey}`
    : `${GEMINI_BASE}/${MODEL_ID}:generateContent?key=${apiKey}`;

  let geminiResponse: Response;
  try {
    geminiResponse = await fetch(upstreamUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(geminiBody),
    });
  } catch (e) {
    return json({ error: `Gemini request failed: ${String(e)}` }, 502);
  }

  if (!geminiResponse.ok) {
    const detail = await geminiResponse.text();
    return json({ error: `Gemini error (${geminiResponse.status}): ${detail}` }, 502);
  }

  if (stream) {
    // Pipe Gemini's Server-Sent Events straight back to the client, unbuffered.
    return new Response(geminiResponse.body, {
      status: 200,
      headers: {
        ...corsHeaders,
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache",
        Connection: "keep-alive",
      },
    });
  }

  // One-shot: return Gemini's JSON verbatim so the client parses the same `candidates` shape.
  const body = await geminiResponse.text();
  return new Response(body, {
    status: 200,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
});

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}
