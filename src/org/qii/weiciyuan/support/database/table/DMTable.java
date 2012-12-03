package org.qii.weiciyuan.support.database.table;

/**
 * User: qii
 * Date: 12-12-3
 */
public class DMTable {
    public static final String TABLE_NAME = "dms_table";
    //support multi user,so primary key can't be message id
    public static final String ID = "_id";
    //support mulit user
    public static final String ACCOUNTID = "accountid";
    //message id
    public static final String MBLOGID = "mblogid";
    //message author avatar url
    public static final String JSONDATA = "json";
}
