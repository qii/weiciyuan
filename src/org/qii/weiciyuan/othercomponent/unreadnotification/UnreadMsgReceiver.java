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
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.NotificationUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class UnreadMsgReceiver extends BroadcastReceiver {

    private Context context;
    private AccountBean accountBean;

    private int sum;

    private CommentListBean commentsToMeData;
    private MessageListBean mentionsWeiboData;
    private CommentListBean mentionsCommentData;
    private UnreadBean unreadBean;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        accountBean = (AccountBean) intent.getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        commentsToMeData = (CommentListBean) intent.getParcelableExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA);
        mentionsWeiboData = (MessageListBean) intent.getParcelableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
        mentionsCommentData = (CommentListBean) intent.getParcelableExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA);
        unreadBean = (UnreadBean) intent.getParcelableExtra(BundleArgsConstants.UNREAD_EXTRA);

        sum = unreadBean.getMention_cmt() + unreadBean.getMention_status() + unreadBean.getCmt();

        if (sum == 0 && accountBean != null) {
            clearNotification(accountBean);
        } else if (allowShowNotification()) {
            showNotification();
        }
    }

    private boolean allowShowNotification() {
        return sum > 0 && (commentsToMeData != null || mentionsWeiboData != null || mentionsCommentData != null);
    }

    private void clearNotification(AccountBean accountBean) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(accountBean.getUid()).intValue());

    }

    private void showNotification() {


        if (!Utility.isJB()) {
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new ICSNotification(context, accountBean, commentsToMeData, mentionsWeiboData, mentionsCommentData, unreadBean).get();
            notificationManager.notify(Integer.valueOf(accountBean.getUid()), notification);
        } else {


            Intent clickNotificationToOpenAppPendingIntentInner = new Intent(GlobalContext.getInstance(), MainTimeLineActivity.class);
            clickNotificationToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
            clickNotificationToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, mentionsWeiboData);
            clickNotificationToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA, mentionsCommentData);
            clickNotificationToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA, commentsToMeData);
            clickNotificationToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.UNREAD_EXTRA, unreadBean);
            clickNotificationToOpenAppPendingIntentInner.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            String ticker = NotificationUtility.getTicker(unreadBean, mentionsWeiboData, mentionsCommentData, commentsToMeData);

            if (mentionsWeiboData != null && mentionsWeiboData.getSize() > 0) {
                Intent intent = new Intent(context, JBMentionsWeiboNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG, mentionsWeiboData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG, clickNotificationToOpenAppPendingIntentInner);
                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
                context.startService(intent);
            }

            if (mentionsCommentData != null && mentionsCommentData.getSize() > 0) {
                Intent intent = new Intent(context, JBMentionsCommentNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG, mentionsCommentData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG, clickNotificationToOpenAppPendingIntentInner);
                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
                context.startService(intent);
            }

            if (commentsToMeData != null && commentsToMeData.getSize() > 0) {
                Intent intent = new Intent(context, JBCommentsToMeNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG, commentsToMeData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG, clickNotificationToOpenAppPendingIntentInner);
                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
                context.startService(intent);
            }
        }
    }
}
