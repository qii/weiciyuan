package org.qii.weiciyuan.ui.search;

import android.os.Bundle;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-10-8
 */
public class AtUserActivity extends AbstractAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = getIntent().getStringExtra("token");

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AtUserFragment(token))
                .commit();
    }
}
