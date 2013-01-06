package org.qii.weiciyuan.ui.login;

import android.content.Intent;
import android.os.Bundle;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-9-1
 * <p/>
 * fuck android
 * <p/>
 * 1.open developer option - dont save activity
 * 2.open this app, them open write weibo activity
 * 3.press home button to reach android home screen
 * 4.press this app's icon from home screen or app launcher to return
 * 5.it restarts! the write weibo activity interface is disappeared! fuck fuck
 * 6.dummyactivity is a workaround to solve this bug
 * <p/>
 * test on android version 4.0 4.2
 */
public class DummyActivity extends AbstractAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GlobalContext.getInstance().startedApp) {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
        }

        finish();
    }
}