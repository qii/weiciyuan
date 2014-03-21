package org.qii.weiciyuan.support.database;

import org.qii.weiciyuan.support.database.table.NotificationTable;
import org.qii.weiciyuan.support.utils.AppConfig;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User: qii
 * Date: 14-2-22
 */
public class NotificationDBTask {

    public static enum UnreadDBType {
        mentionsWeibo("0"), mentionsComment("1"), commentsToMe("2"), dm("3");

        private final String value;

        UnreadDBType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private NotificationDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static void addUnreadNotification(String accountId, ArrayList<String> msgIds,
            UnreadDBType type) {

        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(),
                NotificationTable.TABLE_NAME);
        final int mblogidColumn = ih.getColumnIndex(NotificationTable.MSGID);
        final int accountidColumn = ih.getColumnIndex(NotificationTable.ACCOUNTID);
        final int typeColumn = ih.getColumnIndex(NotificationTable.TYPE);

        try {
            getWsd().beginTransaction();
            for (String msg : msgIds) {

                ih.prepareForReplace();
                ih.bind(mblogidColumn, msg);
                ih.bind(accountidColumn, accountId);
                ih.bind(typeColumn, type.getValue());
                ih.execute();

            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException ignored) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }
    }

    public static Set<String> getUnreadMsgIds(String accountId, UnreadDBType type) {
        HashSet<String> ids = new HashSet<String>();
        String sql = "select * from " + NotificationTable.TABLE_NAME + " where "
                + NotificationTable.ACCOUNTID + "  = "
                + accountId + " and " + NotificationTable.TYPE + " = " + type.getValue()
                + " order by " + NotificationTable.ID + " asc";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(NotificationTable.MSGID));
            ids.add(id);
        }
        c.close();
        return ids;
    }

    private static void cleanUnread(String accountId, UnreadDBType type) {
        String sql = "delete from " + NotificationTable.TABLE_NAME
                + " where " + NotificationTable.ACCOUNTID + " in " + "("
                + accountId + ")"
                + " and " + NotificationTable.TYPE + " = " + type.getValue();

        getWsd().execSQL(sql);
    }

    private static boolean needCleanDB(String accountId) {
        String searchCount = "select count(" + NotificationTable.MSGID + ") as total" + " from "
                + NotificationTable.TABLE_NAME + " where " + NotificationTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();
        return total >= AppConfig.DEFAULT_NOTIFICATION_UNREAD_DB_CACHE_COUNT;
    }


    public static void asyncCleanUnread(final String accountId, final UnreadDBType type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (needCleanDB(accountId)) {
                    cleanUnread(accountId, type);
                }

            }
        }).start();
    }
}
