package com.ajinkya.tailscalewidget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

public class SetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        findViewById(R.id.enable).setOnClickListener(view -> openAccessibilitySettings());
        findViewById(R.id.open_tailscale).setOnClickListener(view -> openTailscaleSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean enabled = AccountStore.isReaderEnabled(this);
        String account = AccountStore.getAccount(this);

        ((TextView) findViewById(R.id.status)).setText(
                enabled ? R.string.setup_status_on : R.string.setup_status_off);
        ((TextView) findViewById(R.id.current)).setText(account != null
                ? getString(R.string.setup_current, account)
                : getString(R.string.setup_no_account));
    }

    private void openAccessibilitySettings() {
        Intent details = new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS");
        details.putExtra(Intent.EXTRA_COMPONENT_NAME,
                new ComponentName(this, AccountReaderService.class));
        try {
            startActivity(details);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
    }

    private void openTailscaleSettings() {
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
    }
}
