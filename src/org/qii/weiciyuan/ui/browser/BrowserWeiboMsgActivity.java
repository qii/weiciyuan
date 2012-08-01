package org.qii.weiciyuan.ui.browser;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.AbstractMainActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 * Time: 上午10:48
 */
public class BrowserWeiboMsgActivity extends AbstractMainActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browserweibomsgactivity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
