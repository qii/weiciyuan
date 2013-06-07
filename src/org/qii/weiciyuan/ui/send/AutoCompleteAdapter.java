package org.qii.weiciyuan.ui.send;

import android.app.Activity;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.bean.AtUserBean;
import org.qii.weiciyuan.dao.search.AtUserDao;
import org.qii.weiciyuan.support.lib.WeiboPatterns;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * User: qii
 * Date: 13-6-7
 */
public class AutoCompleteAdapter extends ArrayAdapter<AtUserBean> implements Filterable {

    private Activity activity;
    private AutoCompleteTextView content;
    private ProgressBar pb;
    private List<AtUserBean> data;
    private int res;

    private int selectPosition = -1;
    private int atSignPosition = -1;

    public AutoCompleteAdapter(Activity context, AutoCompleteTextView content, ProgressBar pb) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        data = new ArrayList<AtUserBean>();
        this.activity = context;
        this.res = android.R.layout.simple_dropdown_item_1line;
        this.pb = pb;
        this.content = content;
        this.content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String searchFetchedWord = getItem(position).getNickname();
                int searchFetchedWordLength = searchFetchedWord.length();
                int calcResultSelectionPosition = atSignPosition + searchFetchedWordLength;
                AutoCompleteAdapter.this.content.setSelection(calcResultSelectionPosition + 2);
            }
        });
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public AtUserBean getItem(int index) {
        return data.get(index);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        TextView text;

        if (convertView == null) {
            view = activity.getLayoutInflater().inflate(res, parent, false);
        } else {
            view = convertView;
        }

        text = (TextView) view;
        text.setText(getItem(position).getNickname());
        return view;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            //AutoCompleteTextView is empty, return empty;
            if (TextUtils.isEmpty(constraint)) {
                return filterResults;
            }

            selectPosition = content.getSelectionStart();

            if (selectPosition == 0) {
                return filterResults;
            }
            String contentStr = constraint.toString();

            String search = contentStr.substring(0, selectPosition);
            //must contain @ sign
            if (!search.contains("@")) {
                return filterResults;
            }

            int start = search.lastIndexOf("@");
            if (start == search.length() - 1) {
                return filterResults;
            }
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    pb.setVisibility(View.VISIBLE);
                }
            });
            String q = "";
            Matcher localMatcher = WeiboPatterns.MENTION_URL.matcher(search);

            while (localMatcher.find()) {
                String str2 = localMatcher.group(0);
                int k = localMatcher.start();
                int m = localMatcher.end();
                if (m == selectPosition) {
                    q = str2.substring(1);
                    atSignPosition = k;
                }
            }

            AtUserDao dao = new AtUserDao(GlobalContext.getInstance().getSpecialToken(), q);
//            SearchDao dao = new SearchDao(GlobalContext.getInstance().getSpecialToken(), q);

            try {
//                data = dao.getUserList().getUsers();
                data = dao.getUserInfo();
            } catch (Exception e) {
            }
            // Now assign the values and count to the FilterResults object
            filterResults.values = data;
            filterResults.count = data.size();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(View.GONE);
                    //                                    int pos = content.getSelectionStart();
                    int pos = atSignPosition;
                    Layout layout = content.getLayout();
                    int line = layout.getLineForOffset(pos);
                    int baseline = layout.getLineBaseline(line);
                    int ascent = layout.getLineAscent(line);
                    float x = layout.getPrimaryHorizontal(pos);
                    float y = baseline + ascent;
                    int height = content.getBottom();
                    content.setDropDownVerticalOffset(-(int) (height - y) + Utility.dip2px(24));
                    content.setDropDownHorizontalOffset((int) x);
                    content.setDropDownWidth(Utility.getScreenWidth() / 2);

                }
            });


            return filterResults;
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String ori = content.getText().toString();
            String result = ((AtUserBean) resultValue).getNickname();
            String left = ori.substring(0, atSignPosition + 1);
            String right = ori.substring(selectPosition);
            ori = left + result + " " + right;
            return ori;
        }

        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}