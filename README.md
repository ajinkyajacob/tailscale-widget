<img src="assets/icon.svg" width="64" height="64" alt="">

# tailscale-widget

A small home-screen widget for the **official Tailscale Android app**: connect or disconnect the
VPN with one tap, and jump straight to Tailscale's account switcher.

Built for a Galaxy S24 Ultra (One UI), but it works on any phone running Android 8.0+.

## What it does

- **Connect / Disconnect** — sends Tailscale's own automation broadcasts
  (`com.tailscale.ipn.CONNECT_VPN` / `DISCONNECT_VPN`) to the official app. Nothing is
  reimplemented; the official app stays the VPN.
- **Settings** — deep-links to Tailscale's **Settings** screen.
- **Account** — shows the signed-in login name under the title; tap it to switch to the next
  profile (via the accessibility reader).
- **Status dot + label** — Connected / Disconnected / Connecting… / Disconnecting…, with a
  best-effort check against Android's VPN state.
- **Tap the card** — opens Tailscale's home screen. The small ↻ glyph refreshes the widget state
  (handy after toggling from the Quick Settings tile).
- **Not installed?** The main button turns into an Install button that opens the Play Store page.

## Design

The card follows One UI's visual language: a 28dp squircle surface, a flat 1dp outline, medium-weight
type, and pill-shaped (26dp) actions with One UI's blue primary and tonal secondary. The connected
state uses a red-tinted tonal pill so "Disconnect" reads as an active destructive action rather than
a disabled one. It switches between the light and dark palettes from `values/` and `values-night/`
with the system theme.

The status line is deliberately large: at the 4×2 size One UI gives this widget (≈358×193dp) there
is room for a glanceable headline. The block (title → account → status → actions) is optically
centered in the card instead of pinned to the top and bottom, so extra height reads as intentional
breathing room.

Icon-only actions keep their visual size but carry real touch targets: the refresh glyph is 18dp
inside a 44dp square, and the account/swap row is a 32dp-tall full-width target.

## Account switching, and why it needs accessibility

Tailscale exposes no account API to other apps:

- The only exported receiver is `com.tailscale.ipn.IPNReceiver`, and it accepts exactly
  `CONNECT_VPN`, `DISCONNECT_VPN` and `USE_EXIT_NODE` — no profile actions.
- `tailscale://navigate/...` deep links exist, but the Android handler only routes
  `main/devices`, `main/exit-nodes` and `settings`; its source comments say deep links must never
  perform an action on the user's behalf.
- Profile switching (`/profiles/{id}`) goes through Tailscale's local API, which is an in-process
  JNI call (`libtailscale.CallLocalAPI`). There is no socket or HTTP port to call from outside.

The widget therefore uses a narrowly scoped **AccessibilityService** (`AccountReaderService`):

- It observes only `com.tailscale.ipn` (`android:packageNames` in its config), reads the signed-in
  login name from Tailscale's account card, and caches it locally for the widget.
- Tapping the account row launches Tailscale Settings through a transparent trampoline
  (`SwitchActivity`), opens the account card, and clicks the next profile in the switcher.
- Nothing leaves the device, and the app has **no internet permission**.

Enable it once from the app icon → accessibility settings. Without it the widget still toggles the
VPN and opens the app; the account row falls back to "Enable account".

This is UI automation, so a Tailscale redesign can break it, and switching needs the phone
unlocked. If it fails, the app simply stays on Settings where you can switch manually.

## Requirements

- The official **Tailscale** app (`com.tailscale.ipn`), installed and signed in.
- Android 8.0 (API 26) or newer.

## Build and install

The project is plain Java + AGP with no third-party dependencies.

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then long-press the home screen → **Widgets** → **Tailscale VPN** → drag it out (4×2 by default).

`local.properties` points at the local Android SDK and is gitignored. The debug APK is signed with
the standard debug key; for a keepable install, add your own signing config.

## How the parts work

| File | Job |
| --- | --- |
| `TailscaleWidgetProvider.java` | Widget lifecycle, tap handling, optimistic + delayed re-checks |
| `TailscaleController.java` | The only place that talks to Tailscale: install check, broadcasts, VPN state |
| `WidgetRenderer.java` | Builds the `RemoteViews` for each state |
| `res/layout/widget_tailscale_v3.xml` | The 4×2 card |
| `res/xml/tailscale_widget_info.xml` | Widget metadata, target cell size, 30-minute update period |

## Limitations (by design, not bugs)

- **VPN state is evidence, not Tailscale truth.** Android exposes that *a* VPN transport is
  active, not which app owns it. Android only allows one VPN at a time, so this is accurate in
  practice, but another VPN app would also read as "Connected".
- **Refresh cadence is the OS's.** The widget updates on tap, after its own toggle (it holds the
  transition until the tunnel actually flips, polling every 300 ms for up to 7 s), and at Android's
  30-minute widget cadence. A change made inside Tailscale or from the Quick Settings tile shows up
  on the next refresh or when you tap the header.
- **The toggle is a request to Tailscale, not a switch.** The widget sends the broadcast and
  Tailscale enqueues a `WorkManager` job, boots its Go core, and brings up the tunnel. That startup
  is the delay you feel, and Samsung's battery management can add to it (keep Tailscale out of
  "Sleeping apps"). The widget shows `Connecting…` for the whole transition so it does not flicker
  back to the old state.
- **Connect/disconnect is a request.** If Tailscale is signed out or needs re-authentication it
  can decline; the widget then falls back to the observed VPN state.
- **The broadcast interface is Tailscale's own,** not a documented public API. A future Tailscale
  release could rename those actions.
