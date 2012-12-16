package org.qii.weiciyuan.ui.maintimeline;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.maintimeline.CommentsTimeLineByMeDao;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MentionsCommentTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
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

    private String[] group = new String[3];
    private int selected = 0;
    private RemoveTask removeTask;
    private DBCacheTask dbTask;

    private Map<Integer, CommentListBean> hashMap = new HashMap<Integer, CommentListBean>();

    private CommentListBean bean = new CommentListBean();

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

    public void setSelected(int positoin) {
        selected = positoin;
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

        outState.putStringArray("group", group);
        outState.putInt("selected", selected);

        outState.putSerializable("0", hashMap.get(0));
        outState.putSerializable("1", hashMap.get(1));
        outState.putSerializable("2", hashMap.get(2));
        outState.putSerializable("3", hashMap.get(3));
        outState.putSerializable("4", hashMap.get(4));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dbTask != null)
            dbTask.cancel(true);
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
        commander = ((AbstractAppActivity) getActivity()).getCommander();

        if (savedInstanceState != null && (getList() == null || getList().getItemList().size() == 0)) {
            userBean = (UserBean) savedInstanceState.getSerializable("userBean");
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
            token = savedInstanceState.getString("token");
            group = savedInstanceState.getStringArray("group");
            selected = savedInstanceState.getInt("selected");

            hashMap.put(0, (CommentListBean) savedInstanceState.getSerializable("0"));
            hashMap.put(1, (CommentListBean) savedInstanceState.getSerializable("1"));
            hashMap.put(2, (CommentListBean) savedInstanceState.getSerializable("2"));
            hashMap.put(3, (CommentListBean) savedInstanceState.getSerializable("3"));
            hashMap.put(4, (CommentListBean) savedInstanceState.getSerializable("4"));

            clearAndReplaceValue((CommentListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(getList());
        } else {
            if (dbTask == null || dbTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                dbTask = new DBCacheTask();
                dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }

            hashMap.put(0, new CommentListBean());
            hashMap.put(1, new CommentListBean());
            hashMap.put(2, new CommentListBean());
            hashMap.put(3, new CommentListBean());
            hashMap.put(4, new CommentListBean());
        }

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
                pullToRefreshListView.startRefreshNow();
            }

            /**when one user open app from android notification center while this app is using another account,
             * activity will restart, and then mentions and comment fragment
             * will fetch new message from server
             **/
            if (getActivity().getActionBar().getTabAt(2).getText().toString().contains(")")) {
                pullToRefreshListView.startRefreshNow();
            }
        }
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), getListView(), true);
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
        menu.findItem(R.id.group_name).setTitle(group[selected]);
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
                    pullToRefreshListView.startRefreshNow();
                break;
            case R.id.group_name:
                if (canSwitchGroup()) {
                    CommentsGroupDialog dialog = new CommentsGroupDialog(group, selected);
                    dialog.setTargetFragment(CommentsTimeLineFragment.this, 0);
                    dialog.show(getFragmentManager(), "");
                }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        if (selected == 0) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
            if (getList() != null && getList().getItemList().size() > 0) {
                dao.setSince_id(getList().getItemList().get(0).getId());
            }

//            if (selected == 1) {
//                dao.setFilter_by_author("1");
//            }

            CommentListBean result = dao.getGSONMsgList();
            if (result != null && selected == 0) {
                DatabaseManager.getInstance().addCommentLineMsg(result, accountBean.getUid());
            }
            return result;
        } else if (selected == 1) {
            MentionsCommentTimeLineDao dao = new MentionsCommentTimeLineDao(token);
            if (getList() != null && getList().getItemList().size() > 0) {
                dao.setSince_id(getList().getItemList().get(0).getId());
            }

//            if (selected == 3) {
//                dao.setFilter_by_author("1");
//            }

            CommentListBean result = dao.getGSONMsgList();
            return result;

        } else if (selected == 2) {
            CommentsTimeLineByMeDao dao = new CommentsTimeLineByMeDao(token);
            if (getList() != null && getList().getItemList().size() > 0) {
                dao.setSince_id(getList().getItemList().get(0).getId());
            }

            CommentListBean result = dao.getGSONMsgList();
            return result;
        }

        return null;
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        if (selected == 0) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
            }
//            if (selected == 1) {
//                dao.setFilter_by_author("1");
//            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        } else if (selected == 1) {
            MentionsCommentTimeLineDao dao = new MentionsCommentTimeLineDao(token);
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
            }
//            if (selected == 3) {
//                dao.setFilter_by_author("1");
//            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        } else if (selected == 2) {
            CommentsTimeLineByMeDao dao = new CommentsTimeLineByMeDao(token);
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        }
        return null;
    }

    @Override
    protected CommentListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        if (selected == 0) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
            if (getList().getItemList().size() > 0) {
                dao.setMax_id(beginId);
                dao.setSince_id(endId);
            }
//            if (selected == 1) {
//                dao.setFilter_by_author("1");
//            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        } else if (selected == 1) {
            MentionsCommentTimeLineDao dao = new MentionsCommentTimeLineDao(token);
            dao.setMax_id(beginId);
            dao.setSince_id(endId);
//            if (selected == 3) {
//                dao.setFilter_by_author("1");
//            }
            CommentListBean result = dao.getGSONMsgList();
            return result;
        } else if (selected == 2) {
            CommentsTimeLineByMeDao dao = new CommentsTimeLineByMeDao(token);
            dao.setMax_id(beginId);
            dao.setSince_id(endId);
            CommentListBean result = dao.getGSONMsgList();
            return result;
        }
        return null;
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            if (newValue.getItemList().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getItemList().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getItemList().size() < Integer.valueOf(SettingUtility.getMsgCount())) {
                    //for speed, add old data after new data
                    newValue.getItemList().addAll(getList().getItemList());
                } else {
                    //null is flag means this position has some old messages which dont appear
                    if (getList().getSize() > 0) {
                        newValue.getItemList().add(null);
                    }
                    newValue.getItemList().addAll(getList().getItemList());
                }

                clearAndReplaceValue(newValue);
                clearAndReplaceValue(selected, getList());
                getAdapter().notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();
            }
        }
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.comments));
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

        if (hashMap.get(selected).getSize() == 0) {
            getList().getItemList().clear();
            getAdapter().notifyDataSetChanged();
            getPullToRefreshListView().startRefreshNow();

        } else {
            clearAndReplaceValue(hashMap.get(selected));
            getAdapter().notifyDataSetChanged();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void clearAndReplaceValue(int position, CommentListBean newValue) {
        hashMap.get(position).getItemList().clear();
        hashMap.get(position).getItemList().addAll(newValue.getItemList());
        hashMap.get(position).setTotal_number(newValue.getTotal_number());
    }
}
