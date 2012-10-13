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
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MentionsTimeLineFragment extends AbstractMessageTimeLineFragment {

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private SimpleTask dbTask;


    private String[] group = new String[3];

    private String filter_by_author = "0";
    private String filter_by_type = "0";

    private int selected = 0;

    private Map<Integer, ListBean<MessageBean>> hashMap = new HashMap<Integer, ListBean<MessageBean>>();

    public void setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
    }

    public void setFilter_by_type(String filter_by_type) {
        this.filter_by_type = filter_by_type;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dbTask != null)
            dbTask.cancel(true);
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
        group[0] = getString(R.string.all_people);
        group[1] = getString(R.string.all_following);
        group[2] = getString(R.string.original);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);


        outState.putStringArray("group", group);
        outState.putInt("selected", selected);
        outState.putString("filter_by_author", filter_by_author);
        outState.putString("filter_by_type", filter_by_type);

        outState.putSerializable("0", hashMap.get(0));
        outState.putSerializable("1", hashMap.get(1));
        outState.putSerializable("2", hashMap.get(2));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {
            if (getActivity().getActionBar().getTabAt(1).getText().toString().contains(")")) {
                pullToRefreshListView.startRefreshNow();
            }
        }
    }

    @Override
    protected void newMsgOnPostExecute(ListBean<MessageBean> newValue) {
        showNewMsgToastMessage(newValue);
        super.newMsgOnPostExecute(newValue);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
            token = savedInstanceState.getString("token");

            group = savedInstanceState.getStringArray("group");
            selected = savedInstanceState.getInt("selected");
            filter_by_author = savedInstanceState.getString("filter_by_author");
            filter_by_type = savedInstanceState.getString("filter_by_type");

            hashMap.put(0, (MessageListBean) savedInstanceState.getSerializable("0"));
            hashMap.put(1, (MessageListBean) savedInstanceState.getSerializable("1"));
            hashMap.put(2, (MessageListBean) savedInstanceState.getSerializable("2"));

            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);

        } else {
            if (dbTask == null || dbTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                dbTask = new SimpleTask();
                dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }

            hashMap.put(0, new MessageListBean());
            hashMap.put(1, new MessageListBean());
            hashMap.put(2, new MessageListBean());
        }


    }

    private class SimpleTask extends MyAsyncTask<Object, Object, Object> {


        @Override
        protected Object doInBackground(Object... params) {
            clearAndReplaceValue(DatabaseManager.getInstance().getRepostLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid()));
            clearAndReplaceValue(0, bean);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            getAdapter().notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);

            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */

            if (bean.getSize() == 0) {
                refresh();
            }

            /**when one user open app from android notification center while this app is using another account,
             * activity will restart, and then mentions and comment fragment
             * will fetch new message from server
             **/
            if (getActivity().getActionBar().getTabAt(1).getText().toString().contains(")")) {
                refresh();
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
        menu.findItem(R.id.mentionstimelinefragment_group).setTitle(group[selected]);
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

            case R.id.mentionstimelinefragment_refresh:
                pullToRefreshListView.startRefreshNow();

                break;
            case R.id.mentionstimelinefragment_group:
                if (canSwitchGroup()) {
                    MentionsGroupDialog dialog = new MentionsGroupDialog(group, selected);
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
        if (result != null && selected == 0) {
            DatabaseManager.getInstance().addRepostLineMsg(result, accountBean.getUid());

        }
        return result;
    }

    @Override
    protected void afterGetNewMsg() {
        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.mentions));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(GlobalContext.getInstance().getCurrentAccountId()).intValue());

        clearAndReplaceValue(selected, bean);
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
    protected ListBean<MessageBean> getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        dao.setMax_id(beginId);
        dao.setSince_id(endId);

        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    public void switchGroup() {


        if (hashMap.get(selected).getSize() == 0) {
            bean.getItemList().clear();
            getAdapter().notifyDataSetChanged();
            pullToRefreshListView.startRefreshNow();

        } else {
            clearAndReplaceValue(hashMap.get(selected));
            getAdapter().notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }


    private void clearAndReplaceValue(int position, ListBean<MessageBean> newValue) {
        hashMap.get(position).getItemList().clear();
        hashMap.get(position).getItemList().addAll(newValue.getItemList());
        hashMap.get(position).setTotal_number(newValue.getTotal_number());
    }

}

