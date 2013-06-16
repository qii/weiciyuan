package org.qii.weiciyuan.ui.preference.filter;

import android.app.Activity;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;

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
        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = activity.obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);
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

        View view = activity.getLayoutInflater().inflate(R.layout.simple_listview_item, parent, false);
        TextView tv = (TextView) view.findViewById(R.id.text1);
        tv.setBackgroundColor(defaultBG);
        if (listView.getCheckedItemPositions().get(position)) {
            tv.setBackgroundColor(checkedBG);
        }
        tv.setText(list.get(position));
        return view;
    }
}