package org.qii.weiciyuan.ui.userinfo;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusesListAdapter extends BaseAdapter {

    Activity activity;
    LayoutInflater inflater;
    MessageListBean bean;
    ListView listView;
    ICommander commander;

    public StatusesListAdapter(Activity activity, ICommander commander, MessageListBean bean, ListView listView) {
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        this.bean = bean;
        this.commander = commander;
        this.listView = listView;

    }


    @Override
    public int getCount() {

        if (bean != null && bean.getStatuses() != null) {
            return bean.getStatuses().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return bean.getStatuses().get(position);
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

        final MessageBean msg = bean.getStatuses().get(position);
        MessageBean repost_msg = msg.getRetweeted_status();

        if (msg.getUser() != null) {
            holder.username.setVisibility(View.VISIBLE);
            holder.username.setText(msg.getUser().getScreen_name());
            String image_url = msg.getUser().getProfile_image_url();
            if (!TextUtils.isEmpty(image_url) && GlobalContext.getInstance().isEnablePic()) {
                holder.avatar.setVisibility(View.VISIBLE);
                commander.downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
            } else {
                holder.avatar.setVisibility(View.GONE);
            }
        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
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
        } else if (!TextUtils.isEmpty(msg.getThumbnail_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildContentPic(msg, holder, position);
        }

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UserInfoActivity.class);
                intent.putExtra("token", ((IToken) activity).getToken());
                intent.putExtra("user", msg.getUser());
                activity.startActivity(intent);
            }
        });
    }

    private void buildRepostContent(final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        if (repost_msg.getUser() != null) {
            holder.repost_content.setText(repost_msg.getUser().getScreen_name() + "：" + repost_msg.getText());
        } else {
            holder.repost_content.setText(repost_msg.getText());

        }
        if (!TextUtils.isEmpty(repost_msg.getThumbnail_pic()) && GlobalContext.getInstance().isEnablePic()) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            commander.downContentPic(holder.repost_content_pic, repost_msg.getThumbnail_pic(), position, listView);
            holder.repost_content_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PictureDialogFragment progressFragment = new PictureDialogFragment(repost_msg);
                    progressFragment.show(activity.getFragmentManager(), "");
                }
            });
        }
    }

    private void buildContentPic(final MessageBean msg, ViewHolder holder, int position) {
        final String main_thumbnail_pic_url = msg.getThumbnail_pic();
        holder.content_pic.setVisibility(View.VISIBLE);
        commander.downContentPic(holder.content_pic, main_thumbnail_pic_url, position, listView);
        holder.content_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureDialogFragment progressFragment = new PictureDialogFragment(msg);
                progressFragment.show(activity.getFragmentManager(), "");
            }
        });
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
