package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * User: qii
 * Date: 12-8-18
 */
public class MyFavActivity extends AbstractAppActivity {

    private UserBean bean;

    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.my_fav_list));
        String token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getParcelableExtra("user");
        if (getSupportFragmentManager().findFragmentByTag(MyFavListFragment.class.getName())
                == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, MyFavListFragment.newInstance(),
                            MyFavListFragment.class.getName())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = MainTimeLineActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }
}
