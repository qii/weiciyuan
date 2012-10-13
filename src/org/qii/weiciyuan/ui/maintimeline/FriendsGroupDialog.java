package org.qii.weiciyuan.ui.maintimeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.qii.weiciyuan.R;

import java.util.HashMap;
import java.util.Set;

/**
 * User: qii
 * Date: 12-10-6
 */
public class FriendsGroupDialog extends DialogFragment {

    private HashMap<Integer, String> group;
    private int selected;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("group", group);
        outState.putSerializable("selected", selected);
    }

    public FriendsGroupDialog() {

    }


    public FriendsGroupDialog(HashMap<Integer, String> group, int selected) {
        this.group = group;
        this.selected = selected;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = (HashMap<Integer, String>) savedInstanceState.getSerializable("group");
            selected = savedInstanceState.getInt("selected");
        }

        Set<Integer> keys = group.keySet();
        final Integer[] keyArray = keys.toArray(new Integer[keys.size()]);

        int position = 0;

        for (int i = 0; i < keyArray.length; i++) {
            if (keyArray[i] == selected)
                position = i;
        }

        String[] valueArray = new String[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            valueArray[i] = group.get(keyArray[i]);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_group));
        builder.setSingleChoiceItems(valueArray, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FriendsTimeLineFragment fragment = (FriendsTimeLineFragment) getTargetFragment();
                int selectedItemId = keyArray[which].intValue();
                if (selected != selectedItemId) {
                    fragment.setSelected(selectedItemId);
                    fragment.switchGroup();
                }
                dismiss();
            }

        });


        return builder.create();
    }
}