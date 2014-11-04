package org.qii.weiciyuan.ui.common;

import org.qii.weiciyuan.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * User: qii
 * Date: 13-12-4
 */
public class CommonErrorDialogFragment extends DialogFragment {

    public CommonErrorDialogFragment() {

    }

    public static CommonErrorDialogFragment newInstance(String error) {
        CommonErrorDialogFragment fragment = new CommonErrorDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("error", error);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.something_wrong))
                .setMessage(getArguments().getString("error"))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }
}
