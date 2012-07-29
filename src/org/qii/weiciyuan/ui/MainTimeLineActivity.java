package org.qii.weiciyuan.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.timeline.TimeLineFriendsFragment;
import org.qii.weiciyuan.ui.timeline.TimeLineMentionsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class MainTimeLineActivity extends FragmentActivity {


    private ViewPager mViewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timeline_viewpage);

        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setTitle("叛逆的心之所在");

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new TimeLinePagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(simpleOnPageChangeListener);


        actionBar.addTab(actionBar.newTab()
                .setText("首页")
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText("回复")
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText("评论")
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText("私信")
                .setTabListener(tabListener));


        Intent intent = getIntent();

        String token = intent.getStringExtra("token");
        String expires = intent.getStringExtra("expires");
        String username = intent.getStringExtra("username");

        if (TextUtils.isEmpty(username))
            setTitle(username);

        GlobalContext.getInstance().setToken(token);
        GlobalContext.getInstance().setExpires(expires);


    }

    ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

            getActionBar().setSelectedNavigationItem(position);
        }
    };

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




}

class TimeLinePagerAdapter extends
        FragmentStatePagerAdapter {

    List<Fragment> list = new ArrayList<Fragment>();

    public TimeLinePagerAdapter(FragmentManager fm) {
        super(fm);
        list.add(new TimeLineFriendsFragment());
        list.add(new TimeLineMentionsFragment());
        list.add(new TimeLineMentionsFragment());
        list.add(new TimeLineMentionsFragment());

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