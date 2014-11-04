package org.qii.weiciyuan.ui.preference.filter;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.ThemeUtility;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * User: qii
 * Date: 13-6-16
 */
public class FilterAdapter extends BaseAdapter {

    private int checkedBG;
    private int defaultBG;
    private Activity activity;
    private List<String> list;
    private ListView listView;

    public FilterAdapter(Activity activity, ListView listView, List<String> list) {
        defaultBG = activity.getResources().getColor(R.color.transparent);
        checkedBG = ThemeUtility
                .getColor(activity, R.attr.listview_checked_color);
        this.activity = activity;
        this.list = list;
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater()
                .inflate(R.layout.simple_listview_item, parent, false);
        TextView tv = (TextView) view.findViewById(R.id.text1);
        tv.setBackgroundColor(defaultBG);
        if (listView.getCheckedItemPositions().get(position)) {
            tv.setBackgroundColor(checkedBG);
        }
        tv.setText(list.get(position));
        return view;
    }
}