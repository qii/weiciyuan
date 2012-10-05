package org.qii.weiciyuan.ui.maintimeline;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: qii
 * Date: 12-7-29
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment {

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;
    private SimpleTask dbTask;

    private AutoRefreshTask autoRefreshTask = null;
    private ScheduledExecutorService scheduledRefreshExecutorService = null;


    public FriendsTimeLineFragment() {

    }

    public FriendsTimeLineFragment(AccountBean accountBean, UserBean userBean, String token) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = token;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void newMsgOnPostExecute(ListBean<MessageBean> newValue) {
        showNewMsgToastMessage(newValue);
        super.newMsgOnPostExecute(newValue);

    }


    @Override
    public void onResume() {
        super.onResume();
        addRefresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeRefresh();
        if (autoRefreshTask != null)
            autoRefreshTask.cancel(true);
        if (dbTask != null)
            dbTask.cancel(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
            token = savedInstanceState.getString("token");
            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

            refreshLayout(bean);
        } else {
            if (dbTask == null || dbTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                dbTask = new SimpleTask();
                dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        getActivity().invalidateOptionsMenu();
        super.onActivityCreated(savedInstanceState);

    }


    private class SimpleTask extends MyAsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            clearAndReplaceValue(DatabaseManager.getInstance().getHomeLineMsgList(accountBean.getUid()));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (bean.getSize() == 0)
                refresh();
        }
    }


    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        MessageBean msg = bean.getItemList().get(position);
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position));
        intent.putExtra("token", token);
        startActivity(intent);


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.friendstimelinefragment_menu, menu);
        if (getResources().getBoolean(R.bool.is_phone)) {
            menu.findItem(R.id.friendstimelinefragment_name).setTitle(userBean.getScreen_name());
        } else {
            menu.removeItem(R.id.friendstimelinefragment_name);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendstimelinefragment_new_weibo:
                Intent intent = new Intent(getActivity(), StatusNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("account", accountBean);
                startActivity(intent);
                break;
            case R.id.friendstimelinefragment_refresh:
                pullToRefreshListView.startRefreshNow();

                refresh();

                break;
            case R.id.friendstimelinefragment_name:
                intent = new Intent(getActivity(), MyInfoActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", userBean);
                intent.putExtra("account", accountBean);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void afterGetNewMsg() {
        super.afterGetNewMsg();
        getActivity().getActionBar().getTabAt(0).setText(getString(R.string.home));

    }

    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(token);
        if (getList().getItemList().size() > 0) {
            dao.setSince_id(getList().getItemList().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();
        if (result != null) {
            DatabaseManager.getInstance().addHomeLineMsg(result, accountBean.getUid());
        }
        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(token);
        if (getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected ListBean<MessageBean> getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(token);
        dao.setMax_id(beginId);
        dao.setSince_id(endId);

        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    private void removeRefresh() {
        if (scheduledRefreshExecutorService != null && !scheduledRefreshExecutorService.isShutdown())
            scheduledRefreshExecutorService.shutdownNow();
    }

    protected void addRefresh() {

        scheduledRefreshExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledRefreshExecutorService
                .scheduleAtFixedRate(new AutoTask(), AppConfig.AUTO_REFRESH_INITIALDELAY, AppConfig.AUTO_REFRESH_PERIOD, TimeUnit.SECONDS);

    }

    class AutoTask implements Runnable {

        @Override
        public void run() {
            if (!GlobalContext.getInstance().getEnableAutoRefresh()) {
                return;
            }

            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            boolean haveNetwork = (networkInfo != null) && (networkInfo.isConnected());

            boolean haveWifi = haveNetwork && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);

            if (!haveWifi) {
                return;
            }

            if (autoRefreshTask == null || autoRefreshTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                autoRefreshTask = new AutoRefreshTask();
                autoRefreshTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    }

    class AutoRefreshTask extends MyAsyncTask<Void, MessageListBean, MessageListBean> {

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

            int firstPosition = getListView().getFirstVisiblePosition();

            int size;
            if (newValue != null && getActivity() != null) {
                if (newValue.getSize() == 0) {

                } else if (newValue.getSize() > 0) {
                    size = newValue.getSize();

                    if (newValue.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        //for speed, add old data after new data
                        newValue.getItemList().addAll(getList().getItemList());
                    } else {
                        //null is flag means this position has some old messages which dont appear
                        if (getList().getSize() > 0) {
                            newValue.getItemList().add(null);
                        }
                        newValue.getItemList().addAll(getList().getItemList());
                    }
                    int index = getListView().getFirstVisiblePosition();
                    clearAndReplaceValue(newValue);
                    View v = getListView().getChildAt(1);
                    int top = (v == null) ? 0 : v.getTop();

                    timeLineAdapter.notifyDataSetChanged();
                    int ss = index + size;

//                    if (firstPosition == 0) {
//
//                    } else {
                    getListView().setSelectionFromTop(ss + 1, top);
//                    }

                }
            }
        }
    }


}