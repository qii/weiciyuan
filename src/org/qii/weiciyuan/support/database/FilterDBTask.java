package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.qii.weiciyuan.support.database.table.FilterTable;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 13-1-7
 */
public class FilterDBTask {

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

    public static OAuthActivity.DBResult addFilterKeyword(String word) {

        ContentValues cv = new ContentValues();
        cv.put(FilterTable.NAME, word);
        cv.put(FilterTable.ACTIVE, "true");

        Cursor c = getRsd().query(FilterTable.TABLE_NAME, null, FilterTable.NAME + "=?",
                new String[]{word}, null, null, null);

        if (c != null && c.getCount() > 0) {

            return OAuthActivity.DBResult.update_successfully;
        } else {

            getWsd().insert(FilterTable.TABLE_NAME,
                    FilterTable.ID, cv);
            return OAuthActivity.DBResult.add_successfuly;
        }

    }


    public static List<String> getFilterList() {

        List<String> keywordList = new ArrayList<String>();
        String sql = "select * from " + FilterTable.TABLE_NAME + " order by " + FilterTable.ID + " desc ";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String word = c.getString(c.getColumnIndex(FilterTable.NAME));
            keywordList.add(word);
        }

        c.close();
        return keywordList;

    }

    public static void removeAndGetNewFilterList(String word) {

        String sql = "delete from " + FilterTable.TABLE_NAME + " where " + FilterTable.NAME + " = " + "\"" + word + "\"";

        getWsd().execSQL(sql);

    }

    public static List<String> removeAndGetNewFilterList(Set<String> words) {
        for (String word : words)
            removeAndGetNewFilterList(word);

        return getFilterList();
    }
}
