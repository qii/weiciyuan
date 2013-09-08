package org.qii.weiciyuan.ui.maintimeline;

import android.support.v4.app.DialogFragment;

/**
 * User: qii
 * Date: 12-10-6
 */
public class FriendsGroupDialog extends DialogFragment {

//    private GroupListBean group;
//    private String selected;
//    private List<GroupBean> list;
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putSerializable("group", group);
//        outState.putSerializable("selected", selected);
//    }
//
//    public FriendsGroupDialog() {
//
//    }
//
//
//    public FriendsGroupDialog(GroupListBean group, String selected) {
//        this.group = group;
//        this.selected = selected;
//    }
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        if (savedInstanceState != null) {
//            group = (GroupListBean) savedInstanceState.getSerializable("group");
//            selected = savedInstanceState.getString("selected");
//        }
//        if (group != null) {
//            list = group.getLists();
//        } else {
//            list = new ArrayList<GroupBean>();
//        }
//        List<String> name = new ArrayList<String>();
//        name.add(getString(R.string.all_people));
//        name.add(getString(R.string.bilateral));
//
//        for (GroupBean b : list) {
//            name.add(b.getName());
//        }
//
//        String[] valueArray = name.toArray(new String[0]);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(getString(R.string.select_group));
//        builder.setItems(valueArray, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                FriendsTimeLineFragment fragment = (FriendsTimeLineFragment) getTargetFragment();
//
//                String selectedItemId;
//
//                if (which == 0) {
//                    selectedItemId = "0";
//                } else if (which == 1) {
//                    selectedItemId = "1";
//                } else {
//                    selectedItemId = list.get(which - 2).getIdstr();
//                }
//
//                if (!selected.equals(selectedItemId)) {
//                    fragment.switchGroup(selectedItemId);
//                }
//                dismiss();
//            }
//
//        });
//
//
//        return builder.create();
//    }
}