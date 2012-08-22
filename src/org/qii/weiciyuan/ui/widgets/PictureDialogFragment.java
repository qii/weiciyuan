package org.qii.weiciyuan.ui.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.imagetool.ImageTool;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-15
 * Time: 下午9:29
 */
public class PictureDialogFragment extends DialogFragment {
    private MessageBean msg;
    private ImageView imageView;
    private ProgressBar pb;
    private FrameLayout fl;
    private PicSimpleBitmapWorkerTask avatarTask;

    public PictureDialogFragment(MessageBean msg) {
        this.msg = msg;
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


        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        avatarTask = new PicSimpleBitmapWorkerTask();
        avatarTask.execute(msg.getBmiddle_pic());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    class PicSimpleBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private LruCache<String, Bitmap> lruCache;
        private String data = "";


        public PicSimpleBitmapWorkerTask() {

            this.lruCache = GlobalContext.getInstance().getAvatarCache();

        }

        @Override
        protected Bitmap doInBackground(String... url) {
            data = url[0];
            if (!isCancelled()) {
                return ImageTool.getAvatarBitmap(data);
            }

            return null;
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


            }

        }


    }
}
