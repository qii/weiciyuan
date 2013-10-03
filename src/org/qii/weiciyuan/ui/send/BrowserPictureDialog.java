package org.qii.weiciyuan.ui.send;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 12-12-20
 */
public class BrowserPictureDialog extends DialogFragment {

    private String path;

    public BrowserPictureDialog() {

    }

    public BrowserPictureDialog(String path) {
        this.path = path;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("path", path);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            this.path = savedInstanceState.getString("path");
        }

        Bitmap bitmap = ImageUtility.decodeBitmapFromSDCard(path, Utility.dip2px(250), Utility.dip2px(250));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View customView = getActivity().getLayoutInflater().inflate(R.layout.browserpicturedialog_layout, null);
        ((ImageView) customView.findViewById(R.id.imageview)).setImageBitmap(bitmap);
        builder.setTitle(getString(R.string.browser_part_picture))
                .setView(customView)
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((WriteWeiboActivity) getActivity()).deletePicture();
                    }
                });

        return builder.create();
    }
}
