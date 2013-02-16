package org.qii.weiciyuan.ui.friendgroup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 13-2-15
 */
public class AddGroupDialog extends DialogFragment {


    public AddGroupDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        builder.setView(et)
                .setTitle(getString(R.string.input_group_name))
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(word)) {
                            ManageGroupFragment fragment = (ManageGroupFragment) getTargetFragment();
                            fragment.addGroup(word);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }
}
