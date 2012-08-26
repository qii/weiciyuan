package org.qii.weiciyuan.ui.userinfo;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class UserInfoActivity extends AbstractAppActivity implements IUserInfo,
        IToken {
    private String token;

    private UserBean bean;

    private ViewPager mViewPager = null;


    @Override
    public String getToken() {
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();
        return token;
    }

    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");
        if (bean == null) {
            Uri data = getIntent().getData();
            String d = data.toString();
            int index = d.lastIndexOf("/");
            String newValue = d.substring(index + 1);
            bean = new UserBean();
            bean.setScreen_name(newValue);

        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.personal_info));
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.intro))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.weibo))
                .setTabListener(tabListener));


    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());

        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {

        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {

        }
    };

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };


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

    class TimeLinePagerAdapter extends
            FragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            list.add(new UserInfoFragment());
            list.add(new StatusesByIdTimeLineFragment(token, bean.getId()));
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
