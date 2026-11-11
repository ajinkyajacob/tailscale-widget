package com.ajinkya.tailscalewidget;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityManager;

final class AccountStore {

    private static final String PREFS = "tailscale_widget";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_SWITCH_UNTIL = "switch_until";
    private static final long SWITCH_WINDOW_MS = 15000L;

    enum SwitchRequestState {
        NONE,
        PENDING
    }

    private AccountStore() {
    }

    static String getAccount(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_ACCOUNT, null);
    }

    static void setAccount(Context context, String account) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCOUNT, account)
                .apply();
    }

    static void requestSwitch(Context context) {
        // Explicit transition: NONE -> PENDING (time-bounded window).
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_SWITCH_UNTIL, SystemClock.elapsedRealtime() + SWITCH_WINDOW_MS)
                .apply();
    }

    static SwitchRequestState getSwitchRequestState(Context context) {
        long until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_SWITCH_UNTIL, 0L);
        if (until == 0L) {
            return SwitchRequestState.NONE;
        }
        if (SystemClock.elapsedRealtime() > until) {
            clearSwitch(context); // Transition: PENDING -> NONE (expired)
            return SwitchRequestState.NONE;
        }
        return SwitchRequestState.PENDING;
    }

    static boolean isSwitchPending(Context context) {
        return getSwitchRequestState(context) == SwitchRequestState.PENDING;
    }

    static void clearSwitch(Context context) {
        // Explicit transition: PENDING -> NONE.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_SWITCH_UNTIL)
                .apply();
    }

    static boolean isReaderEnabled(Context context) {
        AccessibilityManager manager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            ServiceInfo serviceInfo =
                    info.getResolveInfo() == null ? null : info.getResolveInfo().serviceInfo;
            if (serviceInfo != null
                    && context.getPackageName().equals(serviceInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
