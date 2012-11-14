package org.qii.weiciyuan.ui.adapter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMUserBean;
import org.qii.weiciyuan.bean.DMUserListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.ICommander;
import org.qii.weiciyuan.ui.interfaces.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-11-14
 */
public class DMUserListAdapter extends BaseAdapter {
    private DMUserListBean bean;
    private Fragment fragment;
    private LayoutInflater inflater;
    private ListView listView;
    private ICommander commander;


    public DMUserListAdapter(Fragment fragment, ICommander commander, DMUserListBean bean, ListView listView) {
        this.bean = bean;
        this.commander = commander;
        this.inflater = fragment.getActivity().getLayoutInflater();
        this.listView = listView;
        this.fragment = fragment;

    }

    protected Activity getActivity() {
        return fragment.getActivity();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DMViewHolder holder = null;


        if (convertView == null || convertView.getTag() == null) {

            convertView = initSimpleLayout(parent);
            holder = buildHolder(convertView);
            convertView.setTag(R.drawable.ic_launcher + getItemViewType(position), holder);


        } else {
            holder = (DMViewHolder) convertView.getTag();
        }


        configViewFont(holder);
        bindViewData(holder, position);

        return convertView;
    }


    private View initSimpleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater.inflate(R.layout.timeline_listview_item_simple_layout, parent, false);

        return convertView;
    }


    private DMViewHolder buildHolder(View convertView) {
        DMViewHolder holder = new DMViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.time = (TextView) convertView.findViewById(R.id.time);
        holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
        return holder;
    }

    private void configViewFont(DMViewHolder holder) {
        holder.time.setTextSize(GlobalContext.getInstance().getFontSize() - 3);
        holder.content.setTextSize(GlobalContext.getInstance().getFontSize());
        holder.username.setTextSize(GlobalContext.getInstance().getFontSize());

    }

    protected void bindViewData(DMViewHolder holder, int position) {

        final DMUserBean msg = bean.getItem(position);
        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }

            buildAvatar(holder.avatar, position, user);

        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(msg.getListViewSpannableString())) {
            holder.content.setText(msg.getListViewSpannableString());
        } else {
            ListViewTool.addJustHighLightLinks(msg);
            holder.content.setText(msg.getListViewSpannableString());
        }
        String time = msg.getListviewItemShowTime();

        if (!holder.time.getText().toString().equals(time)) {
            holder.time.setText(time);
        }
        holder.time.setTag(msg.getId());

    }


    protected DMUserListBean getList() {
        return bean;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {

        if (getList() != null) {
            return getList().getSize();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && getList() != null && getList().getSize() > 0 && position < getList().getSize())
            return getList().getItem(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().getItem(position) != null && getList().getSize() > 0 && position < getList().getSize())
            return Long.valueOf(getList().getItem(position).getId());
        else
            return -1;
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            //when listview is flying,app dont download avatar and picture
            boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();
            //50px avatar or 180px avatar
            String url;
            if (GlobalContext.getInstance().getEnableBigAvatar()) {
                url = user.getAvatar_large();
            } else {
                url = user.getProfile_image_url();
            }
            commander.downloadAvatar(view, url, position, listView, isFling);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("token", ((IToken) getActivity()).getToken());
                    intent.putExtra("user", user);
                    getActivity().startActivity(intent);
                }
            });

        } else {
            view.setVisibility(View.GONE);
        }
    }


    private static class DMViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;

    }

}
