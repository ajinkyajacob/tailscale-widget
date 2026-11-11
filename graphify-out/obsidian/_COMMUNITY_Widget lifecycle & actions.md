---
type: community
cohesion: 0.26
members: 12
---

# Widget lifecycle & actions

**Cohesion:** 0.26 - loosely connected
**Members:** 12 nodes

## Members
- [[.TailscaleController()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleController.java
- [[.handleToggle()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[.isInstalled()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleController.java
- [[.isVpnActive()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleController.java
- [[.onAppWidgetOptionsChanged()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[.onReceive()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[.onUpdate()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[.refresh()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[.requestConnection()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleController.java
- [[.update()]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java
- [[TailscaleController]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleController.java
- [[TailscaleWidgetProvider]] - code - app/src/main/java/com/ajinkya/tailscalewidget/TailscaleWidgetProvider.java

## Live Query (requires Dataview plugin)

```dataview
TABLE source_file, type FROM #community/Widget_lifecycle__actions
SORT file.name ASC
```

## Connections to other communities
- 5 edges to [[_COMMUNITY_Widget UI & rendering]]
- 3 edges to [[_COMMUNITY_Widget UI & rendering]]

## Top bridge nodes
- [[TailscaleWidgetProvider]] - degree 8, connects to 1 community
- [[TailscaleController]] - degree 5, connects to 1 community
- [[.update()]] - degree 4, connects to 1 community
- [[.isInstalled()]] - degree 3, connects to 1 community
- [[.isVpnActive()]] - degree 3, connects to 1 community