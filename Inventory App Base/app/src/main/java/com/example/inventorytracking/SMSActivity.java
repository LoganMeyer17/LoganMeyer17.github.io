package com.example.inventorytracking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SMSActivity extends AppCompatActivity {

    private Button allowSmsButton;
    private Button denySmsButton;

    // permission flag
    public static boolean smsPermissionGranted = false;

    // permission result
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                smsPermissionGranted = isGranted;

                if (isGranted) {
                    Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS permission denied. App will still work.", Toast.LENGTH_SHORT).show();
                }

                // next screen
                startActivity(new Intent(SMSActivity.this, InventoryActivity.class));
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_smsactivity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        allowSmsButton = findViewById(R.id.allowSmsButton);
        denySmsButton = findViewById(R.id.denySmsButton);

        // allow click
        allowSmsButton.setOnClickListener(v -> requestSmsPermission());

        // deny click
        denySmsButton.setOnClickListener(v -> {
            smsPermissionGranted = false;
            Toast.makeText(this, "SMS alerts disabled.", Toast.LENGTH_SHORT).show();

            // next screen
            startActivity(new Intent(SMSActivity.this, InventoryActivity.class));
        });
    }

    private void requestSmsPermission() {
        // check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            smsPermissionGranted = true;
            Toast.makeText(this, "SMS permission already granted.", Toast.LENGTH_SHORT).show();

            // next screen
            startActivity(new Intent(SMSActivity.this, InventoryActivity.class));

        } else {
            // request permission
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }
}