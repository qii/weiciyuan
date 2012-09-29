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
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.dao.destroy.DestroyCommentDao;
import org.qii.weiciyuan.dao.send.CommentNewMsgDao;
import org.qii.weiciyuan.dao.timeline.CommentsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IRemoveItem;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.actionmenu.CommentByIdSingleChoiceModeLinstener;
import org.qii.weiciyuan.ui.adapter.CommentListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 */
public class CommentsByIdTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> implements IRemoveItem {

    private LinearLayout quick_repost;
    private RemoveTask removeTask;


    protected void clearAndReplaceValue(CommentListBean value) {
        bean.getItemList().clear();
        bean.getItemList().addAll(value.getItemList());
        bean.setTotal_number(value.getTotal_number());
    }

    private EditText et;


    public CommentListBean getList() {
        return bean;
    }

    private String token;
    private String id;

    public CommentsByIdTimeLineFragment(String token, String id) {
        this.token = token;
        this.id = id;
    }

    public CommentsByIdTimeLineFragment() {

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
    }

    //restore from activity destroy
    public void load() {
        String sss = token;
        if ((bean == null || bean.getItemList().size() == 0) && newTask == null) {
            if (pullToRefreshListView != null) {
                pullToRefreshListView.startRefreshNow();
                refresh();
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
                Toast.makeText(getActivity(), getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                et.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
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
        commander = ((AbstractAppActivity) getActivity()).getCommander();

        if (savedInstanceState != null && bean.getItemList().size() == 0) {
            clearAndReplaceValue((CommentListBean) savedInstanceState.getSerializable("bean"));
            token = savedInstanceState.getString("token");
            id = savedInstanceState.getString("id");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        } else {
            pullToRefreshListView.startRefreshNow();
            refresh();
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
                        mActionMode = getActivity().startActionMode(new CommentByIdSingleChoiceModeLinstener(getListView(), timeLineAdapter, CommentsByIdTimeLineFragment.this, quick_repost, bean.getItemList().get(position - 1)));
                        return true;
                    } else {
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new CommentByIdSingleChoiceModeLinstener(getListView(), timeLineAdapter, CommentsByIdTimeLineFragment.this, quick_repost, bean.getItemList().get(position - 1)));
                        return true;
                    }
                }
                return false;
            }

        }

        );
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new CommentListBean();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_listview_layout, container, false);
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

        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
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
                if (position - 1 < getList().getItemList().size() && position - 1 >= 0) {
                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getItemList().size()) {
                    listViewFooterViewClick(view);
                }
            }
        });

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });
        buildListAdapter();
        return view;
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), getListView(), false);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }

    private void sendComment() {

        if (canSend()) {
            new SimpleTask().execute();
        }
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

    class SimpleTask extends AsyncTask<Void, Void, CommentBean> {
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
        protected CommentBean doInBackground(Void... params) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, id, et.getText().toString());
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
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(CommentBean s) {
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

        Intent intent = new Intent(getActivity(), BrowserCommentActivity.class);
        intent.putExtra("comment", bean.getItem(position));
        intent.putExtra("token", token);
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


    @Override
    protected CommentListBean getDoInBackgroundNewData() throws WeiboException {
        CommentsTimeLineByIdDao dao = new CommentsTimeLineByIdDao(token, id);

        if (getList().getItemList().size() > 0) {
            dao.setSince_id(getList().getItemList().get(0).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected CommentListBean getDoInBackgroundOldData() throws WeiboException {
        CommentsTimeLineByIdDao dao = new CommentsTimeLineByIdDao(token, id);
        if (getList().getItemList().size() > 0) {
            dao.setMax_id(getList().getItemList().get(getList().getItemList().size() - 1).getId());
        }
        CommentListBean result = dao.getGSONMsgList();
        return result;
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null) {
            bean.setTotal_number(newValue.getTotal_number());
            if (newValue.getItemList().size() == 0) {
//                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
//                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getItemList().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    newValue.getItemList().addAll(getList().getItemList());
                }

                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();

            }
        }

        invlidateTabText();
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            List<CommentBean> list = newValue.getItemList();
            getList().getItemList().addAll(list.subList(1, list.size()));
            getList().setTotal_number(newValue.getTotal_number());

        }

        timeLineAdapter.notifyDataSetChanged();
        invlidateTabText();
    }


    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(1);
            String num = getString(R.string.comments) + "(" + bean.getTotal_number() + ")";
            tab.setText(num);

        }
    }

}
