package org.qii.weiciyuan.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.qii.weiciyuan.bean.EmotionBean;
import org.qii.weiciyuan.support.database.table.EmotionsTable;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
