package org.qii.weiciyuan.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-12-9
 */
public class UserAvatarDialog extends DialogFragment {

    private String path;

    public UserAvatarDialog() {

    }

    public UserAvatarDialog(String path) {
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
            path = savedInstanceState.getString("path");
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.UserAvatarDialog);
        View customView = getActivity().getLayoutInflater().inflate(R.layout.useravatardialog_layout, null);
        ((ImageView) customView.findViewById(R.id.imageview)).setImageBitmap(bitmap);
        builder.setView(customView);
        return builder.create();
    }
}
