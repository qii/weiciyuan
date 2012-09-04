package org.qii.weiciyuan.ui.userinfo;

import android.app.Activity;
import android.content.Intent;
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
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusesListAdapter extends BaseAdapter {

    Activity activity;
    LayoutInflater inflater;
    List<MessageBean> bean;
    ListView listView;
    ICommander commander;

    public StatusesListAdapter(Activity activity, ICommander commander, List<MessageBean> bean, ListView listView) {
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        this.bean = bean;
        this.commander = commander;
        this.listView = listView;

    }


    @Override
    public int getCount() {

        if (bean != null && bean.size() != 0) {
            return bean.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return bean.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (bean.get(position).getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            ViewHolder holder = new ViewHolder();
            if (convertView == null || convertView.getTag(R.drawable.app) == null) {
                convertView = inflater.inflate(R.layout.fragment_listview_item_myself_layout, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                TextPaint tp = holder.username.getPaint();
                tp.setFakeBoldText(true);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.content_pic = (ImageView) convertView.findViewById(R.id.content_pic);
                holder.repost_content_pic = (ImageView) convertView.findViewById(R.id.repost_content_pic);
                convertView.setTag(R.drawable.app, holder);
            } else {
                holder = (ViewHolder) convertView.getTag(R.drawable.app);
            }
            bindViewData(holder, position);
            return convertView;
        }

        ViewHolder holder;
        if (convertView == null || convertView.getTag(R.drawable.ic_launcher) == null) {
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
            convertView.setTag(R.drawable.ic_launcher, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher);
        }

        bindViewData(holder, position);


        return convertView;
    }

    private void bindViewData(ViewHolder holder, int position) {

        final MessageBean msg = bean.get(position);
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
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.content.setText(msg.getText());
//        ListViewTool.addJustHighLightLinks(holder.content);
        holder.time.setText(msg.getListviewItemShowTime());


        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);
        holder.content_pic.setVisibility(View.GONE);

        if (repost_msg != null) {
            buildRepostContent(repost_msg, holder, position);
        } else if (!TextUtils.isEmpty(msg.getThumbnail_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildPic(msg, holder.content_pic, position);

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
        holder.repost_content.setTextSize(GlobalContext.getInstance().getFontSize());

        if (repost_msg.getUser() != null) {
            holder.repost_content.setText("@" + repost_msg.getUser().getScreen_name() + "：" + repost_msg.getText());
//            ListViewTool.addJustHighLightLinks(holder.repost_content);
        } else {
            holder.repost_content.setText(repost_msg.getText());

        }
        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
    }


    private void buildPic(final MessageBean msg, ImageView view, int position) {
        final String main_thumbnail_pic_url = msg.getThumbnail_pic();
        view.setVisibility(View.VISIBLE);
        commander.downContentPic(view, main_thumbnail_pic_url, position, listView);
        view.setOnClickListener(new View.OnClickListener() {
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
