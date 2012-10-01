package org.qii.weiciyuan.ui.maintimeline;

import android.content.Intent;
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
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment {

    private AccountBean accountBean;
    private UserBean userBean;
    private String token;
    private SimpleTask dbTask;


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
        outState.putString("token",token);
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
    public void onDetach() {
        super.onDetach();
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


}

