package org.qii.weiciyuan.ui.Abstract;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.AvatarBitmapDrawable;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.PictureBitmapDrawable;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class AbstractAppActivity extends Activity {

    private int theme = 0;

    private Drawable defaultAvatar = null;
    private Drawable defaultPic = null;
    private Drawable errorPic = null;
    private Drawable transPic = new ColorDrawable(Color.TRANSPARENT);


    private Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();
    private Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();

    protected String getMemCacheKey(String urlKey, int position) {
        return urlKey + position;
    }

    protected ICommander commander = new PicCommander();

    private class PicCommander implements ICommander {
        @Override
        public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView, boolean isFling) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                cancelPotentialAvatarDownload(urlKey, view);
                avatarBitmapWorkerTaskHashMap.remove(getMemCacheKey(urlKey, position));
            } else {
                view.setImageDrawable(getResources().getDrawable(R.color.transparent));
                if (cancelPotentialAvatarDownload(urlKey, view) && !isFling) {
                    AvatarBitmapWorkerTask task = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), avatarBitmapWorkerTaskHashMap, view, urlKey, position);
                    AvatarBitmapDrawable downloadedDrawable = new AvatarBitmapDrawable(task);
                    view.setImageDrawable(downloadedDrawable);
                    task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    avatarBitmapWorkerTaskHashMap.put(getMemCacheKey(urlKey, position), task);
                }
            }

        }

        @Override
        public void downContentPic(ImageView view, String urlKey, int position, ListView listView, FileLocationMethod method, boolean isFling) {
            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                switch (method) {
                    case picture_thumbnail:

                        view.setImageBitmap(bitmap);
                        view.setBackgroundColor(Color.TRANSPARENT);
                        cancelPotentialDownload(urlKey, view);
                        pictureBitmapWorkerTaskMap.remove(urlKey);

                        break;
                    case picture_bmiddle:

                        view.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
                        cancelPotentialDownload(urlKey, view);
                        pictureBitmapWorkerTaskMap.remove(urlKey);
                        break;
                }

            } else {

                switch (method) {
                    case picture_thumbnail:

                        view.setImageDrawable(transPic);
                        view.setBackgroundDrawable(transPic);

                        break;
                    case picture_bmiddle:
                        view.setBackgroundDrawable(transPic);
                        break;

                }
                if (cancelPotentialDownload(urlKey, view) && !isFling) {

                    PictureBitmapWorkerTask task = new PictureBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), pictureBitmapWorkerTaskMap, view, urlKey, position, AbstractAppActivity.this, method);
                    PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(task);
                    view.setImageDrawable(downloadedDrawable);
                    task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    pictureBitmapWorkerTaskMap.put(urlKey, task);
                }
            }

        }

    }


    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        PictureBitmapWorkerTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else if (bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.PENDING || bitmapDownloaderTask.getStatus() == MyAsyncTask.Status.RUNNING) {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }


    private static boolean cancelPotentialAvatarDownload(String url, ImageView imageView) {
        AvatarBitmapWorkerTask bitmapDownloaderTask = getAvatarBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    private static AvatarBitmapWorkerTask getAvatarBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AvatarBitmapDrawable) {
                AvatarBitmapDrawable downloadedDrawable = (AvatarBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    private static PictureBitmapWorkerTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.getInstance().setActivity(this);
        if (getResources().getBoolean(R.bool.is_phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (theme == GlobalContext.getInstance().getAppTheme()) {

        } else {
            reload();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            theme = GlobalContext.getInstance().getAppTheme();
        } else {
            theme = savedInstanceState.getInt("theme");
        }
        setTheme(theme);
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.is_phone)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        forceShowActionBarOverflowMenu();
        initDefaultAvatar();
        initDefaultPic();
        initErrorPic();
        initNFC();
    }


    private void initDefaultAvatar() {
        int[] attrs = new int[]{R.attr.account};
        TypedArray ta = obtainStyledAttributes(attrs);
        defaultAvatar = ta.getDrawable(0);
    }

    private void initDefaultPic() {
        int[] attrs = new int[]{R.attr.picture};
        TypedArray ta = obtainStyledAttributes(attrs);
        defaultPic = ta.getDrawable(0);
    }

    private void initErrorPic() {
        int[] attrs = new int[]{R.attr.error};
        TypedArray ta = obtainStyledAttributes(attrs);
        errorPic = ta.getDrawable(0);
    }

    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        totalStopLoadPicture();

        defaultAvatar = null;
        defaultPic = null;
    }

    protected void totalStopLoadPicture() {
        if (avatarBitmapWorkerTaskHashMap != null) {
            for (String task : avatarBitmapWorkerTaskHashMap.keySet()) {
                avatarBitmapWorkerTaskHashMap.get(task).cancel(true);
            }
            avatarBitmapWorkerTaskHashMap = null;
        }
        if (pictureBitmapWorkerTaskMap != null) {
            for (String task : pictureBitmapWorkerTaskMap.keySet()) {
                pictureBitmapWorkerTaskMap.get(task).cancel(true);
            }
            pictureBitmapWorkerTaskMap = null;
        }
    }

    private void initNFC() {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            return;
        }

        mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                String text = (GlobalContext.getInstance().getCurrentAccountName());

                NdefMessage msg = new NdefMessage(
                        new NdefRecord[]{createMimeRecord(
                                "application/org.qii.weiciyuan.beam", text.getBytes()), NdefRecord.createApplicationRecord(getPackageName())
                        });
                return msg;
            }
        }, this);

    }

    private NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    private void reload() {

        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    protected Bitmap getBitmapFromMemCache(String key) {
        return GlobalContext.getInstance().getAvatarCache().get(key);
    }

    public Map<String, PictureBitmapWorkerTask> getPictureBitmapWorkerTaskMap() {
        return pictureBitmapWorkerTaskMap;
    }

    public Map<String, AvatarBitmapWorkerTask> getAvatarBitmapWorkerTaskHashMap() {
        return avatarBitmapWorkerTaskHashMap;
    }

    public ICommander getCommander() {
        return commander;
    }

    protected void dealWithException(WeiboException e) {
        Toast.makeText(this, e.getError(), Toast.LENGTH_SHORT).show();
    }
}
