package com.example.inventorytracking;

import java.util.Objects;

/** A user-scoped audit record returned by the database layer. */
public final class InventoryHistoryEntry {
    public enum Action {
        CREATE,
        QUANTITY_CHANGE,
        DELETE,
        LEGACY_CLAIM
    }

    private final long id;
    private final Integer itemId;
    private final String itemName;
    private final String sku;
    private final Action action;
    private final Integer quantityBefore;
    private final Integer quantityAfter;
    private final long createdAtSeconds;

    public InventoryHistoryEntry(long id, Integer itemId, String itemName, String sku,
            Action action, Integer quantityBefore, Integer quantityAfter,
            long createdAtSeconds) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = Objects.requireNonNull(itemName);
        this.sku = Objects.requireNonNull(sku);
        this.action = Objects.requireNonNull(action);
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.createdAtSeconds = createdAtSeconds;
    }

    public long getId() { return id; }
    public Integer getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getSku() { return sku; }
    public Action getAction() { return action; }
    public Integer getQuantityBefore() { return quantityBefore; }
    public Integer getQuantityAfter() { return quantityAfter; }
    public long getCreatedAtSeconds() { return createdAtSeconds; }
}
