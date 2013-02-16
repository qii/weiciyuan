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
public class ModifyGroupDialog extends DialogFragment {

    private String idstr;
    private String oriName;

    public ModifyGroupDialog() {

    }

    public ModifyGroupDialog(String oriName, String idstr) {
        this.idstr = idstr;
        this.oriName = oriName;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        et.setHint(oriName);
        et.addTextChangedListener(new WordLengthLimitWatcher(et));
        builder.setView(et)
                .setTitle(getString(R.string.modify_group_name))
                .setPositiveButton(getString(R.string.modify), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = et.getText().toString().trim();
                        if (!TextUtils.isEmpty(name)) {
                            ManageGroupFragment fragment = (ManageGroupFragment) getTargetFragment();
                            fragment.modifyGroupName(idstr, name);
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

