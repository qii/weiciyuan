package org.qii.weiciyuan.support.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.qii.weiciyuan.support.database.table.AccountTable;
import org.qii.weiciyuan.support.database.table.GroupTable;
import org.qii.weiciyuan.support.database.table.HomeTable;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 上午9:40
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weibo.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_ACCOUNT_TABLE_SQL = "create table" + AccountTable.TABLE_NAME
            + "("
            + AccountTable.ID + "integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.PORTRAIT + " text,"
            + AccountTable.USERNAME + " text,"
            + AccountTable.USERNICK + " text,"
            + AccountTable.USERURL + " text"
            + ");";

    private static final String CREATE_GROUP_TABLE_SQL = "create table" + GroupTable.TABLE_NAME
            + "("
            + GroupTable.COUNT + " text,"
            + GroupTable.GID + " text,"
            + GroupTable.TITLE + " text,"
            + GroupTable.USER_ID + " text,"
            + ");";

    private static final String CREATE_HOME_TABLE_SQL = "create table" + HomeTable.TABLE_NAME
            + "("
            + HomeTable.MBLOGID + "integer primary key autoincrement,"
            + HomeTable.FEEDID + " text,"
            + HomeTable.MBLOGIDNUM + " text,"
            + HomeTable.GID + " text,"
            + HomeTable.GSID + " text,"
            + HomeTable.UID + " text,"
            + HomeTable.NICK + " text,"
            + HomeTable.PORTRAIT + " text,"
            + HomeTable.VIP + " text,"
            + HomeTable.CONTENT + " text,"
            + HomeTable.RTROOTUID + " text,"
            + HomeTable.RTROTNICK + " text,"
            + HomeTable.RTROOTVIP + " text,"
            + HomeTable.RTREASON + " text,"
            + HomeTable.TIME + " text,"
            + HomeTable.PIC + " text,"
            + HomeTable.SRC + " text,"
            + ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);
        db.execSQL(CREATE_GROUP_TABLE_SQL);
        db.execSQL(CREATE_HOME_TABLE_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
