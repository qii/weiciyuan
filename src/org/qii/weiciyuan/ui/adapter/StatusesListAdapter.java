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
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.UpdateString;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.widgets.PictureDialogFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-8-19
 */
public class StatusesListAdapter extends BaseAdapter {

    FragmentActivity activity;
    LayoutInflater inflater;
    List<MessageBean> bean;
    ListView listView;
    ICommander commander;

    public StatusesListAdapter(FragmentActivity activity, ICommander commander, List<MessageBean> bean, ListView listView) {
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
        if (bean != null && bean.size() > 0 && position < bean.size())
            return bean.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (bean.get(position).getUser().getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            ViewHolder holder;
            if (convertView == null || convertView.getTag(R.drawable.app) == null) {
                convertView = initMylayout(parent);
                holder = buildHolder(convertView);
            } else {
                boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
                if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                    holder = (ViewHolder) convertView.getTag(R.drawable.app);
                } else {
                    convertView = initMylayout(parent);
                    holder = buildHolder(convertView);
                }
            }
            convertView.setTag(R.drawable.app, holder);
            convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
            bindViewData(holder, position);
            return convertView;
        }

        ViewHolder holder;
        if (convertView == null || convertView.getTag(R.drawable.ic_launcher) == null) {
            convertView = initNormallayout(parent);
            holder = buildHolder(convertView);
        } else {
            boolean enableBigPic = (Boolean) convertView.getTag(R.drawable.account_black);
            if (enableBigPic == GlobalContext.getInstance().getEnableBigPic()) {
                holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher);
            } else {
                convertView = initNormallayout(parent);
                holder = buildHolder(convertView);
            }
        }
        convertView.setTag(R.drawable.ic_launcher, holder);
        convertView.setTag(R.drawable.account_black, GlobalContext.getInstance().getEnableBigPic());
        bindViewData(holder, position);

        return convertView;
    }


    private View initMylayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_myself_layout, parent, false);
        }
        return convertView;
    }

    private View initNormallayout(ViewGroup parent) {
        View convertView;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.fragment_listview_item_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.fragment_listview_item_layout, parent, false);
        }
        return convertView;
    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
        holder.time = (TextView) convertView.findViewById(R.id.time);
        holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        holder.content_pic = (ImageView) convertView.findViewById(R.id.content_pic);
        holder.repost_content_pic = (ImageView) convertView.findViewById(R.id.repost_content_pic);
        return holder;
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
        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            holder.content.setText(msg.getListViewSpannableString());
        } else {
            ListViewTool.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }
        String time = msg.getListviewItemShowTime();
        UpdateString updateString = new UpdateString(time, holder.time, msg, activity);
        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(updateString);
        }
        holder.time.setTag(msg.getId());

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
        holder.repost_content.setText(repost_msg.getListViewSpannableString());

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic()) && GlobalContext.getInstance().isEnablePic()) {
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
    }


    private void buildPic(final MessageBean msg, ImageView view, int position) {
        String picUrl;
        if (GlobalContext.getInstance().getEnableBigPic()) {
            picUrl = msg.getBmiddle_pic();
        } else {
            picUrl = msg.getThumbnail_pic();
        }

        view.setVisibility(View.VISIBLE);
        commander.downContentPic(view, picUrl, position, listView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureDialogFragment progressFragment = new PictureDialogFragment(msg.getBmiddle_pic());
                progressFragment.show(activity.getSupportFragmentManager(), "");
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
