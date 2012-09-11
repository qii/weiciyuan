package org.qii.weiciyuan.ui.maintimeline;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.maintimeline.MainFriendsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment {

    UserBean userBean;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        userBean = ((IUserInfo) getActivity()).getUser();
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        showNewMsgToastMessage(newValue);
        super.newMsgOnPostExecute(newValue);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainTimeLineActivity) getActivity()).setHomeListView(listView);
        ((MainTimeLineActivity) getActivity()).setHomeFragment(this);
        if (savedInstanceState != null) {
            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

            refreshLayout(bean);
        } else {
            new SimpleTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        getActivity().invalidateOptionsMenu();
    }


    private class SimpleTask extends MyAsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            clearAndReplaceValue(DatabaseManager.getInstance().getHomeLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid()));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);
        }
    }


    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getStatuses().get(position));
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
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
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("accountName", ((IAccountInfo) getActivity()).getAccount().getUsernick());
                startActivity(intent);
                break;
            case R.id.friendstimelinefragment_refresh:

                refresh();

                break;
            case R.id.friendstimelinefragment_name:
                intent = new Intent(getActivity(), MyInfoActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", ((IUserInfo) getActivity()).getUser());
                intent.putExtra("account", ((IAccountInfo) getActivity()).getAccount());
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setSince_id(getList().getStatuses().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();
        if (result != null) {
            if (result.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                DatabaseManager.getInstance().addHomeLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            } else {
                DatabaseManager.getInstance().replaceHomeLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            }
        }
        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        MainFriendsTimeLineDao dao = new MainFriendsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


}

