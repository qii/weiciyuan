package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.StatusesTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractMessageTimeLineFragment {


    private UserBean userBean;


    public StatusesByIdTimeLineFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userbean", userBean);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        commander = ((AbstractAppActivity) getActivity()).getCommander();

        if (savedInstanceState != null) {
            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            userBean = (UserBean) savedInstanceState.getSerializable("userbean");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            userBean = ((IUserInfo) getActivity()).getUser();
            refresh();

        }

        super.onActivityCreated(savedInstanceState);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        intent.putExtra("msg", bean.getStatuses().get(position));
        startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.statusesbyidtimelinefragment_menu, menu);
        if (!TextUtils.isEmpty(userBean.getStatuses_count())) {
            String number = bean.getStatuses().size() + "/" + userBean.getStatuses_count();
            menu.findItem(R.id.statusesbyidtimelinefragment_status_number).setTitle(number);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.statusesbyidtimelinefragment_status_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {

        String token = ((IToken) getActivity()).getToken();
        String id = ((IUserInfo) getActivity()).getUser().getId();
        String screenName = ((IUserInfo) getActivity()).getUser().getScreen_name();

        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);

        if (TextUtils.isEmpty(id)) {
            dao.setScreen_name(screenName);
        }

        if (getList().getStatuses().size() > 0) {
            dao.setSince_id(getList().getStatuses().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
        String token = ((IToken) getActivity()).getToken();
        String id = ((IUserInfo) getActivity()).getUser().getId();
        String screenName = ((IUserInfo) getActivity()).getUser().getScreen_name();

        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);
        if (TextUtils.isEmpty(id)) {
            dao.setScreen_name(screenName);
        }
        if (getList().getStatuses().size() > 0) {
            dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
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


