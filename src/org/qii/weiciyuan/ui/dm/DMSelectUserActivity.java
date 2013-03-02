package org.qii.weiciyuan.ui.dm;

import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 13-3-2
 */
public class DMSelectUserActivity extends AbstractAppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.select_dm_receiver);
    }
}
