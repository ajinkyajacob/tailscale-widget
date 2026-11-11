package com.ajinkya.tailscalewidget;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Talks to the official Tailscale app through the two integration points it exposes to
 * third-party apps: its exported IPNReceiver (connect/disconnect) and the launcher intent.
 */
final class TailscaleController {

    static final String PACKAGE = "com.tailscale.ipn";

    private static final String ACTION_CONNECT = "com.tailscale.ipn.CONNECT_VPN";
    private static final String ACTION_DISCONNECT = "com.tailscale.ipn.DISCONNECT_VPN";

    private TailscaleController() {
    }

    static boolean isInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Android allows a single active VPN, so an active VPN transport means the Tailscale tunnel
     * is up on any phone that uses Tailscale as its VPN. Android does not reveal which app owns
     * the VPN, so this is evidence rather than an authoritative Tailscale session state.
     */
    static boolean isVpnActive(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        if (manager == null) {
            return false;
        }
        Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }

    static void requestConnection(Context context, boolean connect) {
        Intent intent = new Intent(connect ? ACTION_CONNECT : ACTION_DISCONNECT);
        intent.setPackage(PACKAGE);
        context.sendBroadcast(intent);
    }
}
