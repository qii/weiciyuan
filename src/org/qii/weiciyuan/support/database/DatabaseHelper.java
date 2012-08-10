package org.qii.weiciyuan.support.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.qii.weiciyuan.support.database.table.*;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 * Time: 上午9:40
 */
class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "weibo.db";
    private static final int DATABASE_VERSION = 9;

    static final String CREATE_ACCOUNT_TABLE_SQL = "create table " + AccountTable.TABLE_NAME
            + "("
            + AccountTable.UID + " integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.PORTRAIT + " text,"
            + AccountTable.USERNAME + " text,"
            + AccountTable.USERNICK + " text,"
            + AccountTable.AVATAR_URL + " text"
            + ");";

    static final String CREATE_GROUP_TABLE_SQL = "create table " + GroupTable.TABLE_NAME
            + "("
            + GroupTable.COUNT + " text,"
            + GroupTable.GID + " text,"
            + GroupTable.TITLE + " text,"
            + GroupTable.USER_ID + " text"
            + ");";

    static final String CREATE_HOME_TABLE_SQL = "create table " + HomeTable.TABLE_NAME
            + "("
            + HomeTable.ID + " integer primary key autoincrement,"
            + HomeTable.ACCOUNTID + " text,"
            + HomeTable.MBLOGID + " text,"
            + HomeTable.FEEDID + " text,"
            + HomeTable.AVATAR + " text,"
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
            + HomeTable.RTAVATAR + " text,"
            + HomeTable.RTPIC + " text,"
            + HomeTable.RTCONTENT + " text,"
            + HomeTable.RTID + " text,"
            + HomeTable.TIME + " text,"
            + HomeTable.PIC + " text,"
            + HomeTable.SRC + " text"
            + ");";

    static final String CREATE_COMMENTS_TABLE_SQL = "create table " + CommentsTable.TABLE_NAME
            + "("
            + CommentsTable.ID + " integer primary key autoincrement,"
            + CommentsTable.ACCOUNTID + " text,"
            + CommentsTable.MBLOGID + " text,"
            + CommentsTable.JSONDATA + " text"
            + ");";


    static final String CREATE_REPOSTS_TABLE_SQL = "create table " + RepostsTable.TABLE_NAME
            + "("
            + RepostsTable.ID + " integer primary key autoincrement,"
            + RepostsTable.ACCOUNTID + " text,"
            + RepostsTable.MBLOGID + " text,"
            + RepostsTable.FEEDID + " text,"
            + RepostsTable.AVATAR + " text,"
            + RepostsTable.MBLOGIDNUM + " text,"
            + RepostsTable.GID + " text,"
            + RepostsTable.GSID + " text,"
            + RepostsTable.UID + " text,"
            + RepostsTable.NICK + " text,"
            + RepostsTable.PORTRAIT + " text,"
            + RepostsTable.VIP + " text,"
            + RepostsTable.CONTENT + " text,"
            + RepostsTable.RTROOTUID + " text,"
            + RepostsTable.RTROTNICK + " text,"
            + RepostsTable.RTROOTVIP + " text,"
            + RepostsTable.RTREASON + " text,"
            + RepostsTable.RTAVATAR + " text,"
            + RepostsTable.RTPIC + " text,"
            + RepostsTable.RTCONTENT + " text,"
            + RepostsTable.RTID + " text,"
            + RepostsTable.TIME + " text,"
            + RepostsTable.PIC + " text,"
            + RepostsTable.SRC + " text"
            + ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);
        db.execSQL(CREATE_GROUP_TABLE_SQL);
        db.execSQL(CREATE_HOME_TABLE_SQL);
        db.execSQL(CREATE_COMMENTS_TABLE_SQL);
        db.execSQL(CREATE_REPOSTS_TABLE_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        AppLogger.d("Upgrading database from version "
                + oldVersion + " to " + newVersion + ",which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GroupTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HomeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RepostsTable.TABLE_NAME);
        onCreate(db);
    }

    public synchronized static DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }
}
