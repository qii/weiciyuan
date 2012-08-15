package org.qii.weiciyuan.ui.maintimeline;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-7-29
 * Time: 下午12:14
 */
public abstract class AbstractTimeLineFragment extends Fragment {
    protected ListView listView;
    protected TextView empty;
    protected ProgressBar progressBar;


    protected TimeLineAdapter timeLineAdapter;
    protected MessageListBean bean = new MessageListBean();
    protected View headerView;
    protected View footerView;
    public volatile boolean isBusying = false;
    protected ICommander commander;


    public MessageListBean getList() {
        return bean;
    }


    protected abstract void listViewItemClick(AdapterView parent, View view, int position, long id);

    protected abstract void listViewFooterViewClick(View view);

    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }

    protected void downContentPic(ImageView view, String url, int position, ListView listView) {
        commander.downContentPic(view, url, position, listView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
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

        if (bean.getStatuses().size() == 0) {

            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getStatuses().size()) {

                    listViewItemClick(parent, view, position - 1, id);
                } else {

                    listViewFooterViewClick(view);
                }
            }
        });
        return view;
    }

    protected void refreshLayout(MessageListBean bean) {
        if (bean.getStatuses().size() > 0) {
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

    protected class TimeLineAdapter extends BaseAdapter {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        @Override
        public int getCount() {

            if (getList() != null && getList().getStatuses() != null) {
                return getList().getStatuses().size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return getList().getStatuses().get(position);
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
                convertView = inflater.inflate(R.layout.fragment_listview_item_layout, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.content_pic = (ImageView) convertView.findViewById(R.id.content_pic);
                holder.repost_content_pic = (ImageView) convertView.findViewById(R.id.repost_content_pic);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            bindViewData(holder, position);


            return convertView;
        }

        private void bindViewData(ViewHolder holder, int position) {

            final WeiboMsgBean msg = getList().getStatuses().get(position);
            WeiboMsgBean repost_msg = msg.getRetweeted_status();


            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            }

            holder.content.setText(msg.getText());

            if (!TextUtils.isEmpty(msg.getListviewItemShowTime())) {
                holder.time.setText(msg.getListviewItemShowTime());
            } else {
                holder.time.setText(msg.getCreated_at());
            }


            holder.repost_content.setVisibility(View.GONE);
            holder.repost_content_pic.setVisibility(View.GONE);
            holder.content_pic.setVisibility(View.GONE);

            if (repost_msg != null) {
                buildRepostContent(repost_msg, holder, position);
            } else if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
                buildContentPic(msg, holder, position);
            }

            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
                    intent.putExtra("user", msg.getUser());
                    startActivity(intent);
                }
            });
        }

        private void buildRepostContent(WeiboMsgBean repost_msg, ViewHolder holder, int position) {
            holder.repost_content.setVisibility(View.VISIBLE);
            if (repost_msg.getUser() != null) {
                holder.repost_content.setText(repost_msg.getUser().getScreen_name() + "：" + repost_msg.getText());
            } else {
                holder.repost_content.setText(repost_msg.getText());

            }
            if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic())) {
                holder.repost_content_pic.setVisibility(View.VISIBLE);
                downContentPic(holder.repost_content_pic, repost_msg.getThumbnail_pic(), position, listView);
                holder.repost_content_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void buildContentPic(WeiboMsgBean msg, ViewHolder holder, int position) {
            String main_thumbnail_pic_url = msg.getThumbnail_pic();
            holder.content_pic.setVisibility(View.VISIBLE);
            downContentPic(holder.content_pic, main_thumbnail_pic_url, position, listView);
            holder.content_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    static class ViewHolder {
        TextView username;
        TextView content;
        TextView repost_content;
        TextView time;
        ImageView avatar;
        ImageView content_pic;
        ImageView repost_content_pic;
    }

    protected abstract MessageListBean getDoInBackgroundNewData();

    protected class TimeLineGetNewMsgListTask extends AsyncTask<Object, MessageListBean, MessageListBean> {


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
        protected MessageListBean doInBackground(Object... params) {

            return getDoInBackgroundNewData();

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null) {
                if (newValue.getStatuses().size() == 0) {
                    Toast.makeText(getActivity(), "no new message", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "total " + newValue.getStatuses().size() + " new messages", Toast.LENGTH_SHORT).show();
                    if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        newValue.getStatuses().addAll(getList().getStatuses());
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
            if (bean.getStatuses().size() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
            afterGetNewMsg();
            super.onPostExecute(newValue);
        }
    }

    protected abstract MessageListBean getDoInBackgroundOldData();

    protected void afterGetNewMsg() {

    }

    ;

    class TimeLineGetOlderMsgListTask extends AsyncTask<Object, MessageListBean, MessageListBean> {

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
        protected MessageListBean doInBackground(Object... params) {

            return getDoInBackgroundOldData();

        }

        @Override
        protected void onPostExecute(MessageListBean newValue) {
            if (newValue != null) {
                Toast.makeText(getActivity(), "total " + newValue.getStatuses().size() + " old messages", Toast.LENGTH_SHORT).show();

                getList().getStatuses().addAll(newValue.getStatuses().subList(1, newValue.getStatuses().size() - 1));

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
