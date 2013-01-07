package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.dao.timeline.RepostsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.utils.DataMemoryCache;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.actionmenu.RepostSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.StatusListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

/**
 * User: qii
 * Date: 12-8-13
 */
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
        if ((bean == null || bean.getSize() == 0) && newTask == null) {
            if (pullToRefreshListView != null) {
                pullToRefreshListView.startRefreshNow();
                refresh();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
        outState.putSerializable("msg", msg);
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
        commander = ((AbstractAppActivity) getActivity()).getBitmapDownloader();

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                //nothing
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                clearAndReplaceValue((RepostListBean) savedInstanceState.getSerializable("bean"));
                token = savedInstanceState.getString("token");
                id = savedInstanceState.getString("id");
                msg = (MessageBean) savedInstanceState.getSerializable("msg");
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
        setRetainInstance(true);
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

                refresh();

            }
        });
        pullToRefreshListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                listViewFooterViewClick(null);
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

                    listViewFooterViewClick(view);
                }
            }
        });

        if (savedInstanceState == null && msg != null) {
            if (msg.getRetweeted_status() == null) {
                quick_repost.setVisibility(View.VISIBLE);
            }
        } else if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
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
        timeLineAdapter = new StatusListAdapter(this, ((AbstractAppActivity) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView(), false);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    private void sendRepost() {
        if (canSend()) {
            new SimpleTask().execute();
        }
    }


    class SimpleTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        SendProgressFragment progressFragment = new SendProgressFragment();

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
                refresh();
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
                pullToRefreshListView.startRefreshNow();
                refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(2);
            String num = getString(R.string.repost) + "(" + bean.getTotal_number() + ")";
            tab.setText(num);

        }
    }


    @Override
    protected RepostListBean getDoInBackgroundNewData() throws WeiboException {
        RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);
        return dao.getGSONMsgList();
    }

    @Override
    protected RepostListBean getDoInBackgroundOldData() throws WeiboException {
        RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);
        if (getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        return dao.getGSONMsgList();
    }

    @Override
    protected RepostListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        throw new UnsupportedOperationException("repost list dont support this operation");
    }

    @Override
    protected void newMsgOnPostExecute(RepostListBean newValue) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 0) {
            getList().replaceAll(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            invlidateTabText();
            DataMemoryCache.updateTimeLineDataRepostCount(newValue.getItem(0).getRetweeted_status(), newValue.getTotal_number());

        }


    }

    @Override
    protected void oldMsgOnPostExecute(RepostListBean newValue) {
        if (Utility.isAllNotNull(getActivity(), newValue) && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            invlidateTabText();
            DataMemoryCache.updateTimeLineDataRepostCount(newValue.getItem(0).getRetweeted_status(), newValue.getTotal_number());

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();
        }
    }

}

