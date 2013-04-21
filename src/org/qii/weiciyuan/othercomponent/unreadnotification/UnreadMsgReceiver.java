package org.qii.weiciyuan.othercomponent.unreadnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class UnreadMsgReceiver extends BroadcastReceiver {

    private Context context;
    private AccountBean accountBean;

    private int sum;

    private CommentListBean comment;
    private MessageListBean repost;
    private CommentListBean mentionCommentsResult;
    private UnreadBean unreadBean;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        accountBean = (AccountBean) intent.getSerializableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        comment = (CommentListBean) intent.getSerializableExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA);
        repost = (MessageListBean) intent.getSerializableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
        mentionCommentsResult = (CommentListBean) intent.getSerializableExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA);
        unreadBean = (UnreadBean) intent.getSerializableExtra(BundleArgsConstants.UNREAD_EXTRA);

        sum = unreadBean.getMention_cmt() + unreadBean.getMention_status() + unreadBean.getCmt();

        if (sum == 0 && accountBean != null) {
            clearNotification(accountBean);
        } else if (allowShowNotification()) {
            showNotification();
        }
    }

    private boolean allowShowNotification() {
        return sum > 0 && (comment != null || repost != null || mentionCommentsResult != null);
    }

    private void clearNotification(AccountBean accountBean) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(accountBean.getUid()).intValue());

    }

    private void showNotification() {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        if (!Utility.isJB()) {
            notification = new ICSNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
        } else {
            if (sum == 1) {
                notification = new JBBigTextNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
            } else {
                notification = new JBInboxNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
            }
        }

        notificationManager.notify(Long.valueOf(accountBean.getUid()).intValue(), notification);

    }


}
