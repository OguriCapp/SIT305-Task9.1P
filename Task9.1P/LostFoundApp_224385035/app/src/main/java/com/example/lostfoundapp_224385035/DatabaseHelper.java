package com.example.lostfoundapp_224385035;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Manage item database
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database constants
    private static final String DATABASE_NAME = "LostFoundDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_LOCATION = "location";
    // Location columns
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table info
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades
        if (oldVersion < 2) {
            // Add location columns
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LATITUDE + " REAL DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL DEFAULT 0");
            } catch (Exception e) {
                // Recreate table on failure
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            }
        }
    }

    // Insert basic item
    public long insertItem(String type, String name, String phone, String description, String date, String location) {
        return insertItem(type, name, phone, description, date, location, 0, 0);
    }

    // Insert item with location
    public long insertItem(String type, String name, String phone, String description, String date, String location, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        return db.insert(TABLE_NAME, null, values);
    }

    // Get all items
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // Delete item
    public int deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Get items with location data
    public List<Map<String, Object>> getAllItemsWithLocation() {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            // Check column existence
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null);
            boolean hasLatitude = false;
            boolean hasLongitude = false;
            
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        String columnName = cursor.getString(nameIndex);
                        if (COLUMN_LATITUDE.equals(columnName)) {
                            hasLatitude = true;
                        } else if (COLUMN_LONGITUDE.equals(columnName)) {
                            hasLongitude = true;
                        }
                    }
                }
                cursor.close();
            }
            
            // Return empty list if missing columns
            if (!hasLatitude || !hasLongitude) {
                return itemsList;
            }
            
            // Query location items
            Cursor itemCursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + 
                    " WHERE " + COLUMN_LATITUDE + " != 0 AND " + 
                    COLUMN_LONGITUDE + " != 0", null);

            if (itemCursor.moveToFirst()) {
                do {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", itemCursor.getInt(itemCursor.getColumnIndexOrThrow(COLUMN_ID)));
                    item.put("type", itemCursor.getString(itemCursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    item.put("name", itemCursor.getString(itemCursor.getColumnIndexOrThrow(COLUMN_NAME)));
                    item.put("description", itemCursor.getString(itemCursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    item.put("location", itemCursor.getString(itemCursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                    item.put("latitude", itemCursor.getDouble(itemCursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                    item.put("longitude", itemCursor.getDouble(itemCursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
                    itemsList.add(item);
                } while (itemCursor.moveToNext());
            }
            itemCursor.close();
        } catch (Exception e) {
            // Handle errors
            e.printStackTrace();
        }
        
        return itemsList;
    }
} 