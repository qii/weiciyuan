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
import org.qii.weiciyuan.bean.android.MessageTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.dao.maintimeline.BilateralTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.FriendGroupTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.TimeLineReCmtCountDao;
import org.qii.weiciyuan.support.database.FriendsTimeLineDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.adapter.StatusListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.ICommander;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-7-29
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> implements GlobalContext.MyProfileInfoChangeListener {

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
    private HashMap<String, TimeLinePosition> positionCache = new HashMap<String, TimeLinePosition>();

    private MessageListBean bean = new MessageListBean();

    private BaseAdapter navAdapter;


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
                    FriendsTimeLineDBTask.asyncUpdateCount(msg.getId(), msg.getComments_count(), msg.getReposts_count());
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
    protected void onListViewScrollStop() {
        savePositionToPositionsCache();
    }

    private void savePositionToPositionsCache() {
        positionCache.put(currentGroupId, Utility.getCurrentPositionFromListView(getListView()));
    }

    private void setListViewPositionFromPositionsCache() {
        TimeLinePosition p = positionCache.get(currentGroupId);
        if (p != null)
            getListView().setSelectionFromTop(p.position + 1, p.top);
        else
            getListView().setSelectionFromTop(0, 0);
    }

    private void savePositionToDB() {
        final TimeLinePosition position = positionCache.get(currentGroupId);
        final String groupId = currentGroupId;
        FriendsTimeLineDBTask.asyncUpdatePosition(position, GlobalContext.getInstance().getCurrentAccountId(), groupId);
    }

    private void saveGroupIdToDB() {
        FriendsTimeLineDBTask.asyncUpdateRecentGroupId(GlobalContext.getInstance().getCurrentAccountId(), currentGroupId);
    }

    @Override
    public void onPause() {
        super.onPause();
        savePositionToDB();
        saveGroupIdToDB();
        removeRefresh();
        Utility.cancelTasks(autoRefreshTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        addRefresh();
        GlobalContext.getInstance().registerForAccountChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
        GlobalContext.getInstance().unRegisterForAccountChangeListener(this);
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
                buildActionBarNav();

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
                buildActionBarNav();

                break;
        }

        super.onActivityCreated(savedInstanceState);


    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            buildActionBarNav();
            ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);
        }
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new StatusListAdapter(this, ((ICommander) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView(), true, true);
        getListView().setAdapter(timeLineAdapter);
    }

    private int getIndexFromGroupId(String id, List<GroupBean> list) {

        if (list == null || list.size() == 0) {
            return 0;
        }

        int index = 0;

        if (id.equals("0")) {
            index = 0;
        } else if (id.equals("1")) {
            index = 1;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIdstr().equals(id)) {
                index = i + 2;
                break;
            }
        }
        return index;
    }

    private String getGroupIdFromIndex(int index, List<GroupBean> list) {
        String selectedItemId;

        if (index == 0) {
            selectedItemId = "0";
        } else if (index == 1) {
            selectedItemId = "1";
        } else {
            selectedItemId = list.get(index - 2).getIdstr();
        }
        return selectedItemId;
    }


    private String[] buildListNavData(List<GroupBean> list) {
        List<String> name = new ArrayList<String>();

        name.add(getString(R.string.all_people));
        name.add(getString(R.string.bilateral));

        for (GroupBean b : list) {
            name.add(b.getName());
        }

        String[] valueArray = name.toArray(new String[0]);
        return valueArray;
    }


    private void buildActionBarNav() {
        getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        List<GroupBean> list = new ArrayList<GroupBean>();
        if (GlobalContext.getInstance().getGroup() != null) {
            list = GlobalContext.getInstance().getGroup().getLists();
        } else {
            list = new ArrayList<GroupBean>();
        }

        navAdapter = new FriendsTimeLineListNavAdapter(getActivity(), buildListNavData(list));
        final List<GroupBean> finalList = list;
        getActivity().getActionBar().setListNavigationCallbacks(navAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int which, long itemId) {

                if (!Utility.isTaskStopped(dbTask)) {
                    return true;
                }

                String groupId = getGroupIdFromIndex(which, finalList);

                if (!groupId.equals(currentGroupId)) {
                    positionCache.put(currentGroupId, Utility.getCurrentPositionFromListView(getListView()));
                    setSelected(groupId);
                    switchGroup();
                }
                return true;
            }
        });
        currentGroupId = FriendsTimeLineDBTask.getRecentGroupId(GlobalContext.getInstance().getCurrentAccountId());
        getActivity().getActionBar().setSelectedNavigationItem(getRecentNavIndex());

    }

    @Override
    public void onChange(UserBean newUserBean) {
        if (navAdapter != null)
            navAdapter.notifyDataSetChanged();
    }

    private class DBCacheTask extends MyAsyncTask<Void, MessageTimeLineData, List<MessageTimeLineData>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected List<MessageTimeLineData> doInBackground(Void... params) {
            MessageTimeLineData recentGroupData = FriendsTimeLineDBTask.getRecentGroupData(accountBean.getUid());
            publishProgress(recentGroupData);
            return FriendsTimeLineDBTask.getOtherGroupData(accountBean.getUid(), recentGroupData.groupId);
        }

        @Override
        protected void onPostExecute(List<MessageTimeLineData> result) {
            super.onPostExecute(result);
            if (result != null) {
                for (MessageTimeLineData single : result) {
                    putToGroupDataMemoryCache(single.groupId, single.msgList);
                    positionCache.put(single.groupId, single.position);
                }
            }
        }

        @Override
        protected void onProgressUpdate(MessageTimeLineData... result) {
            super.onProgressUpdate(result);
            if (result != null && result.length > 0) {
                MessageTimeLineData recentData = result[0];
                getList().replaceData(recentData.msgList);
                putToGroupDataMemoryCache(recentData.groupId, recentData.msgList);
                positionCache.put(recentData.groupId, recentData.position);
                currentGroupId = recentData.groupId;
            }
            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            setListViewPositionFromPositionsCache();
            getActivity().getActionBar().setSelectedNavigationItem(getRecentNavIndex());
            refreshLayout(getList());
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            } else {
                new RefreshReCmtCountTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private int getRecentNavIndex() {
        List<GroupBean> list = new ArrayList<GroupBean>();
        if (GlobalContext.getInstance().getGroup() != null) {
            list = GlobalContext.getInstance().getGroup().getLists();
        } else {
            list = new ArrayList<GroupBean>();
        }
        return getIndexFromGroupId(currentGroupId, list);
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", getList().getItem(position));
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
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
        return dao.getGSONMsgList();
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
        return dao.getGSONMsgList();
    }

    @Override
    protected MessageListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(token);
        dao.setMax_id(beginId);
        dao.setSince_id(endId);
        return dao.getGSONMsgList();
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 0) {
            showNewMsgToastMessage(newValue);
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            putToGroupDataMemoryCache(currentGroupId, getList());
            final String groupId = currentGroupId;
            Runnable dbRunnable = new Runnable() {
                @Override
                public void run() {
                    FriendsTimeLineDBTask.replace(getList(), accountBean.getUid(), groupId);
                }
            };
            new Thread(dbRunnable).start();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean oldValue) {
        if (Utility.isAllNotNull(getActivity(), oldValue) && oldValue.getSize() > 1) {
            getList().addOldData(oldValue);
            putToGroupDataMemoryCache(currentGroupId, getList());

            final String groupId = currentGroupId;
            Runnable dbRunnable = new Runnable() {
                @Override
                public void run() {
                    FriendsTimeLineDBTask.replace(getList(), accountBean.getUid(), groupId);
                }
            };
            new Thread(dbRunnable).start();

        } else if (Utility.isAllNotNull(getActivity())) {
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
            setListViewPositionFromPositionsCache();
            saveGroupIdToDB();
            new RefreshReCmtCountTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
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
                    final String groupId = currentGroupId;
                    Runnable dbRunnable = new Runnable() {
                        @Override
                        public void run() {
                            FriendsTimeLineDBTask.replace(getList(), accountBean.getUid(), groupId);
                        }
                    };
                    new Thread(dbRunnable).start();
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

    /**
     * refresh timline messages' repost and comment count
     */
    private class RefreshReCmtCountTask extends MyAsyncTask<Void, List<MessageReCmtCountBean>, List<MessageReCmtCountBean>> {
        List<String> msgIds;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            msgIds = new ArrayList<String>();
            List<MessageBean> msgList = getList().getItemList();
            for (MessageBean msg : msgList) {
                if (msg != null) {
                    msgIds.add(msg.getId());
                }
            }
        }

        @Override
        protected List<MessageReCmtCountBean> doInBackground(Void... params) {
            try {
                return new TimeLineReCmtCountDao(GlobalContext.getInstance().getSpecialToken(), msgIds).get();
            } catch (WeiboException e) {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MessageReCmtCountBean> value) {
            super.onPostExecute(value);
            if (getActivity() == null || value == null)
                return;

            for (int i = 0; i < value.size(); i++) {
                MessageBean msg = getList().getItem(i);
                MessageReCmtCountBean count = value.get(i);
                if (msg != null && msg.getId().equals(count.getId())) {
                    msg.setReposts_count(count.getReposts());
                    msg.setComments_count(count.getComments());
                }
            }
            getAdapter().notifyDataSetChanged();
            final String groupId = currentGroupId;
            Runnable dbRunnable = new Runnable() {
                @Override
                public void run() {
                    FriendsTimeLineDBTask.replace(getList(), accountBean.getUid(), groupId);
                }
            };
            new Thread(dbRunnable).start();

        }
    }

}