package org.qii.weiciyuan.ui.friendgroup;

import android.app.ListFragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GroupBean;
import org.qii.weiciyuan.bean.GroupListBean;
import org.qii.weiciyuan.dao.group.CreateGroupDao;
import org.qii.weiciyuan.dao.group.DestroyGroupDao;
import org.qii.weiciyuan.dao.group.UpdateGroupNameDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupDao;
import org.qii.weiciyuan.support.database.GroupDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-2-14
 */
public class ManageGroupActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(getString(R.string.friend_group));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ManageGroupFragment())
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }


    public static class ManageGroupFragment extends ListFragment {

        private GroupAdapter adapter;
        private GroupListBean group;
        private List<String> name;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            setRetainInstance(true);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            name = new ArrayList<String>();
            adapter = new GroupAdapter();
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            getListView().setMultiChoiceModeListener(new GroupMultiChoiceModeListener());
            setListAdapter(adapter);
            group = GlobalContext.getInstance().getGroup();
            if (group != null) {
                final List<GroupBean> list = group.getLists();

                for (int i = 0; i < list.size(); i++) {
                    name.add(list.get(i).getName());
                }
                adapter.notifyDataSetChanged();
            }
        }

        private void refreshListData() {
            if (group != null) {
                name.clear();
                final List<GroupBean> list = group.getLists();

                for (int i = 0; i < list.size(); i++) {
                    name.add(list.get(i).getName());
                }
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.actionbar_menu_managegroupfragment, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_add:
                    AddGroupDialog dialog = new AddGroupDialog();
                    dialog.setTargetFragment(ManageGroupFragment.this, 0);
                    dialog.show(getFragmentManager(), "");
                    break;

            }

            return true;
        }

        public void addGroup(String groupName) {
            new CreateGroupTask(GlobalContext.getInstance().getSpecialToken(), groupName)
                    .executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void modifyGroupName(String idstr, String groupName) {
            new ModifyGroupNameTask(GlobalContext.getInstance().getSpecialToken(), idstr, groupName)
                    .executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void removeGroup(List<String> groupNames) {
            new RemoveGroupTask(GlobalContext.getInstance().getSpecialToken(), groupNames)
                    .executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }

        class GroupAdapter extends BaseAdapter {

            int checkedBG;
            int defaultBG;

            public GroupAdapter() {
                defaultBG = getResources().getColor(R.color.transparent);

                int[] attrs = new int[]{R.attr.listview_checked_color};
                TypedArray ta = getActivity().obtainStyledAttributes(attrs);
                checkedBG = ta.getColor(0, 430);


            }

            @Override
            public int getCount() {
                return name.size();
            }

            @Override
            public String getItem(int position) {
                return name.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = getActivity().getLayoutInflater().inflate(R.layout.managegroupactivity_list_item_layout, parent, false);
                TextView tv = (TextView) view;
                tv.setBackgroundColor(defaultBG);
                if (getListView().getCheckedItemPositions().get(position)) {
                    tv.setBackgroundColor(checkedBG);
                }
                tv.setText(name.get(position));
                return view;
            }
        }


        class GroupMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
            MenuItem modify;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_menu_managegroupfragment, menu);
                modify = menu.findItem(R.id.menu_modify_group_name);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray positions = null;
                ArrayList<String> checkedIdstrs = null;
                switch (item.getItemId()) {
                    case R.id.menu_modify_group_name:
                        positions = getListView().getCheckedItemPositions();
                        checkedIdstrs = new ArrayList<String>();
                        String oriName = null;
                        for (int i = 0; i < positions.size(); i++) {
                            if (positions.get(positions.keyAt(i))) {
                                oriName = group.getLists().get(positions.keyAt(i)).getName();
                                checkedIdstrs.add(group.getLists().get(positions.keyAt(i)).getIdstr());
                            }
                        }
                        ModifyGroupDialog modifyGroupDialog = new ModifyGroupDialog(oriName, checkedIdstrs.get(0));
                        modifyGroupDialog.setTargetFragment(ManageGroupFragment.this, 0);
                        modifyGroupDialog.show(getFragmentManager(), "");
                        mode.finish();
                        return true;
                    case R.id.menu_remove:
                        positions = getListView().getCheckedItemPositions();
                        checkedIdstrs = new ArrayList<String>();
                        for (int i = 0; i < positions.size(); i++) {
                            if (positions.get(positions.keyAt(i))) {
                                checkedIdstrs.add(group.getLists().get(positions.keyAt(i)).getIdstr());
                            }
                        }
                        RemoveGroupDialog removeGroupDialog = new RemoveGroupDialog(checkedIdstrs);
                        removeGroupDialog.setTargetFragment(ManageGroupFragment.this, 0);
                        removeGroupDialog.show(getFragmentManager(), "");
                        mode.finish();
                        return true;
                }
                return false;
            }


            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (getListView().getCheckedItemCount() > 1) {
                    modify.setVisible(false);
                } else {
                    modify.setVisible(true);
                }
                mode.setTitle(String.format(getString(R.string.have_selected), String.valueOf(getListView().getCheckedItemCount())));
                adapter.notifyDataSetChanged();
            }
        }


        class CreateGroupTask extends MyAsyncTask<Void, Void, GroupBean> {
            String token;
            String name;
            WeiboException e;

            public CreateGroupTask(String token, String name) {
                this.token = token;
                this.name = name;
            }

            @Override
            protected GroupBean doInBackground(Void... params) {
                try {
                    return new CreateGroupDao(token, name).create();
                } catch (WeiboException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GroupBean groupBean) {
                super.onPostExecute(groupBean);
                if (getActivity() == null)
                    return;
                if (Utility.isAllNotNull(groupBean)) {
                    new RefreshGroupTask(token).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }

        class RefreshGroupTask extends MyAsyncTask<Void, GroupListBean, GroupListBean> {


            private WeiboException e;

            private String token;

            public RefreshGroupTask(String token) {
                this.token = token;
            }

            @Override
            protected GroupListBean doInBackground(Void... params) {
                try {
                    return new FriendGroupDao(token).getGroup();
                } catch (WeiboException e) {
                    cancel(true);
                }
                return null;
            }


            @Override
            protected void onPostExecute(GroupListBean groupListBean) {
                super.onPostExecute(groupListBean);
                if (getActivity() == null)
                    return;
                GroupDBTask.update(groupListBean, GlobalContext.getInstance().getCurrentAccountId());
                GlobalContext.getInstance().setGroup(groupListBean);
                group = groupListBean;
                refreshListData();
            }
        }

        class RemoveGroupTask extends MyAsyncTask<Void, Void, Boolean> {
            String token;
            List<String> groupNames;
            WeiboException e;

            public RemoveGroupTask(String token, List<String> groupNames) {
                this.token = token;
                this.groupNames = groupNames;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    boolean result = true;
                    for (String groupName : groupNames) {
                        if (!new DestroyGroupDao(token, groupName).destroy()) {
                            result = false;
                        }
                    }
                    return result;
                } catch (WeiboException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean groupBean) {
                super.onPostExecute(groupBean);
                if (getActivity() == null)
                    return;
                if (Utility.isAllNotNull(groupBean)) {
                    new RefreshGroupTask(token).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }

        class ModifyGroupNameTask extends MyAsyncTask<Void, Void, GroupBean> {
            String token;
            String groupIdstr;
            String name;
            WeiboException e;

            public ModifyGroupNameTask(String token, String groupIdstr, String name) {
                this.token = token;
                this.groupIdstr = groupIdstr;
                this.name = name;
            }

            @Override
            protected GroupBean doInBackground(Void... params) {
                try {

                    return new UpdateGroupNameDao(token, groupIdstr, name).update();

                } catch (WeiboException e) {
                    e.printStackTrace();
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(GroupBean groupBean) {
                super.onPostExecute(groupBean);
                if (getActivity() == null)
                    return;
                if (Utility.isAllNotNull(groupBean)) {
                    new RefreshGroupTask(token).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }
}