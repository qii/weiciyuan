package org.qii.weiciyuan.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-5
 */
public class ProfileAvatarAndDetailMsgPicTask extends MyAsyncTask<String, Integer, Bitmap> {

    private LruCache<String, Bitmap> lruCache;
    private String data = "";
    private ImageView view;
    private FileLocationMethod method;

    private ProgressBar pb;

    private boolean pbFlag = false;

    private GlobalContext globalContext;


    public ProfileAvatarAndDetailMsgPicTask(ImageView view, FileLocationMethod method) {

        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.method = method;
        this.globalContext = GlobalContext.getInstance();
    }

    public ProfileAvatarAndDetailMsgPicTask(ImageView view, FileLocationMethod method, ProgressBar pb) {
        this.globalContext = GlobalContext.getInstance();
        this.lruCache = GlobalContext.getInstance().getAvatarCache();
        this.view = view;
        this.method = method;
        this.pb = pb;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pb != null) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        data = url[0];
        if (!isCancelled()) {
            switch (method) {

                case avatar_large:
                    int avatarWidth = globalContext.getResources().getDimensionPixelSize(R.dimen.profile_avatar_width);
                    int avatarHeight = globalContext.getResources().getDimensionPixelSize(R.dimen.profile_avatar_height);
                    try {
                        return ImageTool.getRoundedCornerPic(this.data, avatarWidth, avatarHeight, FileLocationMethod.avatar_large);
                    } catch (WeiboException e) {

                    }

            }
        }

        return null;
    }

    /**
     * sometime picture has been cached in sd card,so only set indeterminate equal false to show progress when downloading
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (pb != null) {
            if (!pbFlag) {
                pb.setIndeterminate(false);
                pbFlag = true;
            }
            Integer progress = values[0];
            Integer max = values[1];
            pb.setMax(max);
            pb.setProgress(progress);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (pb != null)
            pb.setVisibility(View.INVISIBLE);

        super.onCancelled(bitmap);
        clean();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (pb != null)
            pb.setVisibility(View.INVISIBLE);

        if (bitmap != null) {

            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);

            switch (method) {
                case avatar_small:
                    lruCache.put(data, bitmap);
                    break;
                case avatar_large:
                    lruCache.put(data, bitmap);
                    break;
            }

        } else {
            view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        clean();
    }

    private void clean() {

        lruCache = null;
        globalContext = null;
    }
}