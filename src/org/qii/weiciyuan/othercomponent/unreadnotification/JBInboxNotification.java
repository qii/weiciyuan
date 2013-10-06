package org.qii.weiciyuan.othercomponent.unreadnotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.NotificationUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-12-5
 */
@Deprecated
public class JBInboxNotification {

    private Context context;

    private AccountBean accountBean;

    private CommentListBean comment;
    private MessageListBean repost;
    private CommentListBean mentionCommentsResult;

    private UnreadBean unreadBean;

    //only leave one broadcast receiver
    private static BroadcastReceiver clearNotificationEventReceiver;

    public JBInboxNotification(Context context,
                               AccountBean accountBean,
                               CommentListBean comment,
                               MessageListBean repost,
                               CommentListBean mentionCommentsResult, UnreadBean unreadBean) {
        this.context = context;
        this.accountBean = accountBean;
        this.comment = comment;
        this.repost = repost;
        this.mentionCommentsResult = mentionCommentsResult;
        this.unreadBean = unreadBean;
    }


    private PendingIntent getPendingIntent() {
        Intent i = new Intent(context, MainTimeLineActivity.class);
        i.putExtra("account", accountBean);
        i.putExtra("comment", comment);
        i.putExtra("repost", repost);
        i.putExtra("mentionsComment", mentionCommentsResult);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(accountBean.getUid()).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public Notification get() {

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(NotificationUtility.getTicker(unreadBean, null, null, null))
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())
                .setOnlyAlertOnce(true);

        builder.setContentTitle(NotificationUtility.getTicker(unreadBean, null, null, null));

        if (NotificationUtility.getCount(unreadBean) > 1) {
            builder.setNumber(NotificationUtility.getCount(unreadBean));
        }

        if (clearNotificationEventReceiver != null) {
            GlobalContext.getInstance().unregisterReceiver(clearNotificationEventReceiver);
            JBInboxNotification.clearNotificationEventReceiver = null;
        }

        clearNotificationEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new ClearUnreadDao(accountBean.getAccess_token()).clearMentionStatusUnread(unreadBean, accountBean.getUid());
                            new ClearUnreadDao(accountBean.getAccess_token()).clearMentionCommentUnread(unreadBean, accountBean.getUid());
                            new ClearUnreadDao(accountBean.getAccess_token()).clearCommentUnread(unreadBean, accountBean.getUid());
                        } catch (WeiboException ignored) {

                        } finally {
                            GlobalContext.getInstance().unregisterReceiver(clearNotificationEventReceiver);
                            JBInboxNotification.clearNotificationEventReceiver = null;
                        }

                    }
                }).start();
            }
        };

        IntentFilter intentFilter = new IntentFilter("org.qii.weiciyuan.Notification.unread");

        GlobalContext.getInstance().registerReceiver(clearNotificationEventReceiver, intentFilter);

        Intent broadcastIntent = new Intent("org.qii.weiciyuan.Notification.unread");

        PendingIntent deletedPendingIntent = PendingIntent.getBroadcast(GlobalContext.getInstance(), 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletedPendingIntent);

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
        inboxStyle.setBigContentTitle(NotificationUtility.getTicker(unreadBean, null, null, null));
        if (comment != null) {
            for (CommentBean c : comment.getItemList()) {
                inboxStyle.addLine(c.getUser().getScreen_name() + ":" + c.getText());
            }
        }

        if (repost != null) {
            for (MessageBean m : repost.getItemList()) {
                inboxStyle.addLine(m.getUser().getScreen_name() + ":" + m.getText());
            }
        }

        if (mentionCommentsResult != null) {
            for (CommentBean m : mentionCommentsResult.getItemList()) {
                inboxStyle.addLine(m.getUser().getScreen_name() + ":" + m.getText());
            }
        }

        inboxStyle.setSummaryText(accountBean.getUsernick());

        builder.setStyle(inboxStyle);
        Utility.configVibrateLedRingTone(builder);
        return builder.build();
    }


}
