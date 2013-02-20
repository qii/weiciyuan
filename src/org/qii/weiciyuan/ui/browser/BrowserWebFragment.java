package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 13-2-19
 */
public class BrowserWebFragment extends Fragment {

    private WebView mWebView;
    private boolean mIsWebViewAvailable;
    private String mUrl = null;
    private ShareActionProvider mShareActionProvider;


    public BrowserWebFragment(String url) {
        super();
        mUrl = url;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_menu_browserwebfragment, menu);
        MenuItem item = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close:
                getActivity().finish();
                break;
            case R.id.menu_refresh:
                getWebView().clearView();
                getWebView().loadUrl("about:blank");
                getWebView().loadUrl(mUrl);
                break;
            case R.id.menu_open_with_other_app:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                getActivity().startActivity(intent);
                break;
            case R.id.menu_share:
                Utility.setShareIntent(getActivity(), mShareActionProvider, mUrl);
                break;
        }
        return super.onOptionsItemSelected(item);
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