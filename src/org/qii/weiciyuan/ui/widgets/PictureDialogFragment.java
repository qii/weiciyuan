package org.qii.weiciyuan.ui.widgets;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ImageView;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;

/**
 * User: qii
 * Date: 12-8-15
 * Time: 下午9:29
 */
public class PictureDialogFragment extends DialogFragment {
    private WeiboMsgBean msg;
    private ImageView view;
    private SimpleBitmapWorkerTask avatarTask;

    public PictureDialogFragment(WeiboMsgBean msg) {
        this.msg = msg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = new ImageView(getActivity());
        builder.setView(view);
        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {

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
        avatarTask = new SimpleBitmapWorkerTask(view);
        avatarTask.execute(msg.getBmiddle_pic());
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
