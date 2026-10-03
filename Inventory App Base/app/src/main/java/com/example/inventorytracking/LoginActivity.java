package com.example.inventorytracking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText Username;
    private EditText editTextText2;
    private Button button;
    private Button CreateAccount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Username = findViewById(R.id.Username);
        editTextText2 = findViewById(R.id.editTextText2);
        button = findViewById(R.id.button);
        CreateAccount = findViewById(R.id.CreateAccount);

        databaseHelper = new DatabaseHelper(this);

        // login click
        button.setOnClickListener(v -> logInUser());

        // create click
        CreateAccount.setOnClickListener(v -> createAccount());
    }

    private void logInUser() {
        String username = Username.getText().toString().trim();
        String password = editTextText2.getText().toString().trim();

        // check empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // check login
        if (databaseHelper.checkLogin(username, password)) {
            Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();

            // next screen
            startActivity(new Intent(LoginActivity.this, SMSActivity.class));
        } else {
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAccount() {
        String username = Username.getText().toString().trim();
        String password = editTextText2.getText().toString().trim();

        // check empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean created = databaseHelper.createUser(username, password);

        if (created) {
            Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show();

            // next screen
            startActivity(new Intent(LoginActivity.this, SMSActivity.class));
        } else {
            Toast.makeText(this, "Username already exists.", Toast.LENGTH_SHORT).show();
        }
    }
}