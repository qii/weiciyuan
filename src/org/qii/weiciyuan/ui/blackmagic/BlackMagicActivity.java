package org.qii.weiciyuan.ui.blackmagic;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.BMOAuthDao;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.login.AccountActivity;

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

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.hack_login));

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
            case android.R.id.home:
                Intent intent = new Intent(this, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("launcher", false);
                startActivity(intent);
                return true;
            case R.id.menu_login:
                if (Utility.isTaskStopped(loginTask)) {
                    loginTask = new LoginTask();
                    loginTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private class LoginTask extends MyAsyncTask<Void, Void, String> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (username.getText().toString().length() == 0) {
                username.setError("用户名不能为空");
                cancel(true);
            }

            if (password.getText().toString().length() == 0) {
                password.setError("密码不能为空");
                cancel(true);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String token = new BMOAuthDao(username.getText().toString(), password.getText().toString(), appkey, appSecret).login();
                UserBean user = new OAuthDao(token).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccess_token(token);
                account.setUsername(user.getName());
                account.setUid(user.getId());
                account.setUsernick(user.getScreen_name());
                account.setAvatar_url(user.getProfile_image_url());
                account.setInfo(user);
                AccountDBTask.addOrUpdateAccount(account);
                return token;
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return "";
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            if (e != null)
                Toast.makeText(BlackMagicActivity.this, e.getError(), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setResult(RESULT_OK, null);
            finish();
        }
    }
}
