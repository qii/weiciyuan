package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.StatusDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-21
 */
public class SendWeiboService extends Service {
    private String accountId;
    private String token;
    private String picPath;
    private String content;
    private GeoBean geoBean;

    private StatusDraftBean statusDraftBean;

    private Map<WeiboSendTask, Boolean> tasksResult = new HashMap<WeiboSendTask, Boolean>();
    private Map<WeiboSendTask, Integer> tasksNotifications = new HashMap<WeiboSendTask, Integer>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        token = intent.getStringExtra("token");
        accountId = intent.getStringExtra("accountId");
        picPath = intent.getStringExtra("picPath");
        content = intent.getStringExtra("content");
        geoBean = (GeoBean) intent.getSerializableExtra("geo");

        statusDraftBean = (StatusDraftBean) intent.getSerializableExtra("draft");

        WeiboSendTask task = new WeiboSendTask();
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        tasksResult.put(task, false);

        return super.onStartCommand(intent, flags, startId);

    }


    private class WeiboSendTask extends MyAsyncTask<Void, Long, Void> {

        Notification notification;
        WeiboException e;
        long size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                    .setTicker(getString(R.string.sending))
                    .setContentTitle(getString(R.string.sending))
                    .setContentText(content)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.upload_white);

            if (!TextUtils.isEmpty(picPath)) {
                builder.setProgress(0, 100, false);
            } else {
                builder.setProgress(0, 100, true);
            }

            notification = builder.getNotification();

            int notificationId = new Random().nextInt(Integer.MAX_VALUE);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);

            tasksNotifications.put(WeiboSendTask.this, notificationId);

            Toast.makeText(SendWeiboService.this, getString(R.string.background_sending), Toast.LENGTH_SHORT).show();
        }

        private boolean sendPic(String uploadPicPath) throws WeiboException {
            return new StatusNewMsgDao(token).setPic(uploadPicPath).setGeoBean(geoBean).sendNewMsg(content, new FileUploaderHttpHelper.ProgressListener() {

                @Override
                public void transferred(long data) {

                    publishProgress((long) (data * 0.9));
                }

                @Override
                public void completed() {
                    publishProgress(size);
                }
            });
        }

        private boolean sendText() throws WeiboException {
            return new StatusNewMsgDao(token).setGeoBean(geoBean).sendNewMsg(content, null);
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean result = false;

            try {
                if (!TextUtils.isEmpty(picPath)) {
                    String uploadPicPath = ImageTool.compressPic(SendWeiboService.this, picPath);
                    size = new File(uploadPicPath).length();
                    result = sendPic(uploadPicPath);
                } else {
                    result = sendText();
                }
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);

            }
            if (!result)
                cancel(true);
            return null;
        }


        private double lastStatus = -1d;

        @Override
        protected void onProgressUpdate(Long... values) {

            if (values.length > 0) {

                long data = values[0];

                double r = data / (double) size;

                if (Math.abs(r - lastStatus) < 0.01d) {
                    return;
                }

                lastStatus = r;

                Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                        .setTicker(getString(R.string.send_photo))
                        .setContentTitle(getString(R.string.background_sending))
                        .setContentText(content)
                        .setProgress((int) size, (int) data, false)
                        .setOnlyAlertOnce(true)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.upload_white);
                notification = builder.getNotification();
                NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(tasksNotifications.get(WeiboSendTask.this), notification);

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (statusDraftBean != null)
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
            Toast.makeText(SendWeiboService.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
            stopServiceIfTasksAreEnd(WeiboSendTask.this);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            if (statusDraftBean != null) {
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
                DraftDBManager.getInstance().insertStatus(content, geoBean, picPath, accountId);
            } else {
                DraftDBManager.getInstance().insertStatus(content, geoBean, picPath, accountId);
            }
            Toast.makeText(SendWeiboService.this, getString(R.string.send_failed_and_save_to_draft), Toast.LENGTH_SHORT).show();

            stopServiceIfTasksAreEnd(WeiboSendTask.this);
        }

    }

    private void stopServiceIfTasksAreEnd(WeiboSendTask currentTask) {

        tasksResult.put(currentTask, true);

        boolean isAllTaskEnd = true;
        Set<WeiboSendTask> taskSet = tasksResult.keySet();
        for (WeiboSendTask task : taskSet) {
            if (!tasksResult.get(task)) {
                isAllTaskEnd = false;
            } else {
                NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(tasksNotifications.get(task));
            }
        }
        if (isAllTaskEnd) {
            stopForeground(true);
            stopSelf();
        }
    }
}
