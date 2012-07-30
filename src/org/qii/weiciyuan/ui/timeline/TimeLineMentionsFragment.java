package org.qii.weiciyuan.ui.timeline;

import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TimeLineMsgList;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 上午12:52
 * To change this template use File | Settings | File Templates.
 */
public class TimeLineMentionsFragment extends TimeLineAbstractFragment {

    @Override
    protected TimeLineMsgList getList() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
         setHasOptionsMenu(true);
     }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        Bundle args = getArguments();

        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);


//        new AsyncTask<Void, List<Map<String, String>>, List<Map<String, String>>>() {
//
//
//            @Override
//            protected List<Map<String, String>> doInBackground(Void... params) {
//
//                return new TimeLineMentionsMsg().getMsgList();
//
//            }
//
//            @Override
//            protected void onPostExecute(List<Map<String, String>> o) {
//                list = o;
//                timeLineAdapter.notifyDataSetChanged();
//
//                super.onPostExecute(o);
//            }
//        }.execute();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh_timeline:

                break;
        }
        return true;
    }

}

