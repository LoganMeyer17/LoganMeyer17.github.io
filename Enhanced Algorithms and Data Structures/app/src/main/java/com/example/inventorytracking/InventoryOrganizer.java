package com.example.inventorytracking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

/** Maintains the in-memory structures used to search, filter, and order inventory. */
public final class InventoryOrganizer {
    public enum StockFilter { ALL, LOW_STOCK, OUT_OF_STOCK }
    public enum SortOption { NAME, SKU, QUANTITY, RESTOCK_PRIORITY }

    private static final Comparator<InventoryItem> NAME_ORDER =
            Comparator.comparing(InventoryItem::getName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(InventoryItem::getSku, String.CASE_INSENSITIVE_ORDER);
    private static final Comparator<InventoryItem> SKU_ORDER =
            Comparator.comparing(InventoryItem::getSku, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(InventoryItem::getName, String.CASE_INSENSITIVE_ORDER);
    private static final Comparator<InventoryItem> QUANTITY_ORDER =
            Comparator.comparingInt(InventoryItem::getQuantity).thenComparing(NAME_ORDER);
    private static final Comparator<InventoryItem> RESTOCK_ORDER =
            Comparator.comparingInt(InventoryItem::getShortage).reversed().thenComparing(NAME_ORDER);

    private final List<InventoryItem> items = new ArrayList<>();
    private final Map<String, InventoryItem> itemsBySku = new HashMap<>();
    private final PriorityQueue<InventoryItem> restockQueue = new PriorityQueue<>(RESTOCK_ORDER);

    public void replaceAll(List<InventoryItem> replacement) {
        items.clear();
        items.addAll(replacement);
        rebuildIndexes();
    }

    public void add(InventoryItem item) {
        items.add(0, item);
        rebuildIndexes();
    }

    public boolean replace(InventoryItem replacement) {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).getId() == replacement.getId()) {
                items.set(index, replacement);
                rebuildIndexes();
                return true;
            }
        }
        return false;
    }

    public boolean remove(int id) {
        boolean removed = items.removeIf(item -> item.getId() == id);
        if (removed) rebuildIndexes();
        return removed;
    }

    public InventoryItem findBySku(String sku) {
        return sku == null ? null : itemsBySku.get(sku.trim());
    }

    public List<InventoryItem> visibleItems(String searchText, StockFilter filter, SortOption sort) {
        String rawSearch = searchText == null ? "" : searchText.trim();
        List<InventoryItem> matches = new ArrayList<>();

        InventoryItem exactSku = rawSearch.isEmpty() ? null : itemsBySku.get(rawSearch);
        if (exactSku != null) {
            if (matchesFilter(exactSku, filter)) matches.add(exactSku);
        } else {
            String normalizedSearch = rawSearch.toLowerCase(Locale.ROOT);
            for (InventoryItem item : items) {
                if (!matchesFilter(item, filter)) continue;
                if (normalizedSearch.isEmpty()
                        || item.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
                        || item.getSku().toLowerCase(Locale.ROOT).contains(normalizedSearch)) {
                    matches.add(item);
                }
            }
        }

        matches.sort(comparatorFor(sort));
        return matches;
    }

    public List<InventoryItem> restockPriority() {
        PriorityQueue<InventoryItem> copy = new PriorityQueue<>(restockQueue);
        List<InventoryItem> ordered = new ArrayList<>();
        while (!copy.isEmpty()) ordered.add(copy.remove());
        return ordered;
    }

    public int size() {
        return items.size();
    }

    private void rebuildIndexes() {
        itemsBySku.clear();
        restockQueue.clear();
        for (InventoryItem item : items) {
            itemsBySku.put(item.getSku(), item);
            if (item.needsRestocking()) restockQueue.add(item);
        }
    }

    private boolean matchesFilter(InventoryItem item, StockFilter filter) {
        if (filter == StockFilter.LOW_STOCK) return item.isLowStock();
        if (filter == StockFilter.OUT_OF_STOCK) return item.isOutOfStock();
        return true;
    }

    private Comparator<InventoryItem> comparatorFor(SortOption sort) {
        if (sort == SortOption.SKU) return SKU_ORDER;
        if (sort == SortOption.QUANTITY) return QUANTITY_ORDER;
        if (sort == SortOption.RESTOCK_PRIORITY) return RESTOCK_ORDER;
        return NAME_ORDER;
    }
}
