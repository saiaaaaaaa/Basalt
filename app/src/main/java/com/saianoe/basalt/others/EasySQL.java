package com.saianoe.basalt.others;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @noinspection ALL*/
public class EasySQL {
    Context context;

    public EasySQL(Context context){
        this.context = context;
    }

    public void createTable(String databaseName, String tableName, Map<String, String> values){
        SQLiteDatabase db = openDatabase(databaseName);
        String query = "CREATE TABLE " + tableName + "(";
        int counter = 0;
        for (Map.Entry<String, String> entry : values.entrySet()){
            if (counter == values.size() - 1){
                query += entry.getKey() + " " + entry.getValue();
            } else {
                query += entry.getKey() + " " + entry.getValue() + ", ";
            }
            counter++;
        }
        query += ")";
        db.execSQL(query);
        db.close();
    }

    public boolean doesTableExist(String databaseName, String tableName){
        try {
            SQLiteDatabase db = openDatabase(databaseName);
            db.rawQuery("SELECT * FROM " + tableName, null).close();
            return true;
        } catch (Exception ignored){
        }
        return false;
    }

    public void insertToTable(String databaseName, String tableName, Map<String, String> values){
        SQLiteDatabase db = openDatabase(databaseName);
        ContentValues cv = new ContentValues();

        for (Map.Entry<String, String> entry : values.entrySet()){
            cv.put(entry.getKey(), entry.getValue());
        }

        db.insert(tableName, null, cv);
    }

    public void deleteFromTable(String databaseName, String tableName, String where){
        SQLiteDatabase db = openDatabase(databaseName);
        String query = "DELETE FROM " + tableName + " WHERE " + where;
        db.execSQL(query);
    }

    public void deleteTable(String databaseName, String tableName){
        SQLiteDatabase db = openDatabase(databaseName);
        String query = "DROP TABLE " + tableName;
        db.execSQL(query);
    }

    public String whereClauseCreator(String column, String value){
        return column + "='" + value + "'";
    }

    public List<Map<String, String>> getTableValues(String databaseName, String tableName){
        List<Map<String, String>> values = new ArrayList<>();
        SQLiteDatabase db = openDatabase(databaseName);
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        int columnCount = cursor.getColumnCount();
        while (cursor.moveToNext()){
            Map<String, String> map = new HashMap<>();
            for (int e = 0; e < columnCount; e++){
                map.put(cursor.getColumnName(e), cursor.getString(e));
            }
            values.add(map);
        }
        cursor.close();
        return values;
    }

    SQLiteDatabase openDatabase(String databaseName){
        SQLiteOpenHelper sqLiteOpenHelper = new SQLiteOpenHelper(context, databaseName, null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            }
        };
        return sqLiteOpenHelper.getReadableDatabase();
    }

    public boolean doesValueExist(String databaseName, String tableName, String column, String value) {
        SQLiteDatabase db = openDatabase(databaseName);
        String query = "SELECT 1 FROM " + tableName + " WHERE " + whereClauseCreator(column, value);
        Cursor cursor = db.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
