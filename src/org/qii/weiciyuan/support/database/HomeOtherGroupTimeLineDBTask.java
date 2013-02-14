package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.database.table.HomeOtherGroupTable;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-2-11
 */
public class HomeOtherGroupTimeLineDBTask {

    private HomeOtherGroupTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    private static void addHomeLineMsg(MessageListBean list, String accountId, String groupId) {

        if (list == null || list.getSize() == 0) {
            return;
        }

        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();

        for (int i = 0; i < msgList.size(); i++) {
            MessageBean msg = msgList.get(i);
            if (msg != null) {
                ContentValues cv = new ContentValues();
                cv.put(HomeOtherGroupTable.MBLOGID, msg.getId());
                cv.put(HomeOtherGroupTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(HomeOtherGroupTable.JSONDATA, json);
                cv.put(HomeOtherGroupTable.GROUPID, groupId);
                getWsd().insert(HomeOtherGroupTable.TABLE_NAME,
                        HomeOtherGroupTable.ID, cv);
            } else {
                ContentValues cv = new ContentValues();
                cv.put(HomeOtherGroupTable.MBLOGID, "-1");
                cv.put(HomeOtherGroupTable.ACCOUNTID, accountId);
                cv.put(HomeOtherGroupTable.JSONDATA, "");
                cv.put(HomeOtherGroupTable.GROUPID, groupId);
                getWsd().insert(HomeOtherGroupTable.TABLE_NAME,
                        HomeOtherGroupTable.ID, cv);
            }
        }

        reduceHomeOtherGroupTable(accountId, groupId);
    }

    private static void reduceHomeOtherGroupTable(String accountId, String groupId) {
        String searchCount = "select count(" + HomeOtherGroupTable.ID + ") as total"
                + " from " + HomeOtherGroupTable.TABLE_NAME
                + " where " + HomeOtherGroupTable.ACCOUNTID
                + " = " + accountId
                + " and " + HomeOtherGroupTable.GROUPID
                + " = " + groupId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - AppConfig.DEFAULT_DB_CACHE_COUNT;

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.ID + " in "
                    + "( select " + HomeOtherGroupTable.ID + " from " + HomeOtherGroupTable.TABLE_NAME + " where "
                    + HomeOtherGroupTable.ACCOUNTID
                    + " in " + "(" + accountId + ") "
                    + " and " + HomeOtherGroupTable.GROUPID
                    + " = " + groupId
                    + " order by " + HomeOtherGroupTable.ID + " desc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    public static void replace(MessageListBean list, String accountId, String groupId) {

        deleteGroupTimeLine(accountId, groupId);
        addHomeLineMsg(list, accountId, groupId);
    }

    static void deleteGroupTimeLine(String accountId, String groupId) {
        String sql = "delete from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.ACCOUNTID + " in " + "(" + accountId + ")"
                + " and " + HomeOtherGroupTable.GROUPID + " = " + groupId;

        getWsd().execSQL(sql);
    }


    public static MessageListBean get(String accountId, String groupId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.ACCOUNTID + "  = "
                + accountId + " and " + HomeOtherGroupTable.GROUPID + " =  " + groupId + " order by " + HomeOtherGroupTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.getListViewSpannableString();
                    msgList.add(value);
                } catch (JsonSyntaxException e) {
                    AppLogger.e(e.getMessage());
                }

            } else {
                msgList.add(null);
            }
        }

        //delete the null flag at the head positon and the end position
        for (int i = msgList.size() - 1; i >= 0; i--) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }

        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }

        result.setStatuses(msgList);
        c.close();
        return result;

    }

    public static void updateCount(String msgId, int commentCount, int repostCount) {
        String sql = "select * from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.MBLOGID + "  = "
                + msgId + " order by "
                + HomeOtherGroupTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeOtherGroupTable.ID));
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.setComments_count(commentCount);
                    value.setReposts_count(repostCount);
                    String[] args = {id};
                    ContentValues cv = new ContentValues();
                    cv.put(HomeOtherGroupTable.JSONDATA, gson.toJson(value));
                    getWsd().update(HomeOtherGroupTable.TABLE_NAME, cv, HomeOtherGroupTable.ID + "=?", args);
                } catch (JsonSyntaxException e) {

                }

            }
        }
    }

}
