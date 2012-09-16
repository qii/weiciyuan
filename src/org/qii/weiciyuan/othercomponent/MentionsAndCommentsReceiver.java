package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
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
    private String title = "";
    private String content = "";
    private String ticker = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        accountBean = (AccountBean) intent.getSerializableExtra("account");
        comment = (CommentListBean) intent.getSerializableExtra("comment");
        repost = (MessageListBean) intent.getSerializableExtra("repost");

        sum = comment.getSize() + repost.getSize();


        Set<String> peopleNameSet = new HashSet<String>();

        String lastName;

        if (comment.getSize() > 0) {
            lastName = comment.getItemList().get(0).getUser().getScreen_name();
            content = comment.getItemList().get(0).getText();
            for (CommentBean bean : comment.getItemList()) {
                peopleNameSet.add(bean.getUser().getScreen_name());
            }
        } else if (repost.getSize() > 0) {
            lastName = repost.getItemList().get(0).getUser().getScreen_name();
            content = repost.getItemList().get(0).getText();
            for (MessageBean bean : repost.getItemList()) {
                peopleNameSet.add(bean.getUser().getScreen_name());
            }
        } else {
            return;
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

        String avatarUrl;
        if (comment.getSize() > 0)
            avatarUrl = comment.getItemList().get(0).getUser().getAvatar_large();
        else
            avatarUrl = repost.getItemList().get(0).getUser().getAvatar_large();

        new DownloadAvatar(avatarUrl).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void showNotification(Bitmap bitmap) {
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
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setContentIntent(activity);
        if (sum > 1) {
            notification.setNumber(sum);
        }
        notificationManager.notify(Long.valueOf(accountBean.getUid()).intValue(), notification.getNotification());

    }


    class DownloadAvatar extends MyAsyncTask<Void, Bitmap, Bitmap> {

        String url;

        public DownloadAvatar(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {


            int width = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
            int height = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

            return ImageTool.getBigAvatarWithoutRoundedCorner(url, width, height);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            showNotification(bitmap);
        }
    }
}
