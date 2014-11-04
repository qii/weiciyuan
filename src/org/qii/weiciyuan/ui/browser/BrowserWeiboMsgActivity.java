package org.qii.weiciyuan.ui.browser;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.destroy.DestroyStatusDao;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.common.CommonErrorDialogFragment;
import org.qii.weiciyuan.ui.common.CommonProgressDialogFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.loader.AbstractAsyncNetRequestTaskLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.WriteCommentActivity;
import org.qii.weiciyuan.ui.send.WriteRepostActivity;
import org.qii.weiciyuan.ui.task.FavAsyncTask;
import org.qii.weiciyuan.ui.task.UnFavAsyncTask;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.Toast;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class BrowserWeiboMsgActivity extends AbstractAppActivity
        implements RemoveWeiboMsgDialog.IRemove {

    private static final String ACTION_WITH_ID = "action_with_id";
    private static final String ACTION_WITH_DETAIL = "action_with_detail";

    private static final int REFRESH_LOADER_ID = 0;

    private MessageBean msg;
    private String msgId;
    private String token;

    private FavAsyncTask favTask = null;
    private UnFavAsyncTask unFavTask = null;
    private RemoveTask removeTask;

    private ShareActionProvider shareActionProvider;
    private GestureDetector gestureDetector;

    public static Intent newIntent(String weiboId, String token) {
        Intent intent = new Intent(GlobalContext.getInstance(), BrowserWeiboMsgActivity.class);
        intent.putExtra("weiboId", weiboId);
        intent.putExtra("token", token);
        intent.setAction(ACTION_WITH_ID);
        return intent;
    }

    public static Intent newIntent(MessageBean msg, String token) {
        Intent intent = new Intent(GlobalContext.getInstance(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", msg);
        intent.putExtra("token", token);
        intent.setAction(ACTION_WITH_DETAIL);
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("msg", msg);
        outState.putString("token", token);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        if (savedInstanceState != null) {
            msg = savedInstanceState.getParcelable("msg");
            token = savedInstanceState.getString("token");
            if (msg != null) {
                buildContent();
            } else {
                msgId = getIntent().getStringExtra("weiboId");
                fetchUserInfoFromServer();
            }
        } else {

            String action = getIntent().getAction();
            if (ACTION_WITH_ID.equalsIgnoreCase(action)) {
                token = getIntent().getStringExtra("token");
                msgId = getIntent().getStringExtra("weiboId");
                fetchUserInfoFromServer();
                findViewById(android.R.id.content).setBackgroundDrawable(
                        ThemeUtility.getDrawable(android.R.attr.windowBackground));
            } else if (ACTION_WITH_DETAIL.equalsIgnoreCase(action)) {
                Intent intent = getIntent();
                token = intent.getStringExtra("token");
                msg = intent.getParcelableExtra("msg");
                buildContent();
            } else {
                throw new IllegalArgumentException(
                        "activity intent action must be " + ACTION_WITH_DETAIL + " or "
                                + ACTION_WITH_ID);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(removeTask);
    }

    private void fetchUserInfoFromServer() {
        getActionBar().setTitle(getString(R.string.fetching_weibo_info));
        CommonProgressDialogFragment dialog = CommonProgressDialogFragment
                .newInstance(getString(R.string.fetching_weibo_info));
        getSupportFragmentManager().beginTransaction()
                .add(dialog, CommonProgressDialogFragment.class.getName()).commit();
        getSupportLoaderManager().initLoader(REFRESH_LOADER_ID, null, refreshCallback);
    }

    private void initLayout() {
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
    }

    private void buildContent() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getSupportFragmentManager()
                        .findFragmentByTag(BrowserWeiboMsgFragment.class.getName())
                        == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, BrowserWeiboMsgFragment.newInstance(msg),
                                    BrowserWeiboMsgFragment.class.getName())
                            .commitAllowingStateLoss();
                    getSupportFragmentManager().executePendingTransactions();
                    findViewById(android.R.id.content).setBackgroundDrawable(null);
                }
            }
        });

        getActionBar().setTitle(getString(R.string.detail));

        invalidateOptionsMenu();
    }

    private Fragment getBrowserWeiboMsgFragment() {
        return getSupportFragmentManager()
                .findFragmentByTag(BrowserWeiboMsgFragment.class.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (msg == null) {
            return super.onCreateOptionsMenu(menu);
        }

        getMenuInflater().inflate(R.menu.actionbar_menu_browserweibomsgactivity, menu);

        if (msg.getUser() != null && msg.getUser().getId()
                .equals(GlobalContext.getInstance().getCurrentAccountId())) {
            menu.findItem(R.id.menu_delete).setVisible(true);
        }

        MenuItem item = menu.findItem(R.id.menu_share);
        shareActionProvider = (ShareActionProvider) item.getActionProvider();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = MainTimeLineActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_repost:
                intent = new Intent(this, WriteRepostActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("id", getMsg().getId());
                intent.putExtra("msg", getMsg());
                startActivity(intent);
                return true;
            case R.id.menu_comment:
                intent = new Intent(this, WriteCommentActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("id", getMsg().getId());
                intent.putExtra("msg", getMsg());
                startActivity(intent);
                return true;
            case R.id.menu_share:
                buildShareActionMenu();
                return true;
            case R.id.menu_copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", getMsg().getText()));
                Toast.makeText(this, getString(R.string.copy_successfully), Toast.LENGTH_SHORT)
                        .show();
                return true;
            case R.id.menu_fav:
                if (Utility.isTaskStopped(favTask) && Utility.isTaskStopped(unFavTask)) {
                    favTask = new FavAsyncTask(getToken(), msg.getId());
                    favTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                return true;
            case R.id.menu_unfav:
                if (Utility.isTaskStopped(favTask) && Utility.isTaskStopped(unFavTask)) {
                    unFavTask = new UnFavAsyncTask(getToken(), msg.getId());
                    unFavTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                return true;
            case R.id.menu_delete:
                RemoveWeiboMsgDialog dialog = new RemoveWeiboMsgDialog(msg.getId());
                dialog.show(getFragmentManager(), "");
                return true;
        }
        return false;
    }

    private void buildShareActionMenu() {
        Utility.setShareIntent(BrowserWeiboMsgActivity.this, shareActionProvider, msg);
    }

    @Override
    public void removeMsg(String id) {
        if (Utility.isTaskStopped(removeTask)) {
            removeTask = new RemoveTask(id);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void updateCommentCount(int count) {
        msg.setComments_count(count);
        Intent intent = new Intent();
        intent.putExtra("msg", msg);
        setResult(0, intent);
    }

    public void updateRepostCount(int count) {
        msg.setReposts_count(count);
        Intent intent = new Intent();
        intent.putExtra("msg", msg);
        setResult(0, intent);
    }

    public String getToken() {
        return token;
    }

    public MessageBean getMsg() {
        return msg;
    }

    class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {

        String id;
        WeiboException e;

        public RemoveTask(String id) {
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DestroyStatusDao dao = new DestroyStatusDao(token, id);
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
                Toast.makeText(BrowserWeiboMsgActivity.this, e.getError(), Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                finish();
            }
        }
    }

    private static class RefreshLoader extends AbstractAsyncNetRequestTaskLoader<MessageBean> {

        private String msgId;

        public RefreshLoader(Context context, String msgId) {
            super(context);
            this.msgId = msgId;
        }

        @Override
        protected MessageBean loadData() throws WeiboException {
            return new ShowStatusDao(GlobalContext.getInstance().getSpecialToken(), msgId).getMsg();
        }
    }

    private LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<MessageBean>> refreshCallback
            = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<MessageBean>>() {
        @Override
        public Loader<AsyncTaskLoaderResult<MessageBean>> onCreateLoader(int id, Bundle args) {
            return new RefreshLoader(BrowserWeiboMsgActivity.this, msgId);
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<MessageBean>> loader,
                AsyncTaskLoaderResult<MessageBean> result) {
            MessageBean data = result != null ? result.data : null;
            final WeiboException exception = result != null ? result.exception : null;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    CommonProgressDialogFragment dialog
                            = (CommonProgressDialogFragment) getSupportFragmentManager()
                            .findFragmentByTag(CommonProgressDialogFragment.class.getName());
                    if (dialog != null) {
                        dialog.dismiss();
                    }

                    if (exception != null) {
                        CommonErrorDialogFragment userInfoActivityErrorDialog
                                = CommonErrorDialogFragment.newInstance(exception.getError());
                        getSupportFragmentManager().beginTransaction()
                                .add(userInfoActivityErrorDialog,
                                        CommonErrorDialogFragment.class.getName()).commit();
                    }
                }
            });

            if (data != null) {
                BrowserWeiboMsgActivity.this.msg = data;
                buildContent();
            }
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<MessageBean>> loader) {

        }
    };
}
