package org.qii.weiciyuan.ui.basefragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.AbsListView;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-10
 */
public abstract class AbstractFriendsFanListFragment extends AbstractUserListFragment {

    protected UserBean currentUser;
    protected String uid;


    public AbstractFriendsFanListFragment() {

    }

    public AbstractFriendsFanListFragment(String uid) {
        this.uid = uid;
    }

    //this api has bug, check cursor before add data
    @Override
    protected void oldUserOnPostExecute(UserListBean newValue) {
        if (newValue != null && newValue.getUsers().size() > 0 && newValue.getPrevious_cursor() != bean.getPrevious_cursor()) {
            List<UserBean> list = newValue.getUsers();
            getList().getUsers().addAll(list);
            bean.setNext_cursor(newValue.getNext_cursor());
            buildActionBarSubtitle();
        }

    }

    @Override
    protected void newUserOnPostExecute() {
        buildActionBarSubtitle();
    }

    protected void buildActionBarSubtitle() {
        if (!TextUtils.isEmpty(currentUser.getFriends_count())) {

            int size = Integer.valueOf(currentUser.getFriends_count());
            int newSize = bean.getTotal_number();
            String number = "";
            if (size >= newSize) {
                number = bean.getUsers().size() + "/" + size;
            } else {
                number = bean.getUsers().size() + "/" + newSize;
            }
            getActivity().getActionBar().setSubtitle(number);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            pullToRefreshListView.setRefreshing();
                            loadNewMsg();
                        }

                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);

                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                currentUser = savedInstanceState.getParcelable("currentUser");
                uid = savedInstanceState.getString("uid");
                clearAndReplaceValue((UserListBean) savedInstanceState.getParcelable("bean"));
                getAdapter().notifyDataSetChanged();
                break;
        }

        refreshLayout(bean);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ((IUserInfo) getActivity()).getUser();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentUser", currentUser);
        outState.putString("uid", uid);
    }
}
