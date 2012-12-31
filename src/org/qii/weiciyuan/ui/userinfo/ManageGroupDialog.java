package org.qii.weiciyuan.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GroupBean;
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.dao.group.GroupListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-5
 */
public class ManageGroupDialog extends DialogFragment {

    private GroupListBean group;
    private String uid;

    private String[] valueArray;
    private boolean[] selectedArray;

    private MyAsyncTask<Void, Void, List<String>> task;

    private ArrayList<String> currentList = new ArrayList<String>();
    private ArrayList<String> addList = new ArrayList<String>();
    private ArrayList<String> removeList = new ArrayList<String>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("group", group);
        outState.putString("uid", uid);
        outState.putStringArray("valueArray", valueArray);
        outState.putBooleanArray("selectedArray", selectedArray);
        outState.putStringArrayList("currentList", currentList);
        outState.putStringArrayList("addList", addList);
        outState.putStringArrayList("removeList", removeList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (task != null)
            task.cancel(true);
    }

    public ManageGroupDialog() {

    }


    public ManageGroupDialog(GroupListBean group, String uid) {
        this.group = group;
        this.uid = uid;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = (GroupListBean) savedInstanceState.getSerializable("group");
            uid = savedInstanceState.getString("uid");
            valueArray = savedInstanceState.getStringArray("valueArray");
            selectedArray = savedInstanceState.getBooleanArray("selectedArray");
            currentList = savedInstanceState.getStringArrayList("currentList");
            addList = savedInstanceState.getStringArrayList("addList");
            removeList = savedInstanceState.getStringArrayList("removeList");
        }

        final List<GroupBean> list = group.getLists();
        selectedArray = new boolean[list.size()];

        List<String> name = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            name.add(list.get(i).getName());
        }

        valueArray = name.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.manage_group));

        builder.setMultiChoiceItems(valueArray, selectedArray, new DialogMultiChoiceClickListener())
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserInfoActivity activity = (UserInfoActivity) getActivity();
                        activity.handleGroup(addList, removeList);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        task = new Task();
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        return builder.create();
    }

    class DialogMultiChoiceClickListener implements DialogInterface.OnMultiChoiceClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            String id = group.getLists().get(which).getIdstr();
            if (isChecked) {
                if (!currentList.contains(id))
                    addList.add(id);

            } else {
                if (currentList.contains(id))
                    removeList.add(group.getLists().get(which).getIdstr());
            }
        }

    }

    class Task extends MyAsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            GroupListDao dao = new GroupListDao(GlobalContext.getInstance().getSpecialToken(), uid);
            try {
                return dao.getInfo();
            } catch (WeiboException e) {
                cancel(true);
                AppLogger.e(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            if (strings != null && strings.size() > 0) {
                currentList.clear();
                currentList.addAll(strings);
                int length = valueArray.length;
                for (String id : strings) {
                    for (int i = 0; i < length; i++) {
                        if (group.getLists().get(i).getIdstr().equals(id)) {
                            selectedArray[i] = true;
                            ((AlertDialog) getDialog()).getListView().setItemChecked(i, true);
                        }
                    }
                }
            }
        }
    }
}