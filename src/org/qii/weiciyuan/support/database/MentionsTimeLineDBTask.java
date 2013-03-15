package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.database.table.RepostsTable;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-7
 */
public class MentionsTimeLineDBTask {

    private MentionsTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static MessageListBean getRepostLineMsgList(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + "  = "
                + accountId + " order by " + RepostsTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(RepostsTable.JSONDATA));
            try {
                MessageBean value = gson.fromJson(json, MessageBean.class);
                value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }

        }

        result.setStatuses(msgList);
        c.close();
        return result;

    }

    public static void addRepostLineMsg(MessageListBean list, String accountId) {
        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();
        int size = msgList.size();
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < size; i++) {
                MessageBean msg = msgList.get(i);
                ContentValues cv = new ContentValues();
                cv.put(RepostsTable.MBLOGID, msg.getId());
                cv.put(RepostsTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(RepostsTable.JSONDATA, json);
                getWsd().insert(RepostsTable.TABLE_NAME,
                        RepostsTable.ID, cv);
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
        }
        reduceRepostTable(accountId);
    }


    private static void reduceRepostTable(String accountId) {
        String searchCount = "select count(" + RepostsTable.ID + ") as total" + " from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - Integer.valueOf(SettingUtility.getMsgCount());

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ID + " in "
                    + "( select " + RepostsTable.ID + " from " + RepostsTable.TABLE_NAME + " where "
                    + RepostsTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + RepostsTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private static void replaceRepostLineMsg(MessageListBean list, String accountId) {

        deleteAllReposts(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + RepostsTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_REPOSTS_TABLE_SQL);

        addRepostLineMsg(list, accountId);
    }

    static void deleteAllReposts(String accountId) {
        String sql = "delete from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }
}
