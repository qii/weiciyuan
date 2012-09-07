package org.qii.weiciyuan.ui.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.LruCache;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-15
 */
public class PictureDialogFragment extends DialogFragment {
    private String url;
    private ImageView imageView;
    private ProgressBar pb;
    private FrameLayout fl;
    private PicSimpleBitmapWorkerTask avatarTask;

    public PictureDialogFragment() {

    }

    public PictureDialogFragment(String url) {
        this.url = url;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_listview_pic_preview_layout, null);
        imageView = (ImageView) view.findViewById(R.id.iv);
        pb = (ProgressBar) view.findViewById(R.id.pb);
        fl = (FrameLayout) view.findViewById(R.id.fl);
        builder.setView(view);
        builder.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                avatarTask.cancel(true);
                dismissAllowingStateLoss();
            }
        });

        if (savedInstanceState != null)
            url = savedInstanceState.getString("url");

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }

    @Override
    public void onStart() {
        super.onStart();
        avatarTask = new PicSimpleBitmapWorkerTask();
        avatarTask.execute(url);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    class PicSimpleBitmapWorkerTask extends AsyncTask<String, Integer, Bitmap> {

        private LruCache<String, Bitmap> lruCache;
        private String data = "";


        public PicSimpleBitmapWorkerTask() {

            this.lruCache = GlobalContext.getInstance().getAvatarCache();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setIndeterminate(true);
        }

        @Override
        protected Bitmap doInBackground(String... url) {

            FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
                @Override
                public void pushProgress(int progress, int max) {
                    publishProgress(progress, max);
                }
            };

            data = url[0];
            if (!isCancelled()) {
                return ImageTool.getNormalBitmap(data, downloadListener);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            int max = values[1];
            pb.setIndeterminate(false);
            pb.setMax(max);
            pb.setProgress(progress);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {

                lruCache.put(data, bitmap);

            }

            super.onCancelled(bitmap);
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {

            if (bitmap != null) {

                lruCache.put(data, bitmap);
                fl.setLayoutParams(new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
                pb.setVisibility(View.INVISIBLE);
                imageView.setImageBitmap(bitmap);


            } else {
                pb.setVisibility(View.INVISIBLE);
                int[] attrs = new int[]{R.attr.error};
                TypedArray ta = getActivity().obtainStyledAttributes(attrs);
                Drawable drawableFromTheme = ta.getDrawable(0);
                imageView.setImageDrawable(drawableFromTheme);
            }

        }


    }
}
