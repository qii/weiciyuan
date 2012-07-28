package org.qii.weiciyuan.dao;

import org.qii.weiciyuan.support.http.URLManager;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午7:13
 * To change this template use File | Settings | File Templates.
 */
public class URLHelper {


    public static String getHomeLine() {
        return URLManager.getRealUrl("hometimeline");
    }

    public static String getMentionsTimeLine(){


        return URLManager.getRealUrl("mentions");

    }

}
