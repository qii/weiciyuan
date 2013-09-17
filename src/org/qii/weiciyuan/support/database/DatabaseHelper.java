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
    private static final int DATABASE_VERSION = 34;

    static final String CREATE_ACCOUNT_TABLE_SQL = "create table " + AccountTable.TABLE_NAME
            + "("
            + AccountTable.UID + " integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_EXPIRES_TIME + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.BLACK_MAGIC + " boolean,"
            + AccountTable.NAVIGATION_POSITION + " integer,"
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
            + HomeTable.TIMELINEDATA + " text,"
            + HomeTable.RECENT_GROUP_ID + " text"
            + ");";

    static final String CREATE_HOME_DATA_TABLE_SQL = "create table " + HomeTable.HomeDataTable.TABLE_NAME
            + "("
            + HomeTable.HomeDataTable.ID + " integer primary key autoincrement,"
            + HomeTable.HomeDataTable.ACCOUNTID + " text,"
            + HomeTable.HomeDataTable.MBLOGID + " text,"
            + HomeTable.HomeDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_HOME_OTHER_GROUP_TABLE_SQL = "create table " + HomeOtherGroupTable.TABLE_NAME
            + "("
            + HomeOtherGroupTable.ID + " integer primary key autoincrement,"
            + HomeOtherGroupTable.ACCOUNTID + " text,"
            + HomeOtherGroupTable.GROUPID + " text,"
            + HomeOtherGroupTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_HOME_OTHER_GROUP_DATA_TABLE_SQL = "create table " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME
            + "("
            + HomeOtherGroupTable.HomeOtherGroupDataTable.ID + " integer primary key autoincrement,"
            + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID + " text,"
            + HomeOtherGroupTable.HomeOtherGroupDataTable.MBLOGID + " text,"
            + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID + " text,"
            + HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_COMMENTS_TABLE_SQL = "create table " + CommentsTable.TABLE_NAME
            + "("
            + CommentsTable.ID + " integer primary key autoincrement,"
            + CommentsTable.ACCOUNTID + " text,"
            + CommentsTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_COMMENTS_DATA_TABLE_SQL = "create table " + CommentsTable.CommentsDataTable.TABLE_NAME
            + "("
            + CommentsTable.CommentsDataTable.ID + " integer primary key autoincrement,"
            + CommentsTable.CommentsDataTable.ACCOUNTID + " text,"
            + CommentsTable.CommentsDataTable.MBLOGID + " text,"
            + CommentsTable.CommentsDataTable.JSONDATA + " text"
            + ");";


    static final String CREATE_REPOSTS_TABLE_SQL = "create table " + RepostsTable.TABLE_NAME
            + "("
            + RepostsTable.ID + " integer primary key autoincrement,"
            + RepostsTable.ACCOUNTID + " text,"
            + RepostsTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_REPOSTS_DATA_TABLE_SQL = "create table " + RepostsTable.RepostDataTable.TABLE_NAME
            + "("
            + RepostsTable.RepostDataTable.ID + " integer primary key autoincrement,"
            + RepostsTable.RepostDataTable.ACCOUNTID + " text,"
            + RepostsTable.RepostDataTable.MBLOGID + " text,"
            + RepostsTable.RepostDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_COMMENT_BY_ME_TABLE_SQL = "create table " + CommentByMeTable.TABLE_NAME
            + "("
            + CommentByMeTable.ID + " integer primary key autoincrement,"
            + CommentByMeTable.ACCOUNTID + " text,"
            + CommentByMeTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_COMMENT_BY_ME_DATA_TABLE_SQL = "create table " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME
            + "("
            + CommentByMeTable.CommentByMeDataTable.ID + " integer primary key autoincrement,"
            + CommentByMeTable.CommentByMeDataTable.ACCOUNTID + " text,"
            + CommentByMeTable.CommentByMeDataTable.MBLOGID + " text,"
            + CommentByMeTable.CommentByMeDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_MENTION_COMMENTS_TABLE_SQL = "create table " + MentionCommentsTable.TABLE_NAME
            + "("
            + MentionCommentsTable.ID + " integer primary key autoincrement,"
            + MentionCommentsTable.ACCOUNTID + " text,"
            + MentionCommentsTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_MENTION_COMMENTS_DATA_TABLE_SQL = "create table " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME
            + "("
            + MentionCommentsTable.MentionCommentsDataTable.ID + " integer primary key autoincrement,"
            + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID + " text,"
            + MentionCommentsTable.MentionCommentsDataTable.MBLOGID + " text,"
            + MentionCommentsTable.MentionCommentsDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_DMS_TABLE_SQL = "create table " + DMTable.TABLE_NAME
            + "("
            + DMTable.ID + " integer primary key autoincrement,"
            + DMTable.ACCOUNTID + " text,"
            + DMTable.MBLOGID + " text,"
            + DMTable.JSONDATA + " text"
            + ");";

    static final String CREATE_FAVOURITES_TABLE_SQL = "create table " + FavouriteTable.TABLE_NAME
            + "("
            + FavouriteTable.ID + " integer primary key autoincrement,"
            + FavouriteTable.ACCOUNTID + " text,"
            + FavouriteTable.TIMELINEDATA + " text,"
            + FavouriteTable.PAGE + " text"
            + ");";

    static final String CREATE_FAVOURITES_DATA_TABLE_SQL = "create table " + FavouriteTable.FavouriteDataTable.TABLE_NAME
            + "("
            + FavouriteTable.FavouriteDataTable.ID + " integer primary key autoincrement,"
            + FavouriteTable.FavouriteDataTable.ACCOUNTID + " text,"
            + FavouriteTable.FavouriteDataTable.MBLOGID + " text,"
            + FavouriteTable.FavouriteDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_MYSTATUSES_TABLE_SQL = "create table " + MyStatusTable.TABLE_NAME
            + "("
            + MyStatusTable.ID + " integer primary key autoincrement,"
            + MyStatusTable.ACCOUNTID + " text,"
            + MyStatusTable.TIMELINEDATA + " text"
            + ");";

    static final String CREATE_MYSTATUSES_DATA_TABLE_SQL = "create table " + MyStatusTable.StatusDataTable.TABLE_NAME
            + "("
            + MyStatusTable.StatusDataTable.ID + " integer primary key autoincrement,"
            + MyStatusTable.StatusDataTable.ACCOUNTID + " text,"
            + MyStatusTable.StatusDataTable.MBLOGID + " text,"
            + MyStatusTable.StatusDataTable.JSONDATA + " text"
            + ");";

    static final String CREATE_FILTER_TABLE_SQL = "create table " + FilterTable.TABLE_NAME
            + "("
            + FilterTable.ID + " integer primary key autoincrement,"
            + FilterTable.NAME + " text,"
            + FilterTable.ACTIVE + " text,"
            + FilterTable.TYPE + " integer"
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

    static final String CREATE_ATUSERS_TABLE_SQL = "create table " + AtUsersTable.TABLE_NAME
            + "("
            + AtUsersTable.ID + " integer primary key autoincrement,"
            + AtUsersTable.ACCOUNTID + " text,"
            + AtUsersTable.JSONDATA + " text"
            + ");";

    static final String CREATE_TOPICS_TABLE_SQL = "create table " + TopicTable.TABLE_NAME
            + "("
            + TopicTable.ID + " integer primary key autoincrement,"
            + TopicTable.ACCOUNTID + " text,"
            + TopicTable.TOPIC_NAME + " text"
            + ");";

    private static final String CREATE_HOME_INDEX_SQL = "CREATE INDEX idx_"
            + HomeTable.HomeDataTable.TABLE_NAME
            + " ON "
            + HomeTable.HomeDataTable.TABLE_NAME
            + " ( "
            + HomeTable.HomeDataTable.ACCOUNTID
            + " ) ";

    private static final String CREATE_HOME_OTHER_GROUP_INDEX_SQL = "CREATE INDEX idx_"
            + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME
            + " ON "
            + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME
            + " ( "
            + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID
            + " ) ";

    private static final String CREATE_REPOST_INDEX_SQL = "CREATE INDEX idx_"
            + RepostsTable.RepostDataTable.TABLE_NAME
            + " ON "
            + RepostsTable.RepostDataTable.TABLE_NAME
            + "("
            + RepostsTable.RepostDataTable.ACCOUNTID
            + ")";

    private static final String CREATE_COMMENT_INDEX_SQL = "CREATE INDEX idx_"
            + CommentsTable.CommentsDataTable.TABLE_NAME
            + " ON "
            + CommentsTable.CommentsDataTable.TABLE_NAME
            + "("
            + CommentsTable.CommentsDataTable.ACCOUNTID
            + ")";

    private static final String CREATE_MENTION_COMMENTS_INDEX_SQL = "CREATE INDEX idx_"
            + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME
            + " ON "
            + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME
            + "("
            + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID
            + ")";

    private static final String CREATE_COMMENT_BY_ME_INDEX_SQL = "CREATE INDEX idx_"
            + CommentByMeTable.CommentByMeDataTable.TABLE_NAME
            + " ON "
            + CommentByMeTable.CommentByMeDataTable.TABLE_NAME
            + "("
            + CommentByMeTable.CommentByMeDataTable.ACCOUNTID
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
        if (oldVersion < 34) {
            deleteAllTable(db);
            onCreate(db);
        } else {
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
        db.execSQL(CREATE_HOME_DATA_TABLE_SQL);

        db.execSQL(CREATE_HOME_OTHER_GROUP_TABLE_SQL);
        db.execSQL(CREATE_HOME_OTHER_GROUP_DATA_TABLE_SQL);

        db.execSQL(CREATE_COMMENTS_TABLE_SQL);
        db.execSQL(CREATE_COMMENTS_DATA_TABLE_SQL);

        db.execSQL(CREATE_REPOSTS_TABLE_SQL);
        db.execSQL(CREATE_REPOSTS_DATA_TABLE_SQL);

        db.execSQL(CREATE_MENTION_COMMENTS_TABLE_SQL);
        db.execSQL(CREATE_MENTION_COMMENTS_DATA_TABLE_SQL);

        db.execSQL(CREATE_COMMENT_BY_ME_TABLE_SQL);
        db.execSQL(CREATE_COMMENT_BY_ME_DATA_TABLE_SQL);

        db.execSQL(CREATE_DMS_TABLE_SQL);

        db.execSQL(CREATE_FAVOURITES_TABLE_SQL);
        db.execSQL(CREATE_FAVOURITES_DATA_TABLE_SQL);

        db.execSQL(CREATE_MYSTATUSES_TABLE_SQL);
        db.execSQL(CREATE_MYSTATUSES_DATA_TABLE_SQL);

        db.execSQL(CREATE_FILTER_TABLE_SQL);
        db.execSQL(CREATE_EMOTIONS_TABLE_SQL);
        db.execSQL(CREATE_DRAFTS_TABLE_SQL);
        db.execSQL(CREATE_ATUSERS_TABLE_SQL);
        db.execSQL(CREATE_TOPICS_TABLE_SQL);

        db.execSQL(CREATE_HOME_INDEX_SQL);
        db.execSQL(CREATE_HOME_OTHER_GROUP_INDEX_SQL);
        db.execSQL(CREATE_REPOST_INDEX_SQL);
        db.execSQL(CREATE_COMMENT_INDEX_SQL);
        db.execSQL(CREATE_MENTION_COMMENTS_INDEX_SQL);
        db.execSQL(CREATE_COMMENT_BY_ME_INDEX_SQL);
        db.execSQL(CREATE_DM_INDEX_SQL);
        db.execSQL(CREATE_MYSTATUSES_INDEX_SQL);
    }

    private void deleteAllTableExceptAccount(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + GroupTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + HomeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HomeTable.HomeDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + HomeOtherGroupTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsTable.CommentsDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + RepostsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RepostsTable.RepostDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + MentionCommentsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + CommentByMeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + FavouriteTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteTable.FavouriteDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + MyStatusTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MyStatusTable.StatusDataTable.TABLE_NAME);

        db.execSQL("DROP TABLE IF EXISTS " + FilterTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EmotionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DraftTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DMTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AtUsersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TopicTable.TABLE_NAME);

    }

    private void deleteAllTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE_NAME);

        deleteAllTableExceptAccount(db);

    }
}
