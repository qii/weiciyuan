package org.qii.weiciyuan.support.utils;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class AppConfig {

    private AppConfig() {

    }

    public static final boolean DEBUG = true;

    public static final int DEFAULT_MSG_COUNT_25 = 25;
    public static final int DEFAULT_MSG_COUNT_50 = 50;

    public static final int DEFAULT_MENTIONS_COMMENT_DB_CACHE_COUNT = 100;
    public static final int DEFAULT_MENTIONS_WEIBO_DB_CACHE_COUNT = 100;
    public static final int DEFAULT_COMMENTS_TO_ME_DB_CACHE_COUNT = 100;
    public static final int DEFAULT_COMMENTS_BY_ME_DB_CACHE_COUNT = 100;
    public static final int DEFAULT_HOME_DB_CACHE_COUNT = 500;

    public static final int DB_CACHE_COUNT_OFFSET = 10;

    //friend timeline
    public static final long AUTO_REFRESH_INITIALDELAY = 9L;
    public static final long AUTO_REFRESH_PERIOD = 7L;


    //if download pic failed,retry
    public static final int RETRY_TIMES = 6;

    //pic cache saved days
    public static final int SAVED_DAYS = 2;

    //swipe to close,300px
    public static final int SWIPE_MIN_DISTANCE = 300;


    public static final int REFRESH_DELAYED_MILL_SECOND_TIME = 600;

    public static final int CREATE_MODIFY_FRIEND_GROUP_NAME_LENGTH_LIMIT = 10;

    //ViewConfiguration
    public static int getScrollSlop() {
        return Utility.dip2px(2);
    }
}
