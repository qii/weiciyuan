package org.qii.weiciyuan.othercomponent.sendweiboservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.dao.send.ReplyToCommentMsgDao;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.ReplyDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.ui.preference.DraftActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * User: qii
 * Date: 13-1-20
 */
public class SendReplyToCommentService extends Service {
    private String accountId;
    private String token;
    private String content;
    private String repostContent;
    private CommentBean oriMsg;
    private boolean comment_ori;

    private ReplyDraftBean replyDraftBean;

    private Map<WeiboSendTask, Boolean> tasksResult = new HashMap<WeiboSendTask, Boolean>();
    private Map<WeiboSendTask, Integer> tasksNotifications = new HashMap<WeiboSendTask, Integer>();

    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        token = intent.getStringExtra("token");
        accountId = intent.getStringExtra("accountId");
        content = intent.getStringExtra("content");
        repostContent = intent.getStringExtra("repostContent");
        oriMsg = (CommentBean) intent.getSerializableExtra("oriMsg");

        replyDraftBean = (ReplyDraftBean) intent.getSerializableExtra("draft");

        WeiboSendTask task = new WeiboSendTask();
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        tasksResult.put(task, false);

        return START_REDELIVER_INTENT;

    }


    private class WeiboSendTask extends MyAsyncTask<Void, Long, Void> {

        Notification notification;
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Notification.Builder builder = new Notification.Builder(SendReplyToCommentService.this)
                    .setTicker(getString(R.string.sending_comment))
                    .setContentTitle(getString(R.string.sending_comment))
                    .setContentText(content)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.upload_white);


            builder.setProgress(0, 100, true);


            int notificationId = new Random().nextInt(Integer.MAX_VALUE);


            notification = builder.getNotification();

            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);

            tasksNotifications.put(WeiboSendTask.this, notificationId);

        }


        private void sendText() throws WeiboException {
            ReplyToCommentMsgDao dao = new ReplyToCommentMsgDao(token, oriMsg, content);
            CommentBean commentBean = dao.reply();

            if (!TextUtils.isEmpty(repostContent)) {
                RepostNewMsgDao repostNewMsgDao = new RepostNewMsgDao(token, oriMsg.getStatus().getId());
                repostNewMsgDao.setStatus(content);
                repostNewMsgDao.sendNewMsg();
            }


        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendText();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (replyDraftBean != null)
                DraftDBManager.getInstance().remove(replyDraftBean.getId());
            showSuccessfulNotification(WeiboSendTask.this);

        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            showFailedNotification(WeiboSendTask.this);

        }

    }

    private void stopServiceIfTasksAreEnd(WeiboSendTask currentTask) {

        tasksResult.put(currentTask, true);

        boolean isAllTaskEnd = true;
        Set<WeiboSendTask> taskSet = tasksResult.keySet();
        for (WeiboSendTask task : taskSet) {
            if (!tasksResult.get(task)) {
                isAllTaskEnd = false;
                break;
            }
        }
        if (isAllTaskEnd) {
            stopForeground(true);
            stopSelf();
        }
    }

    private void showSuccessfulNotification(final WeiboSendTask task) {
        Notification.Builder builder = new Notification.Builder(SendReplyToCommentService.this)
                .setTicker(getString(R.string.send_successfully))
                .setContentTitle(getString(R.string.send_successfully))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.send_successfully)
                .setOngoing(false);
        Notification notification = builder.getNotification();
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        final int id = tasksNotifications.get(task);
        notificationManager.notify(id, notification);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(id);
                stopServiceIfTasksAreEnd(task);
            }
        }, 3000);
    }

    private void showFailedNotification(final WeiboSendTask task) {
        Notification.Builder builder = new Notification.Builder(SendReplyToCommentService.this)
                .setTicker(getString(R.string.send_failed_and_save_to_draft))
                .setContentTitle(getString(R.string.send_failed))
                .setContentText(getString(R.string.click_to_open_draft))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.send_failed)
                .setOngoing(false);

        Intent notifyIntent = new Intent(SendReplyToCommentService.this, DraftActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        Notification notification = builder.getNotification();
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        final int id = tasksNotifications.get(task);
        notificationManager.notify(id, notification);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopServiceIfTasksAreEnd(task);
            }
        }, 3000);
    }
}

