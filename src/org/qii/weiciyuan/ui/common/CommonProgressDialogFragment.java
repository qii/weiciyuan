package org.qii.weiciyuan.ui.common;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * User: qii
 * Date: 13-12-4
 */
public class CommonProgressDialogFragment extends DialogFragment {


    public CommonProgressDialogFragment() {
    }

    public static CommonProgressDialogFragment newInstance(String content) {
        CommonProgressDialogFragment fragment = new CommonProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getArguments().getString("content"));
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }
}
