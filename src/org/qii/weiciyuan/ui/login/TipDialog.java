package org.qii.weiciyuan.ui.login;

import org.qii.weiciyuan.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * User: qii
 * Date: 12-12-9
 */
@Deprecated
public class TipDialog extends DialogFragment {

    public TipDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.tip_dialog_title))
                .setMessage(getString(R.string.tip_dialog_content))
                .setPositiveButton(getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismissAllowingStateLoss();
                            }
                        });

        return builder.create();
    }
}

