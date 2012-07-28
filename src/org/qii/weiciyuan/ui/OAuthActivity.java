package org.qii.weiciyuan.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.*;
import android.widget.Toast;
import org.qii.weiciyuan.R;
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
    private static final String APP_KEY = "1065511513";
    private static final String CONSUMER_SECRET = "df428e88aae8bd31f20481d149c856ed";
    private static final String DIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    private WebView webView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);
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
        inflater.inflate(R.menu.oauth_refresh, menu);
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
            Toast.makeText(OAuthActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
            setResult(0, intent);

        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login), Toast.LENGTH_SHORT).show();
        }
        finish();
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


}
