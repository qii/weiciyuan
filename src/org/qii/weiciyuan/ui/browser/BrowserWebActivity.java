package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.shorturl.ShareShortUrlCountDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.CheatSheet;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-2-19
 */
public class BrowserWebActivity extends AbstractAppActivity {

    private Button shareCountBtn;
    private int shareCountInt;
    private String url;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("shareCountInt", shareCountInt);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getIntent().getStringExtra("url");

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        View title = getLayoutInflater().inflate(R.layout.browserwebactivity_title_layout, null);
        shareCountBtn = (Button) title.findViewById(R.id.share_count);
        CheatSheet.setup(BrowserWebActivity.this, shareCountBtn, R.string.share_sum);
        shareCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BrowserWebActivity.this, BrowserShareTimeLineActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("count", shareCountInt);
                startActivity(intent);
            }
        });
        getActionBar().setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        getActionBar().setDisplayShowCustomEnabled(true);

        getActionBar().setTitle(url);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new BrowserWebFragment(url)).commit();
            new ShareCountTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            shareCountInt = savedInstanceState.getInt("shareCountInt");
            shareCountBtn.setText(String.valueOf(shareCountInt));
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ShareCountTask extends MyAsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int result = 0;
            try {
                result = new ShareShortUrlCountDao(GlobalContext.getInstance().getSpecialToken(), url).getCount();
            } catch (WeiboException e) {

            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == null)
                return;
            if (shareCountBtn == null)
                return;
            shareCountInt = result;
            shareCountBtn.setText(String.valueOf(shareCountInt));
        }
    }
}

