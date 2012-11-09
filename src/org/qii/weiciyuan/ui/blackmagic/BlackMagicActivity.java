package org.qii.weiciyuan.ui.blackmagic;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.BMOAuthDao;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.login.AccountActivity;

/**
 * User: qii
 * Date: 12-11-9
 */
public class BlackMagicActivity extends AbstractAppActivity {

    private EditText username;
    private EditText password;
    private Button button;
    private Spinner spinner;

    private String appkey;
    private String appSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blackmagicactivity_layout);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        button = (Button) findViewById(R.id.button);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Task().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

            }
        });
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class Task extends MyAsyncTask<Void, Void, String> {

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
                DatabaseManager.getInstance().addOrUpdateAccount(account);
                return token;
            } catch (WeiboException e) {

            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(BlackMagicActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }
}
