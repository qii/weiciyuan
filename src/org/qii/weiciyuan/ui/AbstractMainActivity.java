package org.qii.weiciyuan.ui;

import android.support.v4.app.FragmentActivity;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:30
 */
public class AbstractMainActivity extends FragmentActivity {


    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.getInstance().setActivity(this);
    }




}
