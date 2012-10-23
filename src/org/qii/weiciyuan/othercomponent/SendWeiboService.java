package org.qii.weiciyuan.othercomponent;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.StatusDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.file.FileUploaderHttpHelper;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.ui.preference.SettingActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private UploadTask task;

    private StatusDraftBean statusDraftBean;


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
            Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
                    .setTicker(getString(R.string.sending))
                    .setContentTitle(getString(R.string.sending))
                    .setContentText(content)
                    .setSmallIcon(R.drawable.upload_white);

            if (!TextUtils.isEmpty(picPath)) {
                builder.setProgress(0, 100, false);
            } else {
                builder.setProgress(0, 100, true);
            }

            notification = builder.getNotification();
            startForeground(NOTIFICATION_ID, notification);
            Toast.makeText(SendWeiboService.this, getString(R.string.background_sending), Toast.LENGTH_SHORT).show();
        }

        private boolean sendPic(String uploadPicPath) throws WeiboException {
            return new StatusNewMsgDao(token).setPic(uploadPicPath).setGeoBean(geoBean).sendNewMsg(content, new FileUploaderHttpHelper.ProgressListener() {
                @Override
                public void transferred(long data) {
                    publishProgress(data);
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
                    String uploadPicPath = compressPic();
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

        private String compressPic() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String value = sharedPref.getString(SettingActivity.UPLOAD_PIC_QUALITY, "4");
            if (value.equals("1")) {
                return picPath;
            } else {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 1;

                if (value.equals("2")) {
                    options.inSampleSize = 2;
                } else if (value.equals("3")) {
                    options.inSampleSize = 4;
                } else if (value.equals("4")) {
                    options.inSampleSize = 4;
                    ConnectivityManager cm = (ConnectivityManager)
                            getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return picPath;
                    }

                }
                Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
                FileOutputStream stream = null;
                String tmp = FileManager.getUploadPicTempFile();
                try {
                    new File(tmp).getParentFile().mkdirs();
                    new File(tmp).createNewFile();
                    stream = new FileOutputStream(new File(tmp));
                } catch (IOException ignored) {

                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                if (stream != null) {
                    try {
                        stream.close();
                        bitmap.recycle();
                    } catch (IOException ignored) {

                    }
                }
                return tmp;
            }

        }


        @Override
        protected void onProgressUpdate(Long... values) {

            if (values.length > 0) {
                long data = values[0];

                Notification.Builder builder = new Notification.Builder(SendWeiboService.this)
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
            if (statusDraftBean != null)
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
            Toast.makeText(SendWeiboService.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
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
            stopForeground(true);
            stopSelf();
        }
    }


}
