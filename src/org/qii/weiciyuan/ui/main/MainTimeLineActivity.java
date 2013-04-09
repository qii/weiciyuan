package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.unread.UnreadDao;
import org.qii.weiciyuan.othercomponent.ClearCacheTask;
import org.qii.weiciyuan.othercomponent.unreadnotification.UnreadMsgReceiver;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.IAccountInfo;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainTimeLineActivity extends MainTimeLineParentActivity implements IUserInfo,
        IAccountInfo {

    private AccountBean accountBean;

    private GetUnreadCountTask getUnreadCountTask;

    private NewMsgBroadcastReceiver newMsgBroadcastReceiver;

    private ScheduledExecutorService newMsgScheduledExecutorService;

    private MusicReceiver musicReceiver;

    private AbstractTimeLineFragment currentFragment;

    private TextView titleText;


    public String getToken() {
        return accountBean.getAccess_token();
    }


    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            titleText.setVisibility(View.GONE);
        } else {
            titleText.setText(title);
            titleText.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(int res) {
        setTitle(getString(res));
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

        if (accountBean == null)
            accountBean = GlobalContext.getInstance().getAccountBean();

        GlobalContext.getInstance().setAccountBean(accountBean);
        SettingUtility.setDefaultAccountId(accountBean.getUid());

        buildInterface(savedInstanceState);

        Executors.newSingleThreadScheduledExecutor().schedule(new ClearCacheTask(), 8, TimeUnit.SECONDS);

        startListenMusicPlaying();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicReceiver != null)
            unregisterReceiver(musicReceiver);
    }

    private void startListenMusicPlaying() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IntentFilter musicFilter = new IntentFilter();
                musicFilter.addAction("com.android.music.metachanged");
                musicFilter.addAction("com.android.music.playstatechanged");
                musicFilter.addAction("com.android.music.playbackcomplete");
                musicFilter.addAction("com.android.music.queuechanged");

                musicFilter.addAction("com.htc.music.metachanged");
                musicFilter.addAction("fm.last.android.metachanged");
                musicFilter.addAction("com.sec.android.app.music.metachanged");
                musicFilter.addAction("com.nullsoft.winamp.metachanged");
                musicFilter.addAction("com.amazon.mp3.metachanged");
                musicFilter.addAction("com.miui.player.metachanged");
                musicFilter.addAction("com.real.IMP.metachanged");
                musicFilter.addAction("com.sonyericsson.music.metachanged");
                musicFilter.addAction("com.rdio.android.metachanged");
                musicFilter.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
                musicFilter.addAction("com.andrew.apollo.metachanged");
                musicReceiver = new MusicReceiver();
                registerReceiver(musicReceiver, musicFilter);
            }
        }, 3000);
    }

    private class MusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            if (!TextUtils.isEmpty(track)) {
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.setArtist(artist);
                musicInfo.setAlbum(album);
                musicInfo.setTrack(track);
                AppLogger.d("Music" + artist + ":" + album + ":" + track);
                GlobalContext.getInstance().updateMusicInfo(musicInfo);
            }
        }
    }

    ;

    public static class MusicInfo {
        String artist;
        String album;
        String track;

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public void setTrack(String track) {
            this.track = track;
        }

        @Override
        public String toString() {
            if (!TextUtils.isEmpty(artist))
                return "Now Playing:" + artist + ":" + track;
            else
                return "Now Playing:" + track;
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(track);
        }
    }

    private void getUnreadCount() {
        if (Utility.isTaskStopped(getUnreadCountTask)) {
            getUnreadCountTask = new GetUnreadCountTask();
            getUnreadCountTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void buildInterface(Bundle savedInstanceState) {
        getActionBar().setTitle(GlobalContext.getInstance().getCurrentAccountName());
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.menu_right);
        boolean phone = findViewById(R.id.menu_frame) == null;
        if (phone) {
            buildPhoneInterface(savedInstanceState);
        } else {
            buildPadInterface(savedInstanceState);
        }

        buildCustomActionBarTitle(savedInstanceState);

        if (savedInstanceState == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initFragments();
                }
            }, 3000);

            FragmentTransaction secondFragmentTransaction = getSupportFragmentManager().beginTransaction();
            secondFragmentTransaction.replace(R.id.menu_frame, getMenuFragment(), LeftMenuFragment.class.getName());
            getSlidingMenu().showContent();
            secondFragmentTransaction.commit();
        }


        configSlidingMenu(phone);

    }

    private void initFragments() {
        //            Fragment friend = getFriendsTimeLineFragment();
        Fragment mentions = getMentionsTimeLineFragment();
        Fragment comments = getCommentsTimeLineFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            if (!friend.isAdded()) {
//                fragmentTransaction.add(R.id.menu_right_fl, friend, FriendsTimeLineFragment.class.getName());
//                fragmentTransaction.hide(friend);
//            }
        if (!mentions.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, mentions, MentionsTimeLine.class.getName());
            fragmentTransaction.hide(mentions);

        }
        if (!comments.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, comments, CommentsTimeLine.class.getName());
            fragmentTransaction.hide(comments);

        }
        if (!fragmentTransaction.isEmpty()) {
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void configSlidingMenu(boolean phone) {
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        if (phone)
            slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        else
            slidingMenu.setBehindOffset(Utility.getScreenWidth());

        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setOnPageScrollListener(new SlidingMenu.OnPageScrollListener() {
            @Override
            public void onPageScroll() {
                LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                (getFriendsTimeLineFragment()).clearActionMode();
            }
        });

        slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                LongClickableLinkMovementMethod.getInstance().setLongClickable(true);
            }
        });
    }

    private void buildCustomActionBarTitle(Bundle savedInstanceState) {
        View title = getLayoutInflater().inflate(R.layout.maintimelineactivity_title_layout, null);
        titleText = (TextView) title.findViewById(R.id.tv_title);
        View clickToTop = title.findViewById(R.id.tv_click_to_top);
        clickToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollCurrentListViewToTop();
            }
        });
        View write = title.findViewById(R.id.btn_write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainTimeLineActivity.this, WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
            }
        });
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
        getActionBar().setCustomView(title, layoutParams);
        getActionBar().setDisplayShowCustomEnabled(true);
    }

    private void buildPhoneInterface(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        getSlidingMenu().setSlidingEnabled(true);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    }

    private void buildPadInterface(Bundle savedInstanceState) {
        View v = new View(this);
        setBehindContentView(v);
        getSlidingMenu().setSlidingEnabled(false);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }


    private void scrollCurrentListViewToTop() {
        ListView listView;
        if (currentFragment == null) {
            listView = getFriendsTimeLineFragment().getListView();
        } else {
            listView = currentFragment.getListView();
        }
        Utility.stopListViewScrollingAndScrollToTop(listView);
    }

    public void setCurrentFragment(AbstractTimeLineFragment fragment) {
        this.currentFragment = fragment;
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
            GlobalContext.getInstance().setAccountBean(accountBean);
        } else {
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalContext.getInstance().startedApp = false;
        GlobalContext.getInstance().getAvatarCache().evictAll();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                getSlidingMenu().showMenu();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


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
        IntentFilter filter = new IntentFilter(UnreadMsgReceiver.ACTION);
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

        readClipboard();
    }

    private void readClipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData cmContent = cm.getPrimaryClip();
        if (cmContent == null)
            return;
        ClipData.Item item = cmContent.getItemAt(0);
        if (item != null) {
            String url = item.coerceToText(this).toString();
            boolean a = !TextUtils.isEmpty(url) && !url.equals(SettingUtility.getLastFoundWeiboAccountLink());
            boolean b = Utility.isWeiboAccountIdLink(url) || Utility.isWeiboAccountDomainLink(url);
            if (a && b) {
                OpenWeiboAccountLinkDialog dialog = new OpenWeiboAccountLinkDialog(url);
                dialog.show(getSupportFragmentManager(), "");
                SettingUtility.setLastFoundWeiboAccountLink(url);
            }
        }
    }

    public static class OpenWeiboAccountLinkDialog extends DialogFragment {

        private String url;

        public OpenWeiboAccountLinkDialog() {

        }

        public OpenWeiboAccountLinkDialog(String url) {
            this.url = url;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("url", url);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                this.url = savedInstanceState.getString("url");
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.find_weibo_account_link)
                    .setMessage(url)
                    .setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Utility.isWeiboAccountIdLink(url)) {
                                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                                intent.putExtra("id", Utility.getIdFromWeiboAccountLink(url));
                                startActivity(intent);
                            } else if (Utility.isWeiboAccountDomainLink(url)) {
                                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                                intent.putExtra("domain", Utility.getDomainFromWeiboAccountLink(url));
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            return builder.create();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(newMsgBroadcastReceiver);
        newMsgScheduledExecutorService.shutdownNow();
        if (getUnreadCountTask != null)
            getUnreadCountTask.cancel(true);
    }


    public LeftMenuFragment getMenuFragment() {
        LeftMenuFragment fragment = ((LeftMenuFragment) getSupportFragmentManager().findFragmentByTag(
                LeftMenuFragment.class.getName()));
        if (fragment == null) {
            fragment = new LeftMenuFragment();
        }
        return fragment;
    }


    public FriendsTimeLineFragment getFriendsTimeLineFragment() {
        FriendsTimeLineFragment fragment = ((FriendsTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                FriendsTimeLineFragment.class.getName()));
        if (fragment == null)
            fragment = new FriendsTimeLineFragment(getAccount(), getUser(), getToken());

        return fragment;
    }

    public MentionsTimeLine getMentionsTimeLineFragment() {
        MentionsTimeLine fragment = ((MentionsTimeLine) getSupportFragmentManager().findFragmentByTag(
                MentionsTimeLine.class.getName()));
        if (fragment == null)
            fragment = new MentionsTimeLine();

        return fragment;
    }

    public CommentsTimeLine getCommentsTimeLineFragment() {
        CommentsTimeLine fragment = ((CommentsTimeLine) getSupportFragmentManager().findFragmentByTag(
                CommentsTimeLine.class.getName()));
        if (fragment == null)
            fragment = new CommentsTimeLine();

        return fragment;
    }


    private class GetUnreadCountTask extends MyAsyncTask<Void, Void, UnreadBean> {

        @Override
        protected UnreadBean doInBackground(Void... params) {
            UnreadDao unreadDao = new UnreadDao(getToken(), accountBean.getUid());
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

    }


}