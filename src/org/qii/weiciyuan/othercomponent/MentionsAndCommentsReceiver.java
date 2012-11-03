package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class MentionsAndCommentsReceiver extends BroadcastReceiver {

    public static final String ACTION = "org.qii.weiciyuan.newmsg";

    private Context context;
    private AccountBean accountBean;

    private int sum;

    private CommentListBean comment;
    private MessageListBean repost;
    private CommentListBean mentionCommentsResult;
    private UnreadBean unreadBean;


    private String title = "";
    private String content = "";
    private String ticker = "";

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
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(Long.valueOf(accountBean.getUid()).intValue());
        } else if (sum > 0) {
            Set<String> peopleNameSet = new HashSet<String>();

            String lastName = "";

            if (comment != null && comment.getSize() > 0) {
                for (CommentBean commentBean : comment.getItemList()) {
                    UserBean userBean = commentBean.getUser();
                    if (userBean != null) {
                        lastName = userBean.getScreen_name();
                        content = commentBean.getText();
                        peopleNameSet.add(userBean.getScreen_name());
                    }
                }
            } else if (repost != null && repost.getSize() > 0) {
                for (MessageBean messageBean : repost.getItemList()) {
                    UserBean userBean = messageBean.getUser();
                    if (userBean != null) {
                        lastName = userBean.getScreen_name();
                        content = messageBean.getText();
                        peopleNameSet.add(userBean.getScreen_name());
                    }
                }

            }


            peopleNameSet.remove(lastName);

            StringBuilder nameBuilder = new StringBuilder(lastName);

            for (String name : peopleNameSet) {
                nameBuilder.append("、").append(name);
            }

            title = nameBuilder.toString();
            if (content.length() <= 20) {
                ticker = lastName + ":" + content;
            } else {
                ticker = lastName + ":" + content.substring(0, 20) + "……";
            }

            if (!TextUtils.isEmpty(lastName))
                showNotification();
        }

    }

    private void showNotification() {
        Intent i = new Intent(context, MainTimeLineActivity.class);
        i.putExtra("account", accountBean);
        i.putExtra("comment", comment);
        i.putExtra("repost", repost);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(accountBean.getUid()).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification = buildICSNotification(pendingIntent);
        } else {
            notification = buildJBNotification(pendingIntent);
        }


        notificationManager.notify(Long.valueOf(accountBean.getUid()).intValue(), notification);

    }


    private Notification buildICSNotification(PendingIntent pendingIntent) {
        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        configVibrateLedRingTone(builder);

        if (sum > 1) {
            builder.setNumber(sum);
        }
        return builder.getNotification();
    }


    private Notification buildJBNotification(PendingIntent pendingIntent) {
        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(ticker)
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        configVibrateLedRingTone(builder);

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

        builder.setContentTitle(stringBuilder.toString());

        if (sum > 1) {
            builder.setNumber(sum);
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
        inboxStyle.setBigContentTitle(stringBuilder.toString());
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


        builder.setStyle(inboxStyle);

        return builder.build();
    }


    private boolean allowVibrate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(SettingActivity.ENABLE_VIBRATE, false);
    }

    private boolean allowLed() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(SettingActivity.ENABLE_LED, false);
    }

    private void configVibrateLedRingTone(Notification.Builder builder) {
        configRingTone(builder);
        configLed(builder);
        configVibrate(builder);
    }

    private void configVibrate(Notification.Builder builder) {
        if (allowVibrate()) {
            long[] pattern = {0, 200, 500};
            builder.setVibrate(pattern);
        }
    }

    private void configRingTone(Notification.Builder builder) {
        Uri uri = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String path = sharedPref.getString(SettingActivity.ENABLE_RINGTONE, "");
        if (!TextUtils.isEmpty(path)) {
            uri = Uri.parse(path);
        }

        if (uri != null) {
            builder.setSound(uri);
        }
    }

    private void configLed(Notification.Builder builder) {
        if (allowLed()) {
            builder.setLights(Color.WHITE, 300, 1000);
        }

    }
}
