package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.StatusesTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.DataMemoryCache;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {


    protected UserBean userBean;
    protected String token;


    @Override
    public MessageListBean getList() {
        return DataMemoryCache.getStatusByIdTimeLineData();
    }

    public StatusesByIdTimeLineFragment() {

    }

    public StatusesByIdTimeLineFragment(UserBean userBean, String token) {
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", getList());
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);
    }


    @Override
    protected MessageListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                getPullToRefreshListView().startRefreshNow();
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceData((MessageListBean) savedInstanceState.getSerializable("bean"));
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                token = savedInstanceState.getString("token");
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataMemoryCache.clearStatusByIdTimeLineData();
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("msg", getList().getItem(position));
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getPullToRefreshListView().startRefreshNow();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {

        String id = userBean.getId();
        String screenName = userBean.getScreen_name();

        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);

        if (TextUtils.isEmpty(id)) {
            dao.setScreen_name(screenName);
        }

        if (getList().getSize() > 0) {
            dao.setSince_id(getList().getItem(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        String id = userBean.getId();
        String screenName = userBean.getScreen_name();

        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);
        if (TextUtils.isEmpty(id)) {
            dao.setScreen_name(screenName);
        }
        if (getList().getSize() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getSize() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();

        }


    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            getActivity().invalidateOptionsMenu();

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }
    }

}


