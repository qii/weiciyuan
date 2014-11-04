package org.qii.weiciyuan.ui.preference;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 * User: qii
 * Date: 13-4-18
 */
public class LicenseActivity extends AbstractAppActivity {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (theme) {
            case R.style.AppTheme_Light:
                setTheme(android.R.style.Theme_Holo_Light_DialogWhenLarge);
                break;
            default:
                setTheme(android.R.style.Theme_Holo_DialogWhenLarge);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.pref_open_source_license_title));
        } else {
            setTitle(getString(R.string.pref_open_source_license_title));
        }
        webView = new WebView(this);
        setContentView(webView);
        if (getWindow().isFloating()) {
            WindowManager.LayoutParams layout = new WindowManager.LayoutParams();
            layout.copyFrom(getWindow().getAttributes());
            layout.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(layout);
        }
        webView.loadUrl("file:///android_asset/licenses.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utility.isKK()) {
            getMenuInflater().inflate(R.menu.actionbar_menu_licenseactivity, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, AboutActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_print:
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter();
                printManager.print(getString(R.string.app_name), adapter, null);
                return true;
        }
        return false;
    }
}
