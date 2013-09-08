//package org.qii.weiciyuan.ui.maintimeline;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.Toast;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.bean.MessageListBean;
//import org.qii.weiciyuan.bean.UserBean;
//import org.qii.weiciyuan.dao.user.StatusesTimeLineDao;
//import org.qii.weiciyuan.support.database.MyStatusDBTask;
//import org.qii.weiciyuan.support.error.WeiboException;
//import org.qii.weiciyuan.support.lib.MyAsyncTask;
//import org.qii.weiciyuan.support.utils.GlobalContext;
//import org.qii.weiciyuan.support.utils.Utility;
//import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
//import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
//import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
//import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
//import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;
//
///**
//* User: qii
//* Date: 12-9-22
//*/
//public class MyStatussTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {
//
//    private DBCacheTask dbTask;
//
//    protected UserBean userBean;
//    protected String token;
//
//    private MessageListBean bean = new MessageListBean();
//
//    @Override
//    public MessageListBean getList() {
//        return bean;
//    }
//
//    public MyStatussTimeLineFragment() {
//
//    }
//
//
//    public MyStatussTimeLineFragment(UserBean userBean, String token) {
//        this.userBean = userBean;
//        this.token = token;
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//        inflater.inflate(R.menu.actionbar_menu_mystatustimelinefragment, menu);
//        menu.findItem(R.id.name).setTitle(getString(R.string.personal_info));
//
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//
//        commander = ((AbstractAppActivity) getActivity()).getBitmapDownloader();
//
//        switch (getCurrentState(savedInstanceState)) {
//            case FIRST_TIME_START:
//                if (Utility.isTaskStopped(dbTask)) {
//                    dbTask = new DBCacheTask();
//                    dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//                }
//                break;
//            case SCREEN_ROTATE:
//                //nothing
//                refreshLayout(getList());
//                break;
//            case ACTIVITY_DESTROY_AND_CREATE:
//                getList().addNewData((MessageListBean) savedInstanceState.getSerializable("bean"));
//                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
//                token = savedInstanceState.getString("token");
//                getAdapter().notifyDataSetChanged();
//                refreshLayout(bean);
//                break;
//        }
//
//
//        super.onActivityCreated(savedInstanceState);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent intent;
//        switch (item.getItemId()) {
//            case R.id.name:
//                intent = new Intent(getActivity(), MyInfoActivity.class);
//                intent.putExtra("token", token);
//                intent.putExtra("user", userBean);
//                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
//                startActivity(intent);
//                break;
//            case R.id.write_weibo:
//                intent = new Intent(getActivity(), WriteWeiboActivity.class);
//                intent.putExtra("token", token);
//                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
//                startActivity(intent);
//                break;
//        }
//        return true;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Utility.cancelTasks(dbTask);
//    }
//
//    private class DBCacheTask extends MyAsyncTask<Object, Object, Object> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            getPullToRefreshListView().setVisibility(View.INVISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object... params) {
//            getList().addNewData(MyStatusDBTask.get(GlobalContext.getInstance().getCurrentAccountId()));
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            getPullToRefreshListView().setVisibility(View.VISIBLE);
//            getAdapter().notifyDataSetChanged();
//            refreshLayout(getList());
//            super.onPostExecute(o);
//            /**
//             * when this account first open app,if he don't have any data in database,fetch data from server automally
//             */
//            if (getList().getSize() == 0) {
//                getPullToRefreshListView().startRefreshNow();
//            }
//        }
//    }
//
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putSerializable("bean", bean);
//        outState.putSerializable("userBean", userBean);
//        outState.putString("token", token);
//    }
//
//
//    @Override
//    protected MessageListBean getDoInBackgroundMiddleData(String beginId, String endId) throws WeiboException {
//        String id = userBean.getId();
//
//        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);
//
//        dao.setMax_id(beginId);
//        dao.setSince_id(endId);
//
//        MessageListBean result = dao.getGSONMsgList();
//
//        return result;
//    }
//
//
//    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
//        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
//        intent.putExtra("token", token);
//        intent.putExtra("msg", bean.getItem(position));
//        startActivity(intent);
//    }
//
//
//    @Override
//    protected void newMsgOnPostExecute(MessageListBean newValue) {
//        if (getActivity() != null && newValue.getSize() > 0) {
//            getList().addNewData(newValue);
//            getAdapter().notifyDataSetChanged();
//            getListView().setSelectionAfterHeaderView();
//
//
//        }
//
//    }
//
//    @Override
//    protected void oldMsgOnPostExecute(MessageListBean newValue) {
//        if (newValue != null && newValue.getSize() > 1) {
//            getList().addOldData(newValue);
//
//        } else {
//            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    @Override
//    protected MessageListBean getDoInBackgroundNewData() throws WeiboException {
//
//        String id = userBean.getId();
//        String screenName = userBean.getScreen_name();
//
//        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);
//
//        if (TextUtils.isEmpty(id)) {
//            dao.setScreen_name(screenName);
//        }
//
//        if (getList().getSize() > 0) {
//            dao.setSince_id(getList().getItem(0).getId());
//        }
//        MessageListBean result = dao.getGSONMsgList();
//
//        MyStatusDBTask.add(result, GlobalContext.getInstance().getCurrentAccountId());
//
//        return result;
//    }
//
//    @Override
//    protected MessageListBean getDoInBackgroundOldData() throws WeiboException {
//        String id = userBean.getId();
//        String screenName = userBean.getScreen_name();
//
//        StatusesTimeLineDao dao = new StatusesTimeLineDao(token, id);
//        if (TextUtils.isEmpty(id)) {
//            dao.setScreen_name(screenName);
//        }
//        if (getList().getSize() > 0) {
//            dao.setMax_id(getList().getItemList().get(getList().getSize() - 1).getId());
//        }
//        MessageListBean result = dao.getGSONMsgList();
//
//        return result;
//    }
//
//}
