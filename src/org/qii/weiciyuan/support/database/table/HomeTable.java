package org.qii.weiciyuan.support.database.table;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 */
public class HomeTable {

    public static final String TABLE_NAME = "home_table";
    //support multi user,so primary key can't be message id
    public static final String ID = "_id";
    //support mulit user
    public static final String ACCOUNTID = "accountid";

    public static final String TIMELINEDATA = "timelinedata";

    public static final String RECENT_GROUP_ID = "recent_group_id";


    public static class HomeDataTable {

        public static final String TABLE_NAME = "home_data_table";
        //support multi user,so primary key can't be message id
        public static final String ID = "_id";
        //support mulit user
        public static final String ACCOUNTID = "accountid";
        //message id
        public static final String MBLOGID = "mblogid";

        public static final String JSONDATA = "json";

    }
}
