package org.qii.weiciyuan.ui.maintimeline;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.dao.maintimeline.BilateralTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.othercomponent.SaveToDBService;
import org.qii.weiciyuan.support.database.FriendsTimeLineDBTask;
import org.qii.weiciyuan.support.database.HomeOtherGroupTimeLineDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-7-29
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;
    private DBCacheTask dbTask;

    private AutoRefreshTask autoRefreshTask = null;
    private ScheduledExecutorService autoRefreshExecutor = null;

    private final String ALL_GROUP_ID = "0";
    private final String BILATERAL_GROUP_ID = "1";

    private String currentGroupId = ALL_GROUP_ID;

    private HashMap<String, MessageListBean> groupDataCache = new HashMap<String, MessageListBean>();

    private MessageListBean bean = new MessageListBean();


    @Override
    public MessageListBean getList() {
        return bean;
    }

    public FriendsTimeLineFragment() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //use Up instead of Back to reach this fragment
        if (data == null)
            return;
        final MessageBean msg = (MessageBean) data.getSerializableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getList().getSize(); i++) {
                if (msg.equals(getList().getItem(i))) {
                    getList().getItem(i).setReposts_count(msg.getReposts_count());
                    getList().getItem(i).setComments_count(msg.getComments_count());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FriendsTimeLineDBTask.updateCount(msg.getId(), GlobalContext.getInstance().getCurrentAccountId()
                                    , msg.getComments_count(), msg.getReposts_count());
                        }
                    }).start();

                    break;
                }
            }
            getAdapter().notifyDataSetChanged();
        }
    }

    public FriendsTimeLineFragment(AccountBean accountBean, UserBean userBean, String token) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onPause() {
        super.onPause();
        removeRefresh();
        Utility.cancelTasks(autoRefreshTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        addRefresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
        outState.putSerializable("bean", getList());
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);

        outState.putSerializable("groupDataCache", groupDataCache);
        outState.putString("currentGroupId", currentGroupId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                if (Utility.isTaskStopped(dbTask) && getList().getSize() == 0) {
                    dbTask = new DBCacheTask();
                    dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    GroupInfoTask groupInfoTask = new GroupInfoTask(GlobalContext.getInstance().getSpecialToken());
                    groupInfoTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    getAdapter().notifyDataSetChanged();
                    refreshLayout(getList());
                }

                groupDataCache.put(ALL_GROUP_ID, new MessageListBean());
                groupDataCache.put(BILATERAL_GROUP_ID, new MessageListBean());

                if (getList().getSize() > 0) {
                    groupDataCache.put(ALL_GROUP_ID, getList().copy());
                }
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                accountBean = (AccountBean) savedInstanceState.getSerializable("account");
                token = savedInstanceState.getString("token");

                groupDataCache = (HashMap) savedInstanceState.getSerializable("groupDataCache");
                currentGroupId = savedInstanceState.getString("currentGroupId");

                getList().replaceData((MessageListBean) savedInstanceState.getSerializable("bean"));
                timeLineAdapter.notifyDataSetChanged();

                refreshLayout(getList());
                break;
        }

        super.onActivityCreated(savedInstanceState);

        buildActionBarNav();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            buildActionBarNav();
        }
    }

    private void buildActionBarNav() {
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        List<GroupBean> list = new ArrayList<GroupBean>();
        if (GlobalContext.getInstance().getGroup() != null) {
            list = GlobalContext.getInstance().getGroup().getLists();
        } else {
            list = new ArrayList<GroupBean>();
        }
        List<String> name = new ArrayList<String>();
        name.add(getString(R.string.all_people));
        name.add(getString(R.string.bilateral));

        for (GroupBean b : list) {
            name.add(b.getName());
        }

        final String[] valueArray = name.toArray(new String[0]);

        BaseAdapter adapter = new FriendsTimeLineListNavAdapter(getActivity(), valueArray);
        final List<GroupBean> finalList = list;
        getActivity().getActionBar().setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int which, long itemId) {
                String selectedItemId;

                if (which == 0) {
                    selectedItemId = "0";
                } else if (which == 1) {
                    selectedItemId = "1";
                } else {
                    selectedItemId = finalList.get(which - 2).getIdstr();
                }
                if (!selectedItemId.equals(currentGroupId)) {
                    setSelected(selectedItemId);
                    switchGroup();
                }
                return false;
            }
        });
    }

    private class DBCacheTask extends MyAsyncTask<Void, MessageListBean, Map<String, MessageListBean>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected Map<String, MessageListBean> doInBackground(Void... params) {

            MessageListBean allGroup = FriendsTimeLineDBTask.getHomeLineMsgList(accountBean.getUid());
            publishProgress(allGroup);

            Map<String, MessageListBean> map = new HashMap<String, MessageListBean>();
            MessageListBean biGroup = HomeOtherGroupTimeLineDBTask.get(accountBean.getUid(), BILATERAL_GROUP_ID);
            map.put(BILATERAL_GROUP_ID, biGroup);
            GroupListBean groupListBean = GlobalContext.getInstance().getGroup();
            if (groupListBean != null) {
                List<GroupBean> lists = groupListBean.getLists();
                for (GroupBean groupBean : lists) {
                    MessageListBean dbMsg = HomeOtherGroupTimeLineDBTask.get(accountBean.getUid(), groupBean.getId());
                    map.put(groupBean.getId(), dbMsg);
                }
            }
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, MessageListBean> result) {
            super.onPostExecute(result);
            if (result != null) {
                putToGroupDataMemoryCache(BILATERAL_GROUP_ID, result.get(BILATERAL_GROUP_ID));
                Set<String> keys = result.keySet();
                for (String key : keys) {
                    putToGroupDataMemoryCache(key, result.get(key));
                }
            }
        }

        @Override
        protected void onProgressUpdate(MessageListBean... result) {
            super.onProgressUpdate(result);
            if (result != null && result.length > 0) {
                MessageListBean homeMsg = result[0];
                getList().replaceData(homeMsg);
                putToGroupDataMemoryCache(ALL_GROUP_ID, homeMsg);
            }
            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            refreshLayout(getList());
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }


    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        MessageBean msg = getList().getItemList().get(position);
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", getList().getItemList().get(position));
        intent.putExtra("token", token);
        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.write_weibo:
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("account", accountBean);
                startActivity(intent);
                break;
            case R.id.refresh:
                if (allowRefresh())
                    getPullToRefreshListView().startRefreshNow();
                break;
            case R.id.group_name:

                if (canSwitchGroup()) {
                    FriendsGroupDialog dialog = new FriendsGroupDialog(GlobalContext.getInstance().getGroup(), currentGroupId);
                    dialog.setTargetFragment(this, 1);
                    dialog.show(getFragmentManager(), "");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean allowRefresh() {
        return Utility.isTaskStopped(newTask) && getPullToRefreshListView().getVisibility() == View.VISIBLE && Utility.isTaskStopped(autoRefreshTask);
    }

    public void setSelected(String selectedItemId) {
        currentGroupId = selectedItemId;
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {
        MainFriendsTimeLineDao dao;
        if (currentGroupId.equals(BILATERAL_GROUP_ID)) {
            dao = new BilateralTimeLineDao(token);
        } else if (currentGroupId.equals(ALL_GROUP_ID)) {
            dao = new MainFriendsTimeLineDao(token);
        } else {
            dao = new FriendGroupTimeLineDao(token, currentGroupId);
        }
        if (getList().getItemList().size() > 0) {
            dao.setSince_id(getList().getItemList().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        MainFriendsTimeLineDao dao;
        if (currentGroupId.equals(BILATERAL_GROUP_ID)) {
            dao = new BilateralTimeLineDao(token);
        } else if (currentGroupId.equals(ALL_GROUP_ID)) {
            dao = new MainFriendsTimeLineDao(token);
        } else {
            dao = new FriendGroupTimeLineDao(token, currentGroupId);
        }
        if (getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(token);
        dao.setMax_id(beginId);
        dao.setSince_id(endId);

        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 0) {
            showNewMsgToastMessage(newValue);
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();

            if (getList() != null) {
                if (currentGroupId.equals(ALL_GROUP_ID)) {
                    SaveToDBService.save(getActivity(), SaveToDBService.TYPE_STATUS, getList(), accountBean.getUid());
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HomeOtherGroupTimeLineDBTask.replace(getList(), accountBean.getUid(), currentGroupId);
                        }
                    }).start();
                }
            }
            putToGroupDataMemoryCache(currentGroupId, getList());

        }

    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean oldValue) {
        if (Utility.isAllNotNull(getActivity(), oldValue) && oldValue.getSize() > 1) {
            getList().addOldData(oldValue);
        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }
    }

    public void switchGroup() {

        if (groupDataCache.get(currentGroupId) == null || groupDataCache.get(currentGroupId).getSize() == 0) {
            getList().getItemList().clear();
            getAdapter().notifyDataSetChanged();
            getPullToRefreshListView().startRefreshNow();

        } else {
            getList().replaceData(groupDataCache.get(currentGroupId));
            getAdapter().notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void putToGroupDataMemoryCache(String groupId, MessageListBean value) {
        MessageListBean copy = new MessageListBean();
        copy.addNewData(value);
        groupDataCache.put(groupId, copy);
    }

    private void removeRefresh() {
        if (autoRefreshExecutor != null && !autoRefreshExecutor.isShutdown())
            autoRefreshExecutor.shutdownNow();
    }

    protected void addRefresh() {

        autoRefreshExecutor = Executors.newSingleThreadScheduledExecutor();
        autoRefreshExecutor
                .scheduleAtFixedRate(new AutoTask(), AppConfig.AUTO_REFRESH_INITIALDELAY, AppConfig.AUTO_REFRESH_PERIOD, TimeUnit.SECONDS);

    }

    private class AutoTask implements Runnable {

        @Override
        public void run() {
            if (!SettingUtility.getEnableAutoRefresh()) {
                return;
            }

            if (!Utility.isTaskStopped(dbTask))
                return;

            if (!Utility.isTaskStopped(newTask))
                return;

            if (!Utility.isTaskStopped(oldTask))
                return;

            if (!Utility.isTaskStopped(middleTask))
                return;


            if (!Utility.isWifi(getActivity())) {
                return;
            }

            if (Utility.isTaskStopped(autoRefreshTask)) {
                autoRefreshTask = new AutoRefreshTask();
                autoRefreshTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    }

    private class AutoRefreshTask extends MyAsyncTask<Void, MessageListBean, MessageListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isListViewFling() || !isVisible() || ((MainTimeLineActivity) getActivity()).getSlidingMenu().isMenuShowing())
                cancel(true);
        }

        @Override
        protected MessageListBean doInBackground(Void... params) {
            try {
                return getDoInBackgroundNewData();
            } catch (WeiboException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            super.onPostExecute(newValue);

            if (!Utility.isTaskStopped(newTask))
                return;

            if (!Utility.isTaskStopped(oldTask))
                return;

            if (!Utility.isTaskStopped(middleTask))
                return;

            if (newValue == null || newValue.getSize() == 0 || getActivity() == null
                    || isListViewFling()
                    || !isVisible()
                    || ((MainTimeLineActivity) getActivity()).getSlidingMenu().isMenuShowing())
                return;

            int firstPosition = getListView().getFirstVisiblePosition();

            int size = newValue.getSize();

            if (getActivity() != null && newValue.getSize() > 0) {

                getList().addNewData(newValue);

                if (getList() != null && currentGroupId.equals(ALL_GROUP_ID)) {
                    SaveToDBService.save(getActivity(), SaveToDBService.TYPE_STATUS, getList(), accountBean.getUid());
                }
            }
//            getActivity().getActionBar().getTabAt(0).setText(getString(R.string.home));
            putToGroupDataMemoryCache(currentGroupId, getList());

            int index = getListView().getFirstVisiblePosition();

            View v = getListView().getChildAt(1);
            int top = (v == null) ? 0 : v.getTop();
            getListView().setFastScrollEnabled(false);
            getAdapter().notifyDataSetChanged();
            int ss = index + size;

//            if (firstPosition == 0) {
////
//            } else {

            getListView().setSelectionFromTop(ss + 1, top);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getListView().setFastScrollEnabled(SettingUtility.allowFastScroll());
                }
            }, 2000);
//            }
//            getListView().setLayoutTransition(null);

        }
    }


}