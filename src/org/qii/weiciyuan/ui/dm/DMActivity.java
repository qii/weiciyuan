package org.qii.weiciyuan.ui.dm;

import android.app.ActionBar;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-10
 */
public class DMActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.dm);

        String uid = getIntent().getStringExtra("uid");

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DMListFragment(uid))
                .commit();
    }

}
