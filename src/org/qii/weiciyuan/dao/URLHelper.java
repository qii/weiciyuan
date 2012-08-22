package org.qii.weiciyuan.dao;

import org.qii.weiciyuan.support.http.URLManager;

/**
 * User: qii
 * Date: 12-7-28
 */
public class URLHelper {


    public static String getFriendsTimeLine() {
        return URLManager.getRealUrl("friendstimeline");
    }

    public static String getMentionsTimeLine() {

        return URLManager.getRealUrl("mentionstimeline");

    }

    public static String getCommentList() {
        return URLManager.getRealUrl("commentstimeline");
    }

    public static String getCommentListById() {
        return URLManager.getRealUrl("commentstimelinebymsgid");
    }

    public static String getRepostListById() {
        return URLManager.getRealUrl("repoststimelinebymsgid");
    }

    public static String getTags() {
        return URLManager.getRealUrl("tags");
    }

    public static String getStatuses_Show() {
        return URLManager.getRealUrl("statuses_show");
    }

    public static String new_Repost() {
        return URLManager.getRealUrl("repost");
    }

    public static String new_Comment() {
        return URLManager.getRealUrl("comment");
    }

    public static String getUser() {
        return URLManager.getRealUrl("usershow");
    }

    public static String getStatusesTimeLineById() {
        return URLManager.getRealUrl("statusestimelinebyid");
    }

    public static String getFriendListById() {
        return URLManager.getRealUrl("friendsbyid");
    }

    public static String getFanListById() {
        return URLManager.getRealUrl("followersbyid");
    }

    public static String getFavList() {
        return URLManager.getRealUrl("myfav");
    }

    public static String getFollowitUrl() {
        return URLManager.getRealUrl("followit");
    }

    public static String getUnFollowitUrl() {
        return URLManager.getRealUrl("unfollowit");
    }
}
