package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.io.File;

/**
 * User: qii
 * Date: 12-8-21
 */
public class PhotoUploadService extends Service {
    private String token;
    private String picPath;
    private String content;
    private GeoBean geoBean;
    private UploadTask task;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        token = intent.getStringExtra("token");
        picPath = intent.getStringExtra("picPath");
        content = intent.getStringExtra("content");
        geoBean = (GeoBean) intent.getSerializableExtra("geo");

        if (task == null) {
            task = new UploadTask();
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        return super.onStartCommand(intent, flags, startId);

    }

    class UploadTask extends MyAsyncTask<Void, Long, Void> {

        Notification notification;
        final int NOTIFICATION_ID = 1;
        WeiboException e;
        long size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Notification.Builder builder = new Notification.Builder(PhotoUploadService.this)
                    .setTicker(getString(R.string.send_photo))
                    .setContentTitle(getString(R.string.background_sending))
                    .setContentText(content)
                    .setProgress(0, 100, false)
                    .setSmallIcon(R.drawable.upload_white);
            notification = builder.getNotification();
            startForeground(NOTIFICATION_ID, notification);
            Toast.makeText(PhotoUploadService.this, getString(R.string.background_sending), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean result = false;
            size = new File(picPath).length();

            try {
                result = new StatusNewMsgDao(token).setPic(picPath).setGeoBean(geoBean).sendNewMsg(content, new FileUploaderHttpHelper.ProgressListener() {
                    @Override
                    public void transferred(long data) {
                        publishProgress(data);
                    }
                });
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);

            }
            if (!result)
                cancel(true);
            return null;
        }


        @Override
        protected void onProgressUpdate(Long... values) {

            if (values.length > 0) {
                long data = values[0];

                Notification.Builder builder = new Notification.Builder(PhotoUploadService.this)
                        .setTicker(getString(R.string.send_photo))
                        .setContentTitle(getString(R.string.background_sending))
                        .setContentText(content)
                        .setProgress((int) size, (int) data, false)
                        .setSmallIcon(R.drawable.upload_white);
                notification = builder.getNotification();

                startForeground(NOTIFICATION_ID, notification);

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(PhotoUploadService.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            Toast.makeText(PhotoUploadService.this, getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }
    }


}
