# `:admin` — Feature-Flag Admin Dashboard

Standalone Compose app (wasmJs + jvm) for managing the Supabase `feature_flags` table — create, edit, archive, delete, and target flags at specific users.

```bash
./gradlew :admin:wasmJsBrowserDevelopmentRun   # web (phone-reachable when deployed)
./gradlew :admin:run                           # desktop, fastest local iteration
./gradlew :admin:jvmTest                        # unit tests
./gradlew :admin:wasmJsBrowserDistribution      # static bundle for hosting
```

Full setup, security model (admin RLS), deployment, and usage — including the
"enable a flag for one user only" recipe — are in
[`docs/feature-flag-admin.md`](../docs/feature-flag-admin.md).
