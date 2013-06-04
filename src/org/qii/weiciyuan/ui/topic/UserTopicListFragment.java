package org.qii.weiciyuan.ui.topic;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.topic.TopicDao;
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

    private FollowTopicTask followTopicTask;


    public UserTopicListFragment() {

    }

    public UserTopicListFragment(UserBean userBean) {
        this.userBean = userBean;
    }


    public UserTopicListFragment(UserBean userBean, ArrayList<String> topicList) {
        this.userBean = userBean;
        this.result = topicList;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Utility.cancelTasks(task, followTopicTask);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("userBean", userBean);
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
            userBean = (UserBean) savedInstanceState.getParcelable("userBean");
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
        if (result == null || result.size() == 0) {
            refresh();
        }
    }

    private void refresh() {
        task = new TopicListTask();
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId()))
            inflater.inflate(R.menu.actionbar_menu_usertopiclistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_topic:
                FollowTopicDialog dialog = new FollowTopicDialog();
                dialog.setTargetFragment(this, 1);
                dialog.show(getFragmentManager(), "");
                break;

        }
        return super.onOptionsItemSelected(item);
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

    public void addTopic(String keyWord) {
        if (Utility.isTaskStopped(followTopicTask)) {
            followTopicTask = new FollowTopicTask(keyWord);
            followTopicTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class FollowTopicTask extends MyAsyncTask<Void, Boolean, Boolean> {
        WeiboException e;
        String keyWord;

        public FollowTopicTask(String keyWord) {
            this.keyWord = keyWord;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return new TopicDao(GlobalContext.getInstance().getSpecialToken()).follow(keyWord);
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return false;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null)
                return;
            if (aBoolean) {
                Toast.makeText(getActivity(), getString(R.string.follow_topic_successfully), Toast.LENGTH_SHORT).show();
                refresh();
            } else
                Toast.makeText(getActivity(), getString(R.string.follow_topic_failed), Toast.LENGTH_SHORT).show();
        }
    }

}
