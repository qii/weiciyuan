package org.qii.weiciyuan.support.database.table;

/**
 * User: qii
 * Date: 14-2-22
 */
public class NotificationTable {

    public static final String TABLE_NAME = "notification_table";

    public static final String ID = "_id";

    //support mulit user
    public static final String ACCOUNTID = "accountid";

    public static final String MSGID = "msgid";


    //type '0' mentions to me weibo; '1' mentions to me comment; '2' comments to me; '3' direct message
    public static final String TYPE = "type";


}
