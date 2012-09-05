package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-18
 */
public abstract class AbstractUserListFragment extends Fragment {

    protected View headerView;
    protected View footerView;
    public volatile boolean isBusying = false;
    protected ICommander commander;
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected BaseAdapter timeLineAdapter;
    protected UserBean currentUser;
    protected UserListBean bean = new UserListBean();
    protected String uid;

    private UserListGetNewDataTask newTask;
    private UserListGetOlderDataTask oldTask;

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);
        if (oldTask != null)
            oldTask.cancel(true);
    }

    public AbstractUserListFragment(String uid) {
        this.uid = uid;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getUsers().size() == 0) {
            bean = (UserListBean) savedInstanceState.getSerializable("bean");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            refresh();

        }

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setScrollingCacheEnabled(false);
        headerView = inflater.inflate(R.layout.fragment_listview_header_layout, null);
        listView.addHeaderView(headerView);
        listView.setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        listView.addFooterView(footerView);

        if (bean.getUsers().size() == 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
        if (!isBusying) {
            oldTask = new UserListGetOlderDataTask();
            oldTask.execute();
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
            footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        } else {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }

    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();


        newTask = new UserListGetNewDataTask();
        newTask.execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.userlistfragment_menu, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;

    }


    protected class TimeLineAdapter extends BaseAdapter {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @Override
        public int getCount() {

            if (getList() != null && getList().getUsers() != null) {
                return getList().getUsers().size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return getList().getUsers().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            FriendsListFragment.ViewHolder holder;
            if (convertView == null) {
                holder = new FriendsListFragment.ViewHolder();
                convertView = inflater.inflate(R.layout.fragment_listview_item_comments_layout, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                convertView.setTag(holder);
            } else {
                holder = (FriendsListFragment.ViewHolder) convertView.getTag();
            }

            bindViewData(holder, position);


            return convertView;
        }

        private void bindViewData(FriendsListFragment.ViewHolder holder, int position) {

            UserBean user = getList().getUsers().get(position);


            holder.username.setText(user.getScreen_name());
            String image_url = user.getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, user.getProfile_image_url(), position, listView);
            }
            holder.time.setVisibility(View.GONE);
            holder.content.setText(user.getDescription());

        }

    }

    class UserListGetNewDataTask extends AsyncTask<Void, UserListBean, UserListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_progress).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
            listView.setSelection(0);
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
            if (newValue != null) {
                if (newValue.getUsers().size() == 0) {

                } else {

                    if (newValue.getUsers().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        newValue.getUsers().addAll(getList().getUsers());
                    }

                    bean = newValue;
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();
                    headerView.findViewById(R.id.header_progress).clearAnimation();

                }
            }
            cleanWork();
            getActivity().invalidateOptionsMenu();
            super.onPostExecute(newValue);

        }

        private void cleanWork() {
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            isBusying = false;
            if (bean.getUsers().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        }
    }


    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }


    class UserListGetOlderDataTask extends AsyncTask<Void, UserListBean, UserListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.loading));
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));

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
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
            if (this.e != null)
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            cleanWork();

        }

        @Override
        protected void onPostExecute(UserListBean newValue) {
            if (newValue != null && newValue.getUsers().size() > 1) {
                List<UserBean> list = newValue.getUsers();
                getList().getUsers().addAll(list.subList(1, list.size() - 1));

            }

            cleanWork();
            timeLineAdapter.notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();
            super.onPostExecute(newValue);
        }


        private void cleanWork() {
            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
        }
    }

    protected abstract UserListBean getDoInBackgroundNewData() throws WeiboException;

    protected abstract UserListBean getDoInBackgroundOldData() throws WeiboException;


}
