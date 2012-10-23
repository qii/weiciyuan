package org.qii.weiciyuan.support.database.table;

/**
 * User: qii
 * Date: 12-9-25
 */
public class DraftTable {
    public static final String TABLE_NAME = "draft_table";
    public static final String ID = "_id";
    public static final String ACCOUNTID = "accountid";
    public static final String CONTENT = "content";

    public static final String JSONDATA = "json";
    public static final String PIC = "pic";
    public static final String GPS = "gps";

    public static final String TYPE = "type";

    public static final int TYPE_WEIBO = 1;
    public static final int TYPE_REPOST = 2;
    public static final int TYPE_REPLY = 3;
    public static final int TYPE_COMMENT = 4;

}
