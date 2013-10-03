package org.qii.weiciyuan.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.group.ModifyGroupMemberDao;
import org.qii.weiciyuan.dao.relationship.FanDao;
import org.qii.weiciyuan.dao.relationship.FriendshipsDao;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.dao.user.RemarkDao;
import org.qii.weiciyuan.support.database.FilterDBTask;
import org.qii.weiciyuan.support.error.ErrorCode;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.MyViewPager;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;
import org.qii.weiciyuan.ui.loader.AbstractAsyncNetRequestTaskLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class UserInfoActivity extends AbstractAppActivity implements IUserInfo {
    private String token;

    private UserBean bean;

    private MyViewPager mViewPager = null;

    private MyAsyncTask<Void, UserBean, UserBean> followOrUnfollowTask;

    private ModifyGroupMemberTask modifyGroupMemberTask;

    private GestureDetector gestureDetector;


    private static final int REFRESH_LOADER_ID = 0;


    public String getToken() {
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();
        return token;
    }

    @Override
    public UserBean getUser() {
        return bean;
    }

    public void setUser(UserBean bean) {
        this.bean = bean;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(followOrUnfollowTask, modifyGroupMemberTask);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        token = getIntent().getStringExtra("token");
        bean = getIntent().getParcelableExtra("user");
        if (bean == null) {
            String id = getIntent().getStringExtra("id");
            if (!TextUtils.isEmpty(id)) {
                bean = new UserBean();
                bean.setId(id);
            } else {
                String domain = getIntent().getStringExtra("domain");
                if (!TextUtils.isEmpty(domain)) {
                    bean = new UserBean();
                    bean.setDomain(domain);
                } else {
                    Uri data = getIntent().getData();
                    if (data != null) {
                        String d = data.toString();
                        int index = d.lastIndexOf("@");
                        String newValue = d.substring(index + 1);
                        bean = new UserBean();
                        bean.setScreen_name(newValue);
                    } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
                        processIntent(getIntent());
                    }
                }
            }
            fetchUserInfoFromServer();
        } else {
            buildContent();
        }


        if (isMyselfProfile()) {
            if (getClass() == MyInfoActivity.class) {
                return;
            }
            Intent intent = new Intent(this, MyInfoActivity.class);
            intent.putExtra("token", getToken());

            UserBean userBean = new UserBean();
            userBean.setId(GlobalContext.getInstance().getCurrentAccountId());
            intent.putExtra("user", bean);
            intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
            startActivity(intent);
            finish();
        }


    }

    private boolean isMyselfProfile() {
        boolean screenNameEqualCurrentAccount = bean.getScreen_name() != null
                && bean.getScreen_name().equals(GlobalContext.getInstance().getCurrentAccountName());
        boolean idEqualCurrentAccount = bean.getId() != null && bean.getId().equals(GlobalContext.getInstance().getCurrentAccountId());
        return screenNameEqualCurrentAccount || idEqualCurrentAccount;
    }

    private void fetchUserInfoFromServer() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(bean.getScreen_name());
        FetchingDataDialog dialog = new FetchingDataDialog();
        getSupportFragmentManager().beginTransaction().add(dialog, FetchingDataDialog.class.getName()).commit();
        getSupportLoaderManager().initLoader(REFRESH_LOADER_ID, null, refreshCallback);
    }

    private void initLayout() {
//        getWindow().setBackgroundDrawable(getResources().getDrawable(R.color.transparent));

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setTitle(getString(R.string.personal_info));
    }

    private void buildContent() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (getSupportFragmentManager().findFragmentByTag(NewUserInfoFragment.class.getName()) == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new NewUserInfoFragment(getUser(), getToken()), NewUserInfoFragment.class.getName())
                            .commit();
                }
            }
        });


    }

    private void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_SHORT).show();
        bean = new UserBean();
        bean.setScreen_name(new String(msg.getRecords()[0].getPayload()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (isMyselfProfile()) {

            getMenuInflater().inflate(R.menu.actionbar_menu_myinfoactivity, menu);
            MenuItem edit = menu.findItem(R.id.menu_edit);
            edit.setVisible(GlobalContext.getInstance().getAccountBean().isBlack_magic());
        } else {
            getMenuInflater().inflate(R.menu.actionbar_menu_infofragment, menu);
            if (bean.isFollowing()) {
                menu.findItem(R.id.menu_follow).setVisible(false);
                menu.findItem(R.id.menu_unfollow).setVisible(true);
                menu.findItem(R.id.menu_manage_group).setVisible(true);
            } else {
                menu.findItem(R.id.menu_follow).setVisible(true);
                menu.findItem(R.id.menu_unfollow).setVisible(false);
                menu.findItem(R.id.menu_manage_group).setVisible(false);
            }

            if (!bean.isFollowing() && bean.isFollow_me()) {
                menu.findItem(R.id.menu_remove_fan).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remove_fan).setVisible(false);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_edit:
                intent = new Intent(this, EditMyProfileActivity.class);
                intent.putExtra("userBean", GlobalContext.getInstance().getAccountBean().getInfo());
                startActivity(intent);
                return true;
            case R.id.menu_at:
                intent = new Intent(this, WriteWeiboActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("content", "@" + bean.getScreen_name());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
                break;
            case R.id.menu_modify_remark:
                UpdateRemarkDialog dialog = new UpdateRemarkDialog();
                dialog.show(getFragmentManager(), "");
                break;
            case R.id.menu_follow:
                if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    followOrUnfollowTask = new FollowTask();
                    followOrUnfollowTask.execute();
                }
                break;
            case R.id.menu_unfollow:
                if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    followOrUnfollowTask = new UnFollowTask();
                    followOrUnfollowTask.execute();
                }
                break;
            case R.id.menu_remove_fan:
                if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    followOrUnfollowTask = new RemoveFanTask();
                    followOrUnfollowTask.execute();
                }
                break;
            case R.id.menu_add_to_app_filter:
                if (!TextUtils.isEmpty(bean.getScreen_name())) {
                    FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_USER, bean.getScreen_name());
                    Toast.makeText(this, getString(R.string.filter_successfully), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_manage_group:
                manageGroup();
                break;
        }
        return false;
    }

    public void updateRemark(String remark) {

        new UpdateRemarkTask(remark).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }


    private NewUserInfoFragment getInfoFragment() {
        return ((NewUserInfoFragment) getSupportFragmentManager().findFragmentByTag(
                NewUserInfoFragment.class.getName()));
    }

    private void manageGroup() {
        ManageGroupDialog dialog = new ManageGroupDialog(GlobalContext.getInstance().getGroup(), bean.getId());
        dialog.show(getSupportFragmentManager(), "");

    }

    public void handleGroup(List<String> add, List<String> remove) {
        if (Utility.isTaskStopped(modifyGroupMemberTask)) {
            modifyGroupMemberTask = new ModifyGroupMemberTask(add, remove);
            modifyGroupMemberTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class ModifyGroupMemberTask extends MyAsyncTask<Void, Void, Void> {
        List<String> add;
        List<String> remove;

        public ModifyGroupMemberTask(List<String> add, List<String> remove) {
            this.add = add;
            this.remove = remove;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ModifyGroupMemberDao dao = new ModifyGroupMemberDao(token, bean.getId());
            for (String id : add) {
                try {
                    dao.add(id);
                } catch (WeiboException e) {
                    AppLogger.e(e.getMessage());
                    cancel(true);
                }
            }
            for (String id : remove) {
                try {
                    dao.delete(id);
                } catch (WeiboException e) {
                    AppLogger.e(e.getMessage());
                    cancel(true);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(UserInfoActivity.this, getString(R.string.modify_successfully), Toast.LENGTH_SHORT).show();
        }
    }

    private class UnFollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(getToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }

            try {
                return dao.unFollowIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.unfollow_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            bean.setFollowing(false);
            invalidateOptionsMenu();
        }
    }


    private class FollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(getToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }
            try {
                return dao.followIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                switch (e.getError_code()) {
                    case ErrorCode.ALREADY_FOLLOWED:

                        break;
                }

            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.follow_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            bean.setFollowing(true);
            invalidateOptionsMenu();
            manageGroup();
        }
    }


    private class RemoveFanTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FanDao dao = new FanDao(getToken(), bean.getId());

            try {
                return dao.removeFan();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(UserInfoActivity.this, getString(R.string.remove_fan_successfully), Toast.LENGTH_SHORT).show();
            bean = o;
            getInfoFragment().forceReloadData(o);
        }
    }


    class UpdateRemarkTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;
        String remark;

        UpdateRemarkTask(String remark) {
            this.remark = remark;
        }


        @Override
        protected UserBean doInBackground(Void... params) {
            try {
                return new RemarkDao(getToken(), bean.getId(), remark).updateRemark();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (this.e != null) {
                Toast.makeText(UserInfoActivity.this, this.e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            bean = userBean;
            if (getInfoFragment() != null)
                getInfoFragment().forceReloadData(userBean);

        }
    }


    public static class UserInfoActivityErrorDialog extends DialogFragment {

        private String error;

        public UserInfoActivityErrorDialog() {

        }

        public UserInfoActivityErrorDialog(String error) {
            this.error = error;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("error", error);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            if (savedInstanceState != null) {
                this.error = savedInstanceState.getString("error");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.something_wrong))
                    .setMessage(this.error)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    });
            return builder.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            getActivity().finish();
        }
    }


    private static class RefreshLoader extends AbstractAsyncNetRequestTaskLoader<UserBean> {

        private UserBean bean;

        public RefreshLoader(Context context, UserBean userBean) {
            super(context);
            this.bean = userBean;
        }

        @Override
        protected UserBean loadData() throws WeiboException {
            ShowUserDao dao = new ShowUserDao(GlobalContext.getInstance().getSpecialToken());
            boolean haveId = !TextUtils.isEmpty(bean.getId());
            boolean haveName = !TextUtils.isEmpty(bean.getScreen_name());
            boolean haveDomain = !TextUtils.isEmpty(bean.getDomain());

            if (haveId) {
                dao.setUid(bean.getId());
            } else if (haveName) {
                dao.setScreen_name(bean.getScreen_name());
            } else if (haveDomain) {
                dao.setDomain(bean.getDomain());
            } else {
                return null;
            }

            return dao.getUserInfo();
        }
    }


    private LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserBean>> refreshCallback = new LoaderManager.LoaderCallbacks<AsyncTaskLoaderResult<UserBean>>() {
        @Override
        public Loader<AsyncTaskLoaderResult<UserBean>> onCreateLoader(int id, Bundle args) {
            return new RefreshLoader(UserInfoActivity.this, bean);
        }

        @Override
        public void onLoadFinished(Loader<AsyncTaskLoaderResult<UserBean>> loader, AsyncTaskLoaderResult<UserBean> result) {
            UserBean data = result != null ? result.data : null;
            final WeiboException exception = result != null ? result.exception : null;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    FetchingDataDialog dialog = (FetchingDataDialog) getSupportFragmentManager().findFragmentByTag(FetchingDataDialog.class.getName());
                    if (dialog != null) {
                        dialog.dismiss();
                    }

                    if (exception != null) {
                        UserInfoActivityErrorDialog userInfoActivityErrorDialog = new UserInfoActivityErrorDialog(exception.getError());
                        getSupportFragmentManager().beginTransaction().add(userInfoActivityErrorDialog, UserInfoActivityErrorDialog.class.getName()).commit();
                    }
                }
            });


            if (data != null) {
                bean = data;
                buildContent();
            }
            getLoaderManager().destroyLoader(loader.getId());
        }

        @Override
        public void onLoaderReset(Loader<AsyncTaskLoaderResult<UserBean>> loader) {

        }
    };

    public static class FetchingDataDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.fetching_user_info));
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            getActivity().finish();
        }
    }
}
