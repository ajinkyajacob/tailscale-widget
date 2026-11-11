package com.ajinkya.tailscalewidget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Transparent trampoline: marks a switch request, then opens Tailscale's Settings screen where the
 * accessibility service can drive the profile switcher.
 */
public class SwitchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountStore.requestSwitch(this);

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("tailscale://navigate/settings"));
        intent.setPackage(TailscaleController.PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent launch = getPackageManager()
                    .getLaunchIntentForPackage(TailscaleController.PACKAGE);
            if (launch != null) {
                startActivity(launch);
            }
        }
        finish();
        overridePendingTransition(0, 0);
    }
}
