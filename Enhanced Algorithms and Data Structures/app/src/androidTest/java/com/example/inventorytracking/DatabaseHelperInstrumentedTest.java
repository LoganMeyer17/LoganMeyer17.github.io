package com.example.inventorytracking;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/** Uses real SQLite in a temporary file; never opens the user's inventory_app.db. */
@RunWith(AndroidJUnit4.class)
public class DatabaseHelperInstrumentedTest {
    private Context context;
    private String databaseName;
    private DatabaseHelper helper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue("Run the Milestone Four debug build", context.getPackageName()
                .endsWith(".milestone4database"));
        databaseName = "database_test_" + UUID.randomUUID() + ".db";
        helper = new DatabaseHelper(context, databaseName);
    }

    @After
    public void tearDown() {
        if (helper != null) helper.close();
        if (databaseName != null) context.deleteDatabase(databaseName);
    }

    @Test
    public void passwordsAreSaltedHashedAndNeverStoredAsPlaintext() {
        assertTrue(helper.createUser("review_user", "SharedPass1!"));
        assertTrue(helper.createUser("second_user", "SharedPass1!"));
        assertTrue(helper.checkLogin("review_user", "SharedPass1!"));
        assertFalse(helper.checkLogin("review_user", "WrongPass1!"));

        SQLiteDatabase db = helper.getReadableDatabase();
        String firstHash;
        String firstSalt;
        try (Cursor cursor = db.query("users",
                new String[]{"password_hash", "password_salt"}, "username=?",
                new String[]{"review_user"}, null, null, null)) {
            assertTrue(cursor.moveToFirst());
            firstHash = cursor.getString(0);
            firstSalt = cursor.getString(1);
        }
        try (Cursor cursor = db.query("users",
                new String[]{"password_hash", "password_salt"}, "username=?",
                new String[]{"second_user"}, null, null, null)) {
            assertTrue(cursor.moveToFirst());
            assertNotEquals("SharedPass1!", firstHash);
            assertNotEquals("SharedPass1!", firstSalt);
            assertNotEquals(firstHash, cursor.getString(0));
            assertNotEquals(firstSalt, cursor.getString(1));
        }
        assertFalse(tableHasColumn(db, "users", "password"));
    }

    @Test
    public void duplicateUsernameAndSqlCharactersDoNotBypassAuthentication() {
        assertTrue(helper.createUser("review_user", "A'quoted_password"));
        assertFalse(helper.createUser("review_user", "DifferentPass2!"));
        assertTrue(helper.checkLogin("review_user", "A'quoted_password"));
        assertFalse(helper.checkLogin("review_user", "DifferentPass2!"));
        assertFalse(helper.checkLogin("' OR 1=1 --", "anything"));
        assertFalse(helper.checkLogin("review_user", "' OR 1=1 --"));
    }

    @Test
    public void usersCannotReadChangeOrDeleteAnotherUsersInventory() {
        assertTrue(helper.createUser("owner_a", "OwnerPass1!"));
        assertTrue(helper.createUser("owner_b", "OwnerPass2!"));
        InventoryItem first = helper.addItem(
                "owner_a", "Packing tape", "TAPE-001", 2, 5);
        assertNotNull(first);

        assertEquals(1, helper.getItems("owner_a").size());
        assertTrue(helper.getItems("owner_b").isEmpty());
        assertNull(helper.changeQuantity("owner_b", first.getId(), 1));
        assertFalse(helper.deleteItem("owner_b", first.getId()));
        assertEquals(2, helper.getItems("owner_a").get(0).getQuantity());

        // An SKU must be unique inside one account, but different users may reuse it.
        assertNull(helper.addItem("owner_a", "Duplicate", "TAPE-001", 20, 25));
        assertNotNull(helper.addItem("owner_b", "My tape", "TAPE-001", 7, 10));
        assertEquals(1, helper.getItems("owner_a").size());
        assertEquals(1, helper.getItems("owner_b").size());

        assertEquals(3,
                helper.changeQuantity("owner_a", first.getId(), 1).getQuantity());
        assertTrue(helper.deleteItem("owner_a", first.getId()));
        assertTrue(helper.getItems("owner_a").isEmpty());
    }

    @Test
    public void constraintsAndTransactionsRejectInvalidOrPartialWrites() {
        assertTrue(helper.createUser("owner_a", "OwnerPass1!"));
        InventoryItem item = helper.addItem(
                "owner_a", "Packing tape", "TAPE-001", 3, 5);
        assertNotNull(item);
        SQLiteDatabase db = helper.getWritableDatabase();

        assertThrows(SQLiteConstraintException.class, () -> db.execSQL(
                "INSERT INTO items (owner_username, item_name, sku, quantity, restock_level) "
                        + "VALUES ('missing_user', 'Invalid', 'BAD-001', 1, 1)"));
        assertThrows(SQLiteConstraintException.class, () -> db.execSQL(
                "INSERT INTO items (owner_username, item_name, sku, quantity, restock_level) "
                        + "VALUES ('owner_a', 'Invalid', 'BAD-002', -1, 1)"));

        db.execSQL("CREATE TRIGGER reject_history BEFORE INSERT ON inventory_history "
                + "BEGIN SELECT RAISE(ABORT, 'test history failure'); END");
        assertThrows(SQLiteException.class,
                () -> helper.changeQuantity("owner_a", item.getId(), 1));
        assertEquals(3, helper.getItems("owner_a").get(0).getQuantity());

        db.execSQL("DROP TRIGGER reject_history");
        assertEquals(4,
                helper.changeQuantity("owner_a", item.getId(), 1).getQuantity());
    }

    @Test
    public void createChangeAndDeleteWriteACompleteAuditHistory() {
        assertTrue(helper.createUser("history_user", "HistoryPass1!"));
        InventoryItem item = helper.addItem(
                "history_user", "Shipping boxes", "BOX-001", 4, 8);
        assertNotNull(item);
        assertNotNull(helper.changeQuantity("history_user", item.getId(), 2));
        assertTrue(helper.deleteItem("history_user", item.getId()));

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT action, quantity_before, quantity_after FROM inventory_history "
                        + "WHERE username=? ORDER BY id", new String[]{"history_user"})) {
            assertEquals(3, cursor.getCount());
            assertTrue(cursor.moveToNext());
            assertEquals("CREATE", cursor.getString(0));
            assertTrue(cursor.isNull(1));
            assertEquals(4, cursor.getInt(2));
            assertTrue(cursor.moveToNext());
            assertEquals("QUANTITY_CHANGE", cursor.getString(0));
            assertEquals(4, cursor.getInt(1));
            assertEquals(6, cursor.getInt(2));
            assertTrue(cursor.moveToNext());
            assertEquals("DELETE", cursor.getString(0));
            assertEquals(6, cursor.getInt(1));
            assertTrue(cursor.isNull(2));
        }
    }

    @Test
    public void versionOneRecordsMigrateWithoutDataLossOrPlaintextPasswords() {
        try (SQLiteDatabase original = context.openOrCreateDatabase(databaseName,
                Context.MODE_PRIVATE, null)) {
            original.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)");
            original.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "item_name TEXT NOT NULL, sku TEXT NOT NULL, quantity INTEGER NOT NULL)");
            original.execSQL("INSERT INTO users (username, password) VALUES (?, ?)",
                    new Object[]{"legacy_user", "OldPassword1!"});
            original.execSQL("INSERT INTO items (item_name, sku, quantity) VALUES (?, ?, ?)",
                    new Object[]{"Original item", "OLD-001", 7});
            original.setVersion(1);
        }

        assertFalse(helper.checkLogin("legacy_user", "wrong"));
        assertTrue(helper.getItems("legacy_user").isEmpty());
        assertTrue(helper.checkLogin("legacy_user", "OldPassword1!"));
        List<InventoryItem> migrated = helper.getItems("legacy_user");
        assertEquals(1, migrated.size());
        assertEquals("Original item", migrated.get(0).getName());
        assertEquals(7, migrated.get(0).getQuantity());
        assertEquals(0, migrated.get(0).getRestockLevel());
        assertEquals(3, helper.getReadableDatabase().getVersion());
        assertFalse(tableHasColumn(helper.getReadableDatabase(), "users", "password"));

        helper.changeQuantity("legacy_user", migrated.get(0).getId(), 1);
        helper.close();
        helper = new DatabaseHelper(context, databaseName);
        assertTrue(helper.checkLogin("legacy_user", "OldPassword1!"));
        assertEquals(8, helper.getItems("legacy_user").get(0).getQuantity());
    }

    @Test
    public void validFirstLoginClaimsLegacyInventoryWithoutCrossingAccounts() {
        try (SQLiteDatabase versionTwo = context.openOrCreateDatabase(databaseName,
                Context.MODE_PRIVATE, null)) {
            versionTwo.execSQL("CREATE TABLE users (username TEXT PRIMARY KEY, password TEXT)");
            versionTwo.execSQL("CREATE TABLE items (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "item_name TEXT NOT NULL, sku TEXT NOT NULL, quantity INTEGER NOT NULL, "
                    + "restock_level INTEGER NOT NULL DEFAULT 0 CHECK (restock_level >= 0))");
            versionTwo.execSQL("INSERT INTO users VALUES ('owner_a', 'OwnerPass1!')");
            versionTwo.execSQL("INSERT INTO users VALUES ('owner_b', 'OwnerPass2!')");
            versionTwo.execSQL("INSERT INTO items (item_name, sku, quantity, restock_level) "
                    + "VALUES ('Legacy stock', 'LEG-001', 9, 4)");
            versionTwo.setVersion(2);
        }

        assertFalse(helper.checkLogin("owner_a", "wrong"));
        assertTrue(helper.getItems("owner_a").isEmpty());
        assertTrue(helper.getItems("owner_b").isEmpty());
        assertTrue(helper.checkLogin("owner_b", "OwnerPass2!"));
        assertEquals(1, helper.getItems("owner_b").size());
        assertTrue(helper.getItems("owner_a").isEmpty());
        assertTrue(helper.checkLogin("owner_a", "OwnerPass1!"));
        assertTrue(helper.getItems("owner_a").isEmpty());

        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT action FROM inventory_history WHERE username=?",
                new String[]{"owner_b"})) {
            assertTrue(cursor.moveToFirst());
            assertEquals("LEGACY_CLAIM", cursor.getString(0));
        }
    }

    @Test
    public void quantitiesCannotGoBelowZeroOrOverflow() {
        assertTrue(helper.createUser("owner_a", "OwnerPass1!"));
        InventoryItem item = helper.addItem(
                "owner_a", "Empty stock", "ZERO-001", 0, 3);
        assertNotNull(item);
        assertThrows(IllegalArgumentException.class,
                () -> helper.changeQuantity("owner_a", item.getId(), -1));
        assertEquals(0, helper.getItems("owner_a").get(0).getQuantity());

        InventoryItem maximum = helper.addItem("owner_a", "Maximum stock", "MAX-001",
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(maximum);
        assertThrows(IllegalArgumentException.class,
                () -> helper.changeQuantity("owner_a", maximum.getId(), 1));
    }

    private boolean tableHasColumn(SQLiteDatabase db, String table, String column) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                if (column.equals(cursor.getString(nameIndex))) return true;
            }
        }
        return false;
    }
}
