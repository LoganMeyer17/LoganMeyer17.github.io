package com.example.inventorytracking;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** A small registration form that uses the same rules as the local unit tests. */
public class RegistrationActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        statusText = findViewById(R.id.statusText);
        findViewById(R.id.registerButton).setOnClickListener(view -> createAccount());
        findViewById(R.id.backToLoginButton).setOnClickListener(view -> finish());
    }

    private void createAccount() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmation = confirmPasswordInput.getText().toString();
        usernameInput.setError(null);
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);
        statusText.setText("");
        // Display the complete rule for any invalid value, including spaces or length.
        if (!InputValidator.isValidUsername(username)) {
            showError(usernameInput, R.string.username_rules);
            return;
        }
        if (!InputValidator.isValidPassword(password)) {
            showError(passwordInput, R.string.password_rules);
            return;
        }
        if (!InputValidator.isValidPassword(confirmation)) {
            showError(confirmPasswordInput, R.string.password_rules);
            return;
        }
        if (!password.equals(confirmation)) {
            showError(confirmPasswordInput, R.string.password_mismatch);
            return;
        }
        try {
            if (!databaseHelper.createUser(username, password)) {
                showError(usernameInput, R.string.username_exists);
                return;
            }
            Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteException exception) {
            statusText.setText(R.string.database_error);
        }
    }

    private void showError(EditText field, int message) {
        field.setError(getString(message));
        field.requestFocus();
        statusText.setText(message);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}
