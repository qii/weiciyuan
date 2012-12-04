package org.qii.weiciyuan.support.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.qii.weiciyuan.support.database.table.*;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-7-30
 */
class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "weibo.db";
    private static final int DATABASE_VERSION = 16;

    static final String CREATE_ACCOUNT_TABLE_SQL = "create table " + AccountTable.TABLE_NAME
            + "("
            + AccountTable.UID + " integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.PORTRAIT + " text,"
            + AccountTable.USERNAME + " text,"
            + AccountTable.USERNICK + " text,"
            + AccountTable.AVATAR_URL + " text,"
            + AccountTable.INFOJSON + " text"
            + ");";

    static final String CREATE_GROUP_TABLE_SQL = "create table " + GroupTable.TABLE_NAME
            + "("
            + GroupTable.ID + " integer primary key autoincrement,"
            + GroupTable.ACCOUNTID + " text,"
            + GroupTable.JSONDATA + " text"
            + ");";

    static final String CREATE_HOME_TABLE_SQL = "create table " + HomeTable.TABLE_NAME
            + "("
            + HomeTable.ID + " integer primary key autoincrement,"
            + HomeTable.ACCOUNTID + " text,"
            + HomeTable.MBLOGID + " text,"
            + HomeTable.JSONDATA + " text"
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
            + RepostsTable.JSONDATA + " text"
            + ");";

    static final String CREATE_DMS_TABLE_SQL = "create table " + DMTable.TABLE_NAME
            + "("
            + DMTable.ID + " integer primary key autoincrement,"
            + DMTable.ACCOUNTID + " text,"
            + DMTable.MBLOGID + " text,"
            + DMTable.JSONDATA + " text"
            + ");";

    static final String CREATE_MYSTATUSES_TABLE_SQL = "create table " + MyStatusTable.TABLE_NAME
            + "("
            + MyStatusTable.ID + " integer primary key autoincrement,"
            + MyStatusTable.ACCOUNTID + " text,"
            + MyStatusTable.MBLOGID + " text,"
            + MyStatusTable.JSONDATA + " text"
            + ");";

    static final String CREATE_FILTER_TABLE_SQL = "create table " + FilterTable.TABLE_NAME
            + "("
            + FilterTable.ID + " integer primary key autoincrement,"
            + FilterTable.NAME + " text,"
            + FilterTable.ACTIVE + " text"
            + ");";

    static final String CREATE_EMOTIONS_TABLE_SQL = "create table " + EmotionsTable.TABLE_NAME
            + "("
            + EmotionsTable.ID + " integer primary key autoincrement,"
            + EmotionsTable.JSONDATA + " text"
            + ");";

    static final String CREATE_DRAFTS_TABLE_SQL = "create table " + DraftTable.TABLE_NAME
            + "("
            + DraftTable.ID + " integer primary key autoincrement,"
            + DraftTable.ACCOUNTID + " text,"
            + DraftTable.CONTENT + " text,"
            + DraftTable.JSONDATA + " text,"
            + DraftTable.PIC + " text,"
            + DraftTable.GPS + " text,"
            + DraftTable.TYPE + " integer"
            + ");";

    private static final String CREATE_HOME_INDEX_SQL = "CREATE INDEX idx_"
            + HomeTable.TABLE_NAME
            + " ON "
            + HomeTable.TABLE_NAME
            + " ( "
            + HomeTable.ACCOUNTID
            + " ) ";

    private static final String CREATE_REPOST_INDEX_SQL = "CREATE INDEX idx_"
            + RepostsTable.TABLE_NAME
            + " ON "
            + RepostsTable.TABLE_NAME
            + "("
            + RepostsTable.ACCOUNTID
            + ")";

    private static final String CREATE_COMMENT_INDEX_SQL = "CREATE INDEX idx_"
            + CommentsTable.TABLE_NAME
            + " ON "
            + CommentsTable.TABLE_NAME
            + "("
            + CommentsTable.ACCOUNTID
            + ")";

    private static final String CREATE_DM_INDEX_SQL = "CREATE INDEX idx_"
            + DMTable.TABLE_NAME
            + " ON "
            + DMTable.TABLE_NAME
            + "("
            + DMTable.ACCOUNTID
            + ")";

    private static final String CREATE_MYSTATUSES_INDEX_SQL = "CREATE INDEX idx_"
            + MyStatusTable.TABLE_NAME
            + " ON "
            + MyStatusTable.TABLE_NAME
            + "("
            + MyStatusTable.ACCOUNTID
            + ")";


    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);

        createOtherTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            default:
                deleteAllTableExceptAccount(db);
                createOtherTable(db);
        }


    }

    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }

    private void createOtherTable(SQLiteDatabase db) {

        db.execSQL(CREATE_GROUP_TABLE_SQL);
        db.execSQL(CREATE_HOME_TABLE_SQL);
        db.execSQL(CREATE_COMMENTS_TABLE_SQL);
        db.execSQL(CREATE_REPOSTS_TABLE_SQL);
        db.execSQL(CREATE_DMS_TABLE_SQL);
        db.execSQL(CREATE_MYSTATUSES_TABLE_SQL);
        db.execSQL(CREATE_FILTER_TABLE_SQL);
        db.execSQL(CREATE_EMOTIONS_TABLE_SQL);
        db.execSQL(CREATE_DRAFTS_TABLE_SQL);

        db.execSQL(CREATE_HOME_INDEX_SQL);
        db.execSQL(CREATE_REPOST_INDEX_SQL);
        db.execSQL(CREATE_COMMENT_INDEX_SQL);
        db.execSQL(CREATE_DM_INDEX_SQL);
        db.execSQL(CREATE_MYSTATUSES_INDEX_SQL);
    }

    private void deleteAllTableExceptAccount(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + GroupTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HomeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RepostsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FilterTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EmotionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DraftTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DMTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MyStatusTable.TABLE_NAME);

    }
}
