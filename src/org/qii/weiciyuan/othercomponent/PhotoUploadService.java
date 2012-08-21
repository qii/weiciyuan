package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;

/**
 * User: qii
 * Date: 12-8-21
 */
public class PhotoUploadService extends Service {
    private String token;
    private String picPath;
    private String content;

    private Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        token = intent.getStringExtra("token");
        picPath = intent.getStringExtra("picPath");
        content = intent.getStringExtra("content");

        Notification.Builder builder = new Notification.Builder(PhotoUploadService.this)
                .setTicker(getString(R.string.send_photo))
                .setContentTitle(getString(R.string.background_sending))
                .setProgress(100, 100, true)
                .setSmallIcon(R.drawable.app);
        notification = builder.getNotification();

        new UploadTask().execute();

        return super.onStartCommand(intent, flags, startId);

    }

    class UploadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(PhotoUploadService.this, getString(R.string.background_sending), Toast.LENGTH_SHORT).show();
            startForeground(1, notification);
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean result = new StatusNewMsgDao(token).setPic(picPath).sendNewMsg(content);
            if (!result)
                cancel(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(PhotoUploadService.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
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
