package org.qii.weiciyuan.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private static final String APP_KEY = "1065511513";// 替换为开发者的appkey，例如"1646212960";
    private static final String CONSUMER_SECRET = "df428e88aae8bd31f20481d149c856ed";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";
    private static final String DIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth);
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(getWeiboOAuthUrl());


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

//            if (url.startsWith(mWeibo.getRedirectUrl())) {
//                handleRedirectUrl(view, url);
//                WeiboDialog.this.dismiss();
//                return true;
//            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                                    String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // google issue. shouldOverrideUrlLoading not executed
//            if (url.startsWith(mWeibo.getRedirectUrl())) {
//                handleRedirectUrl(view, url);
//                view.stopLoading();
//                WeiboDialog.this.dismiss();
//                return;
//            }
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);

        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }
}
