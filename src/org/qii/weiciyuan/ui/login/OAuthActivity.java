package org.qii.weiciyuan.ui.login;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.ImageView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.login.OAuthDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-28
 */
public class OAuthActivity extends AbstractAppActivity {

    private static final String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
    private static final String APP_KEY = "1065511513";
    private static final String DIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    private WebView webView;
    private MenuItem refreshItem;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setSubtitle(getString(R.string.only_oauth_three_months));
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.clearCache(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_oauthactivity, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        refresh();
        return true;
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
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void refresh() {
        webView.clearView();
        webView.loadUrl("about:blank");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
        webView.loadUrl(getWeiboOAuthUrl());
    }

    private void completeRefresh() {
        if (refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }


    private String getWeiboOAuthUrl() {


        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("client_id", APP_KEY);
        parameters.put("response_type", "token");
        parameters.put("redirect_uri", DIRECT_URL);
        parameters.put("display", "mobile");
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

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!url.equals("about:blank"))
                completeRefresh();
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
            setResult(RESULT_OK, intent);
            new OAuthTask().execute(access_token);

        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class OAuthTask extends AsyncTask<String, UserBean, DBResult> {

        WeiboException e;

        ProgressFragment progressFragment = ProgressFragment.newInstance();

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected DBResult doInBackground(String... params) {

            String token = params[0];

            try {
                UserBean user = new OAuthDao(token).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccess_token(token);
                account.setUsername(user.getName());
                account.setUid(user.getId());
                account.setUsernick(user.getScreen_name());
                account.setAvatar_url(user.getProfile_image_url());
                account.setInfo(user);
                return DatabaseManager.getInstance().addOrUpdateAccount(account);
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }


        }

        @Override
        protected void onCancelled(DBResult dbResult) {
            super.onCancelled(dbResult);
            if (progressFragment != null) {
                progressFragment.dismissAllowingStateLoss();
            }
            if (e != null)
                Toast.makeText(OAuthActivity.this, e.getError(), Toast.LENGTH_SHORT).show();
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
            dialog.setMessage(getString(R.string.oauthing));
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
