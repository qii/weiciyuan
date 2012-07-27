package org.qii.weiciyuan.weibo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.weibo.android.R;

public class WeiboDialog extends Dialog {

    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);


    private final Weibo mWeibo;
    private String mUrl;
    private WeiboDialogListener mListener;
    private ProgressDialog mSpinner;
    private ImageView mBtnClose;
    private WebView mWebView;
    private RelativeLayout webViewContainer;
    private RelativeLayout mContent;

    private final static String TAG = "Weibo-WebView";

    public WeiboDialog(final Weibo weibo, Context context, String url, WeiboDialogListener listener) {
        super(context, R.style.ContentOverlay);
        mWeibo = weibo;
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContent = new RelativeLayout(getContext());

        setUpWebView();


        addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
    }

    private void setUpWebView() {
        webViewContainer = new RelativeLayout(getContext());


        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WeiboDialog.WeiboWebViewClient());
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.INVISIBLE);

        webViewContainer.addView(mWebView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        Resources resources = getContext().getResources();
        lp.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_left_margin);
        lp.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_top_margin);
        lp.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_right_margin);
        lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.dialog_bottom_margin);
        mContent.addView(webViewContainer, lp);
    }



    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirect URL: " + url);
            // 待后台增加对默认重定向地址的支持后修改下面的逻辑
            if (url.startsWith(mWeibo.getRedirectUrl())) {
                handleRedirectUrl(view, url);
                WeiboDialog.this.dismiss();
                return true;
            }
            // launch non-dialog URLs in a full browser
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
                String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(new DialogError(description, errorCode, failingUrl));
            WeiboDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted URL: " + url);
            // google issue. shouldOverrideUrlLoading not executed
            if (url.startsWith(mWeibo.getRedirectUrl())) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                WeiboDialog.this.dismiss();
                return;
            }
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished URL: " + url);
            super.onPageFinished(view, url);
            mSpinner.dismiss();

            mContent.setBackgroundColor(Color.TRANSPARENT);
//            webViewContainer.setBackgroundResource(R.drawable.dialog_bg);
            // mBtnClose.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.VISIBLE);
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }

    private void handleRedirectUrl(WebView view, String url) {
        Bundle values = Utility.parseUrl(url);

        String error = values.getString("error");
        String error_code = values.getString("error_code");

        if (error == null && error_code == null) {
            mListener.onComplete(values);
        } else if (error.equals("access_denied")) {
            // 用户或授权服务器拒绝授予数据访问权限
            mListener.onCancel();
        } else {
            mListener.onWeiboException(new WeiboException(error, Integer.parseInt(error_code)));
        }
    }



}
