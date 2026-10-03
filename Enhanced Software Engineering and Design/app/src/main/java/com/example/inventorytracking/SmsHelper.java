package com.example.inventorytracking;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

/** Keeps the original emulator destination and remembers each account's SMS choice. */
public class SmsHelper {
    // Original classroom emulator target, NOT a phone number inferred from an account.
    public static final String ALERT_PHONE_NUMBER = "5554";
    public enum SendResult { DISABLED, SUBMITTED, FAILED }
    private final Context context;
    private final SharedPreferences preferences;

    public SmsHelper(Context context) {
        this(context, context.getApplicationContext().getSharedPreferences("sms_choices", Context.MODE_PRIVATE));
    }

    SmsHelper(Context context, SharedPreferences preferences) {
        this.context = context.getApplicationContext();
        this.preferences = preferences;
    }

    public boolean hasChoice(String username) {
        return preferences.contains("sms_choice_" + username);
    }

    public boolean isEnabled(String username) {
        return preferences.getBoolean("sms_choice_" + username, false);
    }

    public void saveChoice(String username, boolean enabled) {
        preferences.edit().putBoolean("sms_choice_" + username, enabled).apply();
    }

    public boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public SendResult sendZeroInventoryAlert(String username, String itemName) {
        if (!isEnabled(username) || !isPermissionGranted()) return SendResult.DISABLED;
        try {
            SmsManager manager = context.getSystemService(SmsManager.class);
            if (manager == null) return SendResult.FAILED;
            String message = context.getString(R.string.sms_message, itemName);
            manager.sendMultipartTextMessage(ALERT_PHONE_NUMBER, null,
                    manager.divideMessage(message), null, null);
            // Successful submission is not confirmation of actual SMS delivery.
            return SendResult.SUBMITTED;
        } catch (SecurityException | IllegalArgumentException | UnsupportedOperationException exception) {
            return SendResult.FAILED;
        }
    }
}
