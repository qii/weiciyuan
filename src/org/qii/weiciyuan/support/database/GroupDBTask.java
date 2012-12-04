package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.support.database.table.GroupTable;
import org.qii.weiciyuan.support.database.table.HomeTable;

/**
 * User: qii
 * Date: 12-11-5
 */
public class GroupDBTask {

    private GroupDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static GroupListBean get(String accountId) {

        String sql = "select * from " + GroupTable.TABLE_NAME + " where " + GroupTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        if (c.moveToNext()) {

            String json = c.getString(c.getColumnIndex(GroupTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                GroupListBean bean = new Gson().fromJson(json, GroupListBean.class);
                if (bean != null)
                    return bean;
            }
        }
        return null;
    }

    public static void update(GroupListBean bean, String accountId) {

        if (bean == null || bean.getLists().size() == 0) {
            return;
        }

        clearGroup(accountId);

        ContentValues cv = new ContentValues();
        cv.put(GroupTable.ACCOUNTID, accountId);
        cv.put(GroupTable.JSONDATA, new Gson().toJson(bean));
        getWsd().insert(GroupTable.TABLE_NAME,
                HomeTable.ID, cv);

    }

    private static void clearGroup(String accountId) {
        String sql = "delete from " + GroupTable.TABLE_NAME + " where "
                + GroupTable.ACCOUNTID + " = " + "\"" + accountId + "\"";
        getWsd().execSQL(sql);
    }
}
