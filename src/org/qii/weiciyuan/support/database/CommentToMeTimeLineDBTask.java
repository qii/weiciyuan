package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.android.CommentTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.support.database.table.CommentsTable;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-7
 */
public class CommentToMeTimeLineDBTask {

    private CommentToMeTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static void addCommentLineMsg(CommentListBean list, String accountId) {
        Gson gson = new Gson();
        List<CommentBean> msgList = list.getItemList();

        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), CommentsTable.CommentsDataTable.TABLE_NAME);
        final int mblogidColumn = ih.getColumnIndex(CommentsTable.CommentsDataTable.MBLOGID);
        final int accountidColumn = ih.getColumnIndex(CommentsTable.CommentsDataTable.ACCOUNTID);
        final int jsondataColumn = ih.getColumnIndex(CommentsTable.CommentsDataTable.JSONDATA);

        try {
            getWsd().beginTransaction();
            for (CommentBean msg : msgList) {

                ih.prepareForInsert();
                ih.bind(mblogidColumn, msg.getId());
                ih.bind(accountidColumn, accountId);
                String json = gson.toJson(msg);
                ih.bind(jsondataColumn, json);
                ih.execute();

            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }
        reduceCommentTable(accountId);
    }

    public static CommentTimeLineData getCommentLineMsgList(String accountId) {
        TimeLinePosition position = getPosition(accountId);

        int limit = position.position + AppConfig.DB_CACHE_COUNT_OFFSET > AppConfig.DEFAULT_MSG_COUNT_50 ? position.position + AppConfig.DB_CACHE_COUNT_OFFSET : AppConfig.DEFAULT_MSG_COUNT_50;


        CommentListBean result = new CommentListBean();

        List<CommentBean> msgList = new ArrayList<CommentBean>();
        String sql = "select * from " + CommentsTable.CommentsDataTable.TABLE_NAME + " where " + CommentsTable.CommentsDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + CommentsTable.CommentsDataTable.MBLOGID + " desc limit " + limit;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentsTable.CommentsDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    CommentBean value = gson.fromJson(json, CommentBean.class);
                    value.getListViewSpannableString();
                    msgList.add(value);
                } catch (JsonSyntaxException e) {
                    AppLogger.e(e.getMessage());
                }
            } else {
                msgList.add(null);
            }
        }

        result.setComments(msgList);
        c.close();
        return new CommentTimeLineData(result, position);

    }


    private static void reduceCommentTable(String accountId) {
        String searchCount = "select count(" + CommentsTable.CommentsDataTable.ID + ") as total" + " from " + CommentsTable.CommentsDataTable.TABLE_NAME + " where " + CommentsTable.CommentsDataTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getRsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

//        AppLogger.e("total=" + total);
//
//        int needDeletedNumber = total - AppConfig.DEFAULT_COMMENTS_TO_ME_DB_CACHE_COUNT;
//
//        if (needDeletedNumber > 0) {
//            AppLogger.e("" + needDeletedNumber);
//            String sql = " delete from " + CommentsTable.CommentsDataTable.TABLE_NAME + " where " + CommentsTable.CommentsDataTable.ID + " in "
//                    + "( select " + CommentsTable.CommentsDataTable.ID + " from " + CommentsTable.CommentsDataTable.TABLE_NAME + " where "
//                    + CommentsTable.CommentsDataTable.ACCOUNTID
//                    + " in " + "(" + accountId + ") order by " + CommentsTable.CommentsDataTable.ID + " asc limit " + needDeletedNumber + " ) ";
//
//            getWsd().execSQL(sql);
//        }
    }

    private void replaceCommentLineMsg(CommentListBean list, String accountId) {

        deleteAllComments(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + CommentsTable.CommentsDataTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_COMMENTS_TABLE_SQL);

        addCommentLineMsg(list, accountId);
    }

    static void deleteAllComments(String accountId) {
        String sql = "delete from " + CommentsTable.CommentsDataTable.TABLE_NAME + " where " + CommentsTable.CommentsDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

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
        String sql = "select * from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(CommentsTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(CommentsTable.TABLE_NAME, cv, CommentsTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(CommentsTable.ACCOUNTID, accountId);
            cv.put(CommentsTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(CommentsTable.TABLE_NAME,
                    CommentsTable.ID, cv);
        }
    }

    public static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentsTable.TIMELINEDATA));
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

    public static void asyncReplace(final CommentListBean list, final String accountId) {
        final CommentListBean data = new CommentListBean();
        data.replaceAll(list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAllComments(accountId);
                addCommentLineMsg(data, accountId);
            }
        }).start();
    }
}
