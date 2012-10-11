package org.qii.weiciyuan.ui.userinfo;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-15
 */
public class MyInfoActivity extends AbstractAppActivity implements IUserInfo,
        IToken, IAccountInfo {
    private String token;

    private UserBean bean;

    private AccountBean account;

    private ViewPager mViewPager = null;


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

        token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");
        account = (AccountBean) getIntent().getSerializableExtra("account");

        setContentView(R.layout.maintimelineactivity_viewpager_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.my_info));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.info))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.weibo))
                .setTabListener(tabListener));

    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean status = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());

            switch (tab.getPosition()) {

                case 1:
                    status = true;
                    break;

            }
        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    status = false;
                    break;

            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    if (status)
                        getStatusFragment().getListView().setSelection(0);
                    break;

            }
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

    @Override
    public AccountBean getAccount() {
        return account;
    }

    private AbstractTimeLineFragment getStatusFragment() {
        return ((AbstractTimeLineFragment) getFragmentManager().findFragmentByTag(
                StatusesByIdTimeLineFragment.class.getName()));
    }

    private Fragment getMyInfoFragment() {
        return getFragmentManager().findFragmentByTag(
                MyInfoFragment.class.getName());
    }


    class TimeLinePagerAdapter extends
            AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            if (getMyInfoFragment() == null) {
                list.add(new MyInfoFragment());
            } else {
                list.add(getMyInfoFragment());
            }
            if (getStatusFragment() == null) {
                list.add(new StatusesByIdTimeLineFragment(getUser(), getToken()));
            } else {
                list.add(getStatusFragment());
            }
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(MyInfoFragment.class.getName());
            tagList.add(StatusesByIdTimeLineFragment.class.getName());
            return tagList.get(position);
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
