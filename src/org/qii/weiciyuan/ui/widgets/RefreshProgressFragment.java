package org.qii.weiciyuan.ui.widgets;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * User: Jiang Qi
 * Date: 12-7-30
 */
public class RefreshProgressFragment extends DialogFragment {

    public static RefreshProgressFragment newInstance() {
        RefreshProgressFragment frag = new RefreshProgressFragment();
        frag.setRetainInstance(true); //注意这句
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("等待");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        return dialog;
    }


}
