package org.qii.weiciyuan.ui.timeline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TimeLineMsgListBean;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午12:14
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTimeLineFragment<T> extends Fragment {
    protected ListView listView;
    protected TimeLineAdapter timeLineAdapter;

    protected MainTimeLineActivity activity;

    public abstract void refresh();

    public abstract void refreshAndScrollTo(int position);

    protected abstract TimeLineMsgListBean getList();

    protected abstract void scrollToBottom();

    protected abstract void listViewItemLongClick(AdapterView parent, View view, int position, long id);

    protected abstract void listViewItemClick(AdapterView parent, View view, int position, long id);

    protected abstract void rememberListViewPosition(int position);

    protected abstract void listViewFooterViewClick(View view);

    protected abstract void downloadAvatar(ImageView view, String url);

    protected abstract void downContentPic(ImageView view, String url);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainTimeLineActivity) getActivity();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    public ListView getListView() {
        return listView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setScrollingCacheEnabled(false);
        View footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        listView.addFooterView(footerView);


        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            scrollToBottom();
                        }
                        rememberListViewPosition(view.getFirstVisiblePosition());

                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:

                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }


        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listViewItemLongClick(parent, view, position, id);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position < getList().getStatuses().size()) {
                    listViewItemClick(parent, view, position, id);
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

            WeiboMsgBean msg = getList().getStatuses().get(position);
            WeiboMsgBean repost_msg = msg.getRetweeted_status();


            holder.username.setText(msg.getUser().getScreen_name());
            holder.content.setText(msg.getText());

            if (!TextUtils.isEmpty(msg.getListviewItemShowTime())) {
                holder.time.setText(msg.getListviewItemShowTime());
            } else {
                holder.time.setText(msg.getCreated_at());
            }


            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url());
            }

            holder.repost_content.setVisibility(View.GONE);
            holder.repost_content_pic.setVisibility(View.GONE);
            holder.content_pic.setVisibility(View.GONE);

            if (repost_msg != null) {
                buildRepostContent(repost_msg, holder);
            } else if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
                buildContentPic(msg, holder);
            }


        }

        private void buildRepostContent(WeiboMsgBean repost_msg, ViewHolder holder) {
            holder.repost_content.setVisibility(View.VISIBLE);

            holder.repost_content.setText(repost_msg.getUser().getScreen_name() + "：" + repost_msg.getText());
            if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic())) {
                holder.repost_content_pic.setVisibility(View.VISIBLE);
                downContentPic(holder.repost_content_pic,repost_msg.getThumbnail_pic());
            }
        }

        private void buildContentPic(WeiboMsgBean msg, ViewHolder holder) {
            String main_thumbnail_pic_url = msg.getThumbnail_pic();
            holder.content_pic.setVisibility(View.VISIBLE);
            downContentPic(holder.content_pic,main_thumbnail_pic_url);
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
}
