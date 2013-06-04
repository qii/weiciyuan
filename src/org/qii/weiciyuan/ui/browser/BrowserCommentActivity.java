package org.qii.weiciyuan.ui.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-9-19
 */
public class BrowserCommentActivity extends AbstractAppActivity {

    private String token;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("token", token);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        CommentBean bean = (CommentBean) intent.getParcelableExtra("comment");
        token = intent.getStringExtra("token");

        if (getFragmentManager().findFragmentByTag(BrowserCommentActivity.class.getName()) == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new BrowserCommentFragment(bean), BrowserCommentFragment.class.getName())
                    .commit();
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
}
