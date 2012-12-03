package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.database.draftbean.*;
import org.qii.weiciyuan.support.database.table.DraftTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 12-10-21
 */
public class DraftDBManager {
    private static DraftDBManager singleton = null;


    private SQLiteDatabase wsd = null;

    private SQLiteDatabase rsd = null;


    private DraftDBManager() {

    }

    public static DraftDBManager getInstance() {

        if (singleton == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            singleton = new DraftDBManager();
            singleton.wsd = wsd;
            singleton.rsd = rsd;
        }

        return singleton;
    }


    public void insertStatus(String content, GeoBean gps, String pic, String accountId) {
        ContentValues cv = new ContentValues();
        cv.put(DraftTable.CONTENT, content);
        cv.put(DraftTable.ACCOUNTID, accountId);
        if (gps != null)
            cv.put(DraftTable.GPS, new Gson().toJson(gps));
        if (!TextUtils.isEmpty(pic))
            cv.put(DraftTable.PIC, pic);
        cv.put(DraftTable.TYPE, DraftTable.TYPE_WEIBO);
        wsd.insert(DraftTable.TABLE_NAME,
                DraftTable.ID, cv);
    }

    public void insertRepost(String content, MessageBean messageBean, String accountId) {
        ContentValues cv = new ContentValues();
        cv.put(DraftTable.CONTENT, content);
        cv.put(DraftTable.ACCOUNTID, accountId);
        cv.put(DraftTable.JSONDATA, new Gson().toJson(messageBean));
        cv.put(DraftTable.TYPE, DraftTable.TYPE_REPOST);
        wsd.insert(DraftTable.TABLE_NAME,
                DraftTable.ID, cv);
    }

    public void insertComment(String content, MessageBean messageBean, String accountId) {
        ContentValues cv = new ContentValues();
        cv.put(DraftTable.CONTENT, content);
        cv.put(DraftTable.ACCOUNTID, accountId);
        cv.put(DraftTable.JSONDATA, new Gson().toJson(messageBean));
        cv.put(DraftTable.TYPE, DraftTable.TYPE_COMMENT);
        wsd.insert(DraftTable.TABLE_NAME,
                DraftTable.ID, cv);
    }

    public void insertReply(String content, CommentBean commentBean, String accountId) {
        ContentValues cv = new ContentValues();
        cv.put(DraftTable.CONTENT, content);
        cv.put(DraftTable.ACCOUNTID, accountId);
        cv.put(DraftTable.JSONDATA, new Gson().toJson(commentBean));
        cv.put(DraftTable.TYPE, DraftTable.TYPE_REPLY);
        wsd.insert(DraftTable.TABLE_NAME,
                DraftTable.ID, cv);
    }


    public List<DraftListViewItemBean> getDraftList(String accountId) {

        Gson gson = new Gson();
        List<DraftListViewItemBean> result = new ArrayList<DraftListViewItemBean>();

        String sql = "select * from " + DraftTable.TABLE_NAME + " where " + DraftTable.ACCOUNTID + "  = "
                + accountId + " order by " + DraftTable.ID + " desc";
        Cursor c = rsd.rawQuery(sql, null);
        while (c.moveToNext()) {
            DraftListViewItemBean item = new DraftListViewItemBean();
            int type = c.getInt(c.getColumnIndex(DraftTable.TYPE));
            item.setType(type);
            item.setId(c.getString(c.getColumnIndex(DraftTable.ID)));
            String content;
            switch (type) {
                case DraftTable.TYPE_WEIBO:

                    content = c.getString(c.getColumnIndex(DraftTable.CONTENT));
                    StatusDraftBean bean = new StatusDraftBean();
                    bean.setId(c.getString(c.getColumnIndex(DraftTable.ID)));
                    bean.setContent(content);
                    bean.setAccountId(accountId);
                    String gpsJson = c.getString(c.getColumnIndex(DraftTable.GPS));
                    if (!TextUtils.isEmpty(gpsJson)) {
                        bean.setGps(new Gson().fromJson(gpsJson, GeoBean.class));
                    }
                    bean.setPic(c.getString(c.getColumnIndex(DraftTable.PIC)));
                    item.setStatusDraftBean(bean);
                    result.add(item);
                    break;
                case DraftTable.TYPE_REPOST:
                    content = c.getString(c.getColumnIndex(DraftTable.CONTENT));
                    RepostDraftBean repostDraftBean = new RepostDraftBean();
                    repostDraftBean.setId(c.getString(c.getColumnIndex(DraftTable.ID)));

                    repostDraftBean.setContent(content);
                    repostDraftBean.setAccountId(accountId);

                    MessageBean messageBean = gson.fromJson(c.getString(c.getColumnIndex(DraftTable.JSONDATA)), MessageBean.class);
                    repostDraftBean.setMessageBean(messageBean);

                    item.setRepostDraftBean(repostDraftBean);
                    result.add(item);
                    break;
                case DraftTable.TYPE_COMMENT:
                    content = c.getString(c.getColumnIndex(DraftTable.CONTENT));
                    CommentDraftBean commentDraftBean = new CommentDraftBean();
                    commentDraftBean.setId(c.getString(c.getColumnIndex(DraftTable.ID)));

                    commentDraftBean.setContent(content);
                    commentDraftBean.setAccountId(accountId);

                    MessageBean commentMessageBean = gson.fromJson(c.getString(c.getColumnIndex(DraftTable.JSONDATA)), MessageBean.class);
                    commentDraftBean.setMessageBean(commentMessageBean);

                    item.setCommentDraftBean(commentDraftBean);
                    result.add(item);
                    break;
                case DraftTable.TYPE_REPLY:
                    content = c.getString(c.getColumnIndex(DraftTable.CONTENT));
                    ReplyDraftBean replyDraftBean = new ReplyDraftBean();
                    replyDraftBean.setId(c.getString(c.getColumnIndex(DraftTable.ID)));

                    replyDraftBean.setContent(content);
                    replyDraftBean.setAccountId(accountId);

                    CommentBean commentBean = gson.fromJson(c.getString(c.getColumnIndex(DraftTable.JSONDATA)), CommentBean.class);
                    replyDraftBean.setCommentBean(commentBean);

                    item.setReplyDraftBean(replyDraftBean);
                    result.add(item);
                    break;
            }


        }

        c.close();
        return result;

    }


    public List<DraftListViewItemBean> removeAndGet(Set<String> checkedItemPosition, String acountId) {
        String[] args = checkedItemPosition.toArray(new String[0]);
        String asString = Arrays.toString(args);
        asString = asString.replace("[", "(");
        asString = asString.replace("]", ")");

        String sql = "delete from " + DraftTable.TABLE_NAME + " where " + DraftTable.ID + " in " + asString;

        wsd.execSQL(sql);
        return getDraftList(acountId);
    }

    public void remove(String id) {
        String sql = "delete from " + DraftTable.TABLE_NAME + " where " + DraftTable.ID + " = " + id;

        wsd.execSQL(sql);
    }

}
