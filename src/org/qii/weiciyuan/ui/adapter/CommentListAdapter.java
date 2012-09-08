package org.qii.weiciyuan.ui.adapter;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends BaseAdapter {

    FragmentActivity activity;
    LayoutInflater inflater;
    List<CommentBean> bean;
    ListView listView;
    ICommander commander;
    boolean showOriStatus = true;

    public CommentListAdapter(FragmentActivity activity, ICommander commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        this.activity = activity;
        this.inflater = activity.getLayoutInflater();
        this.bean = bean;
        this.commander = commander;
        this.listView = listView;
        this.showOriStatus = showOriStatus;

    }

    private List<CommentBean> getList() {
        return bean;
    }


    @Override
    public int getCount() {

        if (getList() != null) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
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
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
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

        final CommentBean msg = getList().get(position);
        MessageBean repost_msg = msg.getStatus();


        holder.username.setText(msg.getUser().getScreen_name());
        String image_url = msg.getUser().getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            commander.downloadAvatar(holder.avatar, msg.getUser().getProfile_image_url(), position, listView);
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
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.content.setText(msg.getListViewSpannableString());

        String time = msg.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, msg, activity);
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(msg.getId());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);
        holder.content_pic.setVisibility(View.GONE);

        if (repost_msg != null && showOriStatus) {
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
            commander.downContentPic(holder.repost_content_pic, picUrl, position, listView);
        }
    }


}


class ViewHolder {
    TextView username;
    TextView content;
    TextView repost_content;
    TextView time;
    ImageView avatar;
    ImageView content_pic;
    ImageView repost_content_pic;
}