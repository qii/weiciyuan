package org.qii.weiciyuan.othercomponent.unreadnotification;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.ItemBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.support.asyncdrawable.TaskCache;
import org.qii.weiciyuan.support.database.NotificationDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.RecordOperationAppBroadcastReceiver;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteCommentActivity;
import org.qii.weiciyuan.ui.send.WriteReplyToCommentActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: qii
 * Date: 14-3-8
 */
public class BigTextNotificationService extends NotificationServiceHelper {

    public static Intent newIntent(AccountBean accountBean,
            MessageListBean mentionsWeiboData, CommentListBean commentsToMeData
            , CommentListBean mentionsCommentData, UnreadBean unreadBean,
            Intent clickNotificationToOpenAppPendingIntentInner, String ticker, int currentIndex) {

        Intent intent = new Intent(GlobalContext.getInstance(),
                BigTextNotificationService.class);

        intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
        intent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG,
                mentionsWeiboData);
        intent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG,
                mentionsCommentData);
        intent.putExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG, commentsToMeData);
        intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
        intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, currentIndex);
        intent.putExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG,
                clickNotificationToOpenAppPendingIntentInner);
        intent.putExtra(NotificationServiceHelper.TICKER, ticker);
        return intent;
    }

    private class ValueWrapper {

        private AccountBean accountBean;
        private MessageListBean mentionsWeibo;
        private CommentListBean mentionsComment;
        private CommentListBean commentsToMe;
        private UnreadBean unreadBean;

        private int currentIndex;

        private Intent clickToOpenAppPendingIntentInner;
        private String ticker;

        private ArrayList<Parcelable> notificationItems;

        private RecordOperationAppBroadcastReceiver clearNotificationEventReceiver;
    }

    //key is account uid
    private static HashMap<String, ValueWrapper> valueBagHashMap
            = new HashMap<String, ValueWrapper>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AccountBean accountBean = intent.getParcelableExtra(NotificationServiceHelper.ACCOUNT_ARG);

        if (accountBean == null) {
            throw new IllegalArgumentException(
                    "you must use BigTextNotificationService newIntent method");
        }

        ValueWrapper valueWrapper = valueBagHashMap.get(accountBean.getUid());

        if (valueWrapper == null) {
            valueWrapper = new ValueWrapper();
        }

        valueWrapper.accountBean = intent.getParcelableExtra(NotificationServiceHelper.ACCOUNT_ARG);
        valueWrapper.mentionsWeibo = intent
                .getParcelableExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG);
        valueWrapper.commentsToMe = intent
                .getParcelableExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG);
        valueWrapper.mentionsComment = intent
                .getParcelableExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG);
        valueWrapper.unreadBean = intent.getParcelableExtra(NotificationServiceHelper.UNREAD_ARG);
        valueWrapper.currentIndex = intent
                .getIntExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
        valueWrapper.clickToOpenAppPendingIntentInner = intent
                .getParcelableExtra(NotificationServiceHelper.PENDING_INTENT_INNER_ARG);
        valueWrapper.ticker = intent.getStringExtra(NotificationServiceHelper.TICKER);

        ArrayList<Parcelable> notificationItems = new ArrayList<Parcelable>();
        if (valueWrapper.commentsToMe != null) {
            notificationItems.addAll(valueWrapper.commentsToMe.getItemList());
        }
        if (valueWrapper.mentionsComment != null) {
            notificationItems.addAll(valueWrapper.mentionsComment.getItemList());
        }
        if (valueWrapper.mentionsWeibo != null) {
            notificationItems.addAll(valueWrapper.mentionsWeibo.getItemList());
        }
        valueWrapper.notificationItems = notificationItems;
        valueBagHashMap.put(valueWrapper.accountBean.getUid(), valueWrapper);

        AppLogger.i("service account name=" + valueWrapper.accountBean.getUsernick());

        buildNotification(valueWrapper.accountBean.getUid());

        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }

    private void buildNotification(String uid) {

        final ValueWrapper valueWrapper = valueBagHashMap.get(uid);

        if (valueWrapper == null) {
            return;
        }

        final AccountBean accountBean = valueWrapper.accountBean;

        final MessageListBean mentionsWeibo = valueWrapper.mentionsWeibo;

        final CommentListBean mentionsComment = valueWrapper.mentionsComment;

        final CommentListBean commentsToMe = valueWrapper.commentsToMe;

        final UnreadBean unreadBean = valueWrapper.unreadBean;

        int currentIndex = valueWrapper.currentIndex;

        Intent clickToOpenAppPendingIntentInner = valueWrapper.clickToOpenAppPendingIntentInner;

        String ticker = valueWrapper.ticker;

        ArrayList<Parcelable> notificationItems = valueWrapper.notificationItems;

//        int count = Math.min(unreadBean.getMention_status(), data.getSize());

        int count = notificationItems.size();

        if (count == 0) {
            return;
        }

        Parcelable itemBean = notificationItems.get(currentIndex);

        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setTicker(ticker)
                .setContentText(ticker)
                .setSubText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(
                        getPendingIntent(clickToOpenAppPendingIntentInner, itemBean, accountBean))
                .setOnlyAlertOnce(true);

        builder.setContentTitle(getString(R.string.app_name));

        if (count > 1) {
            builder.setNumber(count);
        }

        Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                GlobalContext.getInstance(), valueWrapper.clearNotificationEventReceiver);

        valueWrapper.clearNotificationEventReceiver = new RecordOperationAppBroadcastReceiver() {

            //mark these messages as read, write to database
            @Override
            public void onReceive(Context context, Intent intent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<String> ids = new ArrayList<String>();

                            if (mentionsWeibo != null) {
                                for (MessageBean msg : mentionsWeibo.getItemList()) {
                                    ids.add(msg.getId());
                                }

                                NotificationDBTask.addUnreadNotification(accountBean.getUid(), ids,
                                        NotificationDBTask.UnreadDBType.mentionsWeibo);
                            }

                            ids.clear();

                            if (commentsToMe != null) {

                                for (CommentBean msg : commentsToMe.getItemList()) {
                                    ids.add(msg.getId());
                                }

                                NotificationDBTask.addUnreadNotification(accountBean.getUid(), ids,
                                        NotificationDBTask.UnreadDBType.commentsToMe);
                            }
                            ids.clear();
                            if (mentionsComment != null) {
                                for (CommentBean msg : mentionsComment.getItemList()) {
                                    ids.add(msg.getId());
                                }

                                NotificationDBTask.addUnreadNotification(accountBean.getUid(), ids,
                                        NotificationDBTask.UnreadDBType.mentionsComment);
                            }
                        } finally {
                            Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(
                                    GlobalContext.getInstance(),
                                    valueWrapper.clearNotificationEventReceiver);
                            if (Utility.isDebugMode()) {

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "weiciyuan:remove notification items" + System
                                                        .currentTimeMillis(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        };

        IntentFilter intentFilter = new IntentFilter(RESET_UNREAD_MENTIONS_WEIBO_ACTION);

        Utility.registerReceiverIgnoredReceiverHasRegisteredHereException(
                GlobalContext.getInstance(), valueWrapper.clearNotificationEventReceiver,
                intentFilter);

        Intent broadcastIntent = new Intent(RESET_UNREAD_MENTIONS_WEIBO_ACTION);

        PendingIntent deletedPendingIntent = PendingIntent
                .getBroadcast(GlobalContext.getInstance(), accountBean.getUid().hashCode(),
                        broadcastIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(deletedPendingIntent);

        if (itemBean instanceof MessageBean) {
            MessageBean msg = (MessageBean) itemBean;
            Intent intent = WriteCommentActivity
                    .newIntentFromNotification(getApplicationContext(), accountBean, msg);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.comment_light,
                    getApplicationContext().getString(R.string.comments), pendingIntent);
        } else if (itemBean instanceof CommentBean) {
            CommentBean commentBean = (CommentBean) itemBean;
            Intent intent = WriteReplyToCommentActivity
                    .newIntentFromNotification(getApplicationContext(), accountBean, commentBean);
            PendingIntent pendingIntent = PendingIntent
                    .getActivity(getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.reply_to_comment_light,
                    getApplicationContext().getString(R.string.reply_to_comment), pendingIntent);
        }

        String avatar = ((ItemBean) itemBean).getUser().getAvatar_large();
        String avatarPath = FileManager.getFilePathFromUrl(avatar, FileLocationMethod.avatar_large);
        if (ImageUtility.isThisBitmapCanRead(avatarPath) && TaskCache
                .isThisUrlTaskFinished(avatar)) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarPath, new BitmapFactory.Options());
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }
        }

        if (count > 1) {

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
            Intent nextIntent = BigTextNotificationService
                    .newIntent(accountBean, mentionsWeibo, commentsToMe,
                            mentionsComment,
                            unreadBean, clickToOpenAppPendingIntentInner, ticker,
                            nextIndex);
            PendingIntent retrySendIntent = PendingIntent
                    .getService(BigTextNotificationService.this, accountBean.getUid().hashCode(),
                            nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(actionDrawable, actionName, retrySendIntent);
        }

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
        bigTextStyle.setBigContentTitle(
                getItemBigContentTitle(accountBean, notificationItems, currentIndex));
        bigTextStyle.bigText(getItemBigText(notificationItems, currentIndex));
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

    private PendingIntent getPendingIntent(Intent clickToOpenAppPendingIntentInner,
            Parcelable itemBean, AccountBean accountBean) {
        clickToOpenAppPendingIntentInner.setExtrasClassLoader(getClass().getClassLoader());

        UnreadTabIndex unreadTabIndex = UnreadTabIndex.NONE;

        if (itemBean instanceof MessageBean) {
            unreadTabIndex = UnreadTabIndex.MENTION_WEIBO;
        } else if (itemBean instanceof CommentBean) {
            CommentBean commentBean = (CommentBean) itemBean;
            MessageBean messageBean = commentBean.getStatus();
            if (messageBean != null) {
                UserBean userBean = messageBean.getUser();
                if (accountBean.getInfo().equals(userBean)) {
                    unreadTabIndex = UnreadTabIndex.COMMENT_TO_ME;
                } else {
                    unreadTabIndex = UnreadTabIndex.MENTION_COMMENT;
                }
            } else {
                unreadTabIndex = UnreadTabIndex.MENTION_COMMENT;
            }
        }
        clickToOpenAppPendingIntentInner
                .putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                        unreadTabIndex);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(getBaseContext(), getMentionsWeiboNotificationId(accountBean),
                        clickToOpenAppPendingIntentInner,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private String getItemBigContentTitle(AccountBean accountBean,
            ArrayList<Parcelable> notificationItems, int currentIndex) {
//        Parcelable itemBean = notificationItems.get(currentIndex);
//        if (itemBean instanceof MessageBean) {
//            MessageBean msg = (MessageBean) itemBean;
//            if (msg.getText().contains(accountBean.getUsernick())) {
//                // mentioned you
//                return "@"
//                        + msg.getUser().getScreen_name()
//                        + getString(R.string.weibo_at_to_you);
//            } else {
//                // retweeted your weibo
//                return "@"
//                        + msg.getUser().getScreen_name()
//                        + getString(R.string.retweeted_your_weibo);
//            }
//        } else if (itemBean instanceof CommentBean) {
//            CommentBean commentBean = (CommentBean) itemBean;
//            CommentBean oriCommentBean = commentBean.getReply_comment();
//            MessageBean oriMessageBean = commentBean.getStatus();
//            if (oriCommentBean != null && accountBean.getInfo().equals(oriCommentBean.getUser())) {
//                return "@"
//                        + commentBean.getUser().getScreen_name()
//                        + getString(R.string.reply_to_you);
//            } else if (oriMessageBean != null && accountBean.getInfo()
//                    .equals(oriMessageBean.getUser())) {
//                return "@"
//                        + commentBean.getUser().getScreen_name()
//                        + getString(R.string.comment_sent_to_you);
//            } else {
//                return commentBean.getUser().getScreen_name()
//                        + getString(R.string.comment_at_to_you);
//            }
//        }

        ItemBean itemBean = (ItemBean) notificationItems.get(currentIndex);
        return itemBean.getUser().getScreen_name();
    }

    private CharSequence getItemBigText(ArrayList<Parcelable> notificationItems, int currentIndex) {
        ItemBean itemBean = (ItemBean) notificationItems.get(currentIndex);
        return TimeLineUtility.convertNormalStringToSpannableString(itemBean.getText());
    }
}
