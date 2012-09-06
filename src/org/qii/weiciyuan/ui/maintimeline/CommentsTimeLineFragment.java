package org.qii.weiciyuan.ui.maintimeline;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> {

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        ((MainTimeLineActivity) getActivity()).setCommentsListView(listView);
        if (savedInstanceState != null && (bean == null || bean.getComments().size() == 0)) {
            bean = (CommentListBean) savedInstanceState.getSerializable("bean");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            new SimpleTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        }

    }


    private class SimpleTask extends MyAsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            bean = DatabaseManager.getInstance().getCommentLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid());
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
    protected void buildListAdapter() {
        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
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
                TextPaint tp = holder.username.getPaint();
                tp.setFakeBoldText(true);
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

            final CommentBean msg = getList().getComments().get(position);
            MessageBean repost_msg = msg.getStatus();


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
            holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
            holder.content.setText(msg.getListViewSpannableString());

            String time = msg.getListviewItemShowTime();
            UpdateString updateString = new UpdateString(time, holder.time, msg, getActivity());
            if (!holder.time.getText().toString().equals(time)) {
                holder.time.setText(updateString);
            }
            holder.time.setTag(msg.getId());

            holder.repost_content.setVisibility(View.GONE);
            holder.repost_content_pic.setVisibility(View.GONE);
            holder.content_pic.setVisibility(View.GONE);

            if (repost_msg != null) {
                buildRepostContent(repost_msg, holder, position);
            }


        }

        private void buildRepostContent(MessageBean repost_msg, ViewHolder holder, int position) {
            holder.repost_content.setVisibility(View.VISIBLE);
            if (repost_msg.getUser() != null) {
                holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());
                holder.repost_content.setText(repost_msg.getListViewSpannableString());
            } else {
                holder.repost_content.setText(repost_msg.getText());

            }
            if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic())) {
                holder.repost_content_pic.setVisibility(View.VISIBLE);
                String picUrl;
                if (GlobalContext.getInstance().getEnableBigPic()) {
                    picUrl = repost_msg.getBmiddle_pic();
                } else {
                    picUrl = repost_msg.getThumbnail_pic();
                }
                downContentPic(holder.repost_content_pic, picUrl, position, listView);
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


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getComments().get(position).getStatus());
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
        startActivity(intent);
    }


    protected void downloadAvatar(ImageView view, String url, int position, ListView listView) {
        commander.downloadAvatar(view, url, position, listView);
    }

    protected void downContentPic(ImageView view, String url, int position, ListView listView) {
        commander.downContentPic(view, url, position, listView);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);

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


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList() != null && getList().getComments().size() > 0) {
            dao.setSince_id(getList().getComments().get(0).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        if (result != null) {
            if (result.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                DatabaseManager.getInstance().addCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            } else {
                DatabaseManager.getInstance().replaceCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            }
        }
        return result;
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getComments().size() > 0) {
            dao.setMax_id(getList().getComments().get(getList().getComments().size() - 1).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            if (newValue.getComments().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getComments().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    newValue.getComments().addAll(getList().getComments());
                }

                bean = newValue;
                timeLineAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
            }
        }
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.comments));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            getList().getComments().addAll(newValue.getComments().subList(1, newValue.getComments().size() - 1));

        }
    }
}
