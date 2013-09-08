package org.qii.weiciyuan.ui.dm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

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


        UserBean bean = (UserBean) getIntent().getParcelableExtra("user");

        setTitle(bean.getScreen_name());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new DMConversationListFragment(bean), DMConversationListFragment.class.getName())
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        DMConversationListFragment fragment = (DMConversationListFragment) getSupportFragmentManager()
                .findFragmentByTag(DMConversationListFragment.class.getName());
        if (fragment != null) {
            if (!fragment.isSmileyPanelClosed()) {
                fragment.closeSmileyPanel();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
