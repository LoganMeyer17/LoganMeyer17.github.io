package com.example.inventorytracking;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Original Allow/Deny screen, without a phone-number form or inventory settings page. */
public class SMSActivity extends AppCompatActivity {
    private String username;
    private SmsHelper smsHelper;
    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                smsHelper.saveChoice(username, granted);
                Toast.makeText(this, granted ? R.string.sms_enabled : R.string.sms_permission_denied,
                        Toast.LENGTH_SHORT).show();
                openInventory();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getStringExtra(LoginActivity.EXTRA_USERNAME);
        if (username == null || username.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        smsHelper = new SmsHelper(this);
        // A saved choice skips this screen. Revoked permission requires fresh consent.
        if (smsHelper.hasChoice(username)
                && (!smsHelper.isEnabled(username) || smsHelper.isPermissionGranted())) {
            openInventory();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smsactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.allowSmsButton).setOnClickListener(view -> {
            if (smsHelper.isPermissionGranted()) {
                smsHelper.saveChoice(username, true);
                openInventory();
            } else requestPermission.launch(Manifest.permission.SEND_SMS);
        });
        findViewById(R.id.denySmsButton).setOnClickListener(view -> {
            smsHelper.saveChoice(username, false);
            Toast.makeText(this, R.string.sms_disabled, Toast.LENGTH_SHORT).show();
            openInventory();
        });
    }

    private void openInventory() {
        startActivity(new Intent(this, InventoryActivity.class)
                .putExtra(LoginActivity.EXTRA_USERNAME, username));
        finish();
    }
}
