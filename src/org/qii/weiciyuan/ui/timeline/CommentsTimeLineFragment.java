package org.qii.weiciyuan.ui.timeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.dao.CommentsTimeLineMsgDao;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.ProgressFragment;

import java.util.Map;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: qii
* Date: 12-7-29
* Time: 下午1:15
* To change this template use File | Settings | File Templates.
*/
public class CommentsTimeLineFragment extends Fragment {
    protected ListView listView;
    protected TimeLineAdapter timeLineAdapter;

    protected MainTimeLineActivity activity;

    protected CommentListBean bean = new CommentListBean();

    protected int position = 0;



    public CommentListBean getList() {
        return bean;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainTimeLineActivity) getActivity();
        setHasOptionsMenu(true);
        setRetainInstance(true);
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
                        position = view.getFirstVisiblePosition();

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

                if (position < getList().getComments().size()) {
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

            if (getList() != null && getList().getComments() != null) {
                return getList().getComments().size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return getList().getComments().get(position);
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

            CommentBean msg = getList().getComments().get(position);
            WeiboMsgBean repost_msg = msg.getStatus();


            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url)) {
                downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            }

            holder.content.setText(msg.getText());



            holder.repost_content.setVisibility(View.GONE);
            holder.repost_content_pic.setVisibility(View.GONE);
            holder.content_pic.setVisibility(View.GONE);

            if (repost_msg != null) {
                buildRepostContent(repost_msg, holder, position);
            }


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
            }
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

    public volatile boolean isBusying = false;

    private FriendsTimeLineFragment.Commander commander;

    public CommentsTimeLineFragment() {
//        bean = DatabaseManager.getInstance().getHomeLineMsgList();
    }


    public CommentsTimeLineFragment setCommander(FriendsTimeLineFragment.Commander commander) {
        this.commander = commander;
        return this;
    }

     protected void scrollToBottom() {

    }

     public void listViewItemLongClick(AdapterView parent, View view, int position, long id) {
        view.setSelected(true);
        new MyAlertDialogFragment().setView(view).setPosition(position).show(getFragmentManager(), "");
    }

     protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

    }


     protected void listViewFooterViewClick(View view) {
        if (!isBusying) {

            new FriendsTimeLineGetOlderMsgListTask(view).execute();

        }
    }

     protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }

     protected void downContentPic(ImageView view, String url, int position, ListView listView) {
        commander.downContentPic(view, url, position, listView);
    }


    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((MainTimeLineActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();
        Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = ((MainTimeLineActivity) getActivity()).getPictureBitmapWorkerTaskMap();


        new FriendsTimeLineGetNewMsgListTask().execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }

        Set<String> pKeys = pictureBitmapWorkerTaskMap.keySet();
        for (String pkey : pKeys) {
            pictureBitmapWorkerTaskMap.get(pkey).cancel(true);
            pictureBitmapWorkerTaskMap.remove(pkey);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
        menu.add("weibo dont have messages group api");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mentionstimelinefragment_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    class MyAlertDialogFragment extends DialogFragment {
        View view;
        int position;

        @Override
        public void onCancel(DialogInterface dialog) {
            view.setSelected(false);
        }

        public MyAlertDialogFragment setView(View view) {
            this.view = view;
            return this;
        }

        public MyAlertDialogFragment setPosition(int position) {
            this.position = position;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {"刷新", "回复"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select))
                    .setItems(items, onClickListener);

            return builder.create();
        }

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:

                        break;
                    case 1:

                        break;
                }
            }
        };
    }


    class FriendsTimeLineGetNewMsgListTask extends AsyncTask<Void, CommentListBean, CommentListBean> {

        DialogFragment dialogFragment = new ProgressFragment();

        @Override
        protected void onPreExecute() {

            dialogFragment.show(getActivity().getSupportFragmentManager(), "");
        }

        @Override
        protected CommentListBean doInBackground(Void... params) {
            CommentsTimeLineMsgDao dao = new CommentsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getComments().size() > 0) {
                dao.setSince_id(getList().getComments().get(0).getId());
            }
            CommentListBean result = dao.getGSONMsgList();
            if (result != null) {
                if (result.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
//                    DatabaseManager.getInstance().addHomeLineMsg(result);
                } else {
//                    DatabaseManager.getInstance().replaceHomeLineMsg(result);
                }
            }
            return result;

        }

        @Override
        protected void onPostExecute(CommentListBean newValue) {
            if (newValue != null) {
                if (newValue.getComments().size() == 0) {
                    Toast.makeText(getActivity(), "no new message", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "total " + newValue.getComments().size() + " new messages", Toast.LENGTH_SHORT).show();
                    if (newValue.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                        //if position equal 0,listview don't scroll because this is the first time to refresh
                        if (position > 0)
                            position += newValue.getComments().size();
                        newValue.getComments().addAll(getList().getComments());
                    } else {
                        position = 0;
                    }

                    bean = newValue;
                    timeLineAdapter.notifyDataSetChanged();
                    listView.setSelectionAfterHeaderView();

                }
            }
            dialogFragment.dismissAllowingStateLoss();
            super.onPostExecute(newValue);
        }
    }


    class FriendsTimeLineGetOlderMsgListTask extends AsyncTask<Void, CommentListBean, CommentListBean> {
        View footerView;

        public FriendsTimeLineGetOlderMsgListTask(View view) {
            footerView = view;
        }

        @Override
        protected void onPreExecute() {
            isBusying = true;

            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("loading");

        }

        @Override
        protected CommentListBean doInBackground(Void... params) {

            CommentsTimeLineMsgDao dao = new CommentsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getComments().size() > 0) {
                dao.setMax_id(getList().getComments().get(getList().getComments().size() - 1).getId());
            }
            CommentListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(CommentListBean newValue) {
            if (newValue != null) {
                Toast.makeText(getActivity(), "total " + newValue.getComments().size() + " old messages", Toast.LENGTH_SHORT).show();

                getList().getComments().addAll(newValue.getComments().subList(1, newValue.getComments().size() - 1));

            }

            isBusying = false;
            ((TextView) footerView.findViewById(R.id.listview_footer)).setText("click to load older message");
            timeLineAdapter.notifyDataSetChanged();
            super.onPostExecute(newValue);
        }
    }
}
