package org.qii.weiciyuan.ui.basefragment;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppConfig;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.AbsListView;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-10
 */
public abstract class AbstractFriendsFanListFragment extends AbstractUserListFragment {

    public AbstractFriendsFanListFragment() {

    }

    //this api has bug, check cursor before add data
    @Override
    protected void oldUserLoaderSuccessCallback(UserListBean newValue) {
        if (newValue != null && newValue.getUsers().size() > 0
                && newValue.getPrevious_cursor() != bean.getPrevious_cursor()) {
            List<UserBean> list = newValue.getUsers();
            getList().getUsers().addAll(list);
            bean.setNext_cursor(newValue.getNext_cursor());
            buildActionBarSubtitle();
        }
    }

    @Override
    protected void newUserLoaderSuccessCallback() {
        buildActionBarSubtitle();
    }

    protected void buildActionBarSubtitle() {
        if (!TextUtils.isEmpty(getCurrentUser().getFriends_count())) {
            int size = Integer.valueOf(getCurrentUser().getFriends_count());
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
                clearAndReplaceValue((UserListBean) savedInstanceState.getParcelable("bean"));
                getAdapter().notifyDataSetChanged();
                break;
        }

        refreshLayout(bean);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        if (SettingUtility.isFollowingOrFanListFirstShow()) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.tip)
                    .setMessage(R.string.following_and_fan_list_tip)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    protected abstract UserBean getCurrentUser();
}
