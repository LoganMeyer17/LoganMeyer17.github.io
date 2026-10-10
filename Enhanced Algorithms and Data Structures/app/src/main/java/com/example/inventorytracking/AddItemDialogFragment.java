package com.example.inventorytracking;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** A separate add-item form. Invalid input remains visible so the user can correct it. */
public class AddItemDialogFragment extends DialogFragment {
    public static final String TAG = "add_item";
    public interface Listener {
        // Zero means success; otherwise return the string resource explaining the failure.
        int addItem(String name, String sku, int quantity, int restockLevel);
    }
    private EditText nameInput, skuInput, quantityInput, restockLevelInput;
    private TextView statusText;
    private Button addButton;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle state) {
        View content = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        nameInput = content.findViewById(R.id.itemNameInput);
        skuInput = content.findViewById(R.id.skuInput);
        quantityInput = content.findViewById(R.id.quantityInput);
        restockLevelInput = content.findViewById(R.id.restockLevelInput);
        statusText = content.findViewById(R.id.statusText);
        if (state != null) {
            nameInput.setText(state.getString("name", ""));
            skuInput.setText(state.getString("sku", ""));
            quantityInput.setText(state.getString("quantity", ""));
            restockLevelInput.setText(state.getString("restockLevel", ""));
        }
        return new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.add_item)
                .setView(content).setPositiveButton(R.string.add_item, null)
                .setNegativeButton(R.string.cancel, null).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) requireDialog();
        addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addButton.setTextColor(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled}
                },
                new int[]{
                        ContextCompat.getColor(requireContext(), R.color.original_orange),
                        ContextCompat.getColor(requireContext(), R.color.inventory_button_gray)
                }));
        addButton.setOnClickListener(view -> submit());
        configureRequiredField(nameInput, R.string.item_name);
        configureRequiredField(skuInput, R.string.sku);
        configureRequiredField(quantityInput, R.string.quantity);
        configureRequiredField(restockLevelInput, R.string.restock_level);
        updateAddButtonState();
    }

    private void configureRequiredField(EditText field, int labelResource) {
        String label = getString(labelResource);
        SpannableString hint = new SpannableString(label + " *");
        hint.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(requireContext(), R.color.required_red)),
                hint.length() - 1, hint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        field.setHint(hint);
        field.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) validateField(field, true);
            updateAddButtonState();
        });
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start,
                    int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start,
                    int before, int count) { }
            @Override public void afterTextChanged(Editable text) {
                // Once an error is visible, keep it current while the user corrects the field.
                if (field.getError() != null) validateField(field, false);
                updateAddButtonState();
            }
        });
    }

    private boolean validateField(EditText field, boolean announceError) {
        String text = field.getText().toString();
        int message = 0;
        if (text.trim().isEmpty()) {
            message = R.string.missing_field;
        } else if (field == nameInput && !InputValidator.itemNameValid(text)) {
            message = R.string.item_name_error;
        } else if (field == skuInput && !InputValidator.skuValid(text)) {
            message = R.string.sku_error;
        } else if (field == quantityInput && InputValidator.parseQuantity(text) == null) {
            message = R.string.quantity_error;
        } else if (field == restockLevelInput
                && InputValidator.parseQuantity(text) == null) {
            message = R.string.restock_level_error;
        }
        field.setError(message == 0 ? null : getString(message));
        if (message != 0 && announceError) statusText.setText(message);
        if (message == 0 && !anyFieldHasError()) statusText.setText("");
        return message == 0;
    }

    private boolean anyFieldHasError() {
        return nameInput.getError() != null || skuInput.getError() != null
                || quantityInput.getError() != null || restockLevelInput.getError() != null;
    }

    private boolean formIsValid() {
        return InputValidator.itemNameValid(nameInput.getText().toString())
                && InputValidator.skuValid(skuInput.getText().toString())
                && InputValidator.parseQuantity(quantityInput.getText().toString()) != null
                && InputValidator.parseQuantity(restockLevelInput.getText().toString()) != null;
    }

    private void updateAddButtonState() {
        if (addButton == null) return;
        boolean valid = formIsValid();
        addButton.setEnabled(valid);
        addButton.setAlpha(1.0f);
    }

    private void submit() {
        nameInput.setError(null);
        skuInput.setError(null);
        quantityInput.setError(null);
        restockLevelInput.setError(null);
        statusText.setText("");
        String name = nameInput.getText().toString();
        String sku = skuInput.getText().toString();
        Integer quantity = InputValidator.parseQuantity(quantityInput.getText().toString());
        Integer restockLevel = InputValidator.parseQuantity(restockLevelInput.getText().toString());
        if (name.trim().isEmpty()) {
            showError(nameInput, R.string.missing_field);
        } else if (!InputValidator.itemNameValid(name)) {
            showError(nameInput, R.string.item_name_error);
        } else if (sku.trim().isEmpty()) {
            showError(skuInput, R.string.missing_field);
        } else if (!InputValidator.skuValid(sku)) {
            showError(skuInput, R.string.sku_error);
        } else if (quantityInput.getText().toString().trim().isEmpty()) {
            showError(quantityInput, R.string.missing_field);
        } else if (quantity == null) {
            showError(quantityInput, R.string.quantity_error);
        } else if (restockLevelInput.getText().toString().trim().isEmpty()) {
            showError(restockLevelInput, R.string.missing_field);
        } else if (restockLevel == null) {
            showError(restockLevelInput, R.string.restock_level_error);
        } else {
            int error = ((Listener) requireActivity()).addItem(
                    name.trim(), sku.trim(), quantity, restockLevel);
            if (error == 0) dismiss();
            else if (error == R.string.duplicate_sku) showError(skuInput, error);
            else statusText.setText(error);
        }
    }

    private void showError(EditText field, int message) {
        field.setError(getString(message));
        field.requestFocus();
        statusText.setText(message);
        updateAddButtonState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (nameInput != null) {
            state.putString("name", nameInput.getText().toString());
            state.putString("sku", skuInput.getText().toString());
            state.putString("quantity", quantityInput.getText().toString());
            state.putString("restockLevel", restockLevelInput.getText().toString());
        }
    }
}
