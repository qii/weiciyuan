package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboAccountBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.AbstractMainActivity;
import org.qii.weiciyuan.ui.timeline.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class MainTimeLineActivity extends AbstractMainActivity {

    private ViewPager mViewPager = null;
    private TimeLinePagerAdapter timeLinePagerAdapter = null;

    private String token = "";
    private WeiboAccountBean weiboAccountBean = null;

    public void setHomeListView(ListView homeListView) {
        this.homeListView = homeListView;
    }

    private ListView homeListView = null;
    private ListView mentionsListView=null;
    private ListView commentsListView=null;

    public void setMentionsListView(ListView mentionsListView) {
        this.mentionsListView = mentionsListView;
    }

    public void setCommentsListView(ListView commentsListView) {
        this.commentsListView = commentsListView;
    }

    Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new ConcurrentHashMap<String, AvatarBitmapWorkerTask>();
    Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new ConcurrentHashMap<String, PictureBitmapWorkerTask>();

    public Map<String, AvatarBitmapWorkerTask> getAvatarBitmapWorkerTaskHashMap() {
        return avatarBitmapWorkerTaskHashMap;
    }

    public Map<String, PictureBitmapWorkerTask> getPictureBitmapWorkerTaskMap() {
        return pictureBitmapWorkerTaskMap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        Intent intent = getIntent();
        weiboAccountBean = (WeiboAccountBean) intent.getSerializableExtra("account");
        token = weiboAccountBean.getAccess_token();


        //homeData = DatabaseManager.getInstance().getHomeLineMsgList();

        buildViewPager();
        buildActionBarAndViewPagerTitles();


    }


    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        timeLinePagerAdapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(timeLinePagerAdapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private void buildActionBarAndViewPagerTitles() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

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
                .setText(getString(R.string.info))
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

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            mViewPager.setCurrentItem(tab.getPosition());
            switch (tab.getPosition()) {
                case 0:
                    home = true;
                    break;
                case 1:
                    mentions=true;
                    break;
                case 2:
                    comments=true;
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
                    mentions=false;
                    break;
                case 2:
                    comments=false;
                    break;
                case 3:
                    break;
            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {
                case 0:
                    if (home) homeListView.smoothScrollToPosition(0);
                    break;
                case 1:
                    if(mentions)mentionsListView.smoothScrollToPosition(0);
                    break;
                case 2:
                    if(comments)commentsListView.smoothScrollToPosition(0);
                    break;
                case 3:
                    break;
            }
        }
    };

    private Bitmap getBitmapFromMemCache(String key) {
        return GlobalContext.getInstance().getAvatarCache().get(key);
    }


    public FriendsTimeLineFragment.Commander getCommander() {
        return commander;
    }

    FriendsTimeLineFragment.Commander commander = new FriendsTimeLineFragment.Commander() {


        @Override
        public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                avatarBitmapWorkerTaskHashMap.remove(urlKey);
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.account));
                if (avatarBitmapWorkerTaskHashMap.get(urlKey) == null) {
                    AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), avatarBitmapWorkerTaskHashMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    avatarBitmapWorkerTaskHashMap.put(urlKey, avatarTask);
                }
            }

        }

        @Override
        public void downContentPic(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                pictureBitmapWorkerTaskMap.remove(urlKey);
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.picture));
                if (pictureBitmapWorkerTaskMap.get(urlKey) == null) {
                    PictureBitmapWorkerTask avatarTask = new PictureBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), pictureBitmapWorkerTaskMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    pictureBitmapWorkerTaskMap.put(urlKey, avatarTask);
                }
            }


        }


    };

    class TimeLinePagerAdapter extends
            FragmentStatePagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            AbstractTimeLineFragment home = new FriendsTimeLineFragment();


            AbstractTimeLineFragment mentions = new MentionsTimeLineFragment();

            Fragment comments = new CommentsTimeLineFragment();

            MyInfoTimeLineFragment info = new MyInfoTimeLineFragment();
            info.setAccountBean(weiboAccountBean);

            list.add(home);
            list.add(mentions);
            list.add(comments);

            list.add(info);
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


    public String getToken() {
        return token;
    }


}