package com.example.inventorytracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory_app.db";
    private static final int DATABASE_VERSION = 1;

    // users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";

    // items table
    private static final String TABLE_ITEMS = "items";
    private static final String COL_ID = "id";
    private static final String COL_ITEM_NAME = "item_name";
    private static final String COL_SKU = "sku";
    private static final String COL_QUANTITY = "quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // create users
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_PASSWORD + " TEXT)";

        // create items
        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_NAME + " TEXT NOT NULL, " +
                COL_SKU + " TEXT NOT NULL, " +
                COL_QUANTITY + " INTEGER NOT NULL)";

        db.execSQL(createUsersTable);
        db.execSQL(createItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // drop tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);

        // recreate
        onCreate(db);
    }

    // user create
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (userExists(username)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // user exists
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?",
                new String[]{username}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // check login
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}
        );

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    // add item
    public boolean addItem(String itemName, String sku, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ITEM_NAME, itemName);
        values.put(COL_SKU, sku);
        values.put(COL_QUANTITY, quantity);

        long result = db.insert(TABLE_ITEMS, null, values);
        return result != -1;
    }

    // sku exists
    public boolean skuExists(String sku) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COL_SKU + "=?",
                new String[]{sku}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // get items
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COL_ID + " DESC", null);
    }

    // update qty
    public void updateQuantity(int id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_QUANTITY, newQuantity);

        db.update(TABLE_ITEMS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // delete item
    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}