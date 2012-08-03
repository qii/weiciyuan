package org.qii.weiciyuan.ui.main;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TimeLineMsgListBean;
import org.qii.weiciyuan.dao.FriendsTimeLineMsgDao;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.AbstractMainActivity;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;
import org.qii.weiciyuan.ui.timeline.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.timeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.timeline.MyInfoTimeLineFragment;

import java.util.*;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class MainTimeLineActivity extends AbstractMainActivity {

    private ViewPager mViewPager = null;
    private TimeLinePagerAdapter timeLinePagerAdapter = null;

    private String token = "";
    private String screen_name = "";

    private AbstractTimeLineFragment home = null;
    private AbstractTimeLineFragment mentions = null;
    private AbstractTimeLineFragment comments = null;
    private AbstractTimeLineFragment mails = null;
    private Fragment info = null;

    private TimeLineMsgListBean homeList = new TimeLineMsgListBean();
    private TimeLineMsgListBean mentionList = new TimeLineMsgListBean();
    private TimeLineMsgListBean commentList = new TimeLineMsgListBean();
    private TimeLineMsgListBean mailList = new TimeLineMsgListBean();

    private int homelist_position = 0;
    private int mentionList_position = 0;
    private int commentList_position = 0;
    private int mailList_position = 0;

    private LruCache<String, Bitmap> avatarCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        screen_name = intent.getStringExtra("screen_name");

        //homeList = DatabaseManager.getInstance().getHomeLineMsgList();

        buildCache();
        buildViewPager();
        buildActionBarAndViewPagerTitles();


    }

    private void buildCache() {
        final int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();

        final int cacheSize = 1024 * 1024 * memClass / 8;

        avatarCache = new LruCache<String, Bitmap>(cacheSize);

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
        actionBar.setTitle(screen_name);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.home))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.mentions))
                .setTabListener(tabListener));

//        actionBar.addTab(actionBar.newTab()
//                .setText(getString(R.string.comments))
//                .setTabListener(tabListener));
//
//        actionBar.addTab(actionBar.newTab()
//                .setText(getString(R.string.mail))
//                .setTabListener(tabListener));

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

    private Bitmap getBitmapFromMemCache(String key) {
        return avatarCache.get(key);
    }

    FriendsTimeLineFragment.Commander frinedsTimeLineMsgCommand = new FriendsTimeLineFragment.Commander() {

        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = new HashMap<String, AvatarBitmapWorkerTask>();
        Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = new HashMap<String, PictureBitmapWorkerTask>();

        @Override
        public void downloadAvatar(ImageView view, String urlKey, int position, ListView listView) {

            Bitmap bitmap = getBitmapFromMemCache(urlKey);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                avatarBitmapWorkerTaskHashMap.remove(urlKey);
            } else {
                view.setImageDrawable(getResources().getDrawable(R.drawable.app));
                if (avatarBitmapWorkerTaskHashMap.get(urlKey) == null) {
                    AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(avatarCache, avatarBitmapWorkerTaskHashMap, view, listView, position);
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
                view.setImageDrawable(getResources().getDrawable(R.drawable.app));
                if (pictureBitmapWorkerTaskMap.get(urlKey) == null) {
                    PictureBitmapWorkerTask avatarTask = new PictureBitmapWorkerTask(avatarCache, pictureBitmapWorkerTaskMap, view, listView, position);
                    avatarTask.execute(urlKey);
                    pictureBitmapWorkerTaskMap.put(urlKey, avatarTask);
                }
            }


        }

        @Override
        public void listViewFooterViewClick(View view) {
            if (!isBusying) {

                new FriendsTimeLineGetOlderMsgListTask(view).execute();

            }
        }

        @Override
        public void getNewFriendsTimeLineMsgList() {

            new FriendsTimeLineGetNewMsgListTask().execute();
            Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
            for (String key : keys) {
                avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
                avatarBitmapWorkerTaskHashMap.remove(key);
            }
            Set<String> pKeys = pictureBitmapWorkerTaskMap.keySet();
            for (String pkey : pKeys) {
                pictureBitmapWorkerTaskMap.get(pkey).cancel(true);
                pictureBitmapWorkerTaskMap.remove(pkey);
            }
        }

        @Override
        public void getOlderFriendsTimeLineMsgList() {
//            if (!isBusying) {
//                new FriendsTimeLineGetOlderMsgListTask().execute();
//
//            }

        }

        @Override
        public void replayTo(int position, View view) {

            Intent intent = new Intent(MainTimeLineActivity.this, BrowserWeiboMsgActivity.class);
            intent.putExtra("msg", homeList.getStatuses().get(position));
            startActivity(intent);
            view.setSelected(false);

        }

        @Override
        public void newWeibo() {
            Intent intent = new Intent(MainTimeLineActivity.this, StatusNewActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
        }

        @Override
        public void onItemClick(int position) {
            Intent intent = new Intent(MainTimeLineActivity.this, BrowserWeiboMsgActivity.class);
            intent.putExtra("msg", homeList.getStatuses().get(position));
            startActivity(intent);
        }
    };

    class TimeLinePagerAdapter extends
            FragmentStatePagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();

        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            home = new FriendsTimeLineFragment().setCommander(frinedsTimeLineMsgCommand);
//            mentions = new MentionsTimeLineFragment();
//            comments = new CommentsTimeLineFragment();
//            mails = new MailsTimeLineFragment();
            info = new MyInfoTimeLineFragment();

            list.add(home);
//            list.add(mentions);
//            list.add(comments);
//            list.add(mails);
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

    class FriendsTimeLineGetNewMsgListTask extends AsyncTask<Void, TimeLineMsgListBean, TimeLineMsgListBean> {

        DialogFragment dialogFragment = new ProgressFragment();

        @Override
        protected void onPreExecute() {
            dialogFragment.show(getSupportFragmentManager(), "");
        }

        @Override
        protected TimeLineMsgListBean doInBackground(Void... params) {

            FriendsTimeLineMsgDao dao = new FriendsTimeLineMsgDao(token);
            if (homeList.getStatuses().size() > 0) {
                dao.setSince_id(homeList.getStatuses().get(0).getId());
            }
            TimeLineMsgListBean result = dao.getGSONMsgList();
//            if (result != null)
            //DatabaseManager.getInstance().addHomeLineMsg(result);
            return result;

        }

        @Override
        protected void onPostExecute(TimeLineMsgListBean newValue) {
            if (newValue != null) {
                Toast.makeText(MainTimeLineActivity.this, "total " + newValue.getStatuses().size() + " new messages", Toast.LENGTH_SHORT).show();

                if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    //if homelist_position equal 0,listview don't scroll because this is the first time to refresh
                    if (homelist_position > 0)
                        homelist_position += newValue.getStatuses().size();
                    newValue.getStatuses().addAll(getHomeList().getStatuses());
                } else {
                    homelist_position = 0;
                }
                setHomeList(newValue);

                home.refreshAndScrollTo(homelist_position);

            }
            dialogFragment.dismissAllowingStateLoss();
            super.onPostExecute(newValue);
        }
    }

    class FriendsTimeLineGetOlderMsgListTask extends AsyncTask<Void, TimeLineMsgListBean, TimeLineMsgListBean> {
        View footerView;

        public FriendsTimeLineGetOlderMsgListTask(View view) {
            footerView = view;
        }

        @Override
        protected void onPreExecute() {
            frinedsTimeLineMsgCommand.isBusying = true;

            ((TextView) footerView.findViewById(R.id.textView)).setText("loading");

        }

        @Override
        protected TimeLineMsgListBean doInBackground(Void... params) {

            FriendsTimeLineMsgDao dao = new FriendsTimeLineMsgDao(token);
            if (homeList.getStatuses().size() > 0) {
                dao.setMax_id(homeList.getStatuses().get(homeList.getStatuses().size() - 1).getId());
            }
            TimeLineMsgListBean result = dao.getGSONMsgList();

            return result;

        }

        @Override
        protected void onPostExecute(TimeLineMsgListBean newValue) {
            if (newValue != null) {
                Toast.makeText(MainTimeLineActivity.this, "" + newValue.getStatuses().size(), Toast.LENGTH_SHORT).show();

                homeList.getStatuses().addAll(newValue.getStatuses().subList(1, newValue.getStatuses().size() - 1));

            }

            frinedsTimeLineMsgCommand.isBusying = false;
            home.refresh();
            ((TextView) footerView.findViewById(R.id.textView)).setText("click to load older message");

            super.onPostExecute(newValue);
        }
    }


    static class ProgressFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("刷新中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);

            return dialog;
        }
    }

    public void setHomelist_position(int homelist_position) {
        this.homelist_position = homelist_position;
    }

    public String getToken() {
        return token;
    }

    public TimeLineMsgListBean getHomeList() {
        return homeList;
    }

    public void setHomeList(TimeLineMsgListBean homeList) {
        this.homeList = homeList;
    }


}