package org.qii.weiciyuan.ui.maintimeline;

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
 * Date: 12-10-6
 */
public class FriendsGroupDialog extends DialogFragment {

    private GroupListBean group;
    private String selected;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("group", group);
        outState.putSerializable("selected", selected);
    }

    public FriendsGroupDialog() {

    }


    public FriendsGroupDialog(GroupListBean group, String selected) {
        this.group = group;
        this.selected = selected;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = (GroupListBean) savedInstanceState.getSerializable("group");
            selected = savedInstanceState.getString("selected");
        }

        final List<GroupBean> list = group.getLists();

        List<String> name = new ArrayList<String>();
        name.add(getString(R.string.all_people));
        int position = 0;
        for (GroupBean b : list) {
            name.add(b.getName());
            if (b.getIdstr().equals(selected)) {
                position = list.indexOf(b)+1;
            }
        }

        String[] valueArray = name.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_group));
        builder.setSingleChoiceItems(valueArray, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FriendsTimeLineFragment fragment = (FriendsTimeLineFragment) getTargetFragment();

                String selectedItemId = "0";

                if (which == 0) {

                } else {
                    selectedItemId = list.get(which - 1).getIdstr();
                }

                if (!selected.equals(selectedItemId)) {
                    fragment.setSelected(selectedItemId);
                    fragment.switchGroup();
                }
                dismiss();
            }

        });


        return builder.create();
    }
}