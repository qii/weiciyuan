package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * User: qii
 * Date: 13-6-21
 */
public class UserTimeLineActivity extends AbstractAppActivity {

    public static Intent newIntent(String token, UserBean userBean) {
        Intent intent = new Intent(GlobalContext.getInstance(), UserTimeLineActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("user", userBean);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        String token = getIntent().getStringExtra("token");
        UserBean bean = getIntent().getParcelableExtra("user");
        getActionBar().setTitle(bean.getScreen_name());
        if (getSupportFragmentManager()
                .findFragmentByTag(StatusesByIdTimeLineFragment.class.getName()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content,
                            StatusesByIdTimeLineFragment.newInstance(bean, token),
                            StatusesByIdTimeLineFragment.class.getName())
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
