package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.bean.WeiboAccount;
import org.qii.weiciyuan.support.database.table.AccountTable;

import java.util.ArrayList;
import java.util.List;

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
        cv.put(AccountTable.USERNAME, account.getUsername());
        cv.put(AccountTable.USERNICK, account.getUsernick());

        long result = wsd.insert(AccountTable.TABLE_NAME,
                AccountTable.ID, cv);

        return result;

    }


    public List<WeiboAccount> getAccountList() {
        List<WeiboAccount> weiboAccountList = new ArrayList<WeiboAccount>();
        String sql = "select * from " + AccountTable.TABLE_NAME;
        Cursor c = rsd.rawQuery(sql, null);
        while (c.moveToNext()) {
            WeiboAccount account = new WeiboAccount();
            int colid = c.getColumnIndex(AccountTable.OAUTH_TOKEN);
            account.setAccess_token(c.getString(colid));

            colid = c.getColumnIndex(AccountTable.USERNICK);
            account.setUsernick(c.getString(colid));

            weiboAccountList.add(account);
        }

        return weiboAccountList;
    }

    public void removeAccount(String uid) {
        String[] args = {uid};
        String column = AccountTable.ID;
        wsd.delete(AccountTable.TABLE_NAME, column + "=?", args);
    }

}
