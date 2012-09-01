package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.dao.timeline.RepostsTimeLineByIdDao;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IWeiboMsgInfo;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.send.RepostNewActivity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-13
 * Time: 下午10:03
 */
public class RepostsByIdTimeLineFragment extends Fragment {

    protected View headerView;
    protected View footerView;
    public volatile boolean isBusying = false;
    protected ICommander commander;
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected TimeLineAdapter timeLineAdapter;
    protected RepostListBean bean = new RepostListBean();

    public RepostListBean getList() {
        return bean;
    }

    private String token;
    private String id;

    public RepostsByIdTimeLineFragment(String token, String id) {
        this.token = token;
        this.id = id;
    }

    public RepostsByIdTimeLineFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
    }


    protected void refreshLayout(RepostListBean bean) {
        if (bean.getReposts().size() > 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            empty.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            if (bean.getReposts().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            }
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
        if (savedInstanceState != null && bean.getReposts().size() == 0) {
            bean = (RepostListBean) savedInstanceState.getSerializable("bean");
            token = savedInstanceState.getString("token");
            id = savedInstanceState.getString("id");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            new SimpleTask().execute();

        }

    }

    private class SimpleTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            RepostListBean newValue = new RepostsTimeLineByIdDao(token, id).getGSONMsgList();
            if (newValue != null) {
                bean = newValue;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            invlidateTabText();
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

        if (bean.getReposts().size() == 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getReposts().size()) {

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

            if (getList() != null && getList().getReposts() != null) {
                return getList().getReposts().size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return getList().getReposts().get(position);
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

            MessageBean msg = getList().getReposts().get(position);


            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            }
            holder.time.setText(msg.getListviewItemShowTime());
            holder.content.setText(msg.getText());

        }

    }

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;

    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

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

            case R.id.repostsbyidtimelinefragment_repost:
                Intent intent = new Intent(getActivity(), RepostNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", id);
                intent.putExtra("msg", ((IWeiboMsgInfo) getActivity()).getMsg());
                startActivity(intent);
                break;

            case R.id.repostsbyidtimelinefragment_repost_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class FriendsTimeLineGetNewMsgListTask extends AsyncTask<Void, RepostListBean, RepostListBean> {

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
        protected RepostListBean doInBackground(Void... params) {

            RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);

            if (getList().getReposts().size() > 0) {
                dao.setSince_id(getList().getReposts().get(0).getId());
            }
            RepostListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(RepostListBean newValue) {
            if (newValue != null) {
                if (newValue.getReposts().size() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), getString(R.string.total) + newValue.getReposts().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                    if (newValue.getReposts().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        newValue.getReposts().addAll(getList().getReposts());
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
            if (bean.getReposts().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
            invlidateTabText();

            super.onPostExecute(newValue);

        }
    }

    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(2);
            String name = tab.getText().toString();
            String num = "(" + bean.getReposts().size() + ")";
            if (!name.endsWith(")")) {
                tab.setText(name + num);
            } else {
                int index = name.indexOf("(");
                String newName = name.substring(0, index);
                tab.setText(newName + num);
            }
        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends AsyncTask<Void, RepostListBean, RepostListBean> {
        @Override
        protected void onPreExecute() {
            showListView();
            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.loading));
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
        protected RepostListBean doInBackground(Void... params) {

            RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);
            if (getList().getReposts().size() > 0) {
                dao.setMax_id(getList().getReposts().get(getList().getReposts().size() - 1).getId());
            }
            RepostListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(RepostListBean newValue) {
            if (newValue != null && newValue.getReposts().size() > 1) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getReposts().size() + getString(R.string.old_messages), Toast.LENGTH_SHORT).show();
                List<MessageBean> list = newValue.getReposts();
                getList().getReposts().addAll(list.subList(1, list.size() - 1));

            }

            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            timeLineAdapter.notifyDataSetChanged();
            invlidateTabText();
            super.onPostExecute(newValue);
        }
    }

    private void showListView() {
        empty.setVisibility(View.INVISIBLE);
        listView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}

