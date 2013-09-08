package org.qii.weiciyuan.support.database;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.support.database.table.TopicTable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-7-4
 */
public class TopicDBTask {

    private TopicDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void clear(String accountId) {
        String sql = "delete from " + TopicTable.TABLE_NAME + " where " + TopicTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static ArrayList<String> get(String accountId) {
        ArrayList<String> result = new ArrayList<String>();

        String sql = "select * from " + TopicTable.TABLE_NAME + " where " + TopicTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String topic = c.getString(c.getColumnIndex(TopicTable.TOPIC_NAME));
            result.add(topic);
        }
        c.close();
        return result;
    }

    private static void add(String accountId, List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), TopicTable.TABLE_NAME);
        final int accountidColumn = ih.getColumnIndex(TopicTable.ACCOUNTID);
        final int nameColumn = ih.getColumnIndex(TopicTable.TOPIC_NAME);
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                String name = list.get(i);
                ih.prepareForInsert();
                ih.bind(accountidColumn, accountId);
                ih.bind(nameColumn, name);
                ih.execute();
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }

    }

    public static void asyncReplace(final String accountId, final List<String> value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clear(accountId);
                add(accountId, value);
            }
        }).start();
    }
}
