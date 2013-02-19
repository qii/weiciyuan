package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * User: qii
 * Date: 13-2-19
 */
public class BrowserWebFragment extends Fragment {

    private WebView mWebView;
    private boolean mIsWebViewAvailable;
    private String mUrl = null;


    public BrowserWebFragment(String url) {
        super();
        mUrl = url;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mWebView != null) {
            mWebView.destroy();
        }
        mWebView = new WebView(getActivity());
        mWebView.setOnKeyListener(new View.OnKeyListener() {


            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }

        });
        mWebView.setWebViewClient(new InnerWebViewClient());
        mWebView.setWebChromeClient(new InnerWebChromeClient());
        mWebView.loadUrl(mUrl);
        mIsWebViewAvailable = true;
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        return mWebView;
    }


    public void loadUrl(String url) {
        if (mIsWebViewAvailable) getWebView().loadUrl(mUrl = url);
        else Log.w("ImprovedWebViewFragment", "WebView cannot be found. Check the view and fragment have been loaded.");
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }


    @Override
    public void onResume() {
        mWebView.onResume();
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }


    public WebView getWebView() {
        return mIsWebViewAvailable ? mWebView : null;
    }

    private class InnerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            if (getActivity() == null)
                return;
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar == null)
                return;
            actionBar.setTitle(view.getTitle());
        }
    }

    private class InnerWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String sTitle) {
            super.onReceivedTitle(view, sTitle);
            if (sTitle != null && sTitle.length() > 0) {
                if (getActivity() == null)
                    return;
                ActionBar actionBar = getActivity().getActionBar();
                if (actionBar == null)
                    return;
                actionBar.setTitle(view.getTitle());
            }
        }

        public void onProgressChanged(WebView view, int progress) {
            if (getActivity() == null)
                return;

        }
    }

}