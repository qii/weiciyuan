package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.dao.WeiboAccount;
import org.qii.weiciyuan.support.database.table.AccountTable;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 上午9:40
 */
public class DatabaseManager {

    private static DatabaseManager singleton = null;


    private SQLiteDatabase wsd = null;

    private SQLiteDatabase rsd = null;


    private DatabaseManager() {

    }

    public synchronized static DatabaseManager getInstance() {

        if (singleton == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            singleton = new DatabaseManager();
            singleton.wsd = wsd;
            singleton.rsd = rsd;
        }

        return singleton;
    }

    public long addAccount(WeiboAccount account) {

        ContentValues cv = new ContentValues();
        cv.put(AccountTable.ID, account.getUid());
        cv.put(AccountTable.OAUTH_TOKEN, account.getAccess_token());

        long result = wsd.insert(AccountTable.TABLE_NAME,
                AccountTable.ID, cv);

        return result;

    }

}
