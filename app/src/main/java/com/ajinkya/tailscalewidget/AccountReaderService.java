package com.ajinkya.tailscalewidget;

import android.accessibilityservice.AccessibilityService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the login name from Tailscale's own account screen and, when a switch was requested from
 * the widget, drives the profile switcher. Tailscale exposes no API for the active profile, so this
 * observes only its window (see accessibility_service_config.xml) and caches the name locally.
 *
 * <p>Implicit states are made explicit as phases:
 * IDLE_OBSERVE, SWITCH_PENDING, and POST_SWITCH_SETTLE.</p>
 */
public class AccountReaderService extends AccessibilityService {

    private static final String TAILSCALE_PACKAGE = "com.tailscale.ipn";
    private static final long IDLE_SCAN_INTERVAL_MS = 1000L;
    private static final long SWITCH_SCAN_INTERVAL_MS = 400L;
    private static final long CARD_CLICK_COOLDOWN_MS = 800L;
    private static final long POST_SWITCH_SETTLE_MS = 3000L;
    private static final long POST_SWITCH_REFRESH_DELAY_MS = 2500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastScan;
    private long lastCardClick;
    private long lastSwitchAt;

    private enum ReaderPhase {
        SWITCH_PENDING,
        POST_SWITCH_SETTLE,
        IDLE_OBSERVE
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        if (!TAILSCALE_PACKAGE.contentEquals(event.getPackageName())) {
            return;
        }

        ReaderPhase phase = resolvePhase();
        long now = SystemClock.elapsedRealtime();
        if (!shouldScan(phase, now)) {
            return;
        }
        lastScan = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }

        switch (phase) {
            case SWITCH_PENDING:
                handlePendingSwitch(root, now);
                return;
            case POST_SWITCH_SETTLE:
                // Guard window: ignore observations until Tailscale UI settles after a switch.
                return;
            case IDLE_OBSERVE:
                observeAccount(root);
                return;
            default:
                return;
        }
    }

    @Override
    public void onInterrupt() {
    }

    private ReaderPhase resolvePhase() {
        if (AccountStore.isSwitchPending(this)) {
            return ReaderPhase.SWITCH_PENDING;
        }
        if (SystemClock.elapsedRealtime() - lastSwitchAt < POST_SWITCH_SETTLE_MS) {
            return ReaderPhase.POST_SWITCH_SETTLE;
        }
        return ReaderPhase.IDLE_OBSERVE;
    }

    private boolean shouldScan(ReaderPhase phase, long now) {
        long interval = (phase == ReaderPhase.SWITCH_PENDING)
                ? SWITCH_SCAN_INTERVAL_MS : IDLE_SCAN_INTERVAL_MS;
        return now - lastScan >= interval;
    }

    private void observeAccount(AccessibilityNodeInfo root) {
        String account = firstEmail(root);
        if (account != null && !account.equals(AccountStore.getAccount(this))) {
            AccountStore.setAccount(this, account);
            updateWidgets();
        }
    }

    private void handlePendingSwitch(AccessibilityNodeInfo root, long now) {
        String current = AccountStore.getAccount(this);
        if (current == null) {
            AccountStore.clearSwitch(this);
            return;
        }

        List<AccountRow> rows = new ArrayList<>();
        collectAccountRows(root, rows);
        if (rows.isEmpty()) {
            return;
        }

        if (trySwitchToDifferentAccount(rows, current, now)) {
            return;
        }

        // Still on Settings: open the account card to reach the switcher.
        tryOpenSwitcherFromCurrentAccount(rows, current, now);
    }

    private boolean trySwitchToDifferentAccount(List<AccountRow> rows, String current, long now) {
        // On the switcher the current profile has no click action; every other row is clickable.
        for (AccountRow row : rows) {
            if (!row.email.equalsIgnoreCase(current)) {
                if (row.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    AccountStore.setAccount(this, row.email);
                    AccountStore.clearSwitch(this);
                    lastSwitchAt = now;
                    updateWidgets();
                    handler.postDelayed(this::updateWidgets, POST_SWITCH_REFRESH_DELAY_MS);
                }
                return true;
            }
        }
        return false;
    }

    private void tryOpenSwitcherFromCurrentAccount(List<AccountRow> rows, String current, long now) {
        if (now - lastCardClick <= CARD_CLICK_COOLDOWN_MS) {
            return;
        }
        for (AccountRow row : rows) {
            if (row.email.equalsIgnoreCase(current)) {
                if (row.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    lastCardClick = now;
                }
                return;
            }
        }
    }

    private void collectAccountRows(AccessibilityNodeInfo node, List<AccountRow> out) {
        if (node == null) {
            return;
        }
        if (node.isClickable()) {
            String email = firstEmail(node);
            if (email != null) {
                out.add(new AccountRow(node, email));
            }
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectAccountRows(node.getChild(i), out);
        }
    }

    private String firstEmail(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }
        CharSequence text = node.getText();
        if (text != null) {
            String value = text.toString().trim();
            if (looksLikeLogin(value)) {
                return value;
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            String found = firstEmail(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private boolean looksLikeLogin(String value) {
        if (value.length() < 5 || value.length() > 100
                || value.contains(" ") || value.contains("\n")) {
            return false;
        }
        int at = value.indexOf('@');
        if (at <= 0 || at != value.lastIndexOf('@')) {
            return false;
        }
        int dot = value.indexOf('.', at);
        return dot > at + 1 && dot < value.length() - 1;
    }

    private void updateWidgets() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        for (int id : manager.getAppWidgetIds(
                new ComponentName(this, TailscaleWidgetProvider.class))) {
            WidgetRenderer.render(this, manager, id, null);
        }
    }

    private static final class AccountRow {
        final AccessibilityNodeInfo node;
        final String email;

        AccountRow(AccessibilityNodeInfo node, String email) {
            this.node = node;
            this.email = email;
        }
    }
}
