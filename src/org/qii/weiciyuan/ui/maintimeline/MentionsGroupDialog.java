package org.qii.weiciyuan.ui.maintimeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-9-12
 */
public class MentionsGroupDialog extends DialogFragment {

    String[] group;
    int selected;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("group", group);
        outState.putInt("selected", selected);
    }

    public MentionsGroupDialog() {

    }


    public MentionsGroupDialog(String[] group, int selected) {
        this.group = group;
        this.selected = selected;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = savedInstanceState.getStringArray("group");
            selected = savedInstanceState.getInt("selected");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_group));
        builder.setItems(this.group, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MentionsTimeLineFragment fragment = (MentionsTimeLineFragment) getTargetFragment();

                switch (which) {
                    case 0:
                        fragment.setFilter_by_author("0");
                        fragment.setFilter_by_type("0");

                        break;
                    case 1:
                        fragment.setFilter_by_author("1");
                        fragment.setFilter_by_type("0");

                        break;
                    case 2:
                        fragment.setFilter_by_author("0");
                        fragment.setFilter_by_type("1");

                        break;
                }
                if (selected != which) {
                    selected = which;
                    fragment.setCurrentGroupId(which);
                    fragment.switchGroup();
                }
                dismiss();
            }

        });


        return builder.create();
    }
}