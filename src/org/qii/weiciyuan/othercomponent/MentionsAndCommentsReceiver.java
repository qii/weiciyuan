package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class MentionsAndCommentsReceiver extends BroadcastReceiver {

    public static final String ACTION = "org.qii.weiciyuan.newmsg";

    private Context context;
    AccountBean accountBean;
    Integer commentsum;
    Integer repostsum;
    int sum;

    String title = "";
    String content = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        accountBean = (AccountBean) intent.getSerializableExtra("account");
        Integer commentsum = intent.getIntExtra("commentsum", 0);
        Integer repostsum = intent.getIntExtra("repostsum", 0);
        sum = commentsum + repostsum;

        CommentListBean comment = (CommentListBean) intent.getSerializableExtra("comment");
        MessageListBean repost = (MessageListBean) intent.getSerializableExtra("repost");


        if (comment.getSize() > 0) {
            title = comment.getItemList().get(0).getUser().getScreen_name();
            content = comment.getItemList().get(0).getText();
        } else if (repost.getSize() > 0) {
            title = repost.getItemList().get(0).getUser().getScreen_name();
            content = repost.getItemList().get(0).getText();
        } else {
            return;
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
        i.putExtra("commentsum", commentsum);
        i.putExtra("repostsum", repostsum);
        PendingIntent activity = PendingIntent.getActivity(context, 0, i, 0);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.notification)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setContentIntent(activity);
        if (sum > 0) {
            notification.setNumber(sum);
        }
        notificationManager.notify(0, notification.getNotification());
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

            return ImageTool.getPictureHighDensityThumbnailWithoutRoundedCornerBitmap(url, width,height,null);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            showNotification(bitmap);
        }
    }
}
