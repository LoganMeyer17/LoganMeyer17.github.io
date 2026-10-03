package com.example.inventorytracking;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InventoryOrganizerTest {
    private InventoryOrganizer organizer;
    private InventoryItem bolts;
    private InventoryItem boxes;
    private InventoryItem tape;
    private InventoryItem labels;

    @Before
    public void setUp() {
        organizer = new InventoryOrganizer();
        bolts = new InventoryItem(1, "Bolts", "BLT-100", 8, 5);
        boxes = new InventoryItem(2, "Shipping Boxes", "BOX-200", 0, 6);
        tape = new InventoryItem(3, "Packing Tape", "TAPE-300", 2, 10);
        labels = new InventoryItem(4, "Address Labels", "LBL-400", 4, 4);
        organizer.replaceAll(Arrays.asList(bolts, boxes, tape, labels));
    }

    @Test
    public void exactSkuLookupUsesSkuIndex() {
        assertEquals(tape, organizer.findBySku("TAPE-300"));
        assertNull(organizer.findBySku("missing"));

        List<InventoryItem> result = organizer.visibleItems("TAPE-300",
                InventoryOrganizer.StockFilter.ALL, InventoryOrganizer.SortOption.NAME);
        assertEquals(1, result.size());
        assertEquals(tape, result.get(0));
    }

    @Test
    public void partialSearchChecksNamesAndSkusWithoutMatchingCase() {
        assertEquals(Arrays.asList(tape), visible("packing"));
        assertEquals(Arrays.asList(boxes), visible("box-2"));
        assertEquals(Arrays.asList(labels), visible("LABEL"));
    }

    @Test
    public void stockFiltersSeparateLowAndOutOfStockItems() {
        List<InventoryItem> low = organizer.visibleItems("",
                InventoryOrganizer.StockFilter.LOW_STOCK,
                InventoryOrganizer.SortOption.RESTOCK_PRIORITY);
        assertEquals(Arrays.asList(tape), low);

        List<InventoryItem> out = organizer.visibleItems("",
                InventoryOrganizer.StockFilter.OUT_OF_STOCK,
                InventoryOrganizer.SortOption.NAME);
        assertEquals(Arrays.asList(boxes), out);
    }

    @Test
    public void sortingUsesAtoZLowQuantityAndGreatestShortage() {
        assertEquals(Arrays.asList(labels, bolts, tape, boxes), organizer.visibleItems("",
                InventoryOrganizer.StockFilter.ALL, InventoryOrganizer.SortOption.NAME));
        assertEquals(Arrays.asList(bolts, boxes, labels, tape), organizer.visibleItems("",
                InventoryOrganizer.StockFilter.ALL, InventoryOrganizer.SortOption.SKU));
        assertEquals(Arrays.asList(boxes, tape, labels, bolts), organizer.visibleItems("",
                InventoryOrganizer.StockFilter.ALL, InventoryOrganizer.SortOption.QUANTITY));
        assertEquals(Arrays.asList(tape, boxes, labels, bolts), organizer.visibleItems("",
                InventoryOrganizer.StockFilter.ALL,
                InventoryOrganizer.SortOption.RESTOCK_PRIORITY));
    }

    @Test
    public void priorityQueueIncludesOnlyItemsBelowRestockLevel() {
        assertEquals(Arrays.asList(tape, boxes), organizer.restockPriority());
        assertFalse(labels.needsRestocking());
        assertEquals(0, labels.getShortage());
    }

    @Test
    public void replacingAndRemovingItemsRebuildsAllStructures() {
        InventoryItem restockedTape = new InventoryItem(3, "Packing Tape", "TAPE-300", 10, 10);
        assertTrue(organizer.replace(restockedTape));
        assertEquals(restockedTape, organizer.findBySku("TAPE-300"));
        assertEquals(Arrays.asList(boxes), organizer.restockPriority());

        assertTrue(organizer.remove(boxes.getId()));
        assertNull(organizer.findBySku("BOX-200"));
        assertTrue(organizer.restockPriority().isEmpty());
        assertEquals(3, organizer.size());
    }

    private List<InventoryItem> visible(String search) {
        return organizer.visibleItems(search, InventoryOrganizer.StockFilter.ALL,
                InventoryOrganizer.SortOption.NAME);
    }
}
