package org.qii.weiciyuan.ui.common;

import org.qii.weiciyuan.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * User: qii
 */
public class QuickSendProgressFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.sending));
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        return dialog;
    }
}
