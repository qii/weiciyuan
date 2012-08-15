package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class MentionsAndCommentsReceiver extends BroadcastReceiver {

    public static final String ACTION = "org.qii.weiciyuan.newmsg";

    @Override
    public void onReceive(Context context, Intent intent) {
        AccountBean accountBean = (AccountBean) intent.getSerializableExtra("account");
        Integer commentsum = intent.getIntExtra("commentsum", 0);
        Integer repostsum = intent.getIntExtra("repostsum", 0);
        int sum = commentsum + repostsum;

        CommentListBean comment = (CommentListBean) intent.getSerializableExtra("comment");
        MessageListBean repost = (MessageListBean) intent.getSerializableExtra("repost");

        String title = "";
        String content = "";

        if (comment.getComments().size() != 0) {
            title = comment.getComments().get(0).getUser().getScreen_name();
            content = comment.getComments().get(0).getText();
        } else {
            title = repost.getStatuses().get(0).getUser().getScreen_name();
            content = repost.getStatuses().get(0).getText();
        }

        Intent i = new Intent(context, MainTimeLineActivity.class);
        i.putExtra("account", accountBean);
        PendingIntent activity = PendingIntent.getActivity(context, 0, i, 0);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.new_msg)
                .setAutoCancel(true)
                .setContentIntent(activity);
        if (sum > 0) {
            notification.setNumber(repostsum + commentsum);
        }

        notificationManager.notify(0, notification.getNotification());
    }
}
