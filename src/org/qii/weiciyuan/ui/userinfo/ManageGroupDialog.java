package org.qii.weiciyuan.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GroupBean;
import org.qii.weiciyuan.bean.GroupListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-5
 */
public class ManageGroupDialog extends DialogFragment {

    private GroupListBean group;
    private ArrayList<String> selected;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("group", group);
        outState.putSerializable("selected", selected);
    }

    public ManageGroupDialog() {

    }


    public ManageGroupDialog(GroupListBean group, ArrayList<String> selected) {
        this.group = group;
        this.selected = selected;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = (GroupListBean) savedInstanceState.getSerializable("group");
            selected = savedInstanceState.getStringArrayList("selected");
        }

        final List<GroupBean> list = group.getLists();
        boolean[] selectedArray = new boolean[list.size()];

        List<String> name = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            name.add(list.get(i).getName());
            if (selected.contains(list.get(i).getIdstr())) {
                selectedArray[i] = true;
            }
        }

        String[] valueArray = name.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.manage_group));

        builder.setMultiChoiceItems(valueArray, selectedArray, new DialogMultiChoiceClickListener())
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        return builder.create();
    }

    class DialogMultiChoiceClickListener implements DialogInterface.OnMultiChoiceClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {

        }
    }
}