package org.qii.weiciyuan.ui.basefragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.adapter.UserListAdapter;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-18
 */
public abstract class AbstractUserListFragment extends Fragment {

    protected View footerView;
    protected ICommander commander;
    protected PullToRefreshListView pullToRefreshListView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected UserListAdapter timeLineAdapter;
    protected UserBean currentUser;
    protected UserListBean bean = new UserListBean();
    protected String uid;

    private UserListGetNewDataTask newTask;
    private UserListGetOlderDataTask oldTask;

    protected ListView getListView() {
        return pullToRefreshListView.getRefreshableView();
    }

    protected void clearAndReplaceValue(UserListBean value) {


        bean.setNext_cursor(value.getNext_cursor());
        bean.getUsers().clear();
        bean.getUsers().addAll(value.getUsers());
        bean.setTotal_number(value.getTotal_number());
        bean.setPrevious_cursor(value.getPrevious_cursor());

    }

    protected ActionMode mActionMode;

    public void setmActionMode(ActionMode mActionMode) {
        this.mActionMode = mActionMode;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);
        if (oldTask != null)
            oldTask.cancel(true);
    }

    public AbstractUserListFragment() {

    }

    public AbstractUserListFragment(String uid) {
        this.uid = uid;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null) {
            currentUser=(UserBean)savedInstanceState.getSerializable("currentUser");
            uid=savedInstanceState.getString("uid");
            clearAndReplaceValue((UserListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            pullToRefreshListView.startRefreshNow();

        }

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = ((IUserInfo) getActivity()).getUser();

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public UserListBean getList() {
        return bean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putSerializable("currentUser",currentUser);
        outState.putString("uid",uid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {


            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                refresh();
            }
        });
//        listView.setScrollingCacheEnabled(false);
        //        listView.setHeaderDividersEnabled(false);
        pullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                listViewFooterViewClick(null);
            }
        });

        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();


        timeLineAdapter = new UserListAdapter(AbstractUserListFragment.this, ((AbstractAppActivity) getActivity()).getCommander(), bean.getUsers(), getListView());
        pullToRefreshListView.setAdapter(timeLineAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    getListView().clearChoices();
                    mActionMode.finish();
                    mActionMode = null;
                    return;
                }
                getListView().clearChoices();
                if (position - 1 < getList().getUsers().size()) {

                    listViewItemClick(parent, view, position - 1, id);
                } else {

                    listViewFooterViewClick(view);
                }

            }
        });
        return view;
    }

    protected void listViewFooterViewClick(View view) {
        if (oldTask == null || oldTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            oldTask = new UserListGetOlderDataTask();
            oldTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        intent.putExtra("user", bean.getUsers().get(position));
        startActivity(intent);
    }

    protected void refreshLayout(UserListBean bean) {
        if (bean.getUsers().size() > 0) {
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
//            listView.setVisibility(View.INVISIBLE);
        }
    }


    private void showFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.loading));
        View view = footerView.findViewById(R.id.refresh);
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
    }


    protected void dismissFooterView() {
        footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
        footerView.findViewById(R.id.refresh).clearAnimation();
        footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
    }


    private void showErrorFooterView() {
        TextView tv = ((TextView) footerView.findViewById(R.id.listview_footer));
        tv.setVisibility(View.VISIBLE);
        tv.setText(getString(R.string.click_to_load_older_message));
        View view = footerView.findViewById(R.id.refresh);
        view.clearAnimation();
        view.setVisibility(View.GONE);
    }

    public void refresh() {
        if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {

            Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();

            newTask = new UserListGetNewDataTask();
            newTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

            Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
            for (String key : keys) {
                avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
                avatarBitmapWorkerTaskHashMap.remove(key);
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_userlistfragment, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:
                pullToRefreshListView.startRefreshNow();

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class UserListGetNewDataTask extends MyAsyncTask<Void, UserListBean, UserListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            getListView().setSelection(0);
        }


        @Override
        protected UserListBean doInBackground(Void... params) {
            try {
                return getDoInBackgroundNewData();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;

        }

        @Override
        protected void onCancelled(UserListBean newValue) {
            super.onCancelled(newValue);
            if (this.e != null)
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            cleanWork();
        }


        @Override
        protected void onPostExecute(UserListBean newValue) {
            if (newValue != null && newValue.getUsers().size() > 0) {

                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();


            }

            cleanWork();
            getActivity().invalidateOptionsMenu();
            super.onPostExecute(newValue);

        }

        private void cleanWork() {

            pullToRefreshListView.onRefreshComplete();


        }
    }


    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
//        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }


    class UserListGetOlderDataTask extends MyAsyncTask<Void, UserListBean, UserListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            showFooterView();
        }

        @Override
        protected UserListBean doInBackground(Void... params) {


            try {
                return getDoInBackgroundOldData();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;

        }

        @Override
        protected void onCancelled(UserListBean newValue) {
            super.onCancelled(newValue);

            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                showErrorFooterView();
            } else {
                dismissFooterView();
            }

        }

        @Override
        protected void onPostExecute(UserListBean newValue) {
            if (newValue != null && newValue.getUsers().size() > 0 && newValue.getPrevious_cursor() != bean.getPrevious_cursor()) {
                List<UserBean> list = newValue.getUsers();
                getList().getUsers().addAll(list);
                bean.setNext_cursor(newValue.getNext_cursor());
            }

            timeLineAdapter.notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();
            dismissFooterView();
            super.onPostExecute(newValue);
        }


    }

    protected abstract UserListBean getDoInBackgroundNewData() throws WeiboException;

    protected abstract UserListBean getDoInBackgroundOldData() throws WeiboException;


}
