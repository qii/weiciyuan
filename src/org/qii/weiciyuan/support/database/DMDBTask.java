package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.DMUserBean;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.support.database.table.DMTable;

/**
 * User: qii
 * Date: 12-12-3
 */
public class DMDBTask {

    private DMDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void add(DMUserListBean list, String accountId) {

        if (list == null || list.getSize() == 0) {
            return;
        }

        clear(accountId);

        Gson gson = new Gson();

        ContentValues cv = new ContentValues();
        cv.put(DMTable.MBLOGID, list.getItem(0).getId());
        cv.put(DMTable.ACCOUNTID, accountId);
        String json = gson.toJson(list);
        cv.put(DMTable.JSONDATA, json);
        getWsd().insert(DMTable.TABLE_NAME,
                DMTable.ID, cv);
    }

    public static void asyncReplace(final DMUserListBean list, final String accountId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                clear(accountId);
                add(list, accountId);
            }
        }).start();
    }

    public static void clear(String accountId) {
        String sql = "delete from " + DMTable.TABLE_NAME + " where " + DMTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static DMUserListBean get(String accountId) {
        Gson gson = new Gson();
        DMUserListBean result = new DMUserListBean();

        String sql = "select * from " + DMTable.TABLE_NAME + " where " + DMTable.ACCOUNTID + "  = "
                + accountId + " order by " + DMTable.MBLOGID + " desc limit 1";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(DMTable.JSONDATA));
            try {
                DMUserListBean value = gson.fromJson(json, DMUserListBean.class);
                for (DMUserBean b : value.getItemList()) {
                    b.getListViewSpannableString();
                    b.getListviewItemShowTime();
                }
                return value;
            } catch (JsonSyntaxException ignored) {

            }

        }

        c.close();
        return result;

    }

}
