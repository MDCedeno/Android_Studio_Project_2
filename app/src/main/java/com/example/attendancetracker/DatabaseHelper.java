package com.example.attendancetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database and table information
    public static final String DATABASE_NAME = "Attendance.db";
    public static final String TABLE_NAME = "attendance_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "DATE";
    public static final String COL_4 = "STATUS";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, DATE TEXT, STATUS TEXT)");
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert data into the table
    public boolean insertData(String name, String date, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, date);
        contentValues.put(COL_4, status);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Return true if insertion was successful
    }

    // Retrieve all data
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public boolean deleteData(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "name = ? AND date = ?", new String[]{name, date});
        return result != -1; // Return true if deletion was successful
    }

    // Fetch records by date range (weekly, monthly, custom)
    public Cursor getAttendanceByDateRange(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE DATE BETWEEN ? AND ?";
        return db.rawQuery(query, new String[]{startDate, endDate});
    }

    // Search records by name or date
    public Cursor searchAttendance(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        String searchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE NAME LIKE ? OR DATE LIKE ?";
        return db.rawQuery(searchQuery, new String[]{"%" + query + "%", "%" + query + "%"});
    }
}
