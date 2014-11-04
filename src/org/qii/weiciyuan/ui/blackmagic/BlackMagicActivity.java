package org.qii.weiciyuan.ui.blackmagic;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.BMOAuthDao;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * User: qii
 * Date: 12-11-9
 */
public class BlackMagicActivity extends AbstractAppActivity {

    private EditText username;
    private EditText password;
    private Spinner spinner;

    private String appkey;
    private String appSecret;

    private LoginTask loginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blackmagicactivity_layout);

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setTitle(getString(R.string.hack_login));

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        spinner = (Spinner) findViewById(R.id.spinner);

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.tail,
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] array = getResources().getStringArray(R.array.tail_value);
                String value = array[position];
                appkey = value.substring(0, value.indexOf(","));
                appSecret = value.substring(value.indexOf(",") + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(loginTask);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_blackmagicactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                if (username.getText().toString().length() == 0) {
                    username.setError(getString(R.string.email_cant_be_empty));
                    return true;
                }

                if (password.getText().toString().length() == 0) {
                    password.setError(getString(R.string.password_cant_be_empty));
                    return true;
                }
                if (Utility.isTaskStopped(loginTask)) {
                    loginTask = new LoginTask(this, username.getText().toString(),
                            password.getText().toString(), appkey, appSecret);
                    loginTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class LoginTask extends MyAsyncTask<Void, Void, String[]> {

        private WeiboException e;
        private ProgressFragment progressFragment = ProgressFragment.newInstance();
        private WeakReference<BlackMagicActivity> mBlackMagicActivityWeakReference;

        private String username;
        private String password;

        private String appkey;
        private String appSecret;

        private LoginTask(BlackMagicActivity activity, String username, String password,
                String appkey, String appSecret) {
            mBlackMagicActivityWeakReference = new WeakReference<BlackMagicActivity>(activity);
            this.username = username;
            this.password = password;
            this.appkey = appkey;
            this.appSecret = appSecret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressFragment.setAsyncTask(LoginTask.this);

            BlackMagicActivity activity = mBlackMagicActivityWeakReference.get();
            if (activity != null) {
                progressFragment.show(activity.getSupportFragmentManager(), "");
            }
        }

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                String[] result = new BMOAuthDao(username,
                        password, appkey, appSecret).login();
                UserBean user = new OAuthDao(result[0]).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccess_token(result[0]);
                account.setInfo(user);
                account.setExpires_time(
                        System.currentTimeMillis() + Long.valueOf(result[1]) * 1000);
                AccountDBTask.addOrUpdateAccount(account, true);
                AppLogger
                        .e("token expires in " + Utility.calcTokenExpiresInDays(account) + " days");
                return result;
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(String[] s) {
            super.onCancelled(s);
            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
            }

            BlackMagicActivity activity = mBlackMagicActivityWeakReference.get();
            if (activity == null) {
                return;
            }

            if (e != null) {
                Toast.makeText(activity, e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
            }

            BlackMagicActivity activity = mBlackMagicActivityWeakReference.get();
            if (activity == null) {
                return;
            }

            Bundle values = new Bundle();
            values.putString("expires_in", s[1]);
            Intent intent = new Intent();
            intent.putExtras(values);
            activity.setResult(RESULT_OK, intent);
            activity.finish();
        }
    }

    public static class ProgressFragment extends DialogFragment {

        private MyAsyncTask asyncTask = null;

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true);
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.logining));
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
            super.onCancel(dialog);
        }

        void setAsyncTask(MyAsyncTask task) {
            asyncTask = task;
        }
    }
}
