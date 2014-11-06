package org.qii.weiciyuan.support.database;

import org.qii.weiciyuan.support.database.table.DownloadPicturesTable;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.file.FileLocationMethod;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * User: qii
 * Date: 14-6-14
 */
public class DownloadPicturesDBTask {

    //300mb
    private static final long MAX_DISK_CACHE = 300L;

    private DownloadPicturesDBTask() {

    }

    private static SQLiteDatabase getWsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    public static void add(String url, String path, FileLocationMethod method) {
        ContentValues cv = new ContentValues();
        cv.put(DownloadPicturesTable.URL, url);
        cv.put(DownloadPicturesTable.PATH, path);
        cv.put(DownloadPicturesTable.TIME, System.currentTimeMillis());

        int type = DownloadPicturesTable.TYPE_OTHER;
        switch (method) {
            case avatar_small:
            case avatar_large:
                type = DownloadPicturesTable.TYPE_AVATAR;
                break;
        }
        cv.put(DownloadPicturesTable.TYPE, type);

        long size = new File(path).length();

        cv.put(DownloadPicturesTable.SIZE, size);

        getWsd().replace(DownloadPicturesTable.TABLE_NAME,
                DownloadPicturesTable.URL, cv);

        synchronized (DownloadPicturesDBTask.class) {
            trimToSize();
        }
    }

    public static String get(String url) {

        Cursor c = getRsd()
                .query(DownloadPicturesTable.TABLE_NAME, null, DownloadPicturesTable.URL + "=?",
                        new String[]{url}, null, null, null);

        String path = null;
        while (c.moveToNext()) {
            path = c.getString(c.getColumnIndex(DownloadPicturesTable.PATH));
            break;
        }

        c.close();

        if (!TextUtils.isEmpty(path)) {
            ContentValues cv = new ContentValues();
            cv.put(DownloadPicturesTable.TIME, System.currentTimeMillis());
            getWsd().update(DownloadPicturesTable.TABLE_NAME, cv, DownloadPicturesTable.PATH + "=?",
                    new String[]{path});
        }
        return path;
    }

    public static void remove(String url) {
        String sql = "delete from " + DownloadPicturesTable.TABLE_NAME + " where "
                + DownloadPicturesTable.URL
                + " = " + url;
        getWsd().execSQL(sql);
    }

    //return mb;
    public static void trimToSize() {

        long total = 0L;

        String sql = "select sum(" + DownloadPicturesTable.SIZE + ")" + " from "
                + DownloadPicturesTable.TABLE_NAME;

        Cursor cursor = getRsd().rawQuery(
                sql, null);
        if (cursor.moveToFirst()) {
            long size = cursor.getLong(0);
            total = size / 1024L / 1024L;
        }

        cursor.close();

        AppLogger.v("weiciyuan picture cache size: " + total + "mb");

        if (total < MAX_DISK_CACHE) {
            return;
        }

        Cursor c = getRsd()
                .query(DownloadPicturesTable.TABLE_NAME, null, null,
                        null, null, null,
                        DownloadPicturesTable.TIME + " asc LIMIT 100");

        ArrayList<String> pathList = new ArrayList<String>();

        while (c.moveToNext()) {
            String path = c.getString(c.getColumnIndex(DownloadPicturesTable.PATH));
            pathList.add(path);
        }

        c.close();

        String[] pathArray = pathList.toArray(new String[pathList.size()]);

        StringBuilder stringBuilder = new StringBuilder();

        int length = pathArray.length;

        for (int i = 0; i < length; i++) {
            if (i < length - 1) {
                stringBuilder.append("\"").append(pathArray[i]).append("\",");
            } else {
                stringBuilder.append("\"").append(pathArray[i]).append("\"");
            }
        }

        String deleteSql = "delete from " + DownloadPicturesTable.TABLE_NAME + " where "
                + DownloadPicturesTable.PATH + " in (%s)";
        deleteSql = String.format(deleteSql, stringBuilder.toString());

        getWsd().execSQL(deleteSql);

        for (String path : pathList) {
            new File(path).delete();
        }

        trimToSize();
    }

    public static void clearAll() {
        getWsd().delete(DownloadPicturesTable.TABLE_NAME, null, null);
    }
}
