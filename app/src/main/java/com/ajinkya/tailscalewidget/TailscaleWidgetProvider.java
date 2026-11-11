package com.ajinkya.tailscalewidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class TailscaleWidgetProvider extends AppWidgetProvider {

    static final String ACTION_TOGGLE = "com.ajinkya.tailscalewidget.action.TOGGLE";
    static final String ACTION_REFRESH = "com.ajinkya.tailscalewidget.action.REFRESH";
    static final String EXTRA_APP_WIDGET_ID = "app_widget_id";

    private static final long TRANSITION_TIMEOUT_MS = 7000L;
    private static final long TRANSITION_POLL_MS = 300L;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetRenderer.render(context, appWidgetManager, appWidgetId, null);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        WidgetRenderer.render(context, appWidgetManager, appWidgetId, null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (ACTION_TOGGLE.equals(intent.getAction())) {
            handleToggle(context.getApplicationContext(),
                    intent.getIntExtra(EXTRA_APP_WIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID));
        } else if (ACTION_REFRESH.equals(intent.getAction())) {
            refresh(context.getApplicationContext(),
                    intent.getIntExtra(EXTRA_APP_WIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID));
        }
    }

    private void handleToggle(Context context, int appWidgetId) {
        PendingResult pendingResult = goAsync();

        if (!TailscaleController.isInstalled(context)) {
            update(context, appWidgetId, null);
            pendingResult.finish();
            return;
        }

        boolean connect = !TailscaleController.isVpnActive(context);
        TailscaleController.requestConnection(context, connect);
        update(context, appWidgetId, connect);

        new Thread(() -> {
            try {
                // Keep the transition on screen until Tailscale actually flips the tunnel, so the
                // label does not flicker back to the old state while the app is still starting.
                long deadline = SystemClock.elapsedRealtime() + TRANSITION_TIMEOUT_MS;
                while (SystemClock.elapsedRealtime() < deadline) {
                    if (TailscaleController.isVpnActive(context) == connect) {
                        break;
                    }
                    Thread.sleep(TRANSITION_POLL_MS);
                }
            } catch (InterruptedException ignored) {
            } finally {
                update(context, appWidgetId, null);
                pendingResult.finish();
            }
        }, "tailscale-widget-recheck").start();
    }

    private void refresh(Context context, int appWidgetId) {
        update(context, appWidgetId, null);
    }

    private static void update(Context context, int appWidgetId, Boolean pendingConnect) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetRenderer.render(context, manager, appWidgetId, pendingConnect);
            return;
        }
        for (int id : manager.getAppWidgetIds(
                new ComponentName(context, TailscaleWidgetProvider.class))) {
            WidgetRenderer.render(context, manager, id, pendingConnect);
        }
    }
}
