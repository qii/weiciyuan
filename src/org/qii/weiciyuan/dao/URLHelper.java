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


    public static String getFriendsTimeLine() {
        return URLManager.getRealUrl("friendstimeline");
    }

    public static String getMentionsTimeLine() {

        return URLManager.getRealUrl("mentionstimeline");

    }


    public static String getCommentList(){
        return URLManager.getRealUrl("commentstimeline");
    }

    public static String getTags(){
        return  URLManager.getRealUrl("tags");
    }
}
