package org.qii.weiciyuan.ui.userinfo;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.dao.user.FanListDao;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FanListFragment extends Fragment {

    protected View headerView;
    protected View footerView;
    public volatile boolean isBusying = false;
    protected ICommander commander;
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected TimeLineAdapter timeLineAdapter;
    protected UserListBean bean = new UserListBean();

    public UserListBean getList() {
        return bean;
    }

    private String token;
    private String uid;

    public FanListFragment(String token, String uid) {
        this.token = token;
        this.uid = uid;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        if (savedInstanceState != null && bean.getUsers().size() == 0) {
            bean = (UserListBean) savedInstanceState.getSerializable("bean");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            new SimpleTask().execute();

        }

    }

    private class SimpleTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            UserListBean newValue = new FanListDao(token, uid).getGSONMsgList();
            if (newValue != null) {
                bean = newValue;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
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

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.fragment_listview_item_comments_layout, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            bindViewData(holder, position);


            return convertView;
        }

        private void bindViewData(ViewHolder holder, int position) {

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

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;

    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("user", bean.getUsers().get(position));
        startActivity(intent);
    }


    protected void listViewFooterViewClick(View view) {
        if (!isBusying) {
            new FriendsTimeLineGetOlderMsgListTask().execute();
        }
    }

    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }


    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();


        new FriendsTimeLineGetNewMsgListTask().execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.repostsbyidtimelinefragment_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.repostsbyidtimelinefragment_repost_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class FriendsTimeLineGetNewMsgListTask extends AsyncTask<Void, UserListBean, UserListBean> {

        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            headerView.findViewById(R.id.header_progress).startAnimation(rotateAnimation);
            listView.setSelection(0);
        }


        @Override
        protected UserListBean doInBackground(Void... params) {

            FanListDao dao = new FanListDao(token, uid);

            if (getList().getUsers().size() > 0) {
                dao.setCursor(bean.getPrevious_cursor());
            }
            UserListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(UserListBean newValue) {
            if (newValue != null) {
                if (newValue.getUsers().size() == 0) {
                    Toast.makeText(getActivity(), "no new message", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "total " + newValue.getUsers().size() + " new messages", Toast.LENGTH_SHORT).show();
                    if (newValue.getUsers().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        newValue.getUsers().addAll(getList().getUsers());
                    }

                    bean = newValue;
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();
                    headerView.findViewById(R.id.header_progress).clearAnimation();

                }
            }
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            isBusying = false;
            if (bean.getUsers().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }

            super.onPostExecute(newValue);

        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends AsyncTask<Void, UserListBean, UserListBean> {
        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("loading");
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);

            Animation rotateAnimation = new RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            view.startAnimation(rotateAnimation);

        }

        @Override
        protected UserListBean doInBackground(Void... params) {

            FanListDao dao = new FanListDao(token, uid);
            if (getList().getUsers().size() > 0) {
                dao.setCursor(bean.getNext_cursor());
            }
            UserListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(UserListBean newValue) {
            if (newValue != null && newValue.getUsers().size() > 1) {
                Toast.makeText(getActivity(), "total " + newValue.getUsers().size() + " old messages", Toast.LENGTH_SHORT).show();
                List<UserBean> list = newValue.getUsers();
                getList().getUsers().addAll(list.subList(1, list.size() - 1));

            }

            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("click to load older message");
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            timeLineAdapter.notifyDataSetChanged();
            super.onPostExecute(newValue);
        }
    }

    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}

