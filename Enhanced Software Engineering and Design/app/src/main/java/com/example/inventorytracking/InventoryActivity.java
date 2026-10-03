package com.example.inventorytracking;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

/** Keeps screen actions here, validation in InputValidator, and SQL in DatabaseHelper. */
public class InventoryActivity extends AppCompatActivity
        implements InventoryAdapter.Listener, AddItemDialogFragment.Listener {
    private DatabaseHelper databaseHelper;
    private InventoryAdapter adapter;
    private SmsHelper smsHelper;
    private TextView statusText, emptyText;
    private final InventoryOrganizer organizer = new InventoryOrganizer();
    private String searchText = "";
    private InventoryOrganizer.StockFilter stockFilter = InventoryOrganizer.StockFilter.ALL;
    private InventoryOrganizer.SortOption sortOption = InventoryOrganizer.SortOption.NAME;
    private String username;
    private boolean loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getStringExtra(LoginActivity.EXTRA_USERNAME);
        if (username == null || username.isEmpty()) {
            logOut();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);
        // Keep the original content spacing when Android supplies its system-bar insets.
        int padding = findViewById(R.id.main).getPaddingLeft();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(padding + bars.left, padding + bars.top,
                    padding + bars.right, padding + bars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);
        smsHelper = new SmsHelper(this);
        statusText = findViewById(R.id.statusText);
        emptyText = findViewById(R.id.emptyText);
        RecyclerView list = findViewById(R.id.inventoryList);
        adapter = new InventoryAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        configureInventoryControls();
        findViewById(R.id.addItemButton).setOnClickListener(view -> {
            if (getSupportFragmentManager().findFragmentByTag(AddItemDialogFragment.TAG) == null) {
                new AddItemDialogFragment().show(getSupportFragmentManager(), AddItemDialogFragment.TAG);
            }
        });
        findViewById(R.id.retryButton).setOnClickListener(view -> loadItems());
        findViewById(R.id.logoutButton).setOnClickListener(view -> logOut());
        loadItems();
    }

    private void configureInventoryControls() {
        EditText searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable text) {
                searchText = text.toString();
                showItems();
            }
        });

        Spinner filterSpinner = findViewById(R.id.filterSpinner);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                stockFilter = InventoryOrganizer.StockFilter.values()[position];
                showItems();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        Spinner sortSpinner = findViewById(R.id.sortSpinner);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                sortOption = InventoryOrganizer.SortOption.values()[position];
                showItems();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadItems() {
        try {
            List<InventoryItem> storedItems = databaseHelper.getItems(username);
            organizer.replaceAll(storedItems);
            loaded = true;
            statusText.setText("");
            showItems();
        } catch (SQLiteException exception) {
            loaded = false;
            emptyText.setVisibility(View.GONE);
            statusText.setText(R.string.database_error);
        }
        findViewById(R.id.addItemButton).setEnabled(loaded);
        findViewById(R.id.retryButton).setVisibility(loaded ? View.GONE : View.VISIBLE);
    }

    private void showItems() {
        List<InventoryItem> visible = organizer.visibleItems(searchText, stockFilter, sortOption);
        // ListAdapter receives a new snapshot; existing XML row views can be reused.
        adapter.submitList(visible);
        emptyText.setText(organizer.size() == 0
                ? R.string.empty_inventory : R.string.no_matching_items);
        emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int addItem(String name, String sku, int quantity, int restockLevel) {
        if (!loaded) return R.string.database_error;
        try {
            InventoryItem added = databaseHelper.addItem(
                    username, name, sku, quantity, restockLevel);
            if (added == null) return R.string.duplicate_sku;
            organizer.add(added);
            showItems();
            statusText.setText(R.string.item_added);
            if (quantity == 0) sendAlert(name);
            return 0;
        } catch (SQLiteException exception) {
            return R.string.database_error;
        } catch (IllegalArgumentException exception) {
            return R.string.quantity_error;
        }
    }

    @Override
    public void changeQuantity(InventoryItem item, int delta) {
        try {
            InventoryItem updated = databaseHelper.changeQuantity(username, item.getId(), delta);
            if (updated == null) {
                statusText.setText(R.string.item_missing);
                return;
            }
            organizer.replace(updated);
            showItems();
            statusText.setText(R.string.quantity_updated);
            if (delta < 0 && updated.getQuantity() == 0) sendAlert(updated.getName());
        } catch (IllegalArgumentException exception) {
            statusText.setText(R.string.quantity_limit);
        } catch (SQLiteException exception) {
            statusText.setText(R.string.database_error);
        }
    }

    @Override
    public void deleteItem(InventoryItem item) {
        new MaterialAlertDialogBuilder(this).setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    try {
                        if (!databaseHelper.deleteItem(username, item.getId())) {
                            statusText.setText(R.string.item_missing);
                            return;
                        }
                        organizer.remove(item.getId());
                        showItems();
                        statusText.setText(R.string.item_deleted);
                    } catch (SQLiteException exception) {
                        statusText.setText(R.string.database_error);
                    }
                }).show();
    }

    private void sendAlert(String itemName) {
        SmsHelper.SendResult result = smsHelper.sendZeroInventoryAlert(username, itemName);
        if (result == SmsHelper.SendResult.SUBMITTED) {
            Toast.makeText(this, R.string.sms_submitted, Toast.LENGTH_SHORT).show();
        } else if (result == SmsHelper.SendResult.FAILED) {
            Toast.makeText(this, R.string.sms_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void logOut() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) databaseHelper.close();
        super.onDestroy();
    }
}
