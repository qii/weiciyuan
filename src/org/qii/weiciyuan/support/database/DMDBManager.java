package org.qii.weiciyuan.support.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * User: qii
 * Date: 12-12-3
 */
public class DMDBManager {

    private static DMDBManager singleton = null;
    private SQLiteDatabase wsd = null;
    private SQLiteDatabase rsd = null;

    private DMDBManager() {

    }

    public static DMDBManager getInstance() {

        if (singleton == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            singleton = new DMDBManager();
            singleton.wsd = wsd;
            singleton.rsd = rsd;
        }

        return singleton;
    }
}
