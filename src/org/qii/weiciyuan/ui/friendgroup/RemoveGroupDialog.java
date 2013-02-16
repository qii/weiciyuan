package org.qii.weiciyuan.ui.friendgroup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.qii.weiciyuan.R;

import java.util.ArrayList;

/**
 * User: qii
 * Date: 13-2-15
 */
public class RemoveGroupDialog extends DialogFragment {
    private ArrayList<String> checkedNames;

    public RemoveGroupDialog() {

    }

    public RemoveGroupDialog(ArrayList<String> checkedNames) {
        this.checkedNames = checkedNames;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("checkedNames", checkedNames);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            checkedNames = savedInstanceState.getStringArrayList("checkedNames");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.remove_group))
                .setMessage(getString(R.string.remove_group_content))
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ManageGroupActivity.ManageGroupFragment fragment = (ManageGroupActivity.ManageGroupFragment) getTargetFragment();
                        fragment.removeGroup(checkedNames);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        return builder.create();
    }
}
