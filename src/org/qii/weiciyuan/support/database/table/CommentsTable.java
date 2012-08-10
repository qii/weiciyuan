package org.qii.weiciyuan.support.database.table;

/**
 * User: Jiang Qi
 * Date: 12-8-10
 * Time: 上午10:36
 */
public class CommentsTable {

    public static final String TABLE_NAME = "comments_table";
    //support multi user,so primary key can't be message id
    public static final String ID = "_id";
    //support mulit user
    public static final String ACCOUNTID = "accountid";
    //message id
    public static final String MBLOGID = "mblogid";
    //message author avatar url
    public static final String AVATAR = "avatar";
    public static final String FEEDID = "feedid";
    public static final String MBLOGIDNUM = "mblogidnum";
    public static final String GID = "gid";
    public static final String GSID = "gsid";
    public static final String UID = "uid";
    public static final String NICK = "nick";
    public static final String PORTRAIT = "portrait";
    public static final String VIP = "vip";
    public static final String CONTENT = "content";
    public static final String RTROOTUID = "rtrootuid";
    public static final String RTAVATAR = "rtavatar";
    public static final String RTCONTENT = "rtcontent";
    public static final String RTPIC = "rtpic";
    public static final String RTID = "rtid";
    public static final String RTROTNICK = "rtrootnick";
    public static final String RTROOTVIP = "rtrootvip";
    public static final String RTREASON = "rtreason";
    public static final String TIME = "time";
    public static final String PIC = "pic";
    public static final String SRC = "src";

}
