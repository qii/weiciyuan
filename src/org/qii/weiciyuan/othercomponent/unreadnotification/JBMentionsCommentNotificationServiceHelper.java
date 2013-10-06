package org.qii.weiciyuan.othercomponent.unreadnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.dao.unread.ClearUnreadDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

/**
 * User: qii
 * Date: 13-5-4
 */
public class JBMentionsCommentNotificationServiceHelper extends NotificationServiceHelper {


    private AccountBean accountBean;
    private CommentListBean data;
    private UnreadBean unreadBean;
    private int currentIndex;
    private Intent clickToOpenAppPendingIntentInner;
    private String ticker;


    private static BroadcastReceiver clearNotificationEventReceiver;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.accountBean = intent.getParcelableExtra(NotificationServiceHelper.ACCOUNT_ARG);
        this.data = intent.getParcelableExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG);
        this.unreadBean = intent.getParcelableExtra(NotificationServiceHelper.UNREAD_ARG);
        this.currentIndex = intent.getIntExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
        this.clickToOpenAppPendingIntentInner = intent.getParcelableExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG);
        this.ticker = intent.getStringExtra(NotificationServiceHelper.TICKER);

        buildNotification();

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }


    private void buildNotification() {

        int count = (data.getSize() >= Integer.valueOf(SettingUtility.getMsgCount()) ? unreadBean.getMention_cmt() : data.getSize());


        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setTicker(ticker)
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())
                .setOnlyAlertOnce(true);

        builder.setContentTitle(String.format(GlobalContext.getInstance().getString(R.string.new_mentions_comment), String.valueOf(count)));

        if (data.getSize() > 1)
            builder.setNumber(count);


        if (clearNotificationEventReceiver != null) {
            GlobalContext.getInstance().unregisterReceiver(clearNotificationEventReceiver);
            JBMentionsCommentNotificationServiceHelper.clearNotificationEventReceiver = null;
        }

        clearNotificationEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new ClearUnreadDao(accountBean.getAccess_token()).clearMentionCommentUnread(unreadBean, accountBean.getUid());

                        } catch (WeiboException ignored) {

                        } finally {
                            GlobalContext.getInstance().unregisterReceiver(clearNotificationEventReceiver);
                            JBMentionsCommentNotificationServiceHelper.clearNotificationEventReceiver = null;
                        }

                    }
                }).start();
            }
        };

        IntentFilter intentFilter = new IntentFilter(RESET_UNREAD_MENTIONS_COMMENT_ACTION);

        GlobalContext.getInstance().registerReceiver(clearNotificationEventReceiver, intentFilter);

        Intent broadcastIntent = new Intent(RESET_UNREAD_MENTIONS_COMMENT_ACTION);

        PendingIntent deletedPendingIntent = PendingIntent.getBroadcast(GlobalContext.getInstance(), 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletedPendingIntent);


        Intent intent = new Intent(getApplicationContext(), WriteReplyToCommentActivity.class);
        intent.putExtra("token", accountBean.getAccess_token());
        intent.putExtra("msg", data.getItem(0));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.reply_to_comment_light, getApplicationContext().getString(R.string.reply_to_comment), pendingIntent);


        if (data.getSize() > 1) {
            Intent nextIntent = new Intent(JBMentionsCommentNotificationServiceHelper.this, JBMentionsCommentNotificationServiceHelper.class);
            nextIntent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
            nextIntent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG, data);
            nextIntent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
            nextIntent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG, clickToOpenAppPendingIntentInner);
            nextIntent.putExtra(NotificationServiceHelper.TICKER, ticker);

            String actionName;
            int nextIndex;
            int actionDrawable;
            if (currentIndex < data.getSize() - 1) {
                nextIndex = currentIndex + 1;
                actionName = getString(R.string.next_message);
                actionDrawable = R.drawable.notification_action_next;
            } else {
                nextIndex = 0;
                actionName = getString(R.string.first_message);
                actionDrawable = R.drawable.notification_action_previous;
            }
            nextIntent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, nextIndex);
            PendingIntent retrySendIntent = PendingIntent.getService(JBMentionsCommentNotificationServiceHelper.this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(actionDrawable, actionName, retrySendIntent);
        }

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
        bigTextStyle.setBigContentTitle("@"
                + data.getItem(currentIndex).getUser().getScreen_name()
                + getString(R.string.comment_at_to_you));
        bigTextStyle.bigText(data.getItem(currentIndex).getText());
        String summaryText;
        if (data.getSize() > 1)
            summaryText = accountBean.getUsernick() + "(" + (currentIndex + 1) + "/" + data.getSize() + ")";
        else
            summaryText = accountBean.getUsernick();
        bigTextStyle.setSummaryText(summaryText);

        builder.setStyle(bigTextStyle);
        Utility.configVibrateLedRingTone(builder);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(getMentionsCommentNotificationId(accountBean), builder.build());
    }

    private PendingIntent getPendingIntent() {
        clickToOpenAppPendingIntentInner.setExtrasClassLoader(getClass().getClassLoader());
        clickToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA, UnreadTabIndex.MENTION_COMMENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, clickToOpenAppPendingIntentInner, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
