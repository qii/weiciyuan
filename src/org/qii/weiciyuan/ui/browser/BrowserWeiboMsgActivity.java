package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IWeiboMsgInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class BrowserWeiboMsgActivity extends AbstractAppActivity implements IWeiboMsgInfo, IToken {

    private MessageBean msg;
    private String token;


    private String comment_sum = "";
    private String retweet_sum = "";

    private ViewPager mViewPager = null;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("msg", msg);
        outState.putString("token", token);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            token = savedInstanceState.getString("token");
        } else {
            Intent intent = getIntent();
            token = intent.getStringExtra("token");
            msg = (MessageBean) intent.getSerializableExtra("msg");
        }
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        buildViewPager();
        buildActionBarAndViewPagerTitles();
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);

    }

    private void buildActionBarAndViewPagerTitles() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.homepage));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.weibo))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.comments))
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.repost))
                .setTabListener(tabListener));

    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
            switch (position) {
                case 1:
//                    ((CommentsByIdTimeLineFragment) getCommentFragment()).load();
                    break;
                case 2:
                    ((RepostsByIdTimeLineFragment) getRepostFragment()).load();
                    break;
            }

        }
    };


    private AbstractTimeLineFragment getRepostFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                RepostsByIdTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getCommentFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                CommentsByIdTimeLineFragment.class.getName()));
    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean comment = false;
        boolean repost = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            mViewPager.setCurrentItem(tab.getPosition());
            if (getCommentFragment() != null)
                getCommentFragment().clearActionMode();
            if (getRepostFragment() != null)
                getRepostFragment().clearActionMode();

            switch (tab.getPosition()) {

                case 1:
                    comment = true;
                    break;
                case 2:
                    repost = true;
                    break;
                case 3:
                    break;
            }
        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    comment = false;
                    break;
                case 2:
                    repost = false;
                    break;

            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    if (comment) getCommentFragment().getListView().setSelection(0);
                    break;
                case 2:
                    if (repost) getRepostFragment().getListView().setSelection(0);
                    break;
                case 3:
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class TimeLinePagerAdapter extends
            AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            list.add(new BrowserWeiboMsgFragment(msg));
            list.add(new CommentsByIdTimeLineFragment(token, msg.getId()));
            list.add(new RepostsByIdTimeLineFragment(token, msg.getId(), msg));
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(BrowserWeiboMsgFragment.class.getName());
            tagList.add(CommentsByIdTimeLineFragment.class.getName());
            tagList.add(RepostsByIdTimeLineFragment.class.getName());
            return tagList.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.browserweibomsgactivity_menu, menu);
//
//        menu.getItem(0).setTitle(menu.getItem(0).getTitle() + "(" + retweet_sum + ")");
//        menu.getItem(1).setTitle(menu.getItem(1).getTitle() + "(" + comment_sum + ")");
//
//        boolean fav = msg.isFavorited();
//        if (fav) {
//            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_un_black);
//        } else {
//            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_en_black);
//        }
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public MessageBean getMsg() {
        return msg;
    }


}
