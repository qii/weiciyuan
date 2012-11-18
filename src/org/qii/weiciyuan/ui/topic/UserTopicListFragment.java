package org.qii.weiciyuan.ui.topic;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.topic.UserTopicListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-11-18
 */
public class UserTopicListFragment extends ListFragment {

    private ArrayAdapter<String> adapter;

    private ArrayList<String> result = new ArrayList<String>();

    private UserBean userBean;

    private TopicListTask task;


    public UserTopicListFragment() {

    }

    public UserTopicListFragment(UserBean userBean, ArrayList<String> topicList) {
        this.userBean = userBean;
        this.result = topicList;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Utility.cancelTasks(task);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("userBean", userBean);
        outState.putStringArrayList("topicList", result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            result = (ArrayList<String>) savedInstanceState.getStringArrayList("topicList");
        }
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, result);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = result.get(position);
                String q;
                if (str.startsWith("#") && str.endsWith("#")) {
                    q = str.substring(1, str.length() - 1);
                } else {
                    q = str;
                }
                Intent intent = new Intent(getActivity(), SearchTopicByNameActivity.class);
                intent.putExtra("q", q);
                startActivity(intent);
            }
        });
        if (result == null) {
            task = new TopicListTask();
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    class TopicListTask extends MyAsyncTask<Void, List<String>, List<String>> {
        WeiboException e;

        @Override
        protected List<String> doInBackground(Void... params) {
            UserTopicListDao dao = new UserTopicListDao(GlobalContext.getInstance().getSpecialToken(), userBean.getId());
            try {
                return dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> atUserBeans) {
            super.onPostExecute(atUserBeans);
            if (isCancelled())
                return;
            if (atUserBeans == null || atUserBeans.size() == 0) {
                return;
            }

            result.clear();
            result.addAll(atUserBeans);
            adapter.notifyDataSetChanged();
        }
    }
}
