package com.example.android.group_23.helper;

/**
 * Created by saurabh on 3/5/18.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import com.example.android.group_23.interfaces.ServiceCallbacks;
import com.example.android.group_23.model.Patient;
import com.example.android.group_23.service.SensorService;

import java.io.File;

import static java.lang.Double.parseDouble;

public class DatabaseHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String ENV_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_FILE_PATH = ENV_PATH + File.separator + "Android"+ File.separator + "CSE535_ASSIGNMENT2";
    public static final String DATABASE_NAME = "Group23.db";
//    public static final String DOWNLOAD_FILE_PATH = "/sdcard/Android/CSE535_ASSIGNMENT2";
    File mFolderStructure;
    Boolean mSuccess = false;
    private ServiceCallbacks serviceCallbacks;

    private SQLiteDatabase mDatabase;


//    public static final String SQL_DELETE_TABLE =
//            "DROP TABLE IF EXISTS " + Patient.TableSchema.TABLE_NAME;

    public DatabaseHelper() {}

    public void createDatabase(){
        mFolderStructure = new File(DATABASE_FILE_PATH);
        try {
            if (!mFolderStructure.exists()) {
                mSuccess = mFolderStructure.mkdir();
            } else {
                Log.e(TAG,"Directory already exists");
                mSuccess = true;
            }
            if(!mSuccess) {
                Log.e(TAG,"Cannot create Directory");
            }
//            mDatabase = SQLiteDatabase.openDatabase(DOWNLOAD_FILE_PATH + File.separator + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
            Log.d(TAG,"Database path:"+DATABASE_FILE_PATH + File.separator + DATABASE_NAME);
            mDatabase = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH + File.separator + DATABASE_NAME, null);
            createTable();
        } catch (SQLiteException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            createTable();
        } finally {
//            DBUtil.safeCloseDataBase(mDatabase);
        }
    }
    private void createTable() {
        String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + Patient.TableSchema.TABLE_NAME + " (" +
                        Patient.TableSchema.TIMESTAMP + " TIMESTAMP," +
                        Patient.TableSchema.xValues + " REAL," +
                        Patient.TableSchema.yValues + " REAL," +
                        Patient.TableSchema.zValues + " REAL)";
        mDatabase.execSQL(SQL_CREATE_TABLE);
        Log.d(TAG, "Table created!");
    }

    public void close() {
//        DBUtil.safeCloseDataBase(mDatabase);
    }

    public SQLiteDatabase getReadableDatabase() {
        mDatabase = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH
                        + File.separator + DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READONLY);
        return mDatabase;
    }

    public SQLiteDatabase getWritableDatabase() {
        mDatabase = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH
                        + File.separator + DATABASE_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);
        return mDatabase;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    public void getLastTenRecords()
    {

        //SQLiteDatabase database = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH + File.separator + filename, null, SQLiteDatabase.OPEN_READONLY);
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("Select * from "+  Patient.TableSchema.TABLE_NAME + " DESC LIMIT(10)", null);
        Log.d(TAG,"Last 10 records: ");
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                Log.d(TAG, "Timestamp: " + cursor.getString(0));
                Log.d(TAG, "X: " + cursor.getString(1));
                Log.d(TAG, "Y: " + cursor.getString(2));
                Log.d(TAG, "Z: " + cursor.getString(3));
                cursor.moveToNext();
                if (serviceCallbacks != null) {
                    serviceCallbacks.addEntry(Double.parseDouble(cursor.getString(1)), Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3)));
                }
            }
//            SensorService.setCallbacks();
        }
    }
    public void printTables(SQLiteDatabase database){
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                Log.d(TAG,"Table Name: "+cursor.getString(0));
                cursor.moveToNext();
            }
        }
    }
    public void updateTable(SQLiteDatabase database, String tableName, Float x, Float y, Float z){

        database.execSQL("INSERT INTO " + tableName + " VALUES (CURRENT_TIMESTAMP,"+x+","+y+","+z+") ");
    }
    public void printTable(SQLiteDatabase database, String tableName){

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName, null);
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                Log.d(TAG,"Timestamp: "+cursor.getString(0));
                Log.d(TAG,"X: "+cursor.getString(1));
                Log.d(TAG,"Y: "+cursor.getString(2));
                Log.d(TAG,"Z: "+cursor.getString(3));
                cursor.moveToNext();
            }
        }
    }
}