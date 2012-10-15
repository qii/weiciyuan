package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

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

            showNotification();
        }

    }

    private void showNotification() {
        Intent i = new Intent(context, MainTimeLineActivity.class);
        i.putExtra("account", accountBean);
        i.putExtra("comment", comment);
        i.putExtra("repost", repost);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activity = PendingIntent.getActivity(context, Long.valueOf(accountBean.getUid()).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notification = new Notification.Builder(context)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true)
                .setContentIntent(activity);
        if (sum > 1) {
            notification.setNumber(sum);
        }
        notificationManager.notify(Long.valueOf(accountBean.getUid()).intValue(), notification.getNotification());

    }


}
