package org.qii.weiciyuan.ui.adapter;

import android.content.res.TypedArray;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.Abstract.ICommander;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-19
 */
public class UserListAdapter extends BaseAdapter {

    protected List<UserBean> bean;
    protected Fragment activity;
    protected LayoutInflater inflater;
    protected ListView listView;
    protected ICommander commander;
    protected int checkedBG;
    protected int defaultBG;

    public UserListAdapter(Fragment activity, ICommander commander, List<UserBean> bean, ListView listView) {
        this.bean = bean;
        this.commander = commander;
        this.inflater = activity.getActivity().getLayoutInflater();
        this.listView = listView;
        this.activity = activity;

        defaultBG = activity.getResources().getColor(R.color.transparent);

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = activity.getActivity().obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);
    }

    private List<UserBean> getList() {
        return bean;
    }

    @Override
    public int getCount() {

        if (getList() != null && getList().size() > 0) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return getList().get(position);
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
            convertView = inflater.inflate(R.layout.fragment_listview_item_comments_layout, parent, false);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        bindViewData(holder, position);


        return convertView;
    }

    private void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);


        UserBean user = getList().get(position);


        holder.username.setText(user.getScreen_name());
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            commander.downloadAvatar(holder.avatar, user.getProfile_image_url(), position, listView, false);
        }
        holder.time.setVisibility(View.GONE);
        holder.content.setText(user.getDescription());

    }

    private class ViewHolder {
        TextView username;
        TextView content;
        TextView time;
        ImageView avatar;
        RelativeLayout listview_root;
    }

}