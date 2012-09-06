package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.dao.timeline.RepostsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IWeiboMsgInfo;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.send.RepostNewActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-8-13
 */
public class RepostsByIdTimeLineFragment extends Fragment {

    protected View headerView;
    protected View footerView;
    protected ICommander commander;
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;
    protected TimeLineAdapter timeLineAdapter;
    protected RepostListBean bean = new RepostListBean();
    private MessageBean msg;

    private FriendsTimeLineGetNewMsgListTask newTask;
    private FriendsTimeLineGetOlderMsgListTask oldTask;

    private EditText et;


    public RepostListBean getList() {
        return bean;
    }

    private String token;
    private String id;

    public RepostsByIdTimeLineFragment(String token, String id, MessageBean msg) {
        this.token = token;
        this.id = id;
        this.msg = msg;
    }

    public RepostsByIdTimeLineFragment() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (newTask != null)
            newTask.cancel(true);

        if (oldTask != null)
            oldTask.cancel(true);
    }

    public void load() {
        if ((bean == null || bean.getReposts().size() == 0) && newTask == null) {

            refresh();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
        outState.putSerializable("msg", msg);
    }


    private boolean canSend() {

        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveToken) {
                Toast.makeText(getActivity(), getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
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
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
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
        View view = inflater.inflate(R.layout.fragment_repost_listview_layout, container, false);
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

                if (position - 1 < getList().getReposts().size() && position - 1 >= 0) {

                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getReposts().size()) {

                    listViewFooterViewClick(view);
                }
            }
        });

        if (savedInstanceState == null && msg != null) {
            if (msg.getRetweeted_status() == null) {
                view.findViewById(R.id.quick_repost).setVisibility(View.VISIBLE);
            }
        } else if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            if (msg.getRetweeted_status() == null) {
                view.findViewById(R.id.quick_repost).setVisibility(View.VISIBLE);
            }
        }

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRepost();
            }
        });

        return view;
    }

    private void sendRepost() {
        if (canSend()) {
            new SimpleTask().execute();
        }
    }


    class SimpleTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        SendProgressFragment progressFragment = new SendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected MessageBean doInBackground(Void... params) {

            String content = et.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = getString(R.string.repost);
            }

            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
            dao.setStatus(content);
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(MessageBean s) {
            super.onCancelled(s);
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(MessageBean s) {
            if (progressFragment != null)
                progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                et.setText("");
                refresh();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
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

            final MessageBean msg = getList().getReposts().get(position);
            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
                holder.avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                        intent.putExtra("token", ((IToken) getActivity()).getToken());
                        intent.putExtra("user", msg.getUser());
                        startActivity(intent);
                    }
                });
            }
            holder.time.setText(msg.getListviewItemShowTime());
            holder.content.setText(msg.getListViewSpannableString());

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
        if (oldTask == null || oldTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            oldTask = new FriendsTimeLineGetOlderMsgListTask();
            oldTask.execute();
        }
    }

    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }


    public void refresh() {
        if (getActivity() == null)
            return;

        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((AbstractAppActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();


        if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            newTask = new FriendsTimeLineGetNewMsgListTask();
            newTask.execute();
        }
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


    class FriendsTimeLineGetNewMsgListTask extends MyAsyncTask<Void, RepostListBean, RepostListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_progress).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));

            listView.setSelection(0);
        }


        @Override
        protected RepostListBean doInBackground(Void... params) {

            RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);

            if (getList().getReposts().size() > 0) {
                dao.setSince_id(getList().getReposts().get(0).getId());
            }
            RepostListBean result = null;
            try {
                result = dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }

            return result;

        }

        @Override
        protected void onCancelled(RepostListBean newValue) {
            super.onCancelled(newValue);
            cleanWork();
            if (this.e != null)
                Toast.makeText(getActivity(), this.e.getError(), Toast.LENGTH_SHORT).show();
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
            cleanWork();
            invlidateTabText();

            super.onPostExecute(newValue);

        }

        private void cleanWork() {
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);
            if (bean.getReposts().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        }
    }

    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(2);
            String num = getString(R.string.repost) + "(" + bean.getReposts().size() + ")";
            tab.setText(num);

        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends MyAsyncTask<Void, RepostListBean, RepostListBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            showListView();

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.loading));
            View view = footerView.findViewById(R.id.refresh);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));


        }

        @Override
        protected RepostListBean doInBackground(Void... params) {

            RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);
            if (getList().getReposts().size() > 0) {
                dao.setMax_id(getList().getReposts().get(getList().getReposts().size() - 1).getId());
            }
            RepostListBean result = null;
            try {
                result = dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }

            return result;

        }

        @Override
        protected void onCancelled(RepostListBean newValue) {
            super.onCancelled(newValue);
            footerView.findViewById(R.id.refresh).clearAnimation();
            footerView.findViewById(R.id.refresh).setVisibility(View.GONE);
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText(getString(R.string.more));
            if (this.e != null)
                Toast.makeText(getActivity(), this.e.getError(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(RepostListBean newValue) {
            if (newValue != null && newValue.getReposts().size() > 1) {
                List<MessageBean> list = newValue.getReposts();
                getList().getReposts().addAll(list.subList(1, list.size() - 1));

            }

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

