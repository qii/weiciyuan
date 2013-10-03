package org.qii.weiciyuan.othercomponent.sendweiboservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.StatusDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.NotificationUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

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


    private Map<WeiboSendTask, Boolean> tasksResult = new HashMap<WeiboSendTask, Boolean>();
    private Map<WeiboSendTask, Integer> tasksNotifications = new HashMap<WeiboSendTask, Integer>();

    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int lastNotificationId = intent.getIntExtra("lastNotificationId", -1);

        String token = intent.getStringExtra("token");
        AccountBean account = (AccountBean) intent.getParcelableExtra("account");
        String picPath = intent.getStringExtra("picPath");
        String content = intent.getStringExtra("content");
        GeoBean geoBean = (GeoBean) intent.getParcelableExtra("geo");

        StatusDraftBean statusDraftBean = (StatusDraftBean) intent.getParcelableExtra("draft");

        WeiboSendTask task = new WeiboSendTask(lastNotificationId, token, account, picPath, content, geoBean, statusDraftBean);
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        tasksResult.put(task, false);

        return START_REDELIVER_INTENT;

    }

    public void stopServiceIfTasksAreEnd(WeiboSendTask currentTask) {

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

    private class WeiboSendTask extends MyAsyncTask<Void, Long, Void> {

        Notification notification;
        WeiboException e;
        long size;
        BroadcastReceiver receiver;
        PendingIntent pendingIntent;

        int lastNotificationId;

        String token;
        AccountBean account;
        String picPath;
        String content;
        GeoBean geoBean;

        StatusDraftBean statusDraftBean;


        public WeiboSendTask(int lastNotificationId,
                             String token,
                             AccountBean account,
                             String picPath,
                             String content,
                             GeoBean geoBean,
                             StatusDraftBean statusDraftBean) {
            this.lastNotificationId = lastNotificationId;
            this.token = token;
            this.account = account;
            this.content = content;
            this.picPath = picPath;
            this.geoBean = geoBean;
            this.statusDraftBean = statusDraftBean;
        }

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

            int notificationId = (lastNotificationId != -1) ? lastNotificationId : new Random().nextInt(Integer.MAX_VALUE);

            if (Utility.isJB()) {
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        WeiboSendTask.this.cancel(true);
                    }
                };

                IntentFilter intentFilter = new IntentFilter("org.qii.weiciyuan.SendWeiboService.stop." + String.valueOf(notificationId));

                registerReceiver(receiver, intentFilter);

                Intent broadcastIntent = new Intent("org.qii.weiciyuan.SendWeiboService.stop." + String.valueOf(notificationId));

                pendingIntent = PendingIntent.getBroadcast(SendWeiboService.this, 1, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.send_failed, getString(R.string.cancel), pendingIntent);
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }

            NotificationUtility.show(notification, notificationId);
            tasksNotifications.put(WeiboSendTask.this, notificationId);

        }

        private boolean sendPic(String uploadPicPath) throws WeiboException {
            return new StatusNewMsgDao(token).setPic(uploadPicPath).setGeoBean(geoBean).sendNewMsg(content, new FileUploaderHttpHelper.ProgressListener() {

                @Override
                public void transferred(long data) {

                    publishProgress(data);
                }

                @Override
                public void waitServerResponse() {
                    publishProgress(-1L);
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
                    String uploadPicPath = ImageUtility.compressPic(SendWeiboService.this, picPath);
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
        private long lastMillis = -1L;

        @Override
        protected void onProgressUpdate(Long... values) {

            if (values.length > 0) {

                long data = values[0];
                if (data != -1) {
                    double r = data / (double) size;

                    if (Math.abs(r - lastStatus) < 0.01d) {
                        return;
                    }

                    if (System.currentTimeMillis() - lastMillis < 200L) {
                        return;
                    }

                    lastStatus = r;

                    lastMillis = System.currentTimeMillis();

                    Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                            .setTicker(getString(R.string.send_photo))
                            .setContentTitle(getString(R.string.background_sending))
                            .setNumber((int) (r * 100))
                            .setContentText(content)
                            .setProgress((int) size, (int) data, false)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .setSmallIcon(R.drawable.upload_white);

                    if (Utility.isJB()) {
                        builder.addAction(R.drawable.send_failed, getString(R.string.cancel), pendingIntent);
                        notification = builder.build();
                    } else {
                        notification = builder.getNotification();
                    }
                } else {
                    Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                            .setTicker(getString(R.string.send_photo))
                            .setContentTitle(getString(R.string.wait_server_response))
                            .setContentText(content)
                            .setNumber(100)
                            .setProgress(100, 100, false)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .setSmallIcon(R.drawable.upload_white);

                    notification = builder.getNotification();
                }

                NotificationUtility.show(notification, tasksNotifications.get(WeiboSendTask.this));

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (statusDraftBean != null)
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
            showSuccessfulNotification(WeiboSendTask.this);

            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);

            showFailedNotification(WeiboSendTask.this);

            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }

        private void showFailedNotification(final WeiboSendTask task) {
            Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                    .setTicker(getString(R.string.send_failed))
                    .setContentTitle(getString(R.string.send_faile_click_to_open))
                    .setContentText(content)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.send_failed)
                    .setOngoing(false);

            Intent notifyIntent = WriteWeiboActivity.startBecauseSendFailed(SendWeiboService.this,
                    account, content, picPath, geoBean, statusDraftBean,
                    String.format(SendWeiboService.this.getString(R.string.failed_reason), e.getError()));

            PendingIntent pendingIntent = PendingIntent.getActivity(SendWeiboService.this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);

            Notification notification;
            if (Utility.isJB()) {
                if (TextUtils.isEmpty(picPath)) {
                    Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
                    bigTextStyle.setBigContentTitle(getString(R.string.send_faile_click_to_open));
                    bigTextStyle.bigText(content);
                    bigTextStyle.setSummaryText(account.getUsernick());
                    builder.setStyle(bigTextStyle);
                } else {
                    Bitmap bitmap = ImageUtility.getNotificationSendFailedPic(picPath);
                    if (bitmap != null) {
                        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle(builder);
                        bigPictureStyle.setBigContentTitle(getString(R.string.send_faile_click_to_open));
                        bigPictureStyle.bigPicture(bitmap);
                        bigPictureStyle.setSummaryText(account.getUsernick());
                        builder.setStyle(bigPictureStyle);
                    } else {
                        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
                        bigTextStyle.setBigContentTitle(getString(R.string.send_faile_click_to_open));
                        bigTextStyle.bigText(content);
                        bigTextStyle.setSummaryText(account.getUsernick());
                        builder.setStyle(bigTextStyle);
                    }
                }
                Intent intent = new Intent(SendWeiboService.this, SendWeiboService.class);
                intent.putExtra("token", token);
                intent.putExtra("picPath", picPath);
                intent.putExtra("account", account);
                intent.putExtra("content", content);
                intent.putExtra("geo", geoBean);
                intent.putExtra("draft", statusDraftBean);

                intent.putExtra("lastNotificationId", tasksNotifications.get(task));

                PendingIntent retrySendIntent = PendingIntent.getService(SendWeiboService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.send_light, getString(R.string.retry_send), retrySendIntent);
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }


            final int id = tasksNotifications.get(task);
            NotificationUtility.show(notification, id);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopServiceIfTasksAreEnd(task);
                }
            }, 3000);
        }


        private void showSuccessfulNotification(final WeiboSendTask task) {
            Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                    .setTicker(getString(R.string.send_successfully))
                    .setContentTitle(getString(R.string.send_successfully))
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.send_successfully)
                    .setOngoing(false);
            Notification notification = builder.getNotification();
            final int id = tasksNotifications.get(task);
            NotificationUtility.show(notification, id);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationUtility.cancel(id);
                    stopServiceIfTasksAreEnd(task);
                }
            }, 3000);
        }


    }


}
