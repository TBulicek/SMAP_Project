package cz.uhk.bulicek.smartlog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bulicek on 16. 1. 2017.
 */

public class LogDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "LogsDB";

    private static final String LOGS_TABLE_NAME = "LOGS";
    private static final String LOGS_TABLE_CREATE =
            "CREATE TABLE " + LOGS_TABLE_NAME + " (" +
                    "TIME TEXT, " +
                    "TYPE INTEGER);";

    LogDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOGS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + LOGS_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void addLog(Log log) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("TIME", log.get_time());
        values.put("TYPE", log.get_type());

        // Inserting Row
        db.insert(LOGS_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    public List<Log> getAllLogs() {
        List<Log> logList = new ArrayList<Log>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + LOGS_TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log log = new Log(cursor.getString(0), cursor.getInt(1));
                // Adding log to list
                logList.add(log);
            } while (cursor.moveToNext());
        }

        db.close();
        // return list
        return logList;
    }

    public List<Log> getLogsToday() {
        List<Log> logList = new ArrayList<Log>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + LOGS_TABLE_NAME +
                " WHERE " + "TIME" + ">=date('now','localtime','start of day')";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log log = new Log(cursor.getString(0), cursor.getInt(1));
                // Adding log to list
                logList.add(log);
            } while (cursor.moveToNext());
        }

        db.close();
        // return list
        return logList;
    }

    public List<Log> getLogsMonth() {
        List<Log> logList = new ArrayList<Log>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + LOGS_TABLE_NAME +
                " WHERE " + "strftime('%Y',TIME)=strftime('%Y',date('now')) AND " +
                "strftime('%m',TIME)=strftime('%m',date('now'))";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log log = new Log(cursor.getString(0), cursor.getInt(1));
                // Adding log to list
                logList.add(log);
            } while (cursor.moveToNext());
        }

        db.close();
        // return list
        return logList;
    }
}
