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

    private EditText name;
    private String idstr;
    private String oriName;

    public ModifyGroupDialog() {

    }

    public ModifyGroupDialog(String oriName, String idstr) {
        this.idstr = idstr;
        this.oriName = oriName;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("idstr", idstr);
        outState.putString("oriName", oriName);
        outState.putString("name", name.getText().toString());
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            idstr = savedInstanceState.getString("idstr");
            oriName = savedInstanceState.getString("oriName");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        name = new EditText(getActivity());
        name.setHint(oriName);
        name.addTextChangedListener(new WordLengthLimitWatcher(name));
        if (savedInstanceState != null) {
            name.append(savedInstanceState.getString("name"));
        }
        builder.setView(name)
                .setTitle(getString(R.string.modify_group_name))
                .setPositiveButton(getString(R.string.modify), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = ModifyGroupDialog.this.name.getText().toString().trim();
                        if (!TextUtils.isEmpty(name)) {
                            ManageGroupActivity.ManageGroupFragment fragment = (ManageGroupActivity.ManageGroupFragment) getTargetFragment();
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

