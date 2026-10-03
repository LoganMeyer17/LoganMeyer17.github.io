package com.example.inventorytracking;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Login keeps its original job; account creation now has its own screen. */
public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_USERNAME = "signed_in_username";
    private DatabaseHelper databaseHelper;
    private EditText usernameInput, passwordInput;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        statusText = findViewById(R.id.statusText);
        findViewById(R.id.loginButton).setOnClickListener(view -> logIn());
        findViewById(R.id.createAccountButton).setOnClickListener(view ->
                startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void logIn() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            statusText.setText(R.string.required_login);
            return;
        }
        // Registration rules are not retroactively imposed on existing accounts.
        // Do not trim passwords: silently changing credentials hides input mistakes.
        try {
            if (!databaseHelper.checkLogin(username, password)) {
                statusText.setText(R.string.invalid_credentials);
                return;
            }
            passwordInput.setText("");
            startActivity(new Intent(this, SMSActivity.class).putExtra(EXTRA_USERNAME, username));
            finish();
        } catch (SQLiteException exception) {
            statusText.setText(R.string.database_error);
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}
