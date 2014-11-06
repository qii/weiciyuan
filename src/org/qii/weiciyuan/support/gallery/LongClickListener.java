package org.qii.weiciyuan.support.gallery;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;

/**
 * User: qii
 * Date: 14-5-1
 */
public class LongClickListener implements View.OnLongClickListener {

    private Activity context;

    private String url;
    private String filePath;

    private PicSaveTask saveTask;

    public LongClickListener(Activity activity, String url, String path) {
        this.context = activity;
        this.url = url;
        this.filePath = path;
    }

    private String getString(int res) {
        return context.getString(res);
    }

    @Override
    public boolean onLongClick(View v) {
        String[] values = {getString(R.string.copy_link_to_clipboard),
                getString(R.string.share), getString(R.string.save_pic_album)};

        new AlertDialog.Builder(context)
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                ClipboardManager cm = (ClipboardManager) context.getSystemService(
                                        Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", url));
                                Toast.makeText(context,
                                        getString(R.string.copy_successfully),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("image/jpeg");
                                if (!TextUtils.isEmpty(filePath)) {
                                    Uri uri = Uri.fromFile(new File(filePath));
                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    if (Utility.isIntentSafe(context,
                                            sharingIntent)) {
                                        context.startActivity(Intent.createChooser(sharingIntent,
                                                getString(R.string.share)));
                                    }
                                }
                                break;
                            case 2:
                                saveBitmapToPictureDir(filePath);
                                break;
                        }
                    }
                }).show();

        return true;
    }

    private void saveBitmapToPictureDir(String filePath) {
        if (Utility.isTaskStopped(saveTask)) {
            saveTask = new PicSaveTask(context, filePath);
            saveTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}

