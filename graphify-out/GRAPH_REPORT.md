# Graph Report - .  (2026-11-11)

## Corpus Check
- 13 files · ~3,185 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 76 nodes · 109 edges · 12 communities (6 shown, 6 thin omitted)
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Widget UI & rendering|Widget UI & rendering]]
- [[_COMMUNITY_Widget lifecycle & actions|Widget lifecycle & actions]]
- [[_COMMUNITY_Account capture & setup|Account capture & setup]]
- [[_COMMUNITY_Build & app manifest|Build & app manifest]]
- [[_COMMUNITY_Widget UI & rendering|Widget UI & rendering]]
- [[_COMMUNITY_Account capture & setup|Account capture & setup]]
- [[_COMMUNITY_Project preferences|Project preferences]]
- [[_COMMUNITY_Project preferences|Project preferences]]
- [[_COMMUNITY_Account capture & setup|Account capture & setup]]

## God Nodes (most connected - your core abstractions)
1. `WidgetRenderer` - 9 edges
2. `TailscaleWidgetProvider` - 8 edges
3. `tailscale-widget` - 8 edges
4. `AccountReaderService` - 7 edges
5. `SetupActivity` - 6 edges
6. `TailscaleController` - 5 edges
7. `AccountStore` - 5 edges
8. `Tailscale VPN home-screen widget` - 3 edges
9. `Widget states: installed/missing, disconnected, connecting, connected, disconnecting` - 3 edges
10. `Build and install` - 2 edges

## Surprising Connections (you probably didn't know these)
- `Tailscale VPN home-screen widget` --presents--> `Widget states: installed/missing, disconnected, connecting, connected, disconnecting`  [INFERRED]
  README.md → app/src/main/java/com/ajinkya/tailscalewidget/WidgetRenderer.java
- `No third-party account/profile switch API` --works_around--> `Account name read via AccessibilityService from Tailscale UI`  [INFERRED]
  README.md → app/src/main/java/com/ajinkya/tailscalewidget/AccountReaderService.java

## Communities (12 total, 6 thin omitted)

### Community 0 - "Widget UI & rendering"
Cohesion: 0.15
Nodes (12): AccessibilityService, AppWidgetProvider, Account name read via AccessibilityService from Tailscale UI, IPNReceiver broadcasts CONNECT_VPN/DISCONNECT_VPN, No third-party account/profile switch API, Depends on official Tailscale app (com.tailscale.ipn), One UI-inspired 4×2 RemoteViews card, Optimistic toggle with poll-until-flip (≤7s, 300ms) (+4 more)

### Community 3 - "Build & app manifest"
Cohesion: 0.20
Nodes (9): Build and install, code:sh (./gradlew :app:assembleDebug), Design, How the parts work, Limitations (by design, not bugs), Requirements, tailscale-widget, What it does (+1 more)

### Community 5 - "Account capture & setup"
Cohesion: 0.43
Nodes (3): Activity, SetupActivity guides enabling accessibility reader, SetupActivity

## Knowledge Gaps
- **14 isolated node(s):** `What it does`, `Design`, `Why the widget cannot switch accounts directly`, `Requirements`, `code:sh (./gradlew :app:assembleDebug)` (+9 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **6 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `WidgetRenderer` connect `Widget UI & rendering` to `Widget UI & rendering`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `AccountReaderService` connect `Account capture & setup` to `Widget UI & rendering`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Why does `SetupActivity` connect `Account capture & setup` to `Account capture & setup`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **What connects `What it does`, `Design`, `Why the widget cannot switch accounts directly` to the rest of the system?**
  _14 weakly-connected nodes found - possible documentation gaps or missing edges._