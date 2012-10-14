package org.qii.weiciyuan.ui.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileDownloaderHttpHelper;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserBigPicActivity;

import java.io.File;

/**
 * User: qii
 * Date: 12-8-15
 */
public class PictureDialogFragment extends DialogFragment {
    private String url;
    private WebView imageView;
    private ProgressBar pb;
    private FrameLayout fl;
    private PicSimpleBitmapWorkerTask picTask;
    private String bigUrl;

    public PictureDialogFragment() {

    }

    public PictureDialogFragment(String url, String bigUrl) {
        this.url = url;
        this.bigUrl = bigUrl;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_listview_pic_preview_layout, null);
        imageView = (WebView) view.findViewById(R.id.iv);
        imageView.getSettings().setSupportZoom(true);
        imageView.getSettings().setBuiltInZoomControls(true);
        imageView.getSettings().setDisplayZoomControls(false);
        imageView.setBackgroundColor(getResources().getColor(R.color.transparent));

        imageView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        pb = (ProgressBar) view.findViewById(R.id.pb);
        fl = (FrameLayout) view.findViewById(R.id.fl);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ori_picture), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), BrowserBigPicActivity.class);
                intent.putExtra("url", bigUrl);
                startActivity(intent);
            }
        });
        builder.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                picTask.cancel(true);
                dismissAllowingStateLoss();
                //when app close download pic, the timeline picture is empty,so refresh it
                if (!GlobalContext.getInstance().isEnablePic() && GlobalContext.getInstance().getEnableBigPic()) {
                    AbstractTimeLineFragment fragment = (AbstractTimeLineFragment) getTargetFragment();
                    fragment.getAdapter().notifyDataSetChanged();
                }
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
        picTask = new PicSimpleBitmapWorkerTask();
        picTask.execute(url);
    }


    class PicSimpleBitmapWorkerTask extends AsyncTask<String, Integer, String> {

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
        protected String doInBackground(String... url) {

            FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
                @Override
                public void pushProgress(int progress, int max) {
                    publishProgress(progress, max);
                }
            };

            data = url[0];
            if (!isCancelled()) {
                return ImageTool.getMiddlePictureWithoutRoundedCorner(data, downloadListener);
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
        protected void onCancelled(String bitmap) {

            super.onCancelled(bitmap);
        }

        @Override
        protected void onPostExecute(final String bitmap) {

            if (getActivity() == null) {
                return;
            }

            if (!TextUtils.isEmpty(bitmap)) {
                File file = new File(bitmap);
                imageView.loadDataWithBaseURL("file://" + file.getParent() + "/", "<html style=\"BACKGROUND-COLOR: transparent\"><center><img src=\"" + file.getName() + "\"></BODY></html>", "text/html", "utf-8", "");


            } else {
                pb.setVisibility(View.INVISIBLE);
                int[] attrs = new int[]{R.attr.error};
                TypedArray ta = getActivity().obtainStyledAttributes(attrs);
                Drawable drawableFromTheme = ta.getDrawable(0);
                imageView.setBackgroundDrawable(drawableFromTheme);
            }

        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        imageView.loadUrl("about:blank");
        imageView.stopLoading();
        imageView = null;
        if (picTask != null)
            picTask.cancel(true);
    }
}
