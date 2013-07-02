package org.qii.weiciyuan.support.database.table;

/**
 * User: qii
 * Date: 12-12-4
 */
public class MyStatusTable {

    public static final String TABLE_NAME = "mystatus_table";
    //support multi user,so primary key can't be message id
    public static final String ID = "_id";
    //support mulit user
    public static final String ACCOUNTID = "accountid";

    public static final String TIMELINEDATA = "timelinedata";


    public static class StatusDataTable {

        public static final String TABLE_NAME = "mystatus_data_table";
        //support multi user,so primary key can't be message id
        public static final String ID = "_id";
        //support mulit user
        public static final String ACCOUNTID = "accountid";
        //message id
        public static final String MBLOGID = "mblogid";

        public static final String JSONDATA = "json";

    }

}
