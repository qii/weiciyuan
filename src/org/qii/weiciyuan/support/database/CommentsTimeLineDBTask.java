package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.support.database.table.CommentsTable;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-7
 */
public class CommentsTimeLineDBTask {

    private CommentsTimeLineDBTask() {

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
        try {
            getWsd().beginTransaction();
            for (CommentBean msg : msgList) {
                ContentValues cv = new ContentValues();
                cv.put(CommentsTable.MBLOGID, msg.getId());
                cv.put(CommentsTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(CommentsTable.JSONDATA, json);
                getWsd().insert(CommentsTable.TABLE_NAME,
                        CommentsTable.ID, cv);
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
        }
        reduceCommentTable(accountId);
    }

    public static CommentListBean getCommentLineMsgList(String accountId) {

        CommentListBean result = new CommentListBean();

        List<CommentBean> msgList = new ArrayList<CommentBean>();
        String sql = "select * from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + "  = "
                + accountId + " order by " + CommentsTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentsTable.JSONDATA));
            try {
                CommentBean value = gson.fromJson(json, CommentBean.class);
                value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }
        }

        result.setComments(msgList);
        c.close();
        return result;

    }


    private static void reduceCommentTable(String accountId) {
        String searchCount = "select count(" + CommentsTable.ID + ") as total" + " from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getRsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - Integer.valueOf(SettingUtility.getMsgCount());

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ID + " in "
                    + "( select " + CommentsTable.ID + " from " + CommentsTable.TABLE_NAME + " where "
                    + CommentsTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + CommentsTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private void replaceCommentLineMsg(CommentListBean list, String accountId) {

        deleteAllComments(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_COMMENTS_TABLE_SQL);

        addCommentLineMsg(list, accountId);
    }

    static void deleteAllComments(String accountId) {
        String sql = "delete from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

}
