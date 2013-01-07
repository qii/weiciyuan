package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.qii.weiciyuan.bean.EmotionBean;
import org.qii.weiciyuan.support.database.table.EmotionsTable;
import org.qii.weiciyuan.support.database.table.FilterTable;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.*;

/**
 * User: qii
 * Date: 12-7-30
 */
public class DatabaseManager {

    private static DatabaseManager singleton = null;


    private SQLiteDatabase wsd = null;

    private SQLiteDatabase rsd = null;


    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {

        if (singleton == null) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
            SQLiteDatabase wsd = databaseHelper.getWritableDatabase();
            SQLiteDatabase rsd = databaseHelper.getReadableDatabase();

            singleton = new DatabaseManager();
            singleton.wsd = wsd;
            singleton.rsd = rsd;
        }

        return singleton;
    }


    public OAuthActivity.DBResult addFilterKeyword(String word) {

        ContentValues cv = new ContentValues();
        cv.put(FilterTable.NAME, word);
        cv.put(FilterTable.ACTIVE, "true");

        Cursor c = rsd.query(FilterTable.TABLE_NAME, null, FilterTable.NAME + "=?",
                new String[]{word}, null, null, null);

        if (c != null && c.getCount() > 0) {

            return OAuthActivity.DBResult.update_successfully;
        } else {

            wsd.insert(FilterTable.TABLE_NAME,
                    FilterTable.ID, cv);
            return OAuthActivity.DBResult.add_successfuly;
        }

    }


    public List<String> getFilterList() {

        List<String> keywordList = new ArrayList<String>();
        String sql = "select * from " + FilterTable.TABLE_NAME + " order by " + FilterTable.ID + " desc ";
        Cursor c = rsd.rawQuery(sql, null);
        while (c.moveToNext()) {
            String word = c.getString(c.getColumnIndex(FilterTable.NAME));
            keywordList.add(word);
        }

        c.close();
        return keywordList;

    }

    public void removeAndGetNewFilterList(String word) {

        String sql = "delete from " + FilterTable.TABLE_NAME + " where " + FilterTable.NAME + " = " + "\"" + word + "\"";

        wsd.execSQL(sql);

    }

    public List<String> removeAndGetNewFilterList(Set<String> words) {
        for (String word : words)
            removeAndGetNewFilterList(word);

        return getFilterList();
    }


    public OAuthActivity.DBResult addEmotions(List<EmotionBean> word) {

        ContentValues cv = new ContentValues();
        cv.put(EmotionsTable.JSONDATA, new Gson().toJson(word));

        wsd.execSQL("DROP TABLE IF EXISTS " + EmotionsTable.TABLE_NAME);
        wsd.execSQL(DatabaseHelper.CREATE_EMOTIONS_TABLE_SQL);

        wsd.insert(EmotionsTable.TABLE_NAME,
                EmotionsTable.ID, cv);
        return OAuthActivity.DBResult.add_successfuly;

    }


    public Map<String, String> getEmotionsMap() {
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<String, String>();
        String sql = "select * from " + EmotionsTable.TABLE_NAME + " order by " + EmotionsTable.ID + " limit 1 ";
        Cursor c = rsd.rawQuery(sql, null);
        if (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(EmotionsTable.JSONDATA));
            try {
                List<EmotionBean> value = gson.fromJson(json, new TypeToken<ArrayList<EmotionBean>>() {
                }.getType());

                for (EmotionBean bean : value) {
                    map.put(bean.getPhrase(), bean.getUrl());

                }

            } catch (JsonSyntaxException e) {

                AppLogger.e(e.getMessage());
            }
        }

        c.close();
        return map;
    }
}
