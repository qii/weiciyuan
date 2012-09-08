package org.qii.weiciyuan.ui.maintimeline;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> {

    protected void clearAndReplaceValue(CommentListBean value) {
        bean.getComments().clear();
        bean.getComments().addAll(value.getComments());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        ((MainTimeLineActivity) getActivity()).setCommentsListView(listView);
        if (savedInstanceState != null && (bean == null || bean.getComments().size() == 0)) {
            clearAndReplaceValue((CommentListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            new SimpleTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        }

    }


    private class SimpleTask extends MyAsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            CommentListBean value = DatabaseManager.getInstance().getCommentLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid());
            clearAndReplaceValue(value);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new CommentListBean();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList().getComments(), listView, true);
        listView.setAdapter(timeLineAdapter);
    }





    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getComments().get(position).getStatus());
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
        startActivity(intent);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mentionstimelinefragment_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList() != null && getList().getComments().size() > 0) {
            dao.setSince_id(getList().getComments().get(0).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        if (result != null) {
            if (result.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                DatabaseManager.getInstance().addCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            } else {
                DatabaseManager.getInstance().replaceCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            }
        }
        return result;
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getComments().size() > 0) {
            dao.setMax_id(getList().getComments().get(getList().getComments().size() - 1).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            if (newValue.getComments().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getComments().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getComments().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    newValue.getComments().addAll(getList().getComments());
                }

                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
            }
        }
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.comments));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            getList().getComments().addAll(newValue.getComments().subList(1, newValue.getComments().size() - 1));

        }
    }
}
