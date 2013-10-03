package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.database.table.AccountTable;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 13-1-7
 */
public class AccountDBTask {

    private AccountDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static OAuthActivity.DBResult addOrUpdateAccount(AccountBean account, boolean blackMagic) {

        ContentValues cv = new ContentValues();
        cv.put(AccountTable.UID, account.getUid());
        cv.put(AccountTable.OAUTH_TOKEN, account.getAccess_token());
        cv.put(AccountTable.OAUTH_TOKEN_EXPIRES_TIME, String.valueOf(account.getExpires_time()));
        cv.put(AccountTable.BLACK_MAGIC, blackMagic);

        String json = new Gson().toJson(account.getInfo());
        cv.put(AccountTable.INFOJSON, json);

        Cursor c = getWsd().query(AccountTable.TABLE_NAME, null, AccountTable.UID + "=?",
                new String[]{account.getUid()}, null, null, null);

        if (c != null && c.getCount() > 0) {
            String[] args = {account.getUid()};
            getWsd().update(AccountTable.TABLE_NAME, cv, AccountTable.UID + "=?", args);
            return OAuthActivity.DBResult.update_successfully;
        } else {

            getWsd().insert(AccountTable.TABLE_NAME,
                    AccountTable.UID, cv);
            return OAuthActivity.DBResult.add_successfuly;
        }

    }

    public static void asyncUpdateMyProfile(final AccountBean accountBean, final UserBean value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateMyProfile(accountBean, value);
            }
        }).start();
    }

    public static void updateMyProfile(AccountBean account, UserBean value) {
        String uid = account.getUid();
        String json = new Gson().toJson(value);

        ContentValues cv = new ContentValues();
        cv.put(AccountTable.UID, uid);
        cv.put(AccountTable.INFOJSON, json);

        int c = getWsd().update(AccountTable.TABLE_NAME, cv, AccountTable.UID + "=?",
                new String[]{uid});
    }

    public static void updateNavigationPosition(AccountBean account, int position) {
        String uid = account.getUid();

        ContentValues cv = new ContentValues();
        cv.put(AccountTable.UID, uid);
        cv.put(AccountTable.NAVIGATION_POSITION, position);

        int c = getWsd().update(AccountTable.TABLE_NAME, cv, AccountTable.UID + "=?",
                new String[]{uid});
    }

    public static List<AccountBean> getAccountList() {
        List<AccountBean> accountList = new ArrayList<AccountBean>();
        String sql = "select * from " + AccountTable.TABLE_NAME;
        Cursor c = getWsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            AccountBean account = new AccountBean();
            int colid = c.getColumnIndex(AccountTable.OAUTH_TOKEN);
            account.setAccess_token(c.getString(colid));

            colid = c.getColumnIndex(AccountTable.OAUTH_TOKEN_EXPIRES_TIME);
            account.setExpires_time(Long.valueOf(c.getString(colid)));

            colid = c.getColumnIndex(AccountTable.BLACK_MAGIC);
            account.setBlack_magic(c.getInt(colid) == 1);

            colid = c.getColumnIndex(AccountTable.NAVIGATION_POSITION);
            account.setNavigationPosition(c.getInt(colid));

            Gson gson = new Gson();
            String json = c.getString(c.getColumnIndex(AccountTable.INFOJSON));
            try {
                UserBean value = gson.fromJson(json, UserBean.class);
                account.setInfo(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }

            accountList.add(account);
        }
        c.close();
        return accountList;
    }

    public static AccountBean getAccount(String id) {

        String sql = "select * from " + AccountTable.TABLE_NAME + " where " + AccountTable.UID + " = " + id;
        Cursor c = getRsd().rawQuery(sql, null);
        if (c.moveToNext()) {
            AccountBean account = new AccountBean();
            int colid = c.getColumnIndex(AccountTable.OAUTH_TOKEN);
            account.setAccess_token(c.getString(colid));

            colid = c.getColumnIndex(AccountTable.OAUTH_TOKEN_EXPIRES_TIME);
            account.setExpires_time(Long.valueOf(c.getString(colid)));

            colid = c.getColumnIndex(AccountTable.BLACK_MAGIC);
            account.setBlack_magic(c.getInt(colid) == 1);

            colid = c.getColumnIndex(AccountTable.NAVIGATION_POSITION);
            account.setNavigationPosition(c.getInt(colid));

            Gson gson = new Gson();
            String json = c.getString(c.getColumnIndex(AccountTable.INFOJSON));
            try {
                UserBean value = gson.fromJson(json, UserBean.class);
                account.setInfo(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }

            return account;
        }
        return null;

    }

    public static List<AccountBean> removeAndGetNewAccountList(Set<String> checkedItemPosition) {
        String[] args = checkedItemPosition.toArray(new String[0]);
        String asString = Arrays.toString(args);
        asString = asString.replace("[", "(");
        asString = asString.replace("]", ")");

        String sql = "delete from " + AccountTable.TABLE_NAME + " where " + AccountTable.UID + " in " + asString;

        getWsd().execSQL(sql);

        for (String id : args) {
            FriendsTimeLineDBTask.deleteAllHomes(id);
            MentionWeiboTimeLineDBTask.deleteAllReposts(id);
            MentionCommentsTimeLineDBTask.deleteAllComments(id);
            CommentToMeTimeLineDBTask.deleteAllComments(id);
            CommentByMeTimeLineDBTask.deleteAllComments(id);
            MyStatusDBTask.clear(id);
            AtUsersDBTask.clear(id);
            FavouriteDBTask.deleteAllFavourites(id);
        }

        return getAccountList();
    }
}
