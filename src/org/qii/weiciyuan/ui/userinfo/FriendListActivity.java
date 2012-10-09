package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FriendListActivity extends AbstractAppActivity implements IUserInfo,
        IToken {
    private String token;

    private UserBean bean;

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.following_list));
        token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");
        if (getFragmentManager().findFragmentByTag(FriendsListFragment.class.getName()) == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new FriendsListFragment(bean.getId()), FriendsListFragment.class.getName())
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
}

