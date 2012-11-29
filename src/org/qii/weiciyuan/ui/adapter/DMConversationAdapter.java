package org.qii.weiciyuan.ui.adapter;

import android.app.Activity;
import android.app.Fragment;
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
import org.qii.weiciyuan.bean.DMBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.ICommander;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-15
 */
public class DMConversationAdapter extends BaseAdapter {
    private List<DMBean> bean;
    private Fragment fragment;
    private LayoutInflater inflater;
    private ListView listView;
    private ICommander commander;


    public DMConversationAdapter(Fragment fragment, ICommander commander,  List<DMBean> bean, ListView listView) {
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
        convertView = inflater.inflate(R.layout.dm_conversation_list_listview_item_layout, parent, false);

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
        holder.time.setTextSize(SettingUtility.getFontSize() - 3);
        holder.content.setTextSize(SettingUtility.getFontSize());
        holder.username.setTextSize(SettingUtility.getFontSize());

    }

    protected void bindViewData(DMViewHolder holder, int position) {

        final DMBean msg = bean.get(position);
        UserBean user = msg.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            buildUsername(holder, user);

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

    private void buildUsername(DMViewHolder holder, UserBean user) {

        if (!TextUtils.isEmpty(user.getRemark())) {
            holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
        } else {
            holder.username.setText(user.getScreen_name());
        }
    }


    protected  List<DMBean> getList() {
        return bean;
    }

    public boolean hasStableIds() {
        return true;
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
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().get(position) != null && getList().size() > 0 && position < getList().size())
            return Long.valueOf(getList().get(position).getId());
        else
            return -1;
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            //when listview is flying,app dont download avatar and picture
            boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

            String url;
            if (SettingUtility.getEnableBigAvatar()) {
                url = user.getAvatar_large();
            } else {
                url = user.getProfile_image_url();
            }
            commander.downloadAvatar(view, url, position, listView, isFling);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
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