package org.qii.weiciyuan.othercomponent.unreadnotification;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.support.database.NotificationDBTask;
import org.qii.weiciyuan.support.lib.RecordOperationAppBroadcastReceiver;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteCommentActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: qii
 * Date: 13-5-4
 */
@Deprecated
public class JBMentionsWeiboNotificationServiceHelper extends NotificationServiceHelper {

    private AccountBean accountBean;

    private MessageListBean data;

    private UnreadBean unreadBean;

    private int currentIndex;

    private Intent clickToOpenAppPendingIntentInner;

    private String ticker;

    private static HashMap<String, RecordOperationAppBroadcastReceiver>
            clearNotificationEventReceiver
            = new HashMap<String, RecordOperationAppBroadcastReceiver>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.accountBean = intent.getParcelableExtra(NotificationServiceHelper.ACCOUNT_ARG);
        this.data = intent.getParcelableExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG);
        this.unreadBean = intent.getParcelableExtra(NotificationServiceHelper.UNREAD_ARG);
        this.currentIndex = intent.getIntExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
        this.clickToOpenAppPendingIntentInner = intent
                .getParcelableExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG);
        this.ticker = intent.getStringExtra(NotificationServiceHelper.TICKER);

        buildNotification();

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }


    private void buildNotification() {

//        int count = (data.getSize() >= Integer.valueOf(SettingUtility.getMsgCount()) ? unreadBean
//                .getMention_status() : data.getSize());

        int count = Math.min(unreadBean.getMention_status(), data.getSize());

        if (count == 0) {
            return;
        }

        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setTicker(ticker)
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())
                .setOnlyAlertOnce(true);

        builder.setContentTitle(
                String.format(GlobalContext.getInstance().getString(R.string.new_mentions_weibo),
                        String.valueOf(count)));

        if (count > 1) {
            builder.setNumber(count);
        }

        if (clearNotificationEventReceiver.get(accountBean.getUid()) != null) {
            Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                    GlobalContext.getInstance(),
                    clearNotificationEventReceiver.get(accountBean.getUid()));
            JBMentionsWeiboNotificationServiceHelper.clearNotificationEventReceiver
                    .put(accountBean.getUid(), null);
        }

        RecordOperationAppBroadcastReceiver receiver = new RecordOperationAppBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            new ClearUnreadDao(accountBean.getAccess_token())
//                                    .clearMentionStatusUnread(unreadBean, accountBean.getUid());

                            ArrayList<String> ids = new ArrayList<String>();

                            for (MessageBean msg : data.getItemList()) {
                                ids.add(msg.getId());
                            }

                            NotificationDBTask.addUnreadNotification(accountBean.getUid(), ids,
                                    NotificationDBTask.UnreadDBType.mentionsWeibo);

//                        } catch (WeiboException ignored) {

                        } finally {
                            Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                                    GlobalContext.getInstance(),
                                    clearNotificationEventReceiver.get(accountBean.getUid()));
                            JBMentionsWeiboNotificationServiceHelper.clearNotificationEventReceiver
                                    .put(accountBean.getUid(), null);
                        }

                    }
                }).start();
            }
        };

        clearNotificationEventReceiver.put(accountBean.getUid(), receiver);

        IntentFilter intentFilter = new IntentFilter(RESET_UNREAD_MENTIONS_WEIBO_ACTION);

        GlobalContext.getInstance()
                .registerReceiver(clearNotificationEventReceiver.get(accountBean.getUid()),
                        intentFilter);

        Intent broadcastIntent = new Intent(RESET_UNREAD_MENTIONS_WEIBO_ACTION);

        PendingIntent deletedPendingIntent = PendingIntent
                .getBroadcast(GlobalContext.getInstance(), 0, broadcastIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletedPendingIntent);

        Intent intent = WriteCommentActivity
                .newIntentFromNotification(getApplicationContext(), accountBean, data.getItem(
                        currentIndex));
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.comment_light,
                getApplicationContext().getString(R.string.comments), pendingIntent);

        if (count > 1) {
            Intent nextIntent = new Intent(JBMentionsWeiboNotificationServiceHelper.this,
                    JBMentionsWeiboNotificationServiceHelper.class);
            nextIntent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
            nextIntent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG, data);
            nextIntent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
            nextIntent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
                    clickToOpenAppPendingIntentInner);
            nextIntent.putExtra(NotificationServiceHelper.TICKER, ticker);

            String actionName;
            int nextIndex;
            int actionDrawable;
            if (currentIndex < count - 1) {
                nextIndex = currentIndex + 1;
                actionName = getString(R.string.next_message);
                actionDrawable = R.drawable.notification_action_next;
            } else {
                nextIndex = 0;
                actionName = getString(R.string.first_message);
                actionDrawable = R.drawable.notification_action_previous;
            }
            nextIntent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, nextIndex);
            PendingIntent retrySendIntent = PendingIntent
                    .getService(JBMentionsWeiboNotificationServiceHelper.this, 0, nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(actionDrawable, actionName, retrySendIntent);
        }

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
        if (data.getItem(currentIndex).getText().contains(accountBean.getUsernick())) {
            // mentioned you
            bigTextStyle.setBigContentTitle("@"
                    + data.getItem(currentIndex).getUser().getScreen_name()
                    + getString(R.string.weibo_at_to_you));
        } else {
            // retweeted your weibo
            bigTextStyle.setBigContentTitle("@"
                    + data.getItem(currentIndex).getUser().getScreen_name()
                    + getString(R.string.retweeted_your_weibo));
        }
        bigTextStyle.bigText(data.getItem(currentIndex).getText());
        String summaryText;
        if (count > 1) {
            summaryText = accountBean.getUsernick() + "(" + (currentIndex + 1) + "/" + count + ")";
        } else {
            summaryText = accountBean.getUsernick();
        }
        bigTextStyle.setSummaryText(summaryText);

        builder.setStyle(bigTextStyle);
        Utility.configVibrateLedRingTone(builder);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(getMentionsWeiboNotificationId(accountBean), builder.build());
    }

    private PendingIntent getPendingIntent() {
        clickToOpenAppPendingIntentInner.setExtrasClassLoader(getClass().getClassLoader());
        clickToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                UnreadTabIndex.MENTION_WEIBO);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getBaseContext(), 0, clickToOpenAppPendingIntentInner,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

}
