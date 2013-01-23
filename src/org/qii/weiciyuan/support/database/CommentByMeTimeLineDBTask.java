package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.support.database.table.CommentByMeTable;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-23
 */
public class CommentByMeTimeLineDBTask {

    private CommentByMeTimeLineDBTask() {

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
        for (CommentBean msg : msgList) {
            ContentValues cv = new ContentValues();
            cv.put(CommentByMeTable.MBLOGID, msg.getId());
            cv.put(CommentByMeTable.ACCOUNTID, accountId);
            String json = gson.toJson(msg);
            cv.put(CommentByMeTable.JSONDATA, json);
            getWsd().insert(CommentByMeTable.TABLE_NAME,
                    CommentByMeTable.ID, cv);
        }
        reduceCommentTable(accountId);
    }

    public static CommentListBean getCommentLineMsgList(String accountId) {

        CommentListBean result = new CommentListBean();

        List<CommentBean> msgList = new ArrayList<CommentBean>();
        String sql = "select * from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ACCOUNTID + "  = "
                + accountId + " order by " + CommentByMeTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentByMeTable.JSONDATA));
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
        String searchCount = "select count(" + CommentByMeTable.ID + ") as total" + " from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ACCOUNTID
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
            String sql = " delete from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ID + " in "
                    + "( select " + CommentByMeTable.ID + " from " + CommentByMeTable.TABLE_NAME + " where "
                    + CommentByMeTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + CommentByMeTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private void replaceCommentLineMsg(CommentListBean list, String accountId) {

        deleteAllComments(accountId);

        //need modification
        //        wsd.execSQL("DROP TABLE IF EXISTS " + CommentByMeTable.TABLE_NAME);
        //        wsd.execSQL(DatabaseHelper.CREATE_COMMENTS_TABLE_SQL);

        addCommentLineMsg(list, accountId);
    }

    static void deleteAllComments(String accountId) {
        String sql = "delete from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

}
