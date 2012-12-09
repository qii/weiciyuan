package org.qii.weiciyuan.ui.dm;

import android.os.Bundle;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-11-10
 */
public class DMActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        UserBean bean = (UserBean) getIntent().getSerializableExtra("user");

        setTitle(bean.getScreen_name());

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DMConversationListFragment(bean))
                .commit();
    }

}
