# Firestick Dashboard

Android TV / Fire TV dashboard client for the Ivy dashboard server.

## MVP features
- Full-screen dashboard for Fire TV / Fire Stick
- Polls `GET /api/state`
- Optional API key via `X-API-Key`
- Displays weather, headlines, watchlist, portfolio, and last update time
- D-pad friendly refresh action

## Server
Default primary server URL:

- `https://rob-mac-mini-dashboard.tail5d56a7.ts.net/api/state`

Fallback local URL:

- `http://10.0.1.90:8000/api/state`

The app reads configuration from `gradle.properties`:

```properties
DASHBOARD_BASE_URL=https://rob-mac-mini-dashboard.tail5d56a7.ts.net/
DASHBOARD_FALLBACK_BASE_URL=http://10.0.1.90:8000/
DASHBOARD_API_KEY=
```

If the Tailscale URL is unreachable, the app will try the fallback URL.

If `DASHBOARD_API_KEY` is empty, the app sends no auth header.

## Build
Open in Android Studio Hedgehog or newer.

1. Sync Gradle
2. Optionally set `DASHBOARD_API_KEY` in `~/.gradle/gradle.properties` or project `gradle.properties`
3. Run on a Fire TV / Android TV device
4. If using the Tailscale URL, the client device must be on the same Tailscale tailnet and have working `.ts.net` DNS resolution

## Notes
- This repo was scaffolded as an MVP client.
- SSE (`/events`) can be added next if live updates are preferred over polling.
