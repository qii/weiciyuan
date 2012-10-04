package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.StatusesTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractMessageTimeLineFragment {


    private UserBean userBean;
    private String token;


    public StatusesByIdTimeLineFragment() {

    }

    public StatusesByIdTimeLineFragment(UserBean userBean, String token) {
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);
    }


    @Override
    protected ListBean<MessageBean> getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        commander = ((AbstractAppActivity) getActivity()).getCommander();

        if (savedInstanceState != null) {
            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            token = savedInstanceState.getString("token");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {

            pullToRefreshListView.startRefreshNow();

            refresh();

        }

        super.onActivityCreated(savedInstanceState);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                pullToRefreshListView.startRefreshNow();
                refresh();

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
    protected void afterGetNewMsg() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    protected void afterGetOldMsg() {
        getActivity().invalidateOptionsMenu();
    }

}


