package org.qii.weiciyuan.ui.browser;

import org.qii.weiciyuan.bean.ShareListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.loader.BrowserShareMsgLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

/**
 * User: qii
 * Date: 13-2-21
 */
public class BrowserShareTimeLineActivity extends AbstractAppActivity {

    public static Intent newIntent(String url) {
        Intent intent = new Intent(GlobalContext.getInstance(), BrowserShareTimeLineActivity.class);
        intent.putExtra("url", url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("url");
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(url);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, BrowserShareTimeLineFragment.newInstance(url))
                    .commit();
        }
// 0.50 feature
//        int count = getIntent().getIntExtra("count", 0);
//        String subTitle = String.format(getString(R.string.total_share_count), String.valueOf(count));
//        getActionBar().setSubtitle(subTitle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = MainTimeLineActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }

    public static class BrowserShareTimeLineFragment
            extends AbstractMessageTimeLineFragment<ShareListBean> {

        private ShareListBean bean = new ShareListBean();
        private String url;

        public static BrowserShareTimeLineFragment newInstance(String url) {
            BrowserShareTimeLineFragment fragment = new BrowserShareTimeLineFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            fragment.setArguments(bundle);
            return fragment;
        }

        public BrowserShareTimeLineFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            url = getArguments().getString("url");
        }

        @Override
        public ShareListBean getList() {
            return bean;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable("bean", bean);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            switch (getCurrentState(savedInstanceState)) {
                case FIRST_TIME_START:
                    getPullToRefreshListView().setRefreshing();
                    loadNewMsg();
                    break;
                case SCREEN_ROTATE:
                    //nothing
                    refreshLayout(bean);
                    break;
                case ACTIVITY_DESTROY_AND_CREATE:
                    getList().addNewData((ShareListBean) savedInstanceState.getParcelable("bean"));
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(bean);
                    break;
            }
        }

        @Override
        protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
            startActivityForResult(
                    BrowserWeiboMsgActivity.newIntent(getList().getItemList().get(position),
                            GlobalContext.getInstance().getSpecialToken()), 0);
        }

        @Override
        protected void newMsgLoaderSuccessCallback(ShareListBean newValue, Bundle loaderArgs) {
            if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
                getList().addNewData(newValue);
                getAdapter().notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();
            }
        }

        @Override
        protected void oldMsgLoaderSuccessCallback(ShareListBean newValue) {
            if (newValue != null && newValue.getSize() > 0) {
                getList().addOldData(newValue);
                getAdapter().notifyDataSetChanged();
            }
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

        protected Loader<AsyncTaskLoaderResult<ShareListBean>> onCreateNewMsgLoader(int id,
                Bundle args) {
            String token = GlobalContext.getInstance().getSpecialToken();
            String sinceId = null;
            if (getList().getItemList().size() > 0) {
                sinceId = getList().getItemList().get(0).getId();
            }
            return new BrowserShareMsgLoader(getActivity(), token, url, null);
        }

        protected Loader<AsyncTaskLoaderResult<ShareListBean>> onCreateOldMsgLoader(int id,
                Bundle args) {
            String token = GlobalContext.getInstance().getSpecialToken();
            String maxId = null;
            if (getList().getItemList().size() > 0) {
                maxId = getList().getItemList().get(getList().getItemList().size() - 1).getId();
            }
            return new BrowserShareMsgLoader(getActivity(), token, url, maxId);
        }
    }
}
