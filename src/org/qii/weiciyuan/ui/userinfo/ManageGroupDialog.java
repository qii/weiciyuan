package org.qii.weiciyuan.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GroupBean;
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.dao.group.GroupListDao;
import org.qii.weiciyuan.dao.group.ModifyGroupMemberDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.friendgroup.ManageGroupActivity;

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
        outState.putParcelable("group", group);
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

    /**
     * alertdialog use string arry to store data, so you cant add or delete data
     */
    @Override
    public void onResume() {
        super.onResume();
        GroupListBean current = GlobalContext.getInstance().getGroup();
        if (current != this.group) {
            dismissAllowingStateLoss();
            ManageGroupDialog dialog = new ManageGroupDialog(current, uid);
            dialog.show(getFragmentManager(), "");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            group = (GroupListBean) savedInstanceState.getParcelable("group");
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

        View customTitle = getActivity().getLayoutInflater().inflate(R.layout.managegroupdialog_title_layout, null);

        ImageView setting = (ImageView) customTitle.findViewById(R.id.title_button);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ManageGroupActivity.class));
            }
        });

        builder.setMultiChoiceItems(valueArray, selectedArray, new DialogMultiChoiceClickListener())
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ModifyGroupMemberTask modifyGroupMemberTask = new ModifyGroupMemberTask(addList, removeList);
                        modifyGroupMemberTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setCustomTitle(customTitle);

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


    private class ModifyGroupMemberTask extends MyAsyncTask<Void, Void, Void> {
        List<String> add;
        List<String> remove;

        public ModifyGroupMemberTask(List<String> add, List<String> remove) {
            this.add = add;
            this.remove = remove;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ModifyGroupMemberDao dao = new ModifyGroupMemberDao(GlobalContext.getInstance().getSpecialToken(), uid);
            for (String id : add) {
                try {
                    dao.add(id);
                } catch (WeiboException e) {
                    AppLogger.e(e.getMessage());
                    cancel(true);
                }
            }
            for (String id : remove) {
                try {
                    dao.delete(id);
                } catch (WeiboException e) {
                    AppLogger.e(e.getMessage());
                    cancel(true);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(GlobalContext.getInstance(), GlobalContext.getInstance().getString(R.string.modify_successfully), Toast.LENGTH_SHORT).show();
        }
    }
}