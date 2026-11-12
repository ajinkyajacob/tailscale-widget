# Graph Report - tailscale-widget  (2026-11-12)

## Corpus Check
- 14 files · ~13,943 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 104 nodes · 171 edges · 14 communities (7 shown, 7 thin omitted)
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 37 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1fafcc87`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

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
- [[_COMMUNITY_Community 12|Community 12]]

## God Nodes (most connected - your core abstractions)
1. `AccountReaderService` - 15 edges
2. `WidgetRenderer` - 13 edges
3. `AccountStore` - 9 edges
4. `tailscale-widget` - 9 edges
5. `TailscaleWidgetProvider` - 8 edges
6. `SetupActivity` - 6 edges
7. `TailscaleController` - 5 edges
8. `WidgetRendererStateTest` - 4 edges
9. `SwitchActivity` - 3 edges
10. `Tailscale VPN home-screen widget` - 3 edges

## Surprising Connections (you probably didn't know these)
- `Tailscale VPN home-screen widget` --presents--> `Widget states: installed/missing, disconnected, connecting, connected, disconnecting`  [INFERRED]
  README.md → app/src/main/java/com/ajinkya/tailscalewidget/WidgetRenderer.java
- `No third-party account/profile switch API` --works_around--> `Account name read via AccessibilityService from Tailscale UI`  [INFERRED]
  README.md → app/src/main/java/com/ajinkya/tailscalewidget/AccountReaderService.java

## Communities (14 total, 7 thin omitted)

### Community 0 - "Widget UI & rendering"
Cohesion: 0.12
Nodes (14): AccessibilityService, AppWidgetProvider, Account name read via AccessibilityService from Tailscale UI, IPNReceiver broadcasts CONNECT_VPN/DISCONNECT_VPN, No third-party account/profile switch API, Depends on official Tailscale app (com.tailscale.ipn), One UI-inspired 4×2 RemoteViews card, Optimistic toggle with poll-until-flip (≤7s, 300ms) (+6 more)

### Community 3 - "Build & app manifest"
Cohesion: 0.18
Nodes (10): Account switching, and why it needs accessibility, Build and install, code:sh (./gradlew :app:assembleDebug), Design, How the parts work, Limitations (by design, not bugs), Requirements, tailscale-widget (+2 more)

### Community 5 - "Account capture & setup"
Cohesion: 0.24
Nodes (3): Activity, SetupActivity, SwitchActivity

## Knowledge Gaps
- **15 isolated node(s):** `What it does`, `Design`, `Account switching, and why it needs accessibility`, `Requirements`, `code:sh (./gradlew :app:assembleDebug)` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AccountReaderService` connect `Account capture & setup` to `Widget UI & rendering`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `WidgetRenderer` connect `Widget UI & rendering` to `Widget UI & rendering`, `Community 12`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Why does `AccountStore` connect `Account capture & setup` to `Widget UI & rendering`, `Account capture & setup`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **What connects `What it does`, `Design`, `Account switching, and why it needs accessibility` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Widget UI & rendering` be split into smaller, more focused modules?**
  _Cohesion score 0.12380952380952381 - nodes in this community are weakly interconnected._