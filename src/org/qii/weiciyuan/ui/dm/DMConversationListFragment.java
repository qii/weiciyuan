package org.qii.weiciyuan.ui.dm;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.DMBean;
import org.qii.weiciyuan.bean.DMListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.dm.SendDMDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.SmileyPicker;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshListView;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.SmileyPickerUtility;
import org.qii.weiciyuan.ui.adapter.DMConversationAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.loader.DMConversationLoader;
import org.qii.weiciyuan.ui.widgets.QuickSendProgressFragment;

import java.util.Collections;
import java.util.Comparator;

/**
 * User: qii
 * Date: 12-11-15
 */
public class DMConversationListFragment extends AbstractTimeLineFragment<DMListBean> {

    private UserBean userBean;

    private int page = 1;

    private DMListBean bean = new DMListBean();

    private EditText et;

    private SmileyPicker smiley;

    private LinearLayout mContainer;

    private ProgressBar dmProgressBar;

    private Comparator<DMBean> comparator = new Comparator<DMBean>() {
        @Override
        public int compare(DMBean a, DMBean b) {
            long aL = a.getIdLong();
            long bL = b.getIdLong();
            int result = 0;
            if (aL > bL) {
                result = -1;
            } else if (aL < bL) {
                result = 1;
            }
            return result;
        }
    };

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
        outState.putParcelable("bean", bean);
        outState.putParcelable("userBean", userBean);
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
                        if (getActivity() != null)
                            loadNewMsg();

                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().addNewData((DMListBean) savedInstanceState.getParcelable("bean"));
                userBean = (UserBean) savedInstanceState.getParcelable("userBean");
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
        //dirty hack.....in other list, progressbar is used to indicate loading local data; but in this list,
        //use a progressbar to indicate loading new data first time, maybe be refactored at 0.50 version
        progressBar = new ProgressBar(getActivity());
        dmProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listView);
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadOldMsg(null);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNewMsg();
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
                if (smiley.isShown()) {
                    hideSmileyPicker(true);
                } else {
                    showSmileyPicker(SmileyPickerUtility.isKeyBoardShow(getActivity()));
                }
            }
        });

        smiley = (SmileyPicker) view.findViewById(R.id.smiley_picker);
        smiley.setEditText(getActivity(), (ViewGroup) view.findViewById(R.id.root_layout), et);
        mContainer = (LinearLayout) view.findViewById(R.id.container);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker(true);
            }
        });

        buildListAdapter();
        return view;
    }

    private void showSmileyPicker(boolean showAnimation) {
        this.smiley.show(getActivity(), showAnimation);
        lockContainerHeight(SmileyPickerUtility.getAppContentHeight(getActivity()));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_dmconversationlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideSmileyPicker(boolean showKeyBoard) {
        if (this.smiley.isShown()) {
            if (showKeyBoard) {
                //this time softkeyboard is hidden
                LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
                localLayoutParams.height = smiley.getTop();
                localLayoutParams.weight = 0.0F;
                this.smiley.hide(getActivity());

                SmileyPickerUtility.showKeyBoard(et);
                et.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unlockContainerHeightDelayed();
                    }
                }, 200L);
            } else {
                this.smiley.hide(getActivity());
                unlockContainerHeightDelayed();
            }
        }

    }

    private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {

        ((LinearLayout.LayoutParams) mContainer.getLayoutParams()).weight = 1.0F;

    }


    private void send() {

        if (TextUtils.isEmpty(et.getText().toString())) {
            et.setError(getString(R.string.content_cant_be_empty));
            return;
        }

        new QuickCommentTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMConversationAdapter(this, getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }


    @Override
    protected void newMsgOnPostExecute(DMListBean newValue, Bundle loaderArgs) {
        dmProgressBar.setVisibility(View.INVISIBLE);

        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            Collections.sort(getList().getItemList(), comparator);
            getAdapter().notifyDataSetChanged();
            getListView().setSelection(bean.getSize() - 1);
        }

    }

    @Override
    protected void oldMsgOnPostExecute(DMListBean newValue) {
        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            Collections.sort(getList().getItemList(), comparator);
            getAdapter().notifyDataSetChanged();
            page++;
        }
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
                loadNewMsg();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }

    public boolean isSmileyPanelClosed() {
        return !smiley.isShown();
    }

    public void closeSmileyPanel() {
        hideSmileyPicker(false);
    }

    @Override
    public void loadNewMsg() {

        if (bean.getSize() == 0)
            dmProgressBar.setVisibility(View.VISIBLE);

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

    @Override
    protected Loader<AsyncTaskLoaderResult<DMListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        page = 1;
        return new DMConversationLoader(getActivity(), token, userBean.getId(), String.valueOf(page));
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<DMListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        return new DMConversationLoader(getActivity(), token, userBean.getId(), String.valueOf(page + 1));
    }


}
