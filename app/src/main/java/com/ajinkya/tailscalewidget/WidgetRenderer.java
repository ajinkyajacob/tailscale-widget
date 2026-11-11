package com.ajinkya.tailscalewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

/** Draws the widget for one of four states: connected, disconnected, transitioning, missing app. */
final class WidgetRenderer {

    private static final int PENDING_INTENT_FLAGS =
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    private WidgetRenderer() {
    }

    /**
     * @param pendingConnect true while connecting, false while disconnecting, null to show the
     *                       state Android currently reports.
     */
    static void render(Context context, AppWidgetManager manager, int appWidgetId,
                       Boolean pendingConnect) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_tailscale_v3);
        boolean installed = TailscaleController.isInstalled(context);
        boolean vpnActive = installed && TailscaleController.isVpnActive(context);

        views.setOnClickPendingIntent(R.id.widget_root,
                installed ? homeIntent(context, appWidgetId) : storeIntent(context, appWidgetId));
        views.setOnClickPendingIntent(R.id.refresh_button, refreshIntent(context, appWidgetId));

        if (!installed) {
            views.setTextViewText(R.id.status_text, context.getString(R.string.status_not_installed));
            views.setTextColor(R.id.status_text, context.getColor(R.color.text_secondary));
            views.setImageViewResource(R.id.status_dot, R.drawable.dot_disconnected);
            views.setTextViewText(R.id.toggle_button, context.getString(R.string.action_install));
            views.setInt(R.id.toggle_button, "setBackgroundResource", R.drawable.bg_button_connect);
            views.setTextColor(R.id.toggle_button, context.getColor(R.color.on_accent));
            views.setViewVisibility(R.id.account_action, View.GONE);
            views.setViewVisibility(R.id.accounts_button, View.GONE);
            views.setOnClickPendingIntent(R.id.toggle_button, storeIntent(context, appWidgetId));
            manager.updateAppWidget(appWidgetId, views);
            return;
        }

        views.setViewVisibility(R.id.account_action, View.VISIBLE);
        renderAccount(context, views, appWidgetId);
        views.setViewVisibility(R.id.accounts_button, View.VISIBLE);
        views.setOnClickPendingIntent(R.id.accounts_button, appIntent(context, appWidgetId));
        views.setOnClickPendingIntent(R.id.toggle_button, toggleIntent(context, appWidgetId));

        if (pendingConnect != null) {
            int label = pendingConnect ? R.string.status_connecting : R.string.status_disconnecting;
            views.setTextViewText(R.id.status_text, context.getString(label));
            views.setTextColor(R.id.status_text, context.getColor(R.color.text_primary));
            views.setImageViewResource(R.id.status_dot, R.drawable.dot_pending);
            views.setTextViewText(R.id.toggle_button, context.getString(label));
            views.setInt(R.id.toggle_button, "setBackgroundResource",
                    R.drawable.bg_button_tonal);
            views.setTextColor(R.id.toggle_button, context.getColor(R.color.text_primary));
        } else if (vpnActive) {
            views.setTextViewText(R.id.status_text, context.getString(R.string.status_connected));
            views.setTextColor(R.id.status_text, context.getColor(R.color.text_primary));
            views.setImageViewResource(R.id.status_dot, R.drawable.dot_connected);
            views.setTextViewText(R.id.toggle_button, context.getString(R.string.action_disconnect));
            views.setInt(R.id.toggle_button, "setBackgroundResource",
                    R.drawable.bg_button_destructive);
            views.setTextColor(R.id.toggle_button, context.getColor(R.color.disconnect_text));
        } else {
            views.setTextViewText(R.id.status_text, context.getString(R.string.status_disconnected));
            views.setTextColor(R.id.status_text, context.getColor(R.color.text_secondary));
            views.setImageViewResource(R.id.status_dot, R.drawable.dot_disconnected);
            views.setTextViewText(R.id.toggle_button, context.getString(R.string.action_connect));
            views.setInt(R.id.toggle_button, "setBackgroundResource", R.drawable.bg_button_connect);
            views.setTextColor(R.id.toggle_button, context.getColor(R.color.on_accent));
        }

        manager.updateAppWidget(appWidgetId, views);
    }

    private static void renderAccount(Context context, RemoteViews views, int appWidgetId) {
        String account = AccountStore.getAccount(context);
        boolean readerEnabled = AccountStore.isReaderEnabled(context);

        if (account != null && !account.isEmpty()) {
            views.setTextViewText(R.id.account_text, account);
            views.setViewVisibility(R.id.account_switch_icon,
                    readerEnabled ? View.VISIBLE : View.GONE);
            views.setOnClickPendingIntent(R.id.account_action,
                    readerEnabled ? switchIntent(context, appWidgetId)
                            : appIntent(context, appWidgetId));
            return;
        }

        views.setViewVisibility(R.id.account_switch_icon, View.GONE);
        views.setTextViewText(R.id.account_text, context.getString(
                readerEnabled ? R.string.account_waiting : R.string.account_enable));
        views.setOnClickPendingIntent(R.id.account_action,
                readerEnabled ? appIntent(context, appWidgetId) : setupIntent(context, appWidgetId));
    }

    private static PendingIntent homeIntent(Context context, int appWidgetId) {
        // Tailscale's own navigate route pops its Compose back stack to the main Devices screen,
        // whether the app is cold or already sitting on Settings.
        Intent launch = new Intent(Intent.ACTION_VIEW,
                Uri.parse("tailscale://navigate/main/devices"))
                .setPackage(TailscaleController.PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (launch.resolveActivity(context.getPackageManager()) == null) {
            launch = context.getPackageManager()
                    .getLaunchIntentForPackage(TailscaleController.PACKAGE);
            if (launch == null) {
                return storeIntent(context, appWidgetId);
            }
            launch.putExtra("startAtRoot", true);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return PendingIntent.getActivity(context, appWidgetId * 10 + 7, launch,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent switchIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, SwitchActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, appWidgetId * 10 + 6, intent,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent setupIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, SetupActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, appWidgetId * 10 + 5, intent,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent toggleIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, TailscaleWidgetProvider.class)
                .setAction(TailscaleWidgetProvider.ACTION_TOGGLE)
                .putExtra(TailscaleWidgetProvider.EXTRA_APP_WIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId * 10 + 1, intent,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent refreshIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, TailscaleWidgetProvider.class)
                .setAction(TailscaleWidgetProvider.ACTION_REFRESH)
                .putExtra(TailscaleWidgetProvider.EXTRA_APP_WIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId * 10 + 2, intent,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent appIntent(Context context, int appWidgetId) {
        // The profile switcher has no public route. Settings is the closest entry point: its top
        // card is the signed-in account, and tapping that card opens the switcher.
        Intent launch = new Intent(Intent.ACTION_VIEW,
                Uri.parse("tailscale://navigate/settings"))
                .setPackage(TailscaleController.PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (launch.resolveActivity(context.getPackageManager()) == null) {
            launch = context.getPackageManager()
                    .getLaunchIntentForPackage(TailscaleController.PACKAGE);
            if (launch == null) {
                return storeIntent(context, appWidgetId);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return PendingIntent.getActivity(context, appWidgetId * 10 + 3, launch,
                PENDING_INTENT_FLAGS);
    }

    private static PendingIntent storeIntent(Context context, int appWidgetId) {
        Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "https://play.google.com/store/apps/details?id=" + TailscaleController.PACKAGE));
        store.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, appWidgetId * 10 + 4, store,
                PENDING_INTENT_FLAGS);
    }
}
