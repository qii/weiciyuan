package org.qii.weiciyuan.ui.maintimeline;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.maintimeline.CommentsTimeLineByMeDao;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IRemoveItem;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.actionmenu.CommentSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> implements IRemoveItem {

    private String[] group = new String[3];
    private int selected = 0;
    private RemoveTask removeTask;
    private SimpleTask dbTask;


    public void setSelected(int positoin) {
        selected = positoin;
    }

    protected void clearAndReplaceValue(CommentListBean value) {
        bean.getItemList().clear();
        bean.getItemList().addAll(value.getItemList());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putStringArray("group", group);
        outState.putInt("selected", selected);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dbTask != null)
            dbTask.cancel(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();

        if (savedInstanceState != null && (bean == null || bean.getItemList().size() == 0)) {
            group = savedInstanceState.getStringArray("group");
            selected = savedInstanceState.getInt("selected");
            clearAndReplaceValue((CommentListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            if (dbTask == null || dbTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                dbTask = new SimpleTask();
                dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position - 1 < getList().getSize() && position - 1 >= 0) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        listView.setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(listView, timeLineAdapter, CommentsTimeLineFragment.this, bean.getItemList().get(position - 1)));
                        return true;
                    } else {
                        listView.setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(listView, timeLineAdapter, CommentsTimeLineFragment.this, bean.getItemList().get(position - 1)));
                        return true;
                    }
                }
                return false;
            }
        }

        );

    }

    @Override
    public void removeItem(int position) {
        clearActionMode();
        if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeTask = new RemoveTask(((IToken) getActivity()).getToken(), bean.getItemList().get(position).getId(), position);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeCancel() {
        clearActionMode();
    }

    class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {

        String token;
        String id;
        int positon;
        WeiboException e;

        public RemoveTask(String token, String id, int positon) {
            this.token = token;
            this.id = id;
            this.positon = positon;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DestroyCommentDao dao = new DestroyCommentDao(token, id);
            try {
                return dao.destroy();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                ((CommentListAdapter) timeLineAdapter).removeItem(positon);

            }
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


    private class RefreshDBTask extends MyAsyncTask<Object, Object, Object> {

        @Override
        protected void onPreExecute() {
            showListView();
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_progress).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_text).setVisibility(View.VISIBLE);
            headerView.findViewById(R.id.header_progress).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.refresh));
            listView.setSelection(0);
        }

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
            headerView.findViewById(R.id.header_progress).clearAnimation();
            headerView.findViewById(R.id.header_progress).setVisibility(View.GONE);
            headerView.findViewById(R.id.header_text).setVisibility(View.GONE);

            if (bean.getSize() == 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
            } else {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
            super.onPostExecute(o);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new CommentListBean();
        group[0] = getString(R.string.all_people_send_to_me);
        group[1] = getString(R.string.following_people_send_to_me);
        group[2] = getString(R.string.my_comment);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), listView, true);
        listView.setAdapter(timeLineAdapter);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position).getStatus());
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
        startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
        menu.findItem(R.id.mentionstimelinefragment_group).setTitle(group[selected]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mentionstimelinefragment_refresh:

                refresh();

                break;
            case R.id.mentionstimelinefragment_group:
                if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    CommentsGroupDialog dialog = new CommentsGroupDialog(group, selected);
                    dialog.setTargetFragment(CommentsTimeLineFragment.this, 0);
                    dialog.show(getFragmentManager(), "");
                }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        if (selected == 0 || selected == 1) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList() != null && getList().getItemList().size() > 0) {
                dao.setSince_id(getList().getItemList().get(0).getId());
            }

            if (selected == 1) {
                dao.setFilter_by_author("1");
            }

            CommentListBean result = dao.getGSONMsgList();
            if (result != null && selected == 0) {
                if (result.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    DatabaseManager.getInstance().addCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
                } else {
                    DatabaseManager.getInstance().replaceCommentLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
                }
            }
            return result;
        } else {
            CommentsTimeLineByMeDao dao = new CommentsTimeLineByMeDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList() != null && getList().getItemList().size() > 0) {
                dao.setSince_id(getList().getItemList().get(0).getId());
            }

            CommentListBean result = dao.getGSONMsgList();
            return result;
        }
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        if (selected == 0 || selected == 1) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
            }
            if (selected == 1) {
                dao.setFilter_by_author("1");
            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        } else {
            CommentsTimeLineByMeDao dao = new CommentsTimeLineByMeDao(((MainTimeLineActivity) getActivity()).getToken());
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        }
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            if (newValue.getItemList().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getItemList().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    newValue.getItemList().addAll(getList().getItemList());
                }

                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
            }
        }
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.comments));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(GlobalContext.getInstance().getCurrentAccountId()).intValue());
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            getList().getItemList().addAll(newValue.getItemList().subList(1, newValue.getItemList().size() - 1));

        }
    }

    public void refreshAnother() {
        getList().getItemList().clear();
        timeLineAdapter.notifyDataSetChanged();
        if (selected != 0) {
            refresh();
        } else {
            new RefreshDBTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        getActivity().invalidateOptionsMenu();
    }
}
