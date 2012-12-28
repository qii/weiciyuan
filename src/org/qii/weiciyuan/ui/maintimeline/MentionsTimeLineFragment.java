package org.qii.weiciyuan.ui.maintimeline;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MentionsTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private DBCacheTask dbTask;


    private String[] groupNames = new String[3];

    private String filter_by_author = "0";
    private String filter_by_type = "0";

    private int currentGroupId = 0;

    private Map<Integer, MessageListBean> groupDataCache = new HashMap<Integer, MessageListBean>();

    public void setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
    }

    public void setFilter_by_type(String filter_by_type) {
        this.filter_by_type = filter_by_type;
    }

    public void setCurrentGroupId(int currentGroupId) {
        this.currentGroupId = currentGroupId;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Utility.cancelTasks(dbTask);
    }

    private MessageListBean bean = new MessageListBean();

    @Override
    public MessageListBean getList() {
        return bean;
    }

    public MentionsTimeLineFragment() {

    }

    public MentionsTimeLineFragment(AccountBean accountBean, UserBean userBean, String token) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        groupNames[0] = getString(R.string.all_people);
        groupNames[1] = getString(R.string.all_following);
        groupNames[2] = getString(R.string.original);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);


        outState.putStringArray("groupNames", groupNames);
        outState.putInt("currentGroupId", currentGroupId);
        outState.putString("filter_by_author", filter_by_author);
        outState.putString("filter_by_type", filter_by_type);

        outState.putSerializable("0", groupDataCache.get(0));
        outState.putSerializable("1", groupDataCache.get(1));
        outState.putSerializable("2", groupDataCache.get(2));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {
            if (getActivity().getActionBar().getTabAt(1).getText().toString().contains(")")) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (getActivity() != null && newValue.getSize() > 0) {
            showNewMsgToastMessage(newValue);
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();

        }

        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.mentions));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(GlobalContext.getInstance().getCurrentAccountId()).intValue());

        putToGroupDataMemoryCache(currentGroupId, bean);

    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
            token = savedInstanceState.getString("token");

            groupNames = savedInstanceState.getStringArray("groupNames");
            currentGroupId = savedInstanceState.getInt("currentGroupId");
            filter_by_author = savedInstanceState.getString("filter_by_author");
            filter_by_type = savedInstanceState.getString("filter_by_type");

            groupDataCache.put(0, (MessageListBean) savedInstanceState.getSerializable("0"));
            groupDataCache.put(1, (MessageListBean) savedInstanceState.getSerializable("1"));
            groupDataCache.put(2, (MessageListBean) savedInstanceState.getSerializable("2"));

            getList().replaceData((MessageListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(getList());

        } else {
            if (Utility.isTaskStopped(dbTask)) {
                dbTask = new DBCacheTask();
                dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }

            groupDataCache.put(0, new MessageListBean());
            groupDataCache.put(1, new MessageListBean());
            groupDataCache.put(2, new MessageListBean());
        }


    }

    private class DBCacheTask extends MyAsyncTask<Void, MessageListBean, MessageListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected MessageListBean doInBackground(Void... params) {
            return DatabaseManager.getInstance().getRepostLineMsgList(GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        protected void onPostExecute(MessageListBean result) {
            super.onPostExecute(result);

            if (result != null) {
                getList().replaceData(result);
                putToGroupDataMemoryCache(0, result);
            }

            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            refreshLayout(bean);

            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */

            if (bean.getSize() == 0) {
                pullToRefreshListView.startRefreshNow();
            }

            /**when one user open app from android notification center while this app is using another account,
             * activity will restart, and then mentions and comment fragment
             * will fetch new message from server
             **/
            if (getActivity().getActionBar().getTabAt(1).getText().toString().contains(")")) {
                pullToRefreshListView.startRefreshNow();
            }
        }

    }


    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position));
        intent.putExtra("token", token);
        startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_mentionstimelinefragment, menu);
        menu.findItem(R.id.group_name).setTitle(groupNames[currentGroupId]);
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
                    MentionsGroupDialog dialog = new MentionsGroupDialog(groupNames, currentGroupId);
                    dialog.setTargetFragment(MentionsTimeLineFragment.this, 0);
                    dialog.show(getFragmentManager(), "");
                }

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        if (getList().getItemList().size() > 0) {
            dao.setSince_id(getList().getItemList().get(0).getId());
        }
        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();
        if (result != null && currentGroupId == 0) {
            DatabaseManager.getInstance().addRepostLineMsg(result, accountBean.getUid());

        }
        return result;
    }


    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        if (getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        dao.setMax_id(beginId);
        dao.setSince_id(endId);

        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    public void switchGroup() {


        if (groupDataCache.get(currentGroupId).getSize() == 0) {
            bean.getItemList().clear();
            getAdapter().notifyDataSetChanged();
            getPullToRefreshListView().startRefreshNow();

        } else {
            getList().replaceData(groupDataCache.get(currentGroupId));
            getAdapter().notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }


    private void putToGroupDataMemoryCache(int groupId, MessageListBean value) {
        MessageListBean copy = new MessageListBean();
        copy.addNewData(value);
        groupDataCache.put(groupId, copy);
    }

}

