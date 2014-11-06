package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import android.app.Activity;
import android.widget.Toast;

/**
 * User: qii
 * Date: 14-3-21
 */
public class PicSaveTask extends MyAsyncTask<Void, Boolean, Boolean> {

    private String path;
    private Activity activity;

    public PicSaveTask(Activity activity, String path) {
        this.path = path;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return FileManager.saveToPicDir(path);
    }

    @Override
    protected void onPostExecute(Boolean value) {
        super.onPostExecute(value);
        if (value) {
            Toast.makeText(activity, activity.getString(R.string.save_to_album_successfully),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, activity.getString(R.string.cant_save_pic),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
