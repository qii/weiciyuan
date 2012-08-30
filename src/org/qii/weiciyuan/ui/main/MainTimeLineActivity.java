package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.CommentsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsTimeLineFragment;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.search.SearchMainActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class MainTimeLineActivity extends AbstractAppActivity implements IUserInfo,
        IToken,
        IAccountInfo {

    private ViewPager mViewPager = null;
    private String token = "";
    private AccountBean accountBean = null;


    private ListView homeListView = null;
    private ListView mentionsListView = null;
    private ListView commentsListView = null;


    public void setHomeListView(ListView homeListView) {
        this.homeListView = homeListView;
    }

    public void setMentionsListView(ListView mentionsListView) {
        this.mentionsListView = mentionsListView;
    }

    public void setCommentsListView(ListView commentsListView) {
        this.commentsListView = commentsListView;
    }

    public String getToken() {
        return token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        buildThemeSetting();
        buildFontSetting();
        Intent intent = getIntent();
        accountBean = (AccountBean) intent.getSerializableExtra("account");
        token = accountBean.getAccess_token();
        GlobalContext.getInstance().setSpecialToken(token);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintimelineactivity_viewpager_layout);


        buildSetting();

        if (getResources().getBoolean(R.bool.is_phone)) {
            buildPhoneInterface();
        } else {
            buildPadInterface();
        }

    }

    private void buildSetting() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enablePic = sharedPref.getBoolean(SettingActivity.ENABLE_PIC, true);
        if (enablePic) {
            GlobalContext.getInstance().setEnablePic(true);
        } else {
            GlobalContext.getInstance().setEnablePic(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void buildPhoneInterface() {
        buildViewPager();
        buildActionBarAndViewPagerTitles();
        buildTabTitle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        buildTabTitle(intent);
    }

    private void buildPadInterface() {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.maintimelineactivity_list,
                android.R.layout.simple_spinner_dropdown_item);
        final List<Fragment> list = new ArrayList<Fragment>();
        list.add(new FriendsTimeLineFragment());
        list.add(new MentionsTimeLineFragment());
        list.add(new CommentsTimeLineFragment());
        list.add(new MyInfoFragment());
        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, list.get(position))
                        .commit();

                return true;
            }
        };
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);

    }

    private void buildTabTitle(Intent intent) {
        int commentsum = intent.getIntExtra("commentsum", 0);
        int repostsum = intent.getIntExtra("repostsum", 0);

        if (repostsum > 0) {
            invlidateTabText(1, repostsum);
            getActionBar().setSelectedNavigationItem(1);
        }
        if (commentsum > 0) {
            invlidateTabText(2, commentsum);
            getActionBar().setSelectedNavigationItem(2);
        }
    }

    private void invlidateTabText(int index, int number) {

        ActionBar.Tab tab = getActionBar().getTabAt(index);
        String name = tab.getText().toString();
        String num = "(" + number + ")";
        if (!name.endsWith(")")) {
            tab.setText(name + num);
        } else {
            int i = name.indexOf("(");
            String newName = name.substring(0, i);
            tab.setText(newName + num);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.maintimelineactivity_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_account:
                intent = new Intent(this, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;

            case R.id.menu_search:
                startActivity(new Intent(this, SearchMainActivity.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private void buildActionBarAndViewPagerTitles() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        if (getResources().getBoolean(R.bool.is_phone)) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.home))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.mentions))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.comments))
                .setTabListener(tabListener));


//        actionBar.addTab(actionBar.newTab()
//                .setText(getString(R.string.info))
//                .setTabListener(tabListener));
    }


    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean home = false;
        boolean mentions = false;
        boolean comments = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            mViewPager.setCurrentItem(tab.getPosition());
            switch (tab.getPosition()) {
                case 0:
                    home = true;
                    break;
                case 1:
                    mentions = true;
                    break;
                case 2:
                    comments = true;
                    break;
                case 3:
                    break;
            }

        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    home = false;
                    break;
                case 1:
                    mentions = false;
                    break;
                case 2:
                    comments = false;
                    break;
                case 3:
                    break;
            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    if (home) homeListView.setSelection(0);
                    break;
                case 1:
                    if (mentions) mentionsListView.setSelection(0);
                    break;
                case 2:
                    if (comments) commentsListView.setSelection(0);
                    break;
                case 3:
                    break;
            }
        }
    };

    @Override
    public UserBean getUser() {
        return accountBean.getInfo();

    }


    @Override
    public AccountBean getAccount() {
        return accountBean;
    }


    class TimeLinePagerAdapter extends
            FragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            list.add(new FriendsTimeLineFragment());
            list.add(new MentionsTimeLineFragment());
            list.add(new CommentsTimeLineFragment());
            // list.add(new MyInfoFragment());
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