package org.qii.weiciyuan.ui.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * User: qii
 * Date: 12-8-5
 */
public class ProgressFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("刷新中");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);

        return dialog;
    }
}
