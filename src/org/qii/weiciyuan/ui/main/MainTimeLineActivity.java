package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.MotionEvent;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.dao.unread.UnreadDao;
import org.qii.weiciyuan.othercomponent.MentionsAndCommentsReceiver;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.CommentsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MyStatussTimeLineFragment;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainTimeLineActivity extends AbstractAppActivity implements IUserInfo,
        IToken,
        IAccountInfo {

    private ViewPager mViewPager = null;

    private String token = "";
    private AccountBean accountBean = null;

    private GetUnreadCountTask getUnreadCountTask = null;

    private NewMsgBroadcastReceiver newMsgBroadcastReceiver = null;

    private ScheduledExecutorService newMsgScheduledExecutorService = null;

    public String getToken() {
        return token;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("account", accountBean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            accountBean = (AccountBean) savedInstanceState.getSerializable("account");
        } else {
            Intent intent = getIntent();
            accountBean = (AccountBean) intent.getSerializableExtra("account");

        }

        token = accountBean.getAccess_token();

        GlobalContext.getInstance().setSpecialToken(token);
        GlobalContext.getInstance().setAccountBean(accountBean);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("id", accountBean.getUid());
        editor.commit();

        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        buildPhoneInterface();

    }

    private void getUnreadCount() {
        if (getUnreadCountTask == null || getUnreadCountTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            getUnreadCountTask = new GetUnreadCountTask();
            getUnreadCountTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private void buildPhoneInterface() {
        buildViewPager();
        buildActionBarAndViewPagerTitles();
        buildTabTitle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AccountBean newAccountBean = (AccountBean) intent.getSerializableExtra("account");

        if (newAccountBean == null) {
            return;
        }

        if (newAccountBean.getUid().equals(accountBean.getUid())) {
            accountBean = newAccountBean;
            token = newAccountBean.getAccess_token();
            GlobalContext.getInstance().setSpecialToken(token);
            buildTabTitle(intent);
        } else {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

    }


    private void buildTabTitle(Intent intent) {


        CommentListBean comment = (CommentListBean) intent.getSerializableExtra("comment");
        MessageListBean repost = (MessageListBean) intent.getSerializableExtra("repost");

        if (repost != null && repost.getSize() > 0) {
            buildTabText(1, repost.getSize());
            getActionBar().setSelectedNavigationItem(1);
        }
        if (comment != null && comment.getSize() > 0) {
            buildTabText(2, comment.getSize());
            getActionBar().setSelectedNavigationItem(2);
        }
    }

    private void buildTabText(int index, int number) {

        ActionBar.Tab tab = getActionBar().getTabAt(index);
        String name = tab.getText().toString();
        String num;
        if (number < 99) {
            num = "(" + number + ")";
        } else {
            num = "(99+)";
        }
        if (!name.endsWith(")")) {
            tab.setText(name + num);
        } else {
            int i = name.indexOf("(");
            String newName = name.substring(0, i);
            tab.setText(newName + num);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalContext.getInstance().startedApp = false;
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_my_info:
                intent = new Intent(this, MyInfoActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("user", getUser());
                intent.putExtra("account", getAccount());
                startActivity(intent);
                return true;

            case R.id.menu_account:
                intent = new Intent(this, AccountActivity.class);
                intent.putExtra("launcher", false);
                startActivity(intent);
                finish();
                return true;

            case R.id.menu_setting:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }


    private AbstractTimeLineFragment getHomeFragment() {
        return ((AbstractTimeLineFragment) getFragmentManager().findFragmentByTag(
                FriendsTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getMentionFragment() {
        return ((AbstractTimeLineFragment) getFragmentManager().findFragmentByTag(
                MentionsTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getCommentFragment() {
        return ((AbstractTimeLineFragment) getFragmentManager().findFragmentByTag(
                CommentsTimeLineFragment.class.getName()));
    }

    private AbstractTimeLineFragment getMyFragment() {
        return ((AbstractTimeLineFragment) getFragmentManager().findFragmentByTag(
                MyStatussTimeLineFragment.class.getName()));
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

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.me))
                .setTabListener(tabListener));


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
        boolean my = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            /**
             * workaround for fragment option menu bug
             *
             * http://stackoverflow.com/questions/9338122/action-items-from-viewpager-initial-fragment-not-being-displayed
             *
             */
            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());


            if (getHomeFragment() != null) {
                getHomeFragment().clearActionMode();
            }

            if (getMentionFragment() != null) {
                getMentionFragment().clearActionMode();
            }

            if (getCommentFragment() != null) {
                getCommentFragment().clearActionMode();
            }

            if (getMyFragment() != null) {
                getMyFragment().clearActionMode();
            }


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
                    my = true;
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
                    my = false;
                    break;
            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    if (home) {

                        getHomeFragment().getListView().setSelection(0);
                        getHomeFragment().getListView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));


                    }
                    break;
                case 1:
                    if (mentions) {
                        getMentionFragment().getListView().setSelection(0);
                        getMentionFragment().getListView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                    }
                    break;
                case 2:
                    if (comments) {
                        getCommentFragment().getListView().setSelection(0);
                        getCommentFragment().getListView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                    }
                    break;
                case 3:
                    if (my) {
                        getMyFragment().getListView().setSelection(0);
                        getMyFragment().getListView().dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
                    }
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


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MentionsAndCommentsReceiver.ACTION);
        filter.setPriority(1);
        newMsgBroadcastReceiver = new NewMsgBroadcastReceiver();
        registerReceiver(newMsgBroadcastReceiver, filter);

        newMsgScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        newMsgScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getUnreadCount();
            }
        }, 10, 50, TimeUnit.SECONDS);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(newMsgBroadcastReceiver);
        newMsgScheduledExecutorService.shutdownNow();
        if (getUnreadCountTask != null)
            getUnreadCountTask.cancel(true);
    }


    private class TimeLinePagerAdapter extends AppFragmentPagerAdapter {


        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            if (getHomeFragment() == null) {
                list.add(new FriendsTimeLineFragment(getAccount(), getUser(), getToken()));
            } else {
                list.add(getHomeFragment());
            }

            if (getMentionFragment() == null) {
                list.add(new MentionsTimeLineFragment(getAccount(), getUser(), getToken()));
            } else {
                list.add(getMentionFragment());
            }

            if (getCommentFragment() == null) {
                list.add(new CommentsTimeLineFragment(getAccount(), getUser(), getToken()));
            } else {
                list.add(getCommentFragment());
            }

            if (getMyFragment() == null) {
                list.add(new MyStatussTimeLineFragment(getUser(), getToken()));
            } else {
                list.add(getMyFragment());
            }


        }


        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(FriendsTimeLineFragment.class.getName());
            tagList.add(MentionsTimeLineFragment.class.getName());
            tagList.add(CommentsTimeLineFragment.class.getName());
            tagList.add(MyStatussTimeLineFragment.class.getName());
            return tagList.get(position);
        }


        @Override
        public int getCount() {
            return list.size();
        }


    }

    private class GetUnreadCountTask extends MyAsyncTask<Void, Void, UnreadBean> {

        @Override
        protected UnreadBean doInBackground(Void... params) {
            UnreadDao unreadDao = new UnreadDao(token, accountBean.getUid());
            try {
                return unreadDao.getCount();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
            }
            return null;
        }

        @Override
        protected void onPostExecute(UnreadBean unreadBean) {
            super.onPostExecute(unreadBean);
            if (unreadBean != null) {
                buildUnreadTabTxt(unreadBean);

            }
        }
    }

    private class NewMsgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            AccountBean newMsgAccountBean = (AccountBean) intent.getSerializableExtra("account");
            if (newMsgAccountBean.getUid().equals(MainTimeLineActivity.this.accountBean.getUid())) {
                abortBroadcast();
                UnreadBean unreadBean = (UnreadBean) intent.getSerializableExtra("unread");
                buildUnreadTabTxt(unreadBean);

            }

        }
    }

    private void buildUnreadTabTxt(UnreadBean unreadBean) {
        int unreadMentionsCount = unreadBean.getMention_status();
        int unreadCommentsCount = unreadBean.getMention_cmt() + unreadBean.getCmt();

        if (unreadMentionsCount > 0)
            buildTabText(1, unreadMentionsCount);

        if (unreadCommentsCount > 0)
            buildTabText(2, unreadCommentsCount);
    }
}