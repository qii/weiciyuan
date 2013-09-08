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
    private EditText name;

    public AddGroupDialog() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", name.getText().toString());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        name = new EditText(getActivity());
        name.addTextChangedListener(new WordLengthLimitWatcher(name));
        if (savedInstanceState != null) {
            name.append(savedInstanceState.getString("name"));
        }
        builder.setView(name)
                .setTitle(getString(R.string.input_group_name))
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = name.getText().toString().trim();
                        if (!TextUtils.isEmpty(word)) {
                            ManageGroupActivity.ManageGroupFragment fragment = (ManageGroupActivity.ManageGroupFragment) getTargetFragment();
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
