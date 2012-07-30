package org.qii.weiciyuan.ui.login;

import android.app.*;
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
import org.qii.weiciyuan.dao.OAuthDao;
import org.qii.weiciyuan.bean.WeiboAccount;
import org.qii.weiciyuan.bean.WeiboUser;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.weibo.Utility;
import org.qii.weiciyuan.weibo.WeiboParameters;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-28
 * Time: 下午1:44
 * To change this template use File | Settings | File Templates.
 */
public class OAuthActivity extends Activity {

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

    class OAuthTask extends AsyncTask<String, WeiboUser, Void> {


        ProgressFragment progressFragment = ProgressFragment.newInstance();

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);
            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected Void doInBackground(String... params) {

            String token = params[0];

            WeiboUser weiboUser = OAuthDao.getOAuthUserInfo(token);

            WeiboAccount weiboAccount = new WeiboAccount();
            weiboAccount.setAccess_token(token);
            weiboAccount.setUsername(weiboUser.getName());
            weiboAccount.setUid(weiboUser.getId());
            weiboAccount.setUsernick(weiboUser.getScreen_name());

            long result = DatabaseManager.getInstance().addAccount(weiboAccount);

            return null;
        }

        @Override
        protected void onPostExecute(Void weiboUser) {

            progressFragment.dismissAllowingStateLoss();
            Toast.makeText(OAuthActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            finish();

        }
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
}
