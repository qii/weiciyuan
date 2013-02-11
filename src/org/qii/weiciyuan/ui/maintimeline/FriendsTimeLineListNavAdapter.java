package org.qii.weiciyuan.ui.maintimeline;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
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
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.spinner_selector_text_view, parent, false);
        if (position != 0) {
            ((TextView) view).setText(valueArray[position]);
        } else {
            ((TextView) view).setText(GlobalContext.getInstance().getCurrentAccountName());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        ((CheckedTextView) view).setText(valueArray[position]);
        return view;
    }
};