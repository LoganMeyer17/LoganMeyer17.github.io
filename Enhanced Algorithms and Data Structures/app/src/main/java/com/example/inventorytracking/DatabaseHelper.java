package com.example.inventorytracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Owns the versioned schema, migrations, authentication, and user-scoped inventory writes. */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory_app.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_PASSWORD_HASH = "password_hash";
    private static final String COL_PASSWORD_SALT = "password_salt";

    private static final String TABLE_ITEMS = "items";
    private static final String COL_ID = "id";
    private static final String COL_OWNER_USERNAME = "owner_username";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_SKU = "sku";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_RESTOCK_LEVEL = "restock_level";
    private static final String[] ITEM_COLUMNS = {COL_ID, COL_ITEM_NAME, COL_SKU,
            COL_QUANTITY, COL_RESTOCK_LEVEL};

    private static final String TABLE_HISTORY = "inventory_history";
    private static final String COL_ITEM_ID = "item_id";
    private static final String COL_ACTION = "action";
    private static final String COL_QUANTITY_BEFORE = "quantity_before";
    private static final String COL_QUANTITY_AFTER = "quantity_after";
    private static final String COL_CREATED_AT = "created_at";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_QUANTITY_CHANGE = "QUANTITY_CHANGE";
    private static final String ACTION_DELETE = "DELETE";
    private static final String ACTION_LEGACY_CLAIM = "LEGACY_CLAIM";

    public DatabaseHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    // Tests use a different filename so they never clear the user's inventory.
    DatabaseHelper(Context context, String databaseName) {
        super(context.getApplicationContext(), databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createVersionThreeSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradedVersion = oldVersion;
        if (upgradedVersion == 1 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COL_RESTOCK_LEVEL
                    + " INTEGER NOT NULL DEFAULT 0 CHECK (" + COL_RESTOCK_LEVEL + " >= 0)");
            upgradedVersion = 2;
        }
        if (upgradedVersion == 2 && newVersion >= 3) {
            migrateVersionTwoToThree(db);
            upgradedVersion = 3;
        }
        if (upgradedVersion != newVersion) {
            throw new SQLiteException("A migration is required from version "
                    + upgradedVersion + " to " + newVersion);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("Downgrading from version " + oldVersion
                + " to " + newVersion + " is not supported.");
    }

    private void createVersionThreeSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" + COL_USERNAME
                + " TEXT PRIMARY KEY, " + COL_PASSWORD_HASH + " TEXT NOT NULL, "
                + COL_PASSWORD_SALT + " TEXT NOT NULL)");
        createItemsTable(db, TABLE_ITEMS, TABLE_USERS);
        createHistoryTable(db);
        createIndexes(db);
    }

    private void createItemsTable(SQLiteDatabase db, String tableName, String usersTable) {
        db.execSQL("CREATE TABLE " + tableName + " (" + COL_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_OWNER_USERNAME
                + " TEXT, " + COL_ITEM_NAME + " TEXT NOT NULL, " + COL_SKU
                + " TEXT NOT NULL, " + COL_QUANTITY
                + " INTEGER NOT NULL CHECK (" + COL_QUANTITY + " >= 0), "
                + COL_RESTOCK_LEVEL + " INTEGER NOT NULL DEFAULT 0 CHECK ("
                + COL_RESTOCK_LEVEL + " >= 0), FOREIGN KEY (" + COL_OWNER_USERNAME
                + ") REFERENCES " + usersTable + "(" + COL_USERNAME
                + ") ON UPDATE CASCADE ON DELETE CASCADE, UNIQUE (" + COL_OWNER_USERNAME
                + ", " + COL_SKU + "))");
    }

    private void createHistoryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (" + COL_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_USERNAME
                + " TEXT NOT NULL, " + COL_ITEM_ID + " INTEGER, " + COL_ITEM_NAME
                + " TEXT NOT NULL, " + COL_SKU + " TEXT NOT NULL, " + COL_ACTION
                + " TEXT NOT NULL CHECK (" + COL_ACTION + " IN ('" + ACTION_CREATE
                + "', '" + ACTION_QUANTITY_CHANGE + "', '" + ACTION_DELETE + "', '"
                + ACTION_LEGACY_CLAIM + "')), " + COL_QUANTITY_BEFORE + " INTEGER, "
                + COL_QUANTITY_AFTER + " INTEGER, " + COL_CREATED_AT
                + " INTEGER NOT NULL DEFAULT (unixepoch()), FOREIGN KEY (" + COL_USERNAME
                + ") REFERENCES " + TABLE_USERS + "(" + COL_USERNAME
                + ") ON UPDATE CASCADE ON DELETE CASCADE)");
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX index_items_owner ON " + TABLE_ITEMS
                + "(" + COL_OWNER_USERNAME + ")");
        db.execSQL("CREATE INDEX index_history_user_created ON " + TABLE_HISTORY
                + "(" + COL_USERNAME + ", " + COL_CREATED_AT + ")");
    }

    /**
     * Rebuilds both tables so plaintext credentials are removed and ownership constraints
     * can be enforced. Legacy inventory is kept unassigned until a valid user first signs in.
     */
    private void migrateVersionTwoToThree(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users_v3 (" + COL_USERNAME + " TEXT PRIMARY KEY, "
                + COL_PASSWORD_HASH + " TEXT NOT NULL, " + COL_PASSWORD_SALT
                + " TEXT NOT NULL)");
        try (Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USERNAME, COL_PASSWORD}, null, null,
                null, null, null)) {
            while (cursor.moveToNext()) {
                PasswordHasher.PasswordRecord password = PasswordHasher.hash(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)));
                ContentValues values = new ContentValues();
                values.put(COL_USERNAME,
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)));
                values.put(COL_PASSWORD_HASH, password.getHash());
                values.put(COL_PASSWORD_SALT, password.getSalt());
                db.insertOrThrow("users_v3", null, values);
            }
        }

        createItemsTable(db, "items_v3", "users_v3");
        db.execSQL("INSERT INTO items_v3 (" + COL_ID + ", " + COL_ITEM_NAME + ", "
                + COL_SKU + ", " + COL_QUANTITY + ", " + COL_RESTOCK_LEVEL
                + ") SELECT " + COL_ID + ", " + COL_ITEM_NAME + ", " + COL_SKU + ", "
                + COL_QUANTITY + ", " + COL_RESTOCK_LEVEL + " FROM " + TABLE_ITEMS);

        db.execSQL("DROP TABLE " + TABLE_ITEMS);
        db.execSQL("DROP TABLE " + TABLE_USERS);
        db.execSQL("ALTER TABLE users_v3 RENAME TO " + TABLE_USERS);
        db.execSQL("ALTER TABLE items_v3 RENAME TO " + TABLE_ITEMS);
        createHistoryTable(db);
        createIndexes(db);
    }

    /** Returns false only for a duplicate username; storage failures remain exceptions. */
    public boolean createUser(String username, String password) {
        PasswordHasher.PasswordRecord passwordRecord = PasswordHasher.hash(password);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (userExists(db, username)) return false;
            ContentValues values = new ContentValues();
            values.put(COL_USERNAME, username);
            values.put(COL_PASSWORD_HASH, passwordRecord.getHash());
            values.put(COL_PASSWORD_SALT, passwordRecord.getSalt());
            db.insertOrThrow(TABLE_USERS, null, values);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean userExists(String username) {
        return userExists(getReadableDatabase(), username);
    }

    private boolean userExists(SQLiteDatabase db, String username) {
        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USERNAME},
                COL_USERNAME + "=?", new String[]{username}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    /** Verifies the salted hash, then claims preserved unowned records in the same transaction. */
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            String hash;
            String salt;
            try (Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_PASSWORD_HASH, COL_PASSWORD_SALT},
                    COL_USERNAME + "=?", new String[]{username}, null, null, null, "1")) {
                if (!cursor.moveToFirst()) return false;
                hash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH));
                salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_SALT));
            }
            if (!PasswordHasher.verify(password, hash, salt)) return false;
            claimLegacyItems(db, username);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private void claimLegacyItems(SQLiteDatabase db, String username) {
        try (Cursor cursor = db.query(TABLE_ITEMS, ITEM_COLUMNS,
                COL_OWNER_USERNAME + " IS NULL", null, null, null, COL_ID)) {
            while (cursor.moveToNext()) {
                InventoryItem item = readItem(cursor);
                ContentValues owner = new ContentValues();
                owner.put(COL_OWNER_USERNAME, username);
                int affected = db.update(TABLE_ITEMS, owner,
                        COL_ID + "=? AND " + COL_OWNER_USERNAME + " IS NULL",
                        new String[]{Integer.toString(item.getId())});
                if (affected != 1) {
                    throw new SQLiteException("Legacy inventory could not be assigned safely.");
                }
                insertHistory(db, username, item, ACTION_LEGACY_CLAIM,
                        item.getQuantity(), item.getQuantity());
            }
        }
    }

    public List<InventoryItem> getItems(String username) {
        List<InventoryItem> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_ITEMS, ITEM_COLUMNS,
                COL_OWNER_USERNAME + "=?", new String[]{username}, null, null,
                COL_ID + " DESC")) {
            while (cursor.moveToNext()) items.add(readItem(cursor));
        }
        return items;
    }

    /** Returns null only when the same user already owns the supplied SKU. */
    public InventoryItem addItem(String username, String name, String sku,
            int quantity, int restockLevel) {
        if (quantity < 0 || restockLevel < 0) {
            throw new IllegalArgumentException("Quantity and restock level cannot be negative.");
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.query(TABLE_ITEMS, new String[]{COL_ID},
                    COL_OWNER_USERNAME + "=? AND " + COL_SKU + "=?",
                    new String[]{username, sku}, null, null, null, "1")) {
                if (cursor.moveToFirst()) return null;
            }
            ContentValues values = new ContentValues();
            values.put(COL_OWNER_USERNAME, username);
            values.put(COL_ITEM_NAME, name);
            values.put(COL_SKU, sku);
            values.put(COL_QUANTITY, quantity);
            values.put(COL_RESTOCK_LEVEL, restockLevel);
            long insertedId;
            try {
                insertedId = db.insertOrThrow(TABLE_ITEMS, null, values);
            } catch (SQLiteConstraintException exception) {
                if (skuExistsForUser(db, username, sku)) return null;
                throw exception;
            }
            if (insertedId < 1 || insertedId > Integer.MAX_VALUE) {
                throw new SQLiteException("The item could not be assigned a supported ID.");
            }
            InventoryItem item = new InventoryItem((int) insertedId, name, sku,
                    quantity, restockLevel);
            insertHistory(db, username, item, ACTION_CREATE, null, quantity);
            db.setTransactionSuccessful();
            return item;
        } finally {
            db.endTransaction();
        }
    }

    private boolean skuExistsForUser(SQLiteDatabase db, String username, String sku) {
        try (Cursor cursor = db.query(TABLE_ITEMS, new String[]{COL_ID},
                COL_OWNER_USERNAME + "=? AND " + COL_SKU + "=?",
                new String[]{username, sku}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    /** Returns null when the item does not belong to the signed-in user. */
    public InventoryItem changeQuantity(String username, int id, int delta) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            InventoryItem current = findOwnedItem(db, username, id);
            if (current == null) return null;
            Integer quantity = InputValidator.adjustedQuantity(current.getQuantity(), delta);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity must be between 0 and "
                        + Integer.MAX_VALUE);
            }
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, quantity);
            int affected = db.update(TABLE_ITEMS, values,
                    COL_ID + "=? AND " + COL_OWNER_USERNAME + "=?",
                    new String[]{Integer.toString(id), username});
            if (affected != 1) throw new SQLiteException("The quantity could not be saved.");
            InventoryItem updated = new InventoryItem(id, current.getName(), current.getSku(),
                    quantity, current.getRestockLevel());
            insertHistory(db, username, updated, ACTION_QUANTITY_CHANGE,
                    current.getQuantity(), quantity);
            db.setTransactionSuccessful();
            return updated;
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteItem(String username, int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            InventoryItem current = findOwnedItem(db, username, id);
            if (current == null) return false;
            int affected = db.delete(TABLE_ITEMS,
                    COL_ID + "=? AND " + COL_OWNER_USERNAME + "=?",
                    new String[]{Integer.toString(id), username});
            if (affected != 1) throw new SQLiteException("The item could not be deleted.");
            insertHistory(db, username, current, ACTION_DELETE, current.getQuantity(), null);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private InventoryItem findOwnedItem(SQLiteDatabase db, String username, int id) {
        try (Cursor cursor = db.query(TABLE_ITEMS, ITEM_COLUMNS,
                COL_ID + "=? AND " + COL_OWNER_USERNAME + "=?",
                new String[]{Integer.toString(id), username}, null, null, null, "1")) {
            return cursor.moveToFirst() ? readItem(cursor) : null;
        }
    }

    private void insertHistory(SQLiteDatabase db, String username, InventoryItem item,
            String action, Integer quantityBefore, Integer quantityAfter) {
        ContentValues history = new ContentValues();
        history.put(COL_USERNAME, username);
        history.put(COL_ITEM_ID, item.getId());
        history.put(COL_ITEM_NAME, item.getName());
        history.put(COL_SKU, item.getSku());
        history.put(COL_ACTION, action);
        if (quantityBefore == null) history.putNull(COL_QUANTITY_BEFORE);
        else history.put(COL_QUANTITY_BEFORE, quantityBefore);
        if (quantityAfter == null) history.putNull(COL_QUANTITY_AFTER);
        else history.put(COL_QUANTITY_AFTER, quantityAfter);
        db.insertOrThrow(TABLE_HISTORY, null, history);
    }

    private InventoryItem readItem(Cursor cursor) {
        return new InventoryItem(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_SKU)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_RESTOCK_LEVEL)));
    }
}
