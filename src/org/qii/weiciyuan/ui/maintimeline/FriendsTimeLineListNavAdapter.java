package org.qii.weiciyuan.ui.maintimeline;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 13-2-11
 */
public class FriendsTimeLineListNavAdapter extends BaseAdapter {
    private Activity activity;
    private String[] valueArray;

    public FriendsTimeLineListNavAdapter(Activity activity, String[] valueArray) {
        this.activity = activity;
        this.valueArray = valueArray;
    }

    @Override
    public int getCount() {
        return valueArray.length;
    }

    @Override
    public Object getItem(int position) {
        return valueArray[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_selector_text_view, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position != 0) {
            holder.textView.setText(valueArray[position]);
        } else {
            AccountBean accountBean = GlobalContext.getInstance().getAccountBean();
            holder.textView.setText(accountBean.getUsernick());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(valueArray[position]);
        return convertView;

    }

    private static class ViewHolder {
        TextView textView;
    }
};