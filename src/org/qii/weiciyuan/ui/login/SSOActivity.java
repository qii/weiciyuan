package org.qii.weiciyuan.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.sinasso.SsoHandler;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 13-6-18
 */
public class SSOActivity extends AbstractAppActivity {

    private SSOTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.official_app_login);
        SsoHandler mSsoHandler = new SsoHandler(SSOActivity.this);
        mSsoHandler.authorize();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utility.cancelTasks(task);
    }

    private class SSOTask extends MyAsyncTask<String, UserBean, OAuthActivity.DBResult> {

        WeiboException e;

        OAuthActivity.ProgressFragment progressFragment = OAuthActivity.ProgressFragment.newInstance();

        private String token;
        private String expiresIn;


        public SSOTask(String token, String expiresIn) {
            this.token = token;
            this.expiresIn = expiresIn;
        }

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);
            progressFragment.show(getSupportFragmentManager(), "");

        }

        @Override
        protected OAuthActivity.DBResult doInBackground(String... params) {

            try {
                UserBean user = new OAuthDao(token).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccess_token(token);
                account.setExpires_time(System.currentTimeMillis() + Long.valueOf(expiresIn) * 1000);
                account.setInfo(user);
                AppLogger.e("token expires in " + Utility.calcTokenExpiresInDays(account) + " days");
                return AccountDBTask.addOrUpdateAccount(account, false);
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }

        }

        @Override
        protected void onCancelled(OAuthActivity.DBResult dbResult) {
            super.onCancelled(dbResult);
            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
            }
            if (e != null)
                Toast.makeText(SSOActivity.this, e.getError(), Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(OAuthActivity.DBResult dbResult) {
            if (progressFragment.isVisible()) {
                progressFragment.dismissAllowingStateLoss();
            }
            switch (dbResult) {
                case add_successfuly:
                    Bundle values = new Bundle();
                    values.putString("expires_in", expiresIn);
                    Intent intent = new Intent();
                    intent.putExtras(values);
                    setResult(RESULT_OK, intent);
                    finish();
                    Toast.makeText(SSOActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    break;
                case update_successfully:
                    Toast.makeText(SSOActivity.this, getString(R.string.update_account_success), Toast.LENGTH_SHORT).show();
                    break;
            }
            finish();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }


        // Check OAuth 2.0/2.10 error code.
        String error = data.getStringExtra("error");
        if (error == null) {
            error = data.getStringExtra("error_type");
        }

        // error occurred.
        if (error != null) {
            if (error.equals("access_denied")
                    || error.equals("OAuthAccessDeniedException")) {
                Log.d("Weibo-authorize", "Login canceled by user.");

            } else {
                String description = data
                        .getStringExtra("error_description");
                if (description != null) {
                    error = error + ":" + description;
                }
                Log.d("Weibo-authorize", "Login failed: " + error);

            }
            return;

        }

        final String KEY_TOKEN = "access_token";
        final String KEY_EXPIRES = "expires_in";
        final String KEY_REFRESHTOKEN = "refresh_token";

        String token = data.getStringExtra(KEY_TOKEN);
        String expires = data
                .getStringExtra(KEY_EXPIRES);
        String refreshToken = data.getStringExtra(KEY_REFRESHTOKEN);

        if (Utility.isTaskStopped(task)) {
            task = new SSOTask(token, expires);
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

}
