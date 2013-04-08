package org.qii.weiciyuan.support.database.table;

/**
 * User: qii
 * Date: 13-1-23
 */
public class MentionCommentsTable {

    public static final String TABLE_NAME = "mention_comments_table";
    //support multi user,so primary key can't be message id
    public static final String ID = "_id";
    //support mulit user
    public static final String ACCOUNTID = "accountid";

    public static final String TIMELINEDATA = "timelinedata";


    public static class MentionCommentsDataTable {

        public static final String TABLE_NAME = "mention_comments_data_table";
        //support multi user,so primary key can't be message id
        public static final String ID = "_id";
        //support mulit user
        public static final String ACCOUNTID = "accountid";
        //message id
        public static final String MBLOGID = "mblogid";

        public static final String JSONDATA = "json";

    }
}
