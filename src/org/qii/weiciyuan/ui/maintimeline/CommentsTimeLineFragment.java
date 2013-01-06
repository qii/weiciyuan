package org.qii.weiciyuan.ui.maintimeline;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.maintimeline.CommentsTimeLineByMeDao;
import org.qii.weiciyuan.dao.maintimeline.ICommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MentionsCommentTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.CommentFloatingMenu;
import org.qii.weiciyuan.ui.actionmenu.CommentSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> implements IRemoveItem {


    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private String[] groupNames = new String[3];
    private int currentGroupId = 0;
    private RemoveTask removeTask;
    private DBCacheTask dbTask;

    private Map<Integer, CommentListBean> groupDataCache = new HashMap<Integer, CommentListBean>();

    private CommentListBean bean = new CommentListBean();

    private final int TYPE_RECEIVED_COMMENT = 0;
    private final int TYPE_MENTIONED_COMMENT_TO_ME = 1;
    private final int TYPE_COMMENT_BY_ME = 2;

    private UnreadBean unreadBean;


    @Override
    public CommentListBean getList() {
        return bean;
    }

    public CommentsTimeLineFragment() {

    }

    public CommentsTimeLineFragment(AccountBean accountBean, UserBean userBean, String token) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = token;
    }

    public void setCurrentGroupId(int positoin) {
        currentGroupId = positoin;
    }

    protected void clearAndReplaceValue(CommentListBean value) {
        getList().getItemList().clear();
        getList().getItemList().addAll(value.getItemList());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("account", accountBean);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);

        outState.putStringArray("groupNames", groupNames);
        outState.putInt("currentGroupId", currentGroupId);

        outState.putSerializable("0", groupDataCache.get(0));
        outState.putSerializable("1", groupDataCache.get(1));
        outState.putSerializable("2", groupDataCache.get(2));

        outState.putSerializable("unreadBean", unreadBean);
    }


    public void refreshUnread(UnreadBean unreadBean) {

        Activity activity = getActivity();
        if (activity != null) {
            if (unreadBean == null) {
                activity.getActionBar().getTabAt(2).setText(getString(R.string.comments));
                return;
            }
            this.unreadBean = unreadBean;
            String number = Utility.buildTabText(unreadBean.getMention_cmt() + unreadBean.getCmt());
            if (!TextUtils.isEmpty(number))
                activity.getActionBar().getTabAt(2).setText(getString(R.string.comments) + number);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {
            if (getActivity().getActionBar().getTabAt(2).getText().toString().contains(")")) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getBitmapDownloader();

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                if (Utility.isTaskStopped(dbTask)) {
                    dbTask = new DBCacheTask();
                    dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }

                groupDataCache.put(TYPE_RECEIVED_COMMENT, new CommentListBean());
                groupDataCache.put(TYPE_MENTIONED_COMMENT_TO_ME, new CommentListBean());
                groupDataCache.put(TYPE_COMMENT_BY_ME, new CommentListBean());
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                accountBean = (AccountBean) savedInstanceState.getSerializable("account");
                token = savedInstanceState.getString("token");
                groupNames = savedInstanceState.getStringArray("groupNames");
                currentGroupId = savedInstanceState.getInt("currentGroupId");
                unreadBean = (UnreadBean) savedInstanceState.getSerializable("unreadBean");

                groupDataCache.put(TYPE_RECEIVED_COMMENT, (CommentListBean) savedInstanceState.getSerializable("0"));
                groupDataCache.put(TYPE_MENTIONED_COMMENT_TO_ME, (CommentListBean) savedInstanceState.getSerializable("1"));
                groupDataCache.put(TYPE_COMMENT_BY_ME, (CommentListBean) savedInstanceState.getSerializable("2"));

                clearAndReplaceValue((CommentListBean) savedInstanceState.getSerializable("bean"));
                timeLineAdapter.notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

        refreshUnread(unreadBean);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position - 1 < getList().getSize() && position - 1 >= 0) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(getListView(), timeLineAdapter, CommentsTimeLineFragment.this, getList().getItemList().get(position - 1)));
                        return true;
                    } else {
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(getListView(), timeLineAdapter, CommentsTimeLineFragment.this, getList().getItemList().get(position - 1)));
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
            removeTask = new RemoveTask(GlobalContext.getInstance().getSpecialToken(), getList().getItemList().get(position).getId(), position);
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
            if (Utility.isAllNotNull(getActivity(), this.e)) {
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

    private class DBCacheTask extends MyAsyncTask<Void, CommentListBean, CommentListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }


        @Override
        protected CommentListBean doInBackground(Void... params) {
            return DatabaseManager.getInstance().getCommentLineMsgList(accountBean.getUid());
        }

        @Override
        protected void onPostExecute(CommentListBean result) {
            super.onPostExecute(result);

            if (result != null) {
                clearAndReplaceValue(result);
                clearAndReplaceValue(0, result);
            }

            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            refreshLayout(getList());
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            }

            /**when one user open app from android notification center while this app is using another account,
             * activity will restart, and then mentions and comment fragment
             * will fetch new message from server
             **/
            if (getActivity().getActionBar().getTabAt(2).getText().toString().contains(")")) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        groupNames[0] = getString(R.string.all_people_send_to_me);
        groupNames[1] = getString(R.string.mentions_to_me);
        groupNames[2] = getString(R.string.my_comment);

        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, ((AbstractAppActivity) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView(), true);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        CommentFloatingMenu menu = new CommentFloatingMenu(getList().getItem(position));
        menu.show(getFragmentManager(), "");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_mentionstimelinefragment, menu);
        menu.findItem(R.id.group_name).setTitle(groupNames[currentGroupId]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.write_weibo:
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("account", accountBean);

                startActivity(intent);
                break;
            case R.id.refresh:
                if (allowRefresh())
                    getPullToRefreshListView().startRefreshNow();
                break;
            case R.id.group_name:
                if (canSwitchGroup()) {
                    CommentsGroupDialog dialog = new CommentsGroupDialog(groupNames, currentGroupId);
                    dialog.setTargetFragment(CommentsTimeLineFragment.this, 0);
                    dialog.show(getFragmentManager(), "");
                }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        CommentListBean result;
        ICommentsTimeLineDao dao;
        switch (currentGroupId) {
            case TYPE_RECEIVED_COMMENT:
                dao = new MainCommentsTimeLineDao(token);
                break;
            case TYPE_MENTIONED_COMMENT_TO_ME:
                dao = new MentionsCommentTimeLineDao(token);
                break;
            case TYPE_COMMENT_BY_ME:
                dao = new CommentsTimeLineByMeDao(token);
                break;
            default:
                dao = new MainCommentsTimeLineDao(token);
                break;
        }
        if (getList() != null && getList().getItemList().size() > 0) {
            dao.setSince_id(getList().getItemList().get(0).getId());
        }
        result = dao.getGSONMsgList();
        if (result != null && currentGroupId == TYPE_RECEIVED_COMMENT) {
            DatabaseManager.getInstance().addCommentLineMsg(result, accountBean.getUid());
        }
        return result;
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        CommentListBean result;
        ICommentsTimeLineDao dao;
        switch (currentGroupId) {
            case TYPE_RECEIVED_COMMENT:
                dao = new MainCommentsTimeLineDao(token);
                break;
            case TYPE_MENTIONED_COMMENT_TO_ME:
                dao = new MentionsCommentTimeLineDao(token);
                break;
            case TYPE_COMMENT_BY_ME:
                dao = new CommentsTimeLineByMeDao(token);
                break;
            default:
                dao = new MainCommentsTimeLineDao(token);
                break;
        }
        if (getList() != null && getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected CommentListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        throw new UnsupportedOperationException("comment list dont support this operation");
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            if (newValue.getItemList().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getItemList().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                getList().addNewData(newValue);
                clearAndReplaceValue(currentGroupId, getList());
                getAdapter().notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();
            }
        }
        unreadBean = null;
        refreshUnread(unreadBean);
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(GlobalContext.getInstance().getCurrentAccountId()).intValue());
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
        }
    }

    public void switchGroup() {

        if (groupDataCache.get(currentGroupId).getSize() == 0) {
            getList().getItemList().clear();
            getAdapter().notifyDataSetChanged();
            getPullToRefreshListView().startRefreshNow();

        } else {
            clearAndReplaceValue(groupDataCache.get(currentGroupId));
            getAdapter().notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void clearAndReplaceValue(int position, CommentListBean newValue) {
        groupDataCache.get(position).getItemList().clear();
        groupDataCache.get(position).getItemList().addAll(newValue.getItemList());
        groupDataCache.get(position).setTotal_number(newValue.getTotal_number());
    }
}
