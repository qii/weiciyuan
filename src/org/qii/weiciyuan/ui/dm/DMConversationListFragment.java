package org.qii.weiciyuan.ui.dm;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.dm.DMConversationDao;
import org.qii.weiciyuan.dao.dm.SendDMDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.adapter.DMConversationAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.widgets.QuickSendProgressFragment;

/**
 * User: qii
 * Date: 12-11-15
 */
public class DMConversationListFragment extends AbstractTimeLineFragment<DMListBean> {

    private UserBean userBean;

    private int page = 1;

    private DMListBean bean = new DMListBean();

    private EditText et;

    @Override
    public DMListBean getList() {
        return bean;
    }

    public DMConversationListFragment(UserBean userBean) {
        this.userBean = userBean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putSerializable("userBean", userBean);
        outState.putInt("page", page);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPullToRefreshListView().startRefreshNow();

                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().addNewData((DMListBean) savedInstanceState.getSerializable("bean"));
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                page = savedInstanceState.getInt("page");
                getAdapter().notifyDataSetChanged();
                refreshLayout(bean);
                break;
        }

    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dmconversationlistfragment_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();

            }
        });

        getListView().setScrollingCacheEnabled(false);
        getListView().setHeaderDividersEnabled(false);
        getListView().setStackFromBottom(true);

        footerView = inflater.inflate(R.layout.listview_footer_layout, null);
        getListView().addFooterView(footerView);
        dismissFooterView();

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        ImageButton emoticon = (ImageButton) view.findViewById(R.id.emoticon);
        emoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buildListAdapter();
        return view;
    }

    private void send() {
        new QuickCommentTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMConversationAdapter(this, ((AbstractAppActivity) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }


    @Override
    protected void newMsgOnPostExecute(DMListBean newValue) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelection(bean.getSize() - 1);
        }

    }

    @Override
    protected void oldMsgOnPostExecute(DMListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            page++;
        }
    }

    @Override
    protected DMListBean getDoInBackgroundNewData() throws WeiboException {
        page = 1;
        return new DMConversationDao(GlobalContext.getInstance().getSpecialToken())
                .setUid(userBean.getId())
                .setPage(page).getConversationList();
    }

    @Override
    protected DMListBean getDoInBackgroundOldData() throws WeiboException {
        DMConversationDao dao = new DMConversationDao(GlobalContext.getInstance().getSpecialToken())
                .setUid(userBean.getId())
                .setPage(page + 1);
        DMListBean result = dao.getConversationList();
        return result;
    }

    @Override
    protected DMListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
        return null;
    }

    private class QuickCommentTask extends AsyncTask<Void, Void, Boolean> {
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
        protected Boolean doInBackground(Void... params) {
            SendDMDao dao = new SendDMDao(GlobalContext.getInstance().getSpecialToken(), userBean.getId(), et.getText().toString());
            try {
                return dao.send();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onCancelled(Boolean commentBean) {
            super.onCancelled(commentBean);
            progressFragment.dismissAllowingStateLoss();
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
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
}
