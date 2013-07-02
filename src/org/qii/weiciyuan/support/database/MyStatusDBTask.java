package org.qii.weiciyuan.support.database;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.database.table.MyStatusTable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-12-4
 */
public class MyStatusDBTask {

    private MyStatusDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void add(MessageListBean list, String accountId) {

        if (list == null || list.getSize() == 0) {
            return;
        }


        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), MyStatusTable.StatusDataTable.TABLE_NAME);
        final int mblogidColumn = ih.getColumnIndex(MyStatusTable.StatusDataTable.MBLOGID);
        final int accountidColumn = ih.getColumnIndex(MyStatusTable.StatusDataTable.ACCOUNTID);
        final int jsondataColumn = ih.getColumnIndex(MyStatusTable.StatusDataTable.JSONDATA);
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < msgList.size(); i++) {
                MessageBean msg = msgList.get(i);
                ih.prepareForInsert();
                if (msg != null) {
                    ih.bind(mblogidColumn, msg.getId());
                    ih.bind(accountidColumn, accountId);
                    String json = gson.toJson(msg);
                    ih.bind(jsondataColumn, json);
                } else {
                    ih.bind(mblogidColumn, "-1");
                    ih.bind(accountidColumn, accountId);
                    ih.bind(jsondataColumn, "");
                }
                ih.execute();
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }


    }


    public static void clear(String accountId) {
        String sql = "delete from " + MyStatusTable.StatusDataTable.TABLE_NAME + " where " + MyStatusTable.StatusDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static MessageListBean get(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + MyStatusTable.StatusDataTable.TABLE_NAME + " where " + MyStatusTable.StatusDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + MyStatusTable.StatusDataTable.MBLOGID + " desc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(MyStatusTable.StatusDataTable.JSONDATA));
            try {
                MessageBean value = gson.fromJson(json, MessageBean.class);
                value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException ignored) {

            }

        }

        result.setStatuses(msgList);
        c.close();
        return result;

    }

    public static void asyncReplace(final MessageListBean data, final String accountId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clear(accountId);
                add(data, accountId);
            }
        }).start();
    }
}
