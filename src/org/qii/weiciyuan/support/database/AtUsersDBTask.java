package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.AtUserBean;
import org.qii.weiciyuan.support.database.table.AtUsersTable;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-2-4
 */
public class AtUsersDBTask {

    private AtUsersDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static void add(AtUserBean atUserBean, String accountId) {
        Gson gson = new Gson();
        ContentValues cv = new ContentValues();
        cv.put(AtUsersTable.ACCOUNTID, accountId);
        String json = gson.toJson(atUserBean);
        cv.put(AtUsersTable.JSONDATA, json);
        getWsd().insert(AtUsersTable.TABLE_NAME,
                AtUsersTable.ID, cv);

        reduce(accountId);
    }

    public static List<AtUserBean> get(String accountId) {


        List<AtUserBean> msgList = new ArrayList<AtUserBean>();
        String sql = "select * from " + AtUsersTable.TABLE_NAME + " where " + AtUsersTable.ACCOUNTID + "  = "
                + accountId + " order by " + AtUsersTable.ID + " desc";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(AtUsersTable.JSONDATA));
            try {
                AtUserBean value = gson.fromJson(json, AtUserBean.class);
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }
        }

        c.close();
        return msgList;

    }


    private static void reduce(String accountId) {
        String searchCount = "select count(" + AtUsersTable.ID + ") as total" + " from " + AtUsersTable.TABLE_NAME + " where " + AtUsersTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getRsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();


        int needDeletedNumber = total - 15;

        if (needDeletedNumber > 0) {
            String sql = " delete from " + AtUsersTable.TABLE_NAME + " where " + AtUsersTable.ID + " in "
                    + "( select " + AtUsersTable.ID + " from " + AtUsersTable.TABLE_NAME + " where "
                    + AtUsersTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + AtUsersTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }


    static void clear(String accountId) {
        String sql = "delete from " + AtUsersTable.TABLE_NAME + " where " + AtUsersTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

}
