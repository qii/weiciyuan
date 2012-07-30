package org.qii.weiciyuan.support.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 上午9:40
 */
public class DatabaseManager {

    private static DatabaseManager databaseManager = null;


    private SQLiteDatabase wsd = null;

    private SQLiteDatabase rsd = null;


    private DatabaseManager() {

    }

    public synchronized static DatabaseManager getInstance() {

        if (databaseManager == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            databaseManager = new DatabaseManager();
            databaseManager.wsd = wsd;
            databaseManager.rsd = rsd;
        }

        return databaseManager;
    }

}
