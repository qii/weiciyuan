package org.qii.weiciyuan.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TimeLineMsgList;
import org.qii.weiciyuan.dao.TimeLineFriendsMsg;
import org.qii.weiciyuan.ui.send.StatusNewActivity;
import org.qii.weiciyuan.ui.timeline.*;

import java.util.ArrayList;
import java.util.List;

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
    private AbstractTimeLineFragment info = null;

    private TimeLineMsgList homeList = new TimeLineMsgList();
    private TimeLineMsgList mentionList = new TimeLineMsgList();
    private TimeLineMsgList commentList = new TimeLineMsgList();
    private TimeLineMsgList mailList = new TimeLineMsgList();

    private int homelist_position = 0;
    private int mentionList_position = 0;
    private int commentList_position = 0;
    private int mailList_position = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        screen_name = intent.getStringExtra("screen_name");

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
        actionBar.setTitle(screen_name);
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
                .setText(getString(R.string.mail))
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


    FriendsTimeLineFragment.Commander frinedsTimeLineMsgCommand = new FriendsTimeLineFragment.Commander() {
        @Override
        public void getNewFriendsTimeLineMsg() {

            new TimeLineTask().execute();

        }

        @Override
        public void replayTo(int position) {

        }

        @Override
        public void newWeibo() {
            startActivity(new Intent(MainTimeLineActivity.this, StatusNewActivity.class));
        }
    };

    class TimeLinePagerAdapter extends
            FragmentStatePagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();

        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            home = new FriendsTimeLineFragment().setCommander(frinedsTimeLineMsgCommand);
            mentions = new MentionsTimeLineFragment();
            comments = new CommentsTimeLineFragment();
            mails = new MailsTimeLineFragment();
            info = new MyInfoTimeLineFragment();

            list.add(home);
            list.add(mentions);
            list.add(comments);
            list.add(mails);
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

    class TimeLineTask extends AsyncTask<Void, TimeLineMsgList, TimeLineMsgList> {

        DialogFragment dialogFragment = ProgressFragment.newInstance();

        @Override
        protected void onPreExecute() {
            dialogFragment.show(getSupportFragmentManager(), "");
        }

        @Override
        protected TimeLineMsgList doInBackground(Void... params) {

            return new TimeLineFriendsMsg().getGSONMsgList(getToken());

        }

        @Override
        protected void onPostExecute(TimeLineMsgList o) {
            if (o != null) {
                setHomeList(o);

                Toast.makeText(MainTimeLineActivity.this, "" + getHomeList().getStatuses().size(), Toast.LENGTH_SHORT).show();

                home.refresh();
                //   listView.smoothScrollToPosition(activity.getHomelist_position());

            }
            dialogFragment.dismissAllowingStateLoss();
            super.onPostExecute(o);
        }
    }

    static class ProgressFragment extends DialogFragment {

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true); //注意这句
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("刷新中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);

            return dialog;
        }
    }


    public int getMentionList_position() {
        return mentionList_position;
    }

    public void setMentionList_position(int mentionList_position) {
        this.mentionList_position = mentionList_position;
    }

    public int getCommentList_position() {
        return commentList_position;
    }

    public void setCommentList_position(int commentList_position) {
        this.commentList_position = commentList_position;
    }

    public int getMailList_position() {
        return mailList_position;
    }

    public void setMailList_position(int mailList_position) {
        this.mailList_position = mailList_position;
    }

    public int getHomelist_position() {
        return homelist_position;
    }

    public void setHomelist_position(int homelist_position) {
        this.homelist_position = homelist_position;
    }

    public TimeLineMsgList getMentionList() {
        return mentionList;
    }

    public void setMentionList(TimeLineMsgList mentionList) {
        this.mentionList = mentionList;
    }

    public TimeLineMsgList getCommentList() {
        return commentList;
    }

    public void setCommentList(TimeLineMsgList commentList) {
        this.commentList = commentList;
    }

    public TimeLineMsgList getMailList() {
        return mailList;
    }

    public void setMailList(TimeLineMsgList mailList) {
        this.mailList = mailList;
    }

    public String getToken() {
        return token;
    }

    public TimeLineMsgList getHomeList() {
        return homeList;
    }

    public void setHomeList(TimeLineMsgList homeList) {
        this.homeList = homeList;
    }

}