package com.example.inventorytracking;

import java.util.Objects;

/** A stored row without any database or screen dependency. */
public final class InventoryItem {
    private final int id;
    private final String name;
    private final String sku;
    private final int quantity;
    private final int restockLevel;

    public InventoryItem(int id, String name, String sku, int quantity, int restockLevel) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.sku = Objects.requireNonNull(sku);
        this.quantity = quantity;
        this.restockLevel = restockLevel;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public int getRestockLevel() { return restockLevel; }
    public int getShortage() { return Math.max(0, restockLevel - quantity); }
    public boolean needsRestocking() { return quantity < restockLevel; }
    public boolean isLowStock() { return quantity > 0 && needsRestocking(); }
    public boolean isOutOfStock() { return quantity == 0; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof InventoryItem)) return false;
        InventoryItem item = (InventoryItem) other;
        return id == item.id && quantity == item.quantity && restockLevel == item.restockLevel
                && name.equals(item.name) && sku.equals(item.sku);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, sku, quantity, restockLevel); }
}
