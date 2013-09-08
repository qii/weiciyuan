package org.qii.weiciyuan.support.database;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.support.database.table.FilterTable;

import java.util.*;

/**
 * User: qii
 * Date: 13-1-7
 */
public class FilterDBTask {

    public static int TYPE_KEYWORD = 0;
    public static int TYPE_USER = 1;
    public static int TYPE_TOPIC = 2;
    public static int TYPE_SOURCE = 3;

    private FilterDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void addFilterKeyword(int type, String word) {
        Set<String> set = new HashSet<String>();
        set.add(word);
        addFilterKeyword(type, set);
    }

    public static void addFilterKeyword(int type, Collection<String> words) {

        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), FilterTable.TABLE_NAME);
        final int nameColumn = ih.getColumnIndex(FilterTable.NAME);
        final int activeColumn = ih.getColumnIndex(FilterTable.ACTIVE);
        final int typeColumn = ih.getColumnIndex(FilterTable.TYPE);
        try {
            getWsd().beginTransaction();
            for (String word : words) {
                ih.prepareForInsert();

                ih.bind(nameColumn, word);
                ih.bind(activeColumn, true);
                ih.bind(typeColumn, type);

                ih.execute();
            }

            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }

    }


    public static List<String> getFilterKeywordList(int type) {

        List<String> keywordList = new ArrayList<String>();
        String sql = "select * from " + FilterTable.TABLE_NAME + " where " + FilterTable.TYPE
                + "= " + type + " order by " + FilterTable.ID + " desc ";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String word = c.getString(c.getColumnIndex(FilterTable.NAME));
            keywordList.add(word);
        }

        c.close();
        return keywordList;

    }

    private static void removeAndGetFilterKeywordList(int type, String word) {

        String sql = "delete from " + FilterTable.TABLE_NAME + " where " + FilterTable.TYPE + " = " + type + " and " +
                FilterTable.NAME + " = " + "\"" + word + "\"";

        getWsd().execSQL(sql);

    }

    public static List<String> removeAndGetNewFilterKeywordList(int type, Collection<String> words) {
        for (String word : words)
            removeAndGetFilterKeywordList(type, word);

        return getFilterKeywordList(type);
    }


}
