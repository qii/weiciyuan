package org.qii.weiciyuan.othercomponent.unreadnotification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-12-5
 */
@Deprecated
public class ICSNotification {

    private Context context;

    private AccountBean accountBean;

    private CommentListBean comment;
    private MessageListBean repost;
    private CommentListBean mentionCommentsResult;

    private UnreadBean unreadBean;

    //only leave one broadcast receiver
    private static BroadcastReceiver clearNotificationEventReceiver;

    public ICSNotification(Context context,
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
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(accountBean.getUid()).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private String getTicker() {
        int mentionCmt = unreadBean.getMention_cmt();
        int mentionStatus = unreadBean.getMention_status();
        int mention = mentionStatus + mentionCmt;
        int cmt = unreadBean.getCmt();

        StringBuilder stringBuilder = new StringBuilder();
        if (mention > 0) {
            String txt = String.format(context.getString(R.string.new_mentions), String.valueOf(mention));
            stringBuilder.append(txt);
        }

        if (cmt > 0) {
            if (mention > 0)
                stringBuilder.append("、");
            String txt = String.format(context.getString(R.string.new_comments), String.valueOf(cmt));
            stringBuilder.append(txt);
        }
        return stringBuilder.toString();
    }

    private int getCount() {
        int count = 0;

        if (SettingUtility.allowMentionToMe()) {
            count += unreadBean.getMention_status();
        }

        if (SettingUtility.allowMentionToMe()) {
            count += unreadBean.getCmt();
        }

        if (SettingUtility.allowMentionCommentToMe()) {
            count += unreadBean.getMention_cmt();
        }

        return count;

    }


    public Notification get() {

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(getTicker())
                .setContentTitle(getTicker())
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())
                .setOnlyAlertOnce(true);


        if (getCount() > 1) {
            builder.setNumber(getCount());
        }

        Utility.configVibrateLedRingTone(builder);

        if (clearNotificationEventReceiver != null) {
            GlobalContext.getInstance().unregisterReceiver(clearNotificationEventReceiver);
            ICSNotification.clearNotificationEventReceiver = null;
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
                            ICSNotification.clearNotificationEventReceiver = null;
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

        return builder.getNotification();
    }


}
