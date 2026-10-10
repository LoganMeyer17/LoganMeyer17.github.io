package com.example.inventorytracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/** Reuses the original stacked row layout instead of rebuilding every visible control. */
public class InventoryAdapter extends ListAdapter<InventoryItem, InventoryAdapter.ItemHolder> {
    public interface Listener {
        void changeQuantity(InventoryItem item, int delta);
        void deleteItem(InventoryItem item);
    }
    private final Listener listener;

    public InventoryAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<InventoryItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
                return oldItem.getId() == newItem.getId();
            }
            @Override
            public boolean areContentsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    @NonNull @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private final TextView name, sku, quantity, restock;
        private final Button plus, minus, delete;

        ItemHolder(View view) {
            super(view);
            name = view.findViewById(R.id.rowItemName);
            sku = view.findViewById(R.id.rowSku);
            quantity = view.findViewById(R.id.rowQuantity);
            restock = view.findViewById(R.id.rowRestock);
            plus = view.findViewById(R.id.plusButton);
            minus = view.findViewById(R.id.minusButton);
            delete = view.findViewById(R.id.deleteButton);
            plus.setOnClickListener(v -> adjust(1));
            minus.setOnClickListener(v -> adjust(-1));
            delete.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) listener.deleteItem(getItem(position));
            });
        }

        private void adjust(int delta) {
            // Resolve the current row, rather than capturing its quantity in an old listener.
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) listener.changeQuantity(getItem(position), delta);
        }

        void bind(InventoryItem item) {
            name.setText(item.getName());
            sku.setText(itemView.getContext().getString(R.string.sku_format, item.getSku()));
            quantity.setText(itemView.getContext().getString(R.string.quantity_format, item.getQuantity()));
            int status = item.isOutOfStock() ? R.string.stock_out
                    : item.isLowStock() ? R.string.stock_low : R.string.stock_in;
            restock.setText(status);
            plus.setEnabled(item.getQuantity() < Integer.MAX_VALUE);
            minus.setEnabled(item.getQuantity() > 0);
            plus.setContentDescription(itemView.getContext().getString(R.string.increase_quantity_description, item.getName()));
            minus.setContentDescription(itemView.getContext().getString(R.string.decrease_quantity_description, item.getName()));
            delete.setContentDescription(itemView.getContext().getString(R.string.delete_item_description, item.getName()));
        }
    }
}
