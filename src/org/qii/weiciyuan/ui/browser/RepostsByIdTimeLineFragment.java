package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.VelocityListView;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.RepostSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.StatusListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.loader.RepostByIdMsgLoader;
import org.qii.weiciyuan.ui.widgets.QuickSendProgressFragment;

/**
 * User: qii
 * Date: 12-8-13
 */
@Deprecated
public class RepostsByIdTimeLineFragment extends AbstractMessageTimeLineFragment<RepostListBean> {


    private MessageBean msg;

    private EditText et;

    private LinearLayout quick_repost;

    private String token;
    private String id;

    private RepostListBean bean = new RepostListBean();

    @Override
    public RepostListBean getList() {
        return bean;
    }

    public RepostsByIdTimeLineFragment(String token, String id, MessageBean msg) {
        this.token = token;
        this.id = id;
        this.msg = msg;
    }

    public RepostsByIdTimeLineFragment() {

    }

    //restore from activity destroy
    public void load() {
        if ((bean == null || bean.getSize() == 0)) {
            if (pullToRefreshListView != null) {
                pullToRefreshListView.setRefreshing();
                loadNewMsg();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
        outState.putParcelable("msg", msg);
    }


    private boolean canSend() {

        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveToken) {
                Toast.makeText(getActivity(), getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                //nothing
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                clearAndReplaceValue((RepostListBean) savedInstanceState.getParcelable("bean"));
                token = savedInstanceState.getString("token");
                id = savedInstanceState.getString("id");
                msg = (MessageBean) savedInstanceState.getParcelable("msg");
                timeLineAdapter.notifyDataSetChanged();
                refreshLayout(bean);
                break;
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
                        mActionMode = getActivity().startActionMode(new RepostSingleChoiceModeListener(getListView(), (StatusListAdapter) timeLineAdapter, RepostsByIdTimeLineFragment.this, quick_repost, bean.getItemList().get(position - 1)));
                        return true;
                    } else {
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new RepostSingleChoiceModeListener(getListView(), (StatusListAdapter) timeLineAdapter, RepostsByIdTimeLineFragment.this, quick_repost, bean.getItemList().get(position - 1)));
                        return true;
                    }


                }
                return false;
            }

        });


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.repostsbyidtimelinefragment_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        quick_repost = (LinearLayout) view.findViewById(R.id.quick_repost);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                loadNewMsg();

            }
        });
        pullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                loadOldMsg(null);
            }
        });
        getListView().setScrollingCacheEnabled(false);

        getListView().setHeaderDividersEnabled(false);

        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    getListView().clearChoices();
                    mActionMode.finish();
                    mActionMode = null;
                    return;
                }
                getListView().clearChoices();
                if (position - 1 < getList().getSize() && position - 1 >= 0) {

                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getSize()) {

                    loadOldMsg(view);
                }
            }
        });

        if (savedInstanceState == null && msg != null) {
            if (msg.getRetweeted_status() == null) {
                quick_repost.setVisibility(View.VISIBLE);
            }
        } else if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getParcelable("msg");
            if (msg.getRetweeted_status() == null) {
                quick_repost.setVisibility(View.VISIBLE);
            }
        }

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRepost();
            }
        });
        buildListAdapter();
        return view;
    }

    protected void buildListAdapter() {
        timeLineAdapter = new StatusListAdapter(this, getList().getItemList(), getListView(), false);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    private void sendRepost() {
        if (canSend()) {
            new SimpleTask().execute();
        }
    }


    class SimpleTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        QuickSendProgressFragment progressFragment = new QuickSendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected MessageBean doInBackground(Void... params) {

            String content = et.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = getString(R.string.repost);
            }

            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
            dao.setStatus(content);
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(MessageBean s) {
            super.onCancelled(s);
            if (this.e != null && getActivity() != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(MessageBean s) {
            if (progressFragment != null)
                progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                et.setText("");
                loadNewMsg();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getItemList().get(position));
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                pullToRefreshListView.setRefreshing();
                loadNewMsg();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(2);
            Utility.buildTabCount(tab, getString(R.string.repost), bean.getTotal_number());
            ((BrowserWeiboMsgActivity) activity).updateRepostCount(bean.getTotal_number());
        }
    }


    @Override
    protected void newMsgOnPostExecute(RepostListBean newValue, Bundle loaderArgs) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 0) {
            getList().replaceAll(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            invlidateTabText();

        }


    }

    @Override
    protected void oldMsgOnPostExecute(RepostListBean newValue) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            invlidateTabText();

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();
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
        VelocityListView velocityListView = (VelocityListView) getListView();
        bundle.putBoolean("towardsBottom", velocityListView.getTowardsOrientation() == VelocityListView.TOWARDS_BOTTOM);
        getLoaderManager().restartLoader(MIDDLE_MSG_LOADER_ID, bundle, msgCallback);

    }

    @Override
    public void loadNewMsg() {
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }


    @Override
    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgCallback);
    }


    protected Loader<AsyncTaskLoaderResult<RepostListBean>> onCreateNewMsgLoader(int loaderId, Bundle args) {
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new RepostByIdMsgLoader(getActivity(), id, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<RepostListBean>> onCreateMiddleMsgLoader(int loaderId, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        return new RepostByIdMsgLoader(getActivity(), id, token, middleBeginId, middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<RepostListBean>> onCreateOldMsgLoader(int loaderId, Bundle args) {
        String maxId = null;

        if (getList().getSize() > 0) {
            maxId = getList().getItemList().get(getList().getSize() - 1).getId();
        }

        return new RepostByIdMsgLoader(getActivity(), id, token, null, maxId);
    }
}

