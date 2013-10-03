package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-5
 */
public class ProfileAvatarReadWorker extends MyAsyncTask<String, Integer, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;
    private GlobalContext globalContext;


    public ProfileAvatarReadWorker(ImageView view, String url) {
        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.globalContext = GlobalContext.getInstance();
        this.data = url;
    }


    @Override
    protected Bitmap doInBackground(String... url) {
        if (isCancelled())
            return null;

        String path = FileManager.getFilePathFromUrl(data, FileLocationMethod.avatar_large);

        boolean downloaded = TaskCache.waitForPictureDownload(data, null, path, FileLocationMethod.avatar_large);

        int avatarWidth = globalContext.getResources().getDimensionPixelSize(R.dimen.profile_avatar_width);
        int avatarHeight = globalContext.getResources().getDimensionPixelSize(R.dimen.profile_avatar_height);

        return ImageUtility.getRoundedCornerPic(path, avatarWidth, avatarHeight);

    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {
            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);
            lruCache.put(data, bitmap);

        } else {
            view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

    }


}