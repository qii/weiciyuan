package org.qii.weiciyuan.support.utils;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class AppConfig {

    private AppConfig() {

    }

    public static final int DEFAULT_MSG_COUNT_25 = 25;
    public static final int DEFAULT_MSG_COUNT_50 = 50;

    public static final int DEFAULT_DB_CACHE_COUNT = 50;

    //friend timeline
    public static final long AUTO_REFRESH_INITIALDELAY = 9l;
    public static final long AUTO_REFRESH_PERIOD = 7l;


    //if download pic failed,retry
    public static final int RETRY_TIMES = 6;

    //pic cache saved days
    public static final int SAVED_DAYS = 5;

    //swipe to close
    public static final int SWIPE_MIN_DISTANCE = 30;
}
