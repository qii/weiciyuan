package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.loader.StatusesByIdLoader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {

    protected UserBean userBean;
    protected String token;
    private MessageListBean bean = new MessageListBean();

    public static StatusesByIdTimeLineFragment newInstance(UserBean userBean, String token) {
        StatusesByIdTimeLineFragment fragment = new StatusesByIdTimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("userBean", userBean);
        bundle.putString("token", token);
        fragment.setArguments(bundle);
        return fragment;
    }

    public StatusesByIdTimeLineFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userBean = getArguments().getParcelable("userBean");
        token = getArguments().getString("token");
    }

    @Override
    public MessageListBean getList() {
        return bean;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        MessageBean msg = (MessageBean) data.getParcelableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getList().getSize(); i++) {
                if (msg.equals(getList().getItem(i))) {
                    getList().getItem(i).setReposts_count(msg.getReposts_count());
                    getList().getItem(i).setComments_count(msg.getComments_count());
                    break;
                }
            }
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userBean != null
                && userBean.getId() != null
                && userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            GlobalContext.getInstance()
                    .registerForAccountChangeListener(myProfileInfoChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalContext.getInstance().unRegisterForAccountChangeListener(myProfileInfoChangeListener);
    }

    private GlobalContext.MyProfileInfoChangeListener myProfileInfoChangeListener
            = new GlobalContext.MyProfileInfoChangeListener() {
        @Override
        public void onChange(UserBean newUserBean) {
            for (MessageBean msg : getList().getItemList()) {
                msg.setUser(newUserBean);
            }
            getAdapter().notifyDataSetChanged();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", getList());
        outState.putParcelable("userBean", userBean);
        outState.putString("token", token);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getPullToRefreshListView().setRefreshing();
                            loadNewMsg();
                        }
                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceData((MessageListBean) savedInstanceState.getParcelable("bean"));
                userBean = (UserBean) savedInstanceState.getParcelable("userBean");
                token = savedInstanceState.getString("token");
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

        super.onActivityCreated(savedInstanceState);
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        startActivityForResult(
                BrowserWeiboMsgActivity.newIntent(getList().getItem(position),
                        GlobalContext.getInstance().getSpecialToken()),
                0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void newMsgLoaderSuccessCallback(MessageListBean newValue, Bundle loaderArgs) {
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            getActivity().invalidateOptionsMenu();
        }
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id,
            Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateMiddleMsgLoader(int id,
            Bundle args, String middleBeginId, String middleEndId, String middleEndTag,
            int middlePosition) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, middleBeginId,
                middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateOldMsgLoader(int id,
            Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String maxId = null;

        if (getList().getSize() > 0) {
            maxId = getList().getItemList().get(getList().getSize() - 1).getId();
        }

        return new StatusesByIdLoader(getActivity(), uid, screenName, token, null, maxId);
    }
}


