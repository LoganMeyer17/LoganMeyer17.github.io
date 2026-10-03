package com.example.inventorytracking;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;

public class InventoryActivity extends AppCompatActivity {

    private EditText AddItemName;
    private EditText editTextText8;
    private EditText editTextText9;
    private Button AddItem;
    private LinearLayout itemsContainer;

    private DatabaseHelper databaseHelper;

    // test number
    private static final String ALERT_PHONE_NUMBER = "5554";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AddItemName = findViewById(R.id.AddItemName);
        editTextText8 = findViewById(R.id.editTextText8);
        editTextText9 = findViewById(R.id.editTextText9);
        AddItem = findViewById(R.id.AddItem);
        itemsContainer = findViewById(R.id.itemsContainer);

        databaseHelper = new DatabaseHelper(this);

        // add click
        AddItem.setOnClickListener(v -> addInventoryItem());

        // load items
        displayItems();
    }

    private void addInventoryItem() {
        String itemName = AddItemName.getText().toString().trim();
        String sku = editTextText8.getText().toString().trim();
        String quantityText = editTextText9.getText().toString().trim();

        // check empty
        if (itemName.isEmpty() || sku.isEmpty() || quantityText.isEmpty()) {
            Toast.makeText(this, "Enter item name, SKU, and quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Quantity must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // check sku
        if (databaseHelper.skuExists(sku)) {
            Toast.makeText(this, "SKU already exists. Use a unique SKU.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = databaseHelper.addItem(itemName, sku, quantity);

        if (inserted) {
            Toast.makeText(this, "Item added.", Toast.LENGTH_SHORT).show();

            // clear fields
            AddItemName.setText("");
            editTextText8.setText("");
            editTextText9.setText("");

            // refresh list
            displayItems();

            // zero check
            if (quantity == 0) {
                sendZeroInventoryAlert(itemName);
            }
        } else {
            Toast.makeText(this, "Failed to add item.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayItems() {
        // clear list
        itemsContainer.removeAllViews();

        Cursor cursor = databaseHelper.getAllItems();

        // empty list
        if (cursor.getCount() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No inventory items yet.");
            emptyView.setTextSize(16f);
            emptyView.setPadding(8, 16, 8, 16);
            itemsContainer.addView(emptyView);
            cursor.close();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow("item_name"));
            String sku = cursor.getString(cursor.getColumnIndexOrThrow("sku"));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));

            View rowView = inflater.inflate(R.layout.row_item, itemsContainer, false);

            TextView itemNameText = rowView.findViewById(R.id.rowItemName);
            TextView skuText = rowView.findViewById(R.id.rowSku);
            TextView quantityText = rowView.findViewById(R.id.rowQuantity);
            Button plusButton = rowView.findViewById(R.id.plusButton);
            Button minusButton = rowView.findViewById(R.id.minusButton);
            Button deleteButton = rowView.findViewById(R.id.deleteButton);

            itemNameText.setText("Item: " + itemName);
            skuText.setText("SKU: " + sku);
            quantityText.setText("Quantity: " + quantity);

            // plus
            plusButton.setOnClickListener(v -> {
                int newQuantity = quantity + 1;
                databaseHelper.updateQuantity(id, newQuantity);
                displayItems();
            });

            // minus
            minusButton.setOnClickListener(v -> {
                int newQuantity = quantity - 1;
                if (newQuantity < 0) {
                    newQuantity = 0;
                }

                databaseHelper.updateQuantity(id, newQuantity);
                displayItems();

                // zero check
                if (newQuantity == 0) {
                    sendZeroInventoryAlert(itemName);
                }
            });

            // delete
            deleteButton.setOnClickListener(v -> {
                databaseHelper.deleteItem(id);
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
                displayItems();
            });

            itemsContainer.addView(rowView);
        }

        cursor.close();
    }

    private void sendZeroInventoryAlert(String itemName) {

        // log sms
        Log.d("SMS_TEST", "sending sms for: " + itemName);

        // check permission
        if (!SMSActivity.smsPermissionGranted) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(
                    ALERT_PHONE_NUMBER,
                    null,
                    "Inventory alert: " + itemName + " has reached zero quantity.",
                    null,
                    null
            );

            Toast.makeText(this, "SMS alert sent for " + itemName + ".", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed to send.", Toast.LENGTH_SHORT).show();
        }
    }
}