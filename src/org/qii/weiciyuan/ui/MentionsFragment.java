package org.qii.weiciyuan.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.MentionsTimeLineMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 上午12:52
 * To change this template use File | Settings | File Templates.
 */
public class MentionsFragment extends Fragment {

    private ListView listView;

    private List<Map<String, String>> list = new ArrayList<Map<String, String>>();

    private TimeLineAdapter timeLineAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        Bundle args = getArguments();

        View view = inflater.inflate(R.layout.timeline, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);


                new AsyncTask<Void, List<Map<String, String>>, List<Map<String, String>>>() {


                    @Override
                    protected List<Map<String, String>> doInBackground(Void... params) {

                        return new MentionsTimeLineMsg().getMsgList();

                    }

                    @Override
                    protected void onPostExecute(List<Map<String, String>> o) {
                        list = o;
                        timeLineAdapter.notifyDataSetChanged();

                        super.onPostExecute(o);
                    }
                }.execute();

        return view;
    }

    private class TimeLineAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.mentionstimeline_item, parent, false);

            TextView screenName = (TextView) view.findViewById(R.id.username);
            TextView txt = (TextView) view.findViewById(R.id.content);

            screenName.setText(list.get(position).get("screen_name"));

            txt.setText(list.get(position).get("text"));

            return view;

        }
    }
}

