package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.database.table.HomeTable;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-7
 */
public class FriendsTimeLineDBTask {

    private FriendsTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    private static void addHomeLineMsg(MessageListBean list, String accountId) {

        if (list == null || list.getSize() == 0) {
            return;
        }

        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();

        for (int i = 0; i < msgList.size(); i++) {
            MessageBean msg = msgList.get(i);
            if (msg != null) {
                ContentValues cv = new ContentValues();
                cv.put(HomeTable.MBLOGID, msg.getId());
                cv.put(HomeTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(HomeTable.JSONDATA, json);
                getWsd().insert(HomeTable.TABLE_NAME,
                        HomeTable.ID, cv);
            } else {
                ContentValues cv = new ContentValues();
                cv.put(HomeTable.MBLOGID, "-1");
                cv.put(HomeTable.ACCOUNTID, accountId);
                cv.put(HomeTable.JSONDATA, "");
                getWsd().insert(HomeTable.TABLE_NAME,
                        HomeTable.ID, cv);
            }
        }

        reduceHomeTable(accountId);
    }

    private static void reduceHomeTable(String accountId) {
        String searchCount = "select count(" + HomeTable.ID + ") as total" + " from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID
                + " = " + accountId;
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
            String sql = " delete from " + HomeTable.TABLE_NAME + " where " + HomeTable.ID + " in "
                    + "( select " + HomeTable.ID + " from " + HomeTable.TABLE_NAME + " where "
                    + HomeTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + HomeTable.ID + " desc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    public static void replaceHomeLineMsg(MessageListBean list, String accountId) {

        deleteAllHomes(accountId);
        addHomeLineMsg(list, accountId);
    }

    static void deleteAllHomes(String accountId) {
        String sql = "delete from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static void updateCount(String msgId, String accountId, int commentCount, int repostCount) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.MBLOGID + "  = "
                + msgId + " and " + HomeTable.ACCOUNTID + " = " + accountId + " order by "
                + HomeTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeTable.ID));
            String json = c.getString(c.getColumnIndex(HomeTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.setComments_count(commentCount);
                    value.setReposts_count(repostCount);
                    String[] args = {id};
                    ContentValues cv = new ContentValues();
                    cv.put(HomeTable.JSONDATA, gson.toJson(value));
                    getWsd().update(HomeTable.TABLE_NAME, cv, HomeTable.ID + "=?", args);
                } catch (JsonSyntaxException e) {

                }

            }
        }
    }


    public static MessageListBean getHomeLineMsgList(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + "  = "
                + accountId + " order by " + HomeTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeTable.JSONDATA));
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
}
