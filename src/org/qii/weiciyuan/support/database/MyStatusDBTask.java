package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.database.table.HomeTable;
import org.qii.weiciyuan.support.database.table.MyStatusTable;
import org.qii.weiciyuan.support.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-12-4
 */
public class MyStatusDBTask {

    private MyStatusDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void add(MessageListBean list, String accountId) {

        if (list == null || list.getSize() == 0) {
            return;
        }

        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();

        for (MessageBean msg : msgList) {
            ContentValues cv = new ContentValues();
            cv.put(MyStatusTable.MBLOGID, msg.getId());
            cv.put(MyStatusTable.ACCOUNTID, accountId);
            String json = gson.toJson(msg);
            cv.put(MyStatusTable.JSONDATA, json);
            getWsd().insert(MyStatusTable.TABLE_NAME,
                    MyStatusTable.ID, cv);
        }

        reduceHomeTable(accountId);

    }

    private static void reduceHomeTable(String accountId) {
        String searchCount = "select count(" + MyStatusTable.ID + ") as total" + " from " + MyStatusTable.TABLE_NAME + " where " + MyStatusTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        int needDeletedNumber = total - AppConfig.MAX_DATABASE_TABLE_ENTRY_NUMBER;

        if (needDeletedNumber > 0) {
            String sql = " delete from " + MyStatusTable.TABLE_NAME + " where " + MyStatusTable.ID + " in "
                    + "( select " + MyStatusTable.ID + " from " + MyStatusTable.TABLE_NAME + " where "
                    + MyStatusTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + MyStatusTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }


    public static void clear(String accountId) {
        String sql = "delete from " + MyStatusTable.TABLE_NAME + " where " + MyStatusTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static MessageListBean get(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + MyStatusTable.TABLE_NAME + " where " + MyStatusTable.ACCOUNTID + "  = "
                + accountId + " order by " + MyStatusTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeTable.JSONDATA));
            try {
                MessageBean value = gson.fromJson(json, MessageBean.class);
                value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException ignored) {

            }

        }

        result.setStatuses(msgList);
        c.close();
        return result;

    }

}
