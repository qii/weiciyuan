package org.qii.weiciyuan.ui.dm;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.bean.DMListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.dm.DMConversationDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.adapter.DMConversationAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-15
 */
public class DMConversationListFragment extends AbstractTimeLineFragment<DMListBean> {

    private UserBean userBean;

    private int page = 1;

    private DMListBean bean = new DMListBean();

    @Override
    public DMListBean getList() {
        return bean;
    }

    public DMConversationListFragment(UserBean userBean) {
        this.userBean = userBean;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        getPullToRefreshListView().startRefreshNow();
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMConversationAdapter(this, ((AbstractAppActivity) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }


    @Override
    protected void newMsgOnPostExecute(DMListBean newValue) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(DMListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            page++;
        }
    }

    @Override
    protected DMListBean getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        return new DMConversationDao(GlobalContext.getInstance().getSpecialToken())
                .setUid(userBean.getId())
                .setPage(page).getConversationList();
    }

    @Override
    protected DMListBean getDoInBackgroundOldData() throws WeiboException {
        DMConversationDao dao = new DMConversationDao(GlobalContext.getInstance().getSpecialToken())
                .setUid(userBean.getId())
                .setPage(page + 1);
        DMListBean result = dao.getConversationList();
        return result;
    }

    @Override
    protected DMListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }
}
