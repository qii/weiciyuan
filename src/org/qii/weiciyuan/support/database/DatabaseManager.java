package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.bean.TimeLineMsgList;
import org.qii.weiciyuan.bean.WeiboAccount;
import org.qii.weiciyuan.bean.WeiboMsg;
import org.qii.weiciyuan.bean.WeiboUser;
import org.qii.weiciyuan.support.database.table.AccountTable;
import org.qii.weiciyuan.support.database.table.HomeTable;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public OAuthActivity.DBResult addOrUpdateAccount(WeiboAccount account) {

        ContentValues cv = new ContentValues();
        cv.put(AccountTable.UID, account.getUid());
        cv.put(AccountTable.OAUTH_TOKEN, account.getAccess_token());
        cv.put(AccountTable.USERNAME, account.getUsername());
        cv.put(AccountTable.USERNICK, account.getUsernick());

        Cursor c = rsd.query(AccountTable.TABLE_NAME, null, AccountTable.UID + "=?",
                new String[]{account.getUid()}, null, null, null);

        if (c != null && c.getCount() > 0) {
            String[] args = {account.getUid()};
            wsd.update(AccountTable.TABLE_NAME, cv, AccountTable.UID + "=?", args);
            return OAuthActivity.DBResult.update_successfully;
        } else {

            wsd.insert(AccountTable.TABLE_NAME,
                    AccountTable.UID, cv);
            return OAuthActivity.DBResult.add_successfuly;
        }

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

            colid = c.getColumnIndex(AccountTable.UID);
            account.setUid(c.getString(colid));

            weiboAccountList.add(account);
        }

        return weiboAccountList;
    }

    public List<WeiboAccount> removeAndGetNewAccountList(Set<String> checkedItemPosition) {
        String[] args = checkedItemPosition.toArray(new String[0]);

        String column = AccountTable.UID;
        long result = wsd.delete(AccountTable.TABLE_NAME, column + "=?", args);

        return getAccountList();
    }

    public void addHomeLineMsg(TimeLineMsgList list) {

        List<WeiboMsg> msgList = list.getStatuses();
        int size = msgList.size();
        for (int i = 0; i < size; i++) {
            WeiboMsg msg = msgList.get(i);
            WeiboUser user = msg.getUser();
            ContentValues cv = new ContentValues();
            cv.put(HomeTable.MBLOGID, msg.getId());
            cv.put(HomeTable.NICK, user.getScreen_name());
            cv.put(HomeTable.UID, user.getId());
            cv.put(HomeTable.CONTENT, msg.getText());
            cv.put(HomeTable.TIME, msg.getCreated_at());
            long result = wsd.insert(HomeTable.TABLE_NAME,
                    HomeTable.MBLOGID, cv);
        }


    }

    public TimeLineMsgList getHomeLineMsgList() {

        TimeLineMsgList result = new TimeLineMsgList();

        List<WeiboMsg> msgList = new ArrayList<WeiboMsg>();
        String sql = "select * from " + HomeTable.TABLE_NAME;
        Cursor c = rsd.rawQuery(sql, null);
        while (c.moveToNext()) {
            WeiboMsg msg = new WeiboMsg();
            int colid = c.getColumnIndex(HomeTable.MBLOGID);
            msg.setId(c.getString(colid));

            colid = c.getColumnIndex(HomeTable.CONTENT);
            msg.setText(c.getString(colid));

            msg.setListviewItemShowTime(c.getString(c.getColumnIndex(HomeTable.TIME)));

            WeiboUser user = new WeiboUser();

            user.setScreen_name(c.getString(c.getColumnIndex(HomeTable.NICK)));

            msg.setUser(user);

            msgList.add(msg);
        }

        result.setStatuses(msgList);

        return result;

    }

}
