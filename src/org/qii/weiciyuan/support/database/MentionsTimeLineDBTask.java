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
import org.qii.weiciyuan.bean.android.MentionTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.support.database.table.RepostsTable;
import org.qii.weiciyuan.support.utils.AppConfig;
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


    public static MentionTimeLineData getRepostLineMsgList(String accountId) {

        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + RepostsTable.RepostDataTable.TABLE_NAME + " where " + RepostsTable.RepostDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + RepostsTable.RepostDataTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(RepostsTable.RepostDataTable.JSONDATA));
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

        result.setStatuses(msgList);
        c.close();
        MentionTimeLineData mentionTimeLineData = new MentionTimeLineData(result, getPosition(accountId));

        return mentionTimeLineData;

    }

    public static void addRepostLineMsg(MessageListBean list, String accountId) {
        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();
        int size = msgList.size();
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < size; i++) {
                MessageBean msg = msgList.get(i);
                if (msg != null) {
                    ContentValues cv = new ContentValues();
                    cv.put(RepostsTable.RepostDataTable.MBLOGID, msg.getId());
                    cv.put(RepostsTable.RepostDataTable.ACCOUNTID, accountId);
                    String json = gson.toJson(msg);
                    cv.put(RepostsTable.RepostDataTable.JSONDATA, json);
                    getWsd().insert(RepostsTable.RepostDataTable.TABLE_NAME,
                            RepostsTable.RepostDataTable.ID, cv);
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put(RepostsTable.RepostDataTable.MBLOGID, "-1");
                    cv.put(RepostsTable.RepostDataTable.ACCOUNTID, accountId);
                    cv.put(RepostsTable.RepostDataTable.JSONDATA, "");
                    getWsd().insert(RepostsTable.RepostDataTable.TABLE_NAME,
                            RepostsTable.RepostDataTable.ID, cv);
                }
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
        }
        reduceRepostTable(accountId);
    }


    private static void reduceRepostTable(String accountId) {
        String searchCount = "select count(" + RepostsTable.RepostDataTable.ID + ") as total" + " from " + RepostsTable.RepostDataTable.TABLE_NAME + " where " + RepostsTable.RepostDataTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - AppConfig.DEFAULT_MENTIONS_WEIBO_DB_CACHE_COUNT;

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + RepostsTable.RepostDataTable.TABLE_NAME + " where " + RepostsTable.RepostDataTable.ID + " in "
                    + "( select " + RepostsTable.RepostDataTable.ID + " from " + RepostsTable.RepostDataTable.TABLE_NAME + " where "
                    + RepostsTable.RepostDataTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + RepostsTable.RepostDataTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private static void replaceRepostLineMsg(MessageListBean list, String accountId) {

        deleteAllReposts(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + RepostsTable.RepostDataTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_REPOSTS_TABLE_SQL);

        addRepostLineMsg(list, accountId);
    }

    static void deleteAllReposts(String accountId) {
        String sql = "delete from " + RepostsTable.RepostDataTable.TABLE_NAME + " where " + RepostsTable.RepostDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static void asyncUpdatePosition(final TimeLinePosition position, final String accountId) {
        if (position == null)
            return;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updatePosition(position, accountId);
            }
        };

        new Thread(runnable).start();
    }


    private static void updatePosition(TimeLinePosition position, String accountId) {
        String sql = "select * from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(RepostsTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(RepostsTable.TABLE_NAME, cv, RepostsTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(RepostsTable.ACCOUNTID, accountId);
            cv.put(RepostsTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(RepostsTable.TABLE_NAME,
                    RepostsTable.ID, cv);
        }
    }

    public static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(RepostsTable.TIMELINEDATA));
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

    public static void asyncReplace(final MessageListBean list, final String accountId) {
        final MessageListBean data = new MessageListBean();
        data.replaceData(list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAllReposts(accountId);
                addRepostLineMsg(data, accountId);
            }
        }).start();

    }
}
