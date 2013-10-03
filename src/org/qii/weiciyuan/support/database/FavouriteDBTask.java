package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.qii.weiciyuan.bean.FavBean;
import org.qii.weiciyuan.bean.FavListBean;
import org.qii.weiciyuan.bean.android.FavouriteTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.support.database.table.FavouriteTable;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-5-30
 */
public class FavouriteDBTask {
    private FavouriteDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void add(FavListBean list, int page, String accountId) {
        Gson gson = new Gson();
        List<FavBean> msgList = list.getFavorites();

        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), FavouriteTable.FavouriteDataTable.TABLE_NAME);
        final int mblogidColumn = ih.getColumnIndex(FavouriteTable.FavouriteDataTable.MBLOGID);
        final int accountidColumn = ih.getColumnIndex(FavouriteTable.FavouriteDataTable.ACCOUNTID);
        final int jsondataColumn = ih.getColumnIndex(FavouriteTable.FavouriteDataTable.JSONDATA);

        try {
            getWsd().beginTransaction();
            for (FavBean msg : msgList) {
                ih.prepareForInsert();
                ih.bind(mblogidColumn, msg.getStatus().getId());
                ih.bind(accountidColumn, accountId);
                String json = gson.toJson(msg);
                ih.bind(jsondataColumn, json);
                ih.execute();
            }

            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {

        } finally {
            getWsd().endTransaction();
            ih.close();
        }

        String sql = "select * from " + FavouriteTable.TABLE_NAME + " where " + FavouriteTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(FavouriteTable.PAGE, page);
                getWsd().update(FavouriteTable.TABLE_NAME, cv, FavouriteTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(FavouriteTable.ACCOUNTID, accountId);
            cv.put(FavouriteTable.PAGE, page);
            getWsd().insert(FavouriteTable.TABLE_NAME,
                    FavouriteTable.ID, cv);
        }

    }

    public static FavouriteTimeLineData getFavouriteMsgList(String accountId) {

        FavListBean result = new FavListBean();

        List<FavBean> msgList = new ArrayList<FavBean>();
        String sql = "select * from " + FavouriteTable.FavouriteDataTable.TABLE_NAME + " where " + FavouriteTable.FavouriteDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + FavouriteTable.FavouriteDataTable.MBLOGID + " desc";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(FavouriteTable.FavouriteDataTable.JSONDATA));
            try {
                FavBean value = gson.fromJson(json, FavBean.class);
                if (value != null)
                    value.getStatus().getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }
        }

        result.setFavorites(msgList);
        c.close();

        sql = "select * from " + FavouriteTable.TABLE_NAME + " where " + FavouriteTable.ACCOUNTID + "  = "
                + accountId;
        c = getRsd().rawQuery(sql, null);
        int page = 0;
        while (c.moveToNext()) {
            page = c.getInt(c.getColumnIndex(FavouriteTable.PAGE));
        }
        c.close();
        return new FavouriteTimeLineData(result, page, getPosition(accountId));

    }


    static void deleteAllFavourites(String accountId) {
        String sql = "delete from " + FavouriteTable.FavouriteDataTable.TABLE_NAME + " where " + FavouriteTable.FavouriteDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);

        sql = "delete from " + FavouriteTable.TABLE_NAME + " where " + FavouriteTable.ACCOUNTID + " in " + "(" + accountId + ")";
        getWsd().execSQL(sql);
    }

    public static void asyncUpdatePosition(final TimeLinePosition position, final String accountId) {
        if (position == null)
            return;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updatePosition(position, accountId);
            }
        };

        new Thread(runnable).start();
    }


    private static void updatePosition(TimeLinePosition position, String accountId) {
        String sql = "select * from " + FavouriteTable.TABLE_NAME + " where " + FavouriteTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(FavouriteTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(FavouriteTable.TABLE_NAME, cv, FavouriteTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(FavouriteTable.ACCOUNTID, accountId);
            cv.put(FavouriteTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(FavouriteTable.TABLE_NAME,
                    FavouriteTable.ID, cv);
        }
    }

    private static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + FavouriteTable.TABLE_NAME + " where " + FavouriteTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(FavouriteTable.TIMELINEDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    TimeLinePosition value = gson.fromJson(json, TimeLinePosition.class);
                    return value;

                } catch (JsonSyntaxException e) {

                }
            }

        }
        c.close();
        return new TimeLinePosition(0, 0);
    }

    public static void asyncReplace(final FavListBean data, final int page, final String accountId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAllFavourites(accountId);
                add(data, page, accountId);
            }
        }).start();
    }

}
