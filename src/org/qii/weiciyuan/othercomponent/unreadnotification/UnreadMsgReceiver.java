package org.qii.weiciyuan.othercomponent.unreadnotification;

import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.support.database.NotificationDBTask;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.NotificationUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class UnreadMsgReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        AccountBean accountBean = (AccountBean) intent
                .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        CommentListBean commentsToMeData = (CommentListBean) intent
                .getParcelableExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA);
        MessageListBean mentionsWeiboData = (MessageListBean) intent
                .getParcelableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
        CommentListBean mentionsCommentData = (CommentListBean) intent
                .getParcelableExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA);
        UnreadBean unreadBean = (UnreadBean) intent
                .getParcelableExtra(BundleArgsConstants.UNREAD_EXTRA);

        showNotification(context, accountBean, mentionsWeiboData, commentsToMeData,
                mentionsCommentData, unreadBean);

    }


    private void showNotification(Context context, AccountBean accountBean,
            MessageListBean mentionsWeiboData, CommentListBean commentsToMeData
            , CommentListBean mentionsCommentData, UnreadBean unreadBean) {

        if (!Utility.isJB()) {
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new ICSNotification(context, accountBean, commentsToMeData,
                    mentionsWeiboData, mentionsCommentData, unreadBean).get();
            notificationManager.notify(Integer.valueOf(accountBean.getUid()), notification);
        } else {

            Intent clickNotificationToOpenAppPendingIntentInner = MainTimeLineActivity
                    .newIntent(accountBean, mentionsWeiboData, mentionsCommentData,
                            commentsToMeData, unreadBean);
            clickNotificationToOpenAppPendingIntentInner
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            String accountId = accountBean.getUid();

            Set<String> dbUnreadMentionsWeibo = NotificationDBTask.getUnreadMsgIds(accountId,
                    NotificationDBTask.UnreadDBType.mentionsWeibo);
            Set<String> dbUnreadMentionsComment = NotificationDBTask.getUnreadMsgIds(accountId,
                    NotificationDBTask.UnreadDBType.mentionsComment);
            Set<String> dbUnreadCommentsToMe = NotificationDBTask.getUnreadMsgIds(accountId,
                    NotificationDBTask.UnreadDBType.commentsToMe);

            if (mentionsWeiboData != null && mentionsWeiboData.getSize() > 0) {

                List<MessageBean> msgList = mentionsWeiboData.getItemList();
                Iterator<MessageBean> iterator = msgList.iterator();
                while (iterator.hasNext()) {
                    MessageBean msg = iterator.next();
                    if (dbUnreadMentionsWeibo.contains(msg.getId())) {
                        iterator.remove();
                    }
                }

            }

            if (mentionsCommentData != null && mentionsCommentData.getSize() > 0) {
                List<CommentBean> msgList = mentionsCommentData.getItemList();
                Iterator<CommentBean> iterator = msgList.iterator();
                while (iterator.hasNext()) {
                    CommentBean msg = iterator.next();
                    if (dbUnreadMentionsComment.contains(msg.getId())) {
                        iterator.remove();
                    }
                }

            }

            if (commentsToMeData != null && commentsToMeData.getSize() > 0) {
                List<CommentBean> msgList = commentsToMeData.getItemList();
                Iterator<CommentBean> iterator = msgList.iterator();
                while (iterator.hasNext()) {
                    CommentBean msg = iterator.next();
                    if (dbUnreadCommentsToMe.contains(msg.getId())) {
                        iterator.remove();
                    }
                }

            }

            String ticker = NotificationUtility
                    .getTicker(unreadBean, mentionsWeiboData, mentionsCommentData,
                            commentsToMeData);

            Intent intent = new Intent(context,
                    GeneralNotificationService.class);
            intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
            intent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG,
                    mentionsWeiboData);
            intent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG,
                    mentionsCommentData);
            intent.putExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG, commentsToMeData);
            intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
            intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
            intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
                    clickNotificationToOpenAppPendingIntentInner);
            intent.putExtra(NotificationServiceHelper.TICKER, ticker);
            context.startService(intent);

//            if (mentionsWeiboData != null && mentionsWeiboData.getSize() > 0) {
//
//                Intent intent = new Intent(context,
//                        JBMentionsWeiboNotificationServiceHelper.class);
//                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
//                intent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG,
//                        mentionsWeiboData);
//                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
//                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
//                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
//                        clickNotificationToOpenAppPendingIntentInner);
//                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
//                context.startService(intent);
//
//            }
//
//            if (mentionsCommentData != null && mentionsCommentData.getSize() > 0) {
//
//                Intent intent = new Intent(context,
//                        JBMentionsCommentNotificationServiceHelper.class);
//                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
//                intent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG,
//                        mentionsCommentData);
//                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
//                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
//                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
//                        clickNotificationToOpenAppPendingIntentInner);
//                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
//                context.startService(intent);
//
//            }
//
//            if (commentsToMeData != null && commentsToMeData.getSize() > 0) {
//
//                Intent intent = new Intent(context,
//                        JBCommentsToMeNotificationServiceHelper.class);
//                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
//                intent.putExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG, commentsToMeData);
//                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
//                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
//                intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
//                        clickNotificationToOpenAppPendingIntentInner);
//                intent.putExtra(NotificationServiceHelper.TICKER, ticker);
//                context.startService(intent);
//
//            }
        }
    }


}
