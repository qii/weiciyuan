package org.qii.weiciyuan.othercomponent.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class UnreadMsgReceiver extends BroadcastReceiver {

    public static final String ACTION = "org.qii.weiciyuan.newmsg";

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
        accountBean = (AccountBean) intent.getSerializableExtra("account");
        comment = (CommentListBean) intent.getSerializableExtra("comment");
        repost = (MessageListBean) intent.getSerializableExtra("repost");
        mentionCommentsResult = (CommentListBean) intent.getSerializableExtra("mention_comment");
        unreadBean = (UnreadBean) intent.getSerializableExtra("unread");

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

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification = new ICSNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
        } else {
            switch (SettingUtility.getNotificationStyle()) {
                case 1:
                    notification = new JBInboxNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
                    break;
                case 2:
                    notification = new JBBigTextNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();
                    break;
                default:
                    notification = new JBInboxNotification(context, accountBean, comment, repost, mentionCommentsResult, unreadBean).get();

            }
        }

        notificationManager.notify(Long.valueOf(accountBean.getUid()).intValue(), notification);

    }


}
