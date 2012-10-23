package org.qii.weiciyuan.dao;

/**
 * User: qii
 * Date: 12-7-28
 */
public class URLHelper {
    //base url
    private static final String URL_SINA_WEIBO = "https://api.weibo.com/2/";

    //login
    public static final String UID = URL_SINA_WEIBO + "account/get_uid.json";

    //main timeline
    public static final String FRIENDS_TIMELINE = URL_SINA_WEIBO + "statuses/friends_timeline.json";
    public static final String COMMENTS_MENTIONS_TIMELINE = URL_SINA_WEIBO + "comments/mentions.json";
    public static final String STATUSES_MENTIONS_TIMELINE = URL_SINA_WEIBO + "statuses/mentions.json";
    public static final String COMMENTS_TO_ME_TIMELINE = URL_SINA_WEIBO + "comments/to_me.json";
    public static final String COMMENTS_BY_ME_TIMELINE = URL_SINA_WEIBO + "comments/by_me.json";
    public static final String BILATERAL_TIMELINE = URL_SINA_WEIBO + "statuses/bilateral_timeline.json";

    //group timeline
    public static final String FRIENDSGROUP_INFO = URL_SINA_WEIBO + "friendships/groups.json";
    public static final String FRIENDSGROUP_TIMELINE = URL_SINA_WEIBO + "friendships/groups/timeline.json";

    //general timeline
    public static final String COMMENTS_TIMELINE_BY_MSGID = URL_SINA_WEIBO + "comments/show.json";
    public static final String REPOSTS_TIMELINE_BY_MSGID = URL_SINA_WEIBO + "statuses/repost_timeline.json";

    //user profile
    public static final String STATUSES_TIMELINE_BY_ID = URL_SINA_WEIBO + "statuses/user_timeline.json";
    public static final String USER_SHOW = URL_SINA_WEIBO + "users/show.json";


    //browser
    public static final String STATUSES_SHOW = URL_SINA_WEIBO + "statuses/show.json";

    //send weibo
    public static final String STATUSES_UPDATE = URL_SINA_WEIBO + "statuses/update.json";
    public static final String STATUSES_UPLOAD = URL_SINA_WEIBO + "statuses/upload.json";
    public static final String STATUSES_DESTROY = URL_SINA_WEIBO + "statuses/destroy.json";

    public static final String REPOST_CREATE = URL_SINA_WEIBO + "statuses/repost.json";

    public static final String COMMENT_CREATE = URL_SINA_WEIBO + "comments/create.json";
    public static final String COMMENT_DESTROY = URL_SINA_WEIBO + "comments/destroy.json";
    public static final String COMMENT_REPLY = URL_SINA_WEIBO + "comments/reply.json";


    //favourite
    public static final String MYFAV_LIST = URL_SINA_WEIBO + "favorites.json";

    public static final String FAV_CREATE = URL_SINA_WEIBO + "favorites/create.json";
    public static final String FAV_DESTROY = URL_SINA_WEIBO + "favorites/destroy.json";


    //relationship
    public static final String FRIENDS_LIST_BYID = URL_SINA_WEIBO + "friendships/friends.json";
    public static final String FOLLOWERS_LIST_BYID = URL_SINA_WEIBO + "friendships/followers.json";

    public static final String FRIENDSHIPS_CREATE = URL_SINA_WEIBO + "friendships/create.json";
    public static final String FRIENDSHIPS_DESTROY = URL_SINA_WEIBO + "friendships/destroy.json";
    public static final String FRIENDSHIPS_FOLLOWERS_DESTROY = URL_SINA_WEIBO + "friendships/followers/destroy.json";

    //gps location info
    public static final String GOOGLELOCATION = "http://maps.google.com/maps/api/geocode/json";


    //search
    public static final String AT_USER = URL_SINA_WEIBO + "search/suggestions/at_users.json";
    public static final String TOPIC_SEARCH = URL_SINA_WEIBO + "search/topics.json";

    //unread messages
    public static final String UNREAD_COUNT = URL_SINA_WEIBO + "remind/unread_count.json";
    public static final String UNREAD_CLEAR = URL_SINA_WEIBO + "remind/set_count.json";


    public static final String REMARK_UPDATE = URL_SINA_WEIBO + "friendships/remark/update.json";

    public static final String TAGS = URL_SINA_WEIBO + "tags.json";

    public static final String EMOTIONS = URL_SINA_WEIBO + "emotions.json";
}
