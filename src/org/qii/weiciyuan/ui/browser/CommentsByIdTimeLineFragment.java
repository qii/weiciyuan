package org.qii.weiciyuan.ui.browser;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.send.CommentNewMsgDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.CommentByIdSingleChoiceModeLinstener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.common.QuickSendProgressFragment;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;
import org.qii.weiciyuan.ui.loader.CommentsByIdMsgLoader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * User: qii
 * Date: 12-7-29
 */
@Deprecated
public class CommentsByIdTimeLineFragment extends AbstractTimeLineFragment<CommentListBean>
        implements IRemoveItem {

    private LinearLayout quick_repost;

    private RemoveTask removeTask;

    private CommentListBean bean = new CommentListBean();

    private EditText et;

    private String token;

    private MessageBean msg;

    private BroadcastReceiver sendCompletedReceiver;


    @Override
    public CommentListBean getList() {
        return bean;
    }

    public CommentsByIdTimeLineFragment(String token, MessageBean msg) {
        this.token = token;
        this.msg = msg;
    }

    public CommentsByIdTimeLineFragment() {

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", bean);
        outState.putParcelable("msg", msg);
        outState.putString("token", token);
    }

    //restore from activity destroy
    public void load() {
        String sss = token;
        if ((bean == null || bean.getItemList().size() == 0)) {
            if (pullToRefreshListView != null) {
                pullToRefreshListView.setRefreshing();
                loadNewMsg();
            }
        }
    }

    private boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(et.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(getActivity(),
                        getString(R.string.content_cant_be_empty_and_dont_have_account),
                        Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                et.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(getActivity(), getString(R.string.dont_have_account),
                        Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }
        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        et.startAnimation(shake);
        return false;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getPullToRefreshListView().setRefreshing();
                            loadNewMsg();
                        }

                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceAll((CommentListBean) savedInstanceState.getParcelable("bean"));
                token = savedInstanceState.getString("token");
                msg = (MessageBean) savedInstanceState.getParcelable("msg");
                timeLineAdapter.notifyDataSetChanged();
                refreshLayout(bean);
                break;
        }


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new CommentListBean();
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.commentsbyidtimelinefragment_layout, container, false);
        buildLayout(inflater, view);
        quick_repost = (LinearLayout) view.findViewById(R.id.quick_repost);
        et = (EditText) view.findViewById(R.id.content);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(onItemLongClickListener);
    }

    private AdapterView.OnItemLongClickListener onItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position - 1 < getList().getSize() && position - 1 >= 0) {
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    actionMode = getActivity().startActionMode(
                            new CommentByIdSingleChoiceModeLinstener(getListView(), timeLineAdapter,
                                    CommentsByIdTimeLineFragment.this, quick_repost,
                                    bean.getItemList().get(position - 1)));
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    actionMode = getActivity().startActionMode(
                            new CommentByIdSingleChoiceModeLinstener(getListView(), timeLineAdapter,
                                    CommentsByIdTimeLineFragment.this, quick_repost,
                                    bean.getItemList().get(position - 1)));
                    return true;
                }
            }
            return false;
        }

    };

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, getList().getItemList(), getListView(),
                false, false);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    private void sendComment() {

        if (canSend()) {
            new QuickCommentTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeItem(int position) {
        clearActionMode();
        if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeTask = new RemoveTask(GlobalContext.getInstance().getSpecialToken(),
                    bean.getItemList().get(position).getId(), position);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeCancel() {
        clearActionMode();
    }


    @Override
    public void onResume() {
        super.onResume();
        sendCompletedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(sendCompletedReceiver,
                new IntentFilter("org.qii.weiciyuan.SEND.COMMENT.COMPLETED"));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(sendCompletedReceiver);
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

    private class QuickCommentTask extends AsyncTask<Void, Void, CommentBean> {

        WeiboException e;

        QuickSendProgressFragment progressFragment = new QuickSendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    QuickCommentTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    QuickCommentTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected CommentBean doInBackground(Void... params) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, msg.getId(),
                    et.getText().toString());
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(CommentBean commentBean) {
            super.onCancelled(commentBean);
            progressFragment.dismissAllowingStateLoss();
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(CommentBean s) {
            progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                et.setText("");
                loadNewMsg();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT)
                        .show();
            }
            super.onPostExecute(s);

        }
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

//        CommentByIdFloatingMenu menu = new CommentByIdFloatingMenu(getList().getItem(position));
//        menu.show(getFragmentManager(), "");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void newMsgLoaderSuccessCallback(CommentListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().replaceAll(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            invlidateTabText();
        } else if (newValue != null && newValue.getSize() == 0) {
            getList().clear();
            getAdapter().notifyDataSetChanged();
            invlidateTabText();
        }
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            invlidateTabText();

        }
    }

    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
//            ActionBar.Tab tab = activity.getActionBar().getTabAt(1);
//            Utility.buildTabCount(tab, getString(R.string.comments), bean.getTotal_number());
            ((BrowserWeiboMsgActivity) activity).updateCommentCount(bean.getTotal_number());
        }
    }

    @Override
    public void loadMiddleMsg(String beginId, String endId, int position) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        dismissFooterView();

        Bundle bundle = new Bundle();
        bundle.putString("beginId", beginId);
        bundle.putString("endId", endId);
        bundle.putInt("position", position);
        getLoaderManager().restartLoader(MIDDLE_MSG_LOADER_ID, bundle, msgAsyncTaskLoaderCallback);

    }

    @Override
    public void loadNewMsg() {
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgAsyncTaskLoaderCallback);
    }

    @Override
    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgAsyncTaskLoaderCallback);
    }


    protected android.support.v4.content.Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateNewMsgLoader(
            int loaderId, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();

        String sinceId = null;
//        if (getList().getItemList().size() > 0) {
//            sinceId = getList().getItemList().get(0).getId();
//        }
        return new CommentsByIdMsgLoader(getActivity(), msg.getId(), token, sinceId, null);
    }

    protected android.support.v4.content.Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateMiddleMsgLoader(
            int loaderId, Bundle args, String middleBeginId, String middleEndId,
            String middleEndTag, int middlePosition) {
        String token = GlobalContext.getInstance().getSpecialToken();

        return new CommentsByIdMsgLoader(getActivity(), msg.getId(), token, middleBeginId,
                middleEndId);
    }

    protected android.support.v4.content.Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateOldMsgLoader(
            int loaderId, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String maxId = null;
        if (getList().getItemList().size() > 0) {
            maxId = getList().getItemList().get(getList().getItemList().size() - 1).getId();
        }
        return new CommentsByIdMsgLoader(getActivity(), msg.getId(), token, null, maxId);
    }
}
