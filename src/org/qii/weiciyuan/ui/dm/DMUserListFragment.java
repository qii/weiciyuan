package org.qii.weiciyuan.ui.dm;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.dao.dm.DMDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.adapter.DMUserListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListFragment extends AbstractTimeLineFragment<DMUserListBean> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new DMUserListBean();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pullToRefreshListView.startRefreshNow();
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMUserListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }

    protected void clearAndReplaceValue(DMUserListBean value) {
        bean.getItemList().clear();
        bean.getItemList().addAll(value.getItemList());
        bean.setTotal_number(value.getTotal_number());
    }

    @Override
    protected void newMsgOnPostExecute(DMUserListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getSize() == 0) {

            } else if (newValue.getSize() > 0) {
                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();

            }
        }
    }

    @Override
    protected void oldMsgOnPostExecute(DMUserListBean newValue) {

    }

    @Override
    protected DMUserListBean getDoInBackgroundNewData() throws WeiboException {
        return new DMDao(GlobalContext.getInstance().getSpecialToken()).getUserList();
    }

    @Override
    protected DMUserListBean getDoInBackgroundOldData() throws WeiboException {
        return null;
    }

    @Override
    protected DMUserListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }
}
