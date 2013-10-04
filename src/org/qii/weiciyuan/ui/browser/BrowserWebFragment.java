package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 13-2-19
 */
public class BrowserWebFragment extends Fragment {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private boolean mIsWebViewAvailable;
    private String mUrl = null;
    private ShareActionProvider mShareActionProvider;
    private MenuItem refreshItem;


    public BrowserWebFragment(String url) {
        super();
        mUrl = url;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        /**
         *some devices for example Nexus 7 4.2.2 version will receive website favicon, but some
         * devices may cant, Galaxy Nexus 4.2.2 version
         */
        String path = FileManager.getWebViewFaviconDirPath();
        if (!TextUtils.isEmpty(path))
            WebIconDatabase.getInstance().open(FileManager.getWebViewFaviconDirPath());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browserwebfragment_layout, container, false);
        if (mWebView != null) {
            mWebView.destroy();
        }
        mWebView = (WebView) view.findViewById(R.id.webView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
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
        mIsWebViewAvailable = true;
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        return view;
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
        refreshItem = menu.findItem(R.id.menu_refresh);
        super.onCreateOptionsMenu(menu, inflater);
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close:
                getActivity().finish();
                break;
            case R.id.menu_refresh:
                getWebView().reload();
                break;
            case R.id.menu_open_with_other_app:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                getActivity().startActivity(intent);
                break;
            case R.id.menu_copy:
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", buildShareCopyContent()));
                Toast.makeText(getActivity(), getString(R.string.copy_successfully), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_share:
                Utility.setShareIntent(getActivity(), mShareActionProvider, buildShareCopyContent());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String buildShareCopyContent() {
        String title = mWebView.getTitle();
        String url = mWebView.getUrl();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(url)) {
            return title + " " + url;
        } else {
            return mUrl;
        }
    }

    private void startRefreshAnimation() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);
        iv.startAnimation(rotation);
        finishRefreshAnimation();
        refreshItem.setActionView(iv);
    }

    private void finishRefreshAnimation() {
        if (refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }

    private class InnerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            startRefreshAnimation();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (getActivity() == null)
                return;
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar == null)
                return;
            if (!TextUtils.isEmpty(view.getTitle()))
                actionBar.setTitle(view.getTitle());
            finishRefreshAnimation();
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
                if (!TextUtils.isEmpty(view.getTitle()))
                    actionBar.setTitle(view.getTitle());
            }
        }

        //website icon is too small
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            if (getActivity() == null)
                return;
//            getActivity().getActionBar().setIcon(new BitmapDrawable(getActivity().getResources(), icon));
        }


        public void onProgressChanged(WebView view, int progress) {
            if (getActivity() == null)
                return;
            if (!mProgressBar.isShown())
                mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(progress);
            if (progress == 100)
                mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}