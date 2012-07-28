package org.qii.weiciyuan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.MentionsTimeLineMsg;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class MentionsTimeLineActivity extends Activity {

    private ListView listView;

    private List<Map<String, String>> list=new ArrayList<Map<String, String>>();

    private TimeLineAdapter timeLineAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timeline);
        listView = (ListView) findViewById(R.id.listView);
        timeLineAdapter=new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);


        Intent intent = getIntent();

        String token = intent.getStringExtra("token");
        String expires = intent.getStringExtra("expires");
        String username = intent.getStringExtra("username");

        if (TextUtils.isEmpty(username))
            setTitle(username);

        GlobalContext.getInstance().setToken(token);
        GlobalContext.getInstance().setExpires(expires);


        new AsyncTask<Void, List<Map<String, String>>, List<Map<String, String>>>() {


            @Override
            protected List<Map<String, String>> doInBackground(Void... params) {

                return new MentionsTimeLineMsg().getMsgList();

            }

            @Override
            protected void onPostExecute(List<Map<String, String>> o) {
                ((TextView) findViewById(R.id.tvResult)).setText(o.get(0).get("text"));
                list=o;
                timeLineAdapter.notifyDataSetChanged();

                super.onPostExecute(o);
            }
        }.execute();
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

            LayoutInflater inflater = getLayoutInflater();

            View view = inflater.inflate(R.layout.mentionstimeline_item, parent, false);

            TextView screenName = (TextView)view.findViewById(R.id.username);
            TextView txt=(TextView)view.findViewById(R.id.content);

            screenName.setText(list.get(position).get("id"));

            txt.setText(list.get(position).get("text"));

            return view;

        }
    }
}
