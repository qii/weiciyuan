package org.qii.weiciyuan.othercomponent.unreadnotification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.qii.weiciyuan.bean.AccountBean;

/**
 * User: qii
 * Date: 13-5-4
 */
public class NotificationServiceHelper extends Service {

    protected static final String RESET_UNREAD_MENTIONS_WEIBO_ACTION = "org.qii.weiciyuan.Notification.unread.reset.mentionsweibo";
    protected static final String RESET_UNREAD_MENTIONS_COMMENT_ACTION = "org.qii.weiciyuan.Notification.unread.reset.mentionscomment";
    protected static final String RESET_UNREAD_COMMENTS_TO_ME_ACTION = "org.qii.weiciyuan.Notification.unread.reset.comments";

    private static final int MENTIONS_WEIBO_NOTIFICATION_ID = 1;
    private static final int MENTIONS_COMMENT_NOTIFICATION_ID = 2;
    private static final int COMMENTS_TO_ME_NOTIFICATION_ID = 3;

    public static final String ACCOUNT_ARG = "accountBean";
    public static final String UNREAD_ARG = "unreadBean";
    public static final String CURRENT_INDEX_ARG = "currentIndex";
    public static final String MENTIONS_WEIBO_ARG = "repost";
    public static final String MENTIONS_COMMENT_ARG = "mentionsComment";
    public static final String COMMENTS_TO_ME_ARG = "comment";


    protected int getMentionsWeiboNotificationId(AccountBean accountBean) {
        return Integer.valueOf(accountBean.getUid()) + MENTIONS_WEIBO_NOTIFICATION_ID;
    }

    protected int getMentionsCommentNotificationId(AccountBean accountBean) {
        return Integer.valueOf(accountBean.getUid()) + MENTIONS_COMMENT_NOTIFICATION_ID;
    }

    protected int getCommentsToMeNotificationId(AccountBean accountBean) {
        return Integer.valueOf(accountBean.getUid()) + COMMENTS_TO_ME_NOTIFICATION_ID;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
