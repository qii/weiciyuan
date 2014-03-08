package org.qii.weiciyuan.othercomponent.unreadnotification;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.support.database.NotificationDBTask;
import org.qii.weiciyuan.support.lib.RecordOperationAppBroadcastReceiver;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

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
public class JBMentionsCommentNotificationServiceHelper extends NotificationServiceHelper {


    private AccountBean accountBean;

    private CommentListBean data;

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
        this.data = intent.getParcelableExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG);
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
//                .getMention_cmt() : data.getSize());

        int count = Math.min(unreadBean.getMention_cmt(), data.getSize());

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
                String.format(GlobalContext.getInstance().getString(R.string.new_mentions_comment),
                        String.valueOf(count)));

        if (count > 1) {
            builder.setNumber(count);
        }

        if (clearNotificationEventReceiver.get(accountBean.getUid()) != null) {
            Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                    GlobalContext.getInstance(),
                    clearNotificationEventReceiver.get(accountBean.getUid()));
            JBMentionsCommentNotificationServiceHelper.clearNotificationEventReceiver
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
//                                    .clearMentionCommentUnread(unreadBean, accountBean.getUid());

//                        } catch (WeiboException ignored) {

                            ArrayList<String> ids = new ArrayList<String>();

                            for (CommentBean msg : data.getItemList()) {
                                ids.add(msg.getId());
                            }

                            NotificationDBTask.addUnreadNotification(accountBean.getUid(), ids,
                                    NotificationDBTask.UnreadDBType.mentionsComment);

                        } finally {
                            Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                                    GlobalContext.getInstance(),
                                    clearNotificationEventReceiver.get(accountBean.getUid()));
                            JBMentionsCommentNotificationServiceHelper
                                    .clearNotificationEventReceiver
                                    .put(accountBean.getUid(), null);
                        }

                    }
                }).start();
            }
        };

        clearNotificationEventReceiver.put(accountBean.getUid(), receiver);

        IntentFilter intentFilter = new IntentFilter(RESET_UNREAD_MENTIONS_COMMENT_ACTION);

        GlobalContext.getInstance().registerReceiver(receiver, intentFilter);

        Intent broadcastIntent = new Intent(RESET_UNREAD_MENTIONS_COMMENT_ACTION);

        PendingIntent deletedPendingIntent = PendingIntent
                .getBroadcast(GlobalContext.getInstance(), 0, broadcastIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletedPendingIntent);

        Intent intent = WriteReplyToCommentActivity
                .newIntentFromNotification(getApplicationContext(), accountBean,
                        data.getItem(
                                currentIndex));
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.reply_to_comment_light,
                getApplicationContext().getString(R.string.reply_to_comment), pendingIntent);

        if (count > 1) {
            Intent nextIntent = new Intent(JBMentionsCommentNotificationServiceHelper.this,
                    JBMentionsCommentNotificationServiceHelper.class);
            nextIntent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
            nextIntent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG, data);
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
                    .getService(JBMentionsCommentNotificationServiceHelper.this, 0, nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(actionDrawable, actionName, retrySendIntent);
        }

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
        bigTextStyle.setBigContentTitle("@"
                + data.getItem(currentIndex).getUser().getScreen_name()
                + getString(R.string.comment_at_to_you));
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
        notificationManager.notify(getMentionsCommentNotificationId(accountBean), builder.build());
    }

    private PendingIntent getPendingIntent() {
        clickToOpenAppPendingIntentInner.setExtrasClassLoader(getClass().getClassLoader());
        clickToOpenAppPendingIntentInner.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                UnreadTabIndex.MENTION_COMMENT);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getBaseContext(), 0, clickToOpenAppPendingIntentInner,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
