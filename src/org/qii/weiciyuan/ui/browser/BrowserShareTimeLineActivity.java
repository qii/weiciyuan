package org.qii.weiciyuan.ui.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ShareListBean;
import org.qii.weiciyuan.dao.shorturl.ShareShortUrlTimeLineDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-2-21
 */
public class BrowserShareTimeLineActivity extends AbstractAppActivity {

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
                    .replace(android.R.id.content, new BrowserShareTimeLineFragment(url))
                    .commit();
        }
        int count = getIntent().getIntExtra("count", 0);
        String subTitle = String.format(getString(R.string.total_share_count), String.valueOf(count));
        getActionBar().setSubtitle(subTitle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

        }
        return false;
    }

    public static class BrowserShareTimeLineFragment extends AbstractMessageTimeLineFragment<ShareListBean> {

        private ShareListBean bean = new ShareListBean();
        private String url;


        public BrowserShareTimeLineFragment() {

        }

        public BrowserShareTimeLineFragment(String url) {
            this.url = url;
        }

        @Override
        public ShareListBean getList() {
            return bean;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("bean", bean);
            outState.putString("url", url);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            switch (getCurrentState(savedInstanceState)) {
                case FIRST_TIME_START:
                    getPullToRefreshListView().startRefreshNow();
                    break;
                case SCREEN_ROTATE:
                    //nothing
                    refreshLayout(bean);
                    break;
                case ACTIVITY_DESTROY_AND_CREATE:
                    getList().addNewData((ShareListBean) savedInstanceState.getSerializable("bean"));
                    url = savedInstanceState.getString("url");
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(bean);
                    break;
            }

        }


        @Override
        protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
            intent.putExtra("msg", getList().getItemList().get(position));
            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
            startActivityForResult(intent, 0);
        }

        @Override
        protected void newMsgOnPostExecute(ShareListBean newValue) {
            if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
                getList().addNewData(newValue);
                getAdapter().notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();
            }
        }

        @Override
        protected void oldMsgOnPostExecute(ShareListBean newValue) {
            if (newValue != null && newValue.getSize() > 0) {
                getList().addOldData(newValue);
                getAdapter().notifyDataSetChanged();
            }
        }

        @Override
        protected ShareListBean getDoInBackgroundNewData() throws WeiboException {
            return new ShareShortUrlTimeLineDao(GlobalContext.getInstance().getSpecialToken(), url).getGSONMsgList();
        }

        @Override
        protected ShareListBean getDoInBackgroundOldData() throws WeiboException {
            ShareShortUrlTimeLineDao dao = new ShareShortUrlTimeLineDao(GlobalContext.getInstance().getSpecialToken(), url);
            dao.setMaxId(getList().getItemList().get(getList().getSize() - 1).getId());
            return dao.getGSONMsgList();
        }

        @Override
        protected ShareListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
            return null;
        }
    }
}
