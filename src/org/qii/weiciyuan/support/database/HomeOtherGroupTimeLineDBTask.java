package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.android.MessageTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
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
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < msgList.size(); i++) {
                MessageBean msg = msgList.get(i);
                if (msg != null) {
                    ContentValues cv = new ContentValues();
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.MBLOGID, msg.getId());
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID, accountId);
                    String json = gson.toJson(msg);
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA, json);
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID, groupId);
                    getWsd().insert(HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME,
                            HomeOtherGroupTable.HomeOtherGroupDataTable.ID, cv);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.MBLOGID, "-1");
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID, accountId);
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA, "");
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID, groupId);
                    getWsd().insert(HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME,
                            HomeOtherGroupTable.HomeOtherGroupDataTable.ID, cv);
                }
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
        }
        reduceHomeOtherGroupTable(accountId, groupId);
    }

    private static void reduceHomeOtherGroupTable(String accountId, String groupId) {
        String searchCount = "select count(" + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + ") as total"
                + " from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME
                + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID
                + " = " + accountId
                + " and " + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID
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
            String sql = " delete from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " in "
                    + "( select " + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME + " where "
                    + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID
                    + " in " + "(" + accountId + ") "
                    + " and " + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID
                    + " = " + groupId
                    + " order by " + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " desc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    public static void replace(MessageListBean list, String accountId, String groupId) {

        deleteGroupTimeLine(accountId, groupId);
        addHomeLineMsg(list, accountId, groupId);
    }

    static void deleteGroupTimeLine(String accountId, String groupId) {
        String sql = "delete from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID + " in " + "(" + accountId + ")"
                + " and " + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID + " = " + groupId;

        getWsd().execSQL(sql);
    }

    public static void updatePosition(TimeLinePosition position, String accountId, String groupId) {
        String sql = "select * from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.ACCOUNTID + "  = " +
                accountId + " and " + HomeOtherGroupTable.GROUPID + " = " + groupId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId, groupId};
                ContentValues cv = new ContentValues();
                cv.put(HomeOtherGroupTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(HomeOtherGroupTable.TABLE_NAME, cv, HomeOtherGroupTable.ACCOUNTID + "=? AND " + HomeOtherGroupTable.GROUPID + " =? "
                        , args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(HomeOtherGroupTable.ACCOUNTID, accountId);
            cv.put(HomeOtherGroupTable.GROUPID, groupId);
            cv.put(HomeOtherGroupTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(HomeOtherGroupTable.TABLE_NAME,
                    HomeOtherGroupTable.ID, cv);
        }
    }

    private static TimeLinePosition getPosition(String accountId, String groupId) {
        String sql = "select * from " + HomeOtherGroupTable.TABLE_NAME + " where " + HomeOtherGroupTable.ACCOUNTID + "  = "
                + accountId + " and " + HomeOtherGroupTable.GROUPID + " = " + groupId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.TIMELINEDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    TimeLinePosition value = gson.fromJson(json, TimeLinePosition.class);
                    return value;

                } catch (JsonSyntaxException e) {

                }
            }

        }
        c.close();
        return new TimeLinePosition(0, 0);
    }

    public static MessageTimeLineData getTimeLineData(String accountId, String groupId) {
        MessageListBean msgList = get(accountId, groupId);
        TimeLinePosition position = getPosition(accountId, groupId);
        return new MessageTimeLineData(msgList, position);
    }

    public static MessageListBean get(String accountId, String groupId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID + "  = "
                + accountId + " and " + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID + " =  " + groupId + " order by " + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA));
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
        String sql = "select * from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.MBLOGID + "  = "
                + msgId + " order by "
                + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeOtherGroupTable.HomeOtherGroupDataTable.ID));
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.setComments_count(commentCount);
                    value.setReposts_count(repostCount);
                    String[] args = {id};
                    ContentValues cv = new ContentValues();
                    cv.put(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA, gson.toJson(value));
                    getWsd().update(HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME, cv, HomeOtherGroupTable.HomeOtherGroupDataTable.ID + "=?", args);
                } catch (JsonSyntaxException e) {

                }

            }
        }
    }

}
