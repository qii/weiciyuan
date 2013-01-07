package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.support.database.table.CommentsTable;
import org.qii.weiciyuan.support.database.table.EmotionsTable;
import org.qii.weiciyuan.support.database.table.FilterTable;
import org.qii.weiciyuan.support.database.table.RepostsTable;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.*;

/**
 * User: qii
 * Date: 12-7-30
 */
public class DatabaseManager {

    private static DatabaseManager singleton = null;


    private SQLiteDatabase wsd = null;

    private SQLiteDatabase rsd = null;


    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {

        if (singleton == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            singleton = new DatabaseManager();
            singleton.wsd = wsd;
            singleton.rsd = rsd;
        }

        return singleton;
    }


    public MessageListBean getRepostLineMsgList(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + "  = "
                + accountId + " order by " + RepostsTable.MBLOGID + " desc limit 50";
        Cursor c = rsd.rawQuery(sql, null);
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

    public void addRepostLineMsg(MessageListBean list, String accountId) {
        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();
        int size = msgList.size();
        for (int i = 0; i < size; i++) {
            MessageBean msg = msgList.get(i);
            ContentValues cv = new ContentValues();
            cv.put(RepostsTable.MBLOGID, msg.getId());
            cv.put(RepostsTable.ACCOUNTID, accountId);
            String json = gson.toJson(msg);
            cv.put(RepostsTable.JSONDATA, json);
            wsd.insert(RepostsTable.TABLE_NAME,
                    RepostsTable.ID, cv);
        }

        reduceRepostTable(accountId);
    }


    private void reduceRepostTable(String accountId) {
        String searchCount = "select count(" + RepostsTable.ID + ") as total" + " from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = rsd.rawQuery(searchCount, null);
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

            wsd.execSQL(sql);
        }
    }

    private void replaceRepostLineMsg(MessageListBean list, String accountId) {

        deleteAllReposts(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + RepostsTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_REPOSTS_TABLE_SQL);

        addRepostLineMsg(list, accountId);
    }

    void deleteAllReposts(String accountId) {
        String sql = "delete from " + RepostsTable.TABLE_NAME + " where " + RepostsTable.ACCOUNTID + " in " + "(" + accountId + ")";

        wsd.execSQL(sql);
    }

    public void addCommentLineMsg(CommentListBean list, String accountId) {
        Gson gson = new Gson();
        List<CommentBean> msgList = list.getItemList();
        for (CommentBean msg : msgList) {
            ContentValues cv = new ContentValues();
            cv.put(CommentsTable.MBLOGID, msg.getId());
            cv.put(CommentsTable.ACCOUNTID, accountId);
            String json = gson.toJson(msg);
            cv.put(CommentsTable.JSONDATA, json);
            wsd.insert(CommentsTable.TABLE_NAME,
                    CommentsTable.ID, cv);
        }
        reduceCommentTable(accountId);
    }

    public CommentListBean getCommentLineMsgList(String accountId) {

        CommentListBean result = new CommentListBean();

        List<CommentBean> msgList = new ArrayList<CommentBean>();
        String sql = "select * from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + "  = "
                + accountId + " order by " + CommentsTable.MBLOGID + " desc limit 50";
        Cursor c = rsd.rawQuery(sql, null);
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


    private void reduceCommentTable(String accountId) {
        String searchCount = "select count(" + CommentsTable.ID + ") as total" + " from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = rsd.rawQuery(searchCount, null);
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

            wsd.execSQL(sql);
        }
    }

    private void replaceCommentLineMsg(CommentListBean list, String accountId) {

        deleteAllComments(accountId);

        //need modification
//        wsd.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
//        wsd.execSQL(DatabaseHelper.CREATE_COMMENTS_TABLE_SQL);

        addCommentLineMsg(list, accountId);
    }

    void deleteAllComments(String accountId) {
        String sql = "delete from " + CommentsTable.TABLE_NAME + " where " + CommentsTable.ACCOUNTID + " in " + "(" + accountId + ")";

        wsd.execSQL(sql);
    }


    public OAuthActivity.DBResult addFilterKeyword(String word) {

        ContentValues cv = new ContentValues();
        cv.put(FilterTable.NAME, word);
        cv.put(FilterTable.ACTIVE, "true");

        Cursor c = rsd.query(FilterTable.TABLE_NAME, null, FilterTable.NAME + "=?",
                new String[]{word}, null, null, null);

        if (c != null && c.getCount() > 0) {

            return OAuthActivity.DBResult.update_successfully;
        } else {

            wsd.insert(FilterTable.TABLE_NAME,
                    FilterTable.ID, cv);
            return OAuthActivity.DBResult.add_successfuly;
        }

    }


    public List<String> getFilterList() {

        List<String> keywordList = new ArrayList<String>();
        String sql = "select * from " + FilterTable.TABLE_NAME + " order by " + FilterTable.ID + " desc ";
        Cursor c = rsd.rawQuery(sql, null);
        while (c.moveToNext()) {
            String word = c.getString(c.getColumnIndex(FilterTable.NAME));
            keywordList.add(word);
        }

        c.close();
        return keywordList;

    }

    public void removeAndGetNewFilterList(String word) {

        String sql = "delete from " + FilterTable.TABLE_NAME + " where " + FilterTable.NAME + " = " + "\"" + word + "\"";

        wsd.execSQL(sql);

    }

    public List<String> removeAndGetNewFilterList(Set<String> words) {
        for (String word : words)
            removeAndGetNewFilterList(word);

        return getFilterList();
    }


    public OAuthActivity.DBResult addEmotions(List<EmotionBean> word) {

        ContentValues cv = new ContentValues();
        cv.put(EmotionsTable.JSONDATA, new Gson().toJson(word));

        wsd.execSQL("DROP TABLE IF EXISTS " + EmotionsTable.TABLE_NAME);
        wsd.execSQL(DatabaseHelper.CREATE_EMOTIONS_TABLE_SQL);

        wsd.insert(EmotionsTable.TABLE_NAME,
                EmotionsTable.ID, cv);
        return OAuthActivity.DBResult.add_successfuly;

    }


    public Map<String, String> getEmotionsMap() {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        String sql = "select * from " + EmotionsTable.TABLE_NAME + " order by " + EmotionsTable.ID + " limit 1 ";
        Cursor c = rsd.rawQuery(sql, null);
        if (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(EmotionsTable.JSONDATA));
            try {
                List<EmotionBean> value = gson.fromJson(json, new TypeToken<ArrayList<EmotionBean>>() {
                }.getType());

                for (EmotionBean bean : value) {
                    map.put(bean.getPhrase(), bean.getUrl());

                }

            } catch (JsonSyntaxException e) {

                AppLogger.e(e.getMessage());
            }
        }

        c.close();
        return map;
    }
}
