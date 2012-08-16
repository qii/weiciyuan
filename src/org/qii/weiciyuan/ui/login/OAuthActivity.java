package org.qii.weiciyuan.ui.login;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.weibo.Utility;
import org.qii.weiciyuan.weibo.WeiboParameters;

/**
 * User: qii
 * Date: 12-7-28
 */
public class OAuthActivity extends AbstractAppActivity {

    public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
    public static final String APP_KEY = "1065511513";
    private static final String CONSUMER_SECRET = "df428e88aae8bd31f20481d149c856ed";
    private static final String DIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    private WebView webView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);


        webView.loadUrl(getWeiboOAuthUrl());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.oauthactivity_menu, menu);
        return true;
    }

    public void refresh(MenuItem menu) {

        webView.loadUrl(getWeiboOAuthUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getWeiboOAuthUrl() {
        WeiboParameters parameters = new WeiboParameters();
        parameters.add("client_id", APP_KEY);
        parameters.add("response_type", "token");
        parameters.add("redirect_uri", DIRECT_URL);
        parameters.add("display", "mobile");
        return URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
    }

    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (url.startsWith(DIRECT_URL)) {

                handleRedirectUrl(view, url);
                view.stopLoading();
                return;
            }
            super.onPageStarted(view, url, favicon);

        }


    }

    private void handleRedirectUrl(WebView view, String url) {
        Bundle values = Utility.parseUrl(url);

        String error = values.getString("error");
        String error_code = values.getString("error_code");

        Intent intent = new Intent();
        intent.putExtras(values);

        if (error == null && error_code == null) {

            String access_token = values.getString("access_token");
            setResult(0, intent);
            new OAuthTask().execute(access_token);

        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class OAuthTask extends AsyncTask<String, UserBean, DBResult> {


        ProgressFragment progressFragment = ProgressFragment.newInstance();

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected DBResult doInBackground(String... params) {

            String token = params[0];

            UserBean user = new OAuthDao(token).getOAuthUserInfo();
            if (user != null) {
                AccountBean account = new AccountBean();
                account.setAccess_token(token);
                account.setUsername(user.getName());
                account.setUid(user.getId());
                account.setUsernick(user.getScreen_name());
                account.setAvatar_url(user.getProfile_image_url());
                account.setInfo(user);

                return DatabaseManager.getInstance().addOrUpdateAccount(account);
            } else {
                cancel(true);
                return null;
            }


        }

        @Override
        protected void onCancelled(DBResult dbResult) {
            super.onCancelled(dbResult);
            if (progressFragment.isVisible()) {
                progressFragment.dismissAllowingStateLoss();
            }
            Toast.makeText(OAuthActivity.this, "retry it", Toast.LENGTH_SHORT).show();
            webView.loadUrl(getWeiboOAuthUrl());
        }

        @Override
        protected void onPostExecute(DBResult dbResult) {
            if (progressFragment.isVisible()) {
                progressFragment.dismissAllowingStateLoss();
            }
            switch (dbResult) {
                case add_successfuly:
                    Toast.makeText(OAuthActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    break;
                case update_successfully:
                    Toast.makeText(OAuthActivity.this, getString(R.string.update_account_success), Toast.LENGTH_SHORT).show();
                    break;
            }
            finish();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            webView.stopLoading();
    }

    static class ProgressFragment extends DialogFragment {

        AsyncTask asyncTask = null;

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
            dialog.setMessage("授权中");
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

        void setAsyncTask(AsyncTask task) {
            asyncTask = task;
        }
    }

    public static enum DBResult {
        add_successfuly, update_successfully
    }
}
