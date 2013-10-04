package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.MusicInfo;
import org.qii.weiciyuan.othercomponent.ClearCacheTask;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppEventAction;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.dm.DMUserListFragment;
import org.qii.weiciyuan.ui.interfaces.IAccountInfo;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.search.SearchMainParentFragment;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.MyFavListFragment;
import org.qii.weiciyuan.ui.userinfo.NewUserInfoFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainTimeLineActivity extends MainTimeLineParentActivity implements IUserInfo,
        IAccountInfo {

    public static final int REQUEST_CODE_UPDATE_FRIENDS_TIMELINE_COMMENT_REPOST_COUNT = 0;
    public static final int REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT = 1;
    public static final int REQUEST_CODE_UPDATE_MY_FAV_TIMELINE_COMMENT_REPOST_COUNT = 2;

    private AccountBean accountBean;
    private NewMsgInterruptBroadcastReceiver newMsgInterruptBroadcastReceiver;
    private MusicReceiver musicReceiver;
    private ScrollableListFragment currentFragment;
    private TextView titleText;


    public static interface ScrollableListFragment {
        public void scrollToTop();
    }


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
        outState.putParcelable("account", accountBean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            accountBean = (AccountBean) savedInstanceState.getParcelable("account");
        } else {
            Intent intent = getIntent();
            accountBean = (AccountBean) intent.getParcelableExtra("account");
        }

        if (accountBean == null)
            accountBean = GlobalContext.getInstance().getAccountBean();


        GlobalContext.getInstance().setAccountBean(accountBean);
        SettingUtility.setDefaultAccountId(accountBean.getUid());

        buildInterface(savedInstanceState);
        Executors.newSingleThreadScheduledExecutor().schedule(new ClearCacheTask(), 8, TimeUnit.SECONDS);
    }


    private void startListenMusicPlaying() {
        musicReceiver = new MusicReceiver();
        registerReceiver(musicReceiver, AppEventAction.getSystemMusicBroadcastFilterAction());
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
            initFragments();
            FragmentTransaction secondFragmentTransaction = getSupportFragmentManager().beginTransaction();
            secondFragmentTransaction.replace(R.id.menu_frame, getMenuFragment(), LeftMenuFragment.class.getName());
            getSlidingMenu().showContent();
            secondFragmentTransaction.commit();
        }
        configSlidingMenu(phone);
    }

    private void initFragments() {
        Fragment friend = getFriendsTimeLineFragment();
        Fragment mentions = getMentionsTimeLineFragment();
        Fragment comments = getCommentsTimeLineFragment();
        Fragment search = getSearchFragment();
        Fragment dm = getDMFragment();
        Fragment fav = getFavFragment();
        Fragment myself = getMyProfileFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!friend.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, friend, FriendsTimeLineFragment.class.getName());
            fragmentTransaction.hide(friend);
        }
        if (!mentions.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, mentions, MentionsTimeLine.class.getName());
            fragmentTransaction.hide(mentions);

        }
        if (!comments.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, comments, CommentsTimeLine.class.getName());
            fragmentTransaction.hide(comments);

        }
        if (!search.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, search, SearchMainParentFragment.class.getName());
            fragmentTransaction.hide(search);

        }

        if (!dm.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, dm, DMUserListFragment.class.getName());
            fragmentTransaction.hide(dm);

        }

        if (!fav.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, fav, MyFavListFragment.class.getName());
            fragmentTransaction.hide(fav);
        }

        if (!myself.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, myself, NewUserInfoFragment.class.getName());
            fragmentTransaction.hide(myself);
        }


        if (!fragmentTransaction.isEmpty()) {
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void configSlidingMenu(boolean phone) {
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow_slidingmenu);
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
                LocalBroadcastManager.getInstance(MainTimeLineActivity.this).sendBroadcast(new Intent(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
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
        if (this.currentFragment != null)
            this.currentFragment.scrollToTop();
    }

    public void setCurrentFragment(ScrollableListFragment fragment) {
        this.currentFragment = fragment;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AccountBean newAccountBean = (AccountBean) intent.getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        if (newAccountBean == null) {
            return;
        }

        if (newAccountBean.getUid().equals(accountBean.getUid())) {
            accountBean = newAccountBean;
            GlobalContext.getInstance().setAccountBean(accountBean);
            setIntent(intent);
        } else {
            finish();
            overridePendingTransition(0, 0);
            intent.putExtra("account", newAccountBean);
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
        IntentFilter filter = new IntentFilter(AppEventAction.NEW_MSG_PRIORITY_BROADCAST);
        filter.setPriority(1);
        newMsgInterruptBroadcastReceiver = new NewMsgInterruptBroadcastReceiver();
        registerReceiver(newMsgInterruptBroadcastReceiver, filter);
        startListenMusicPlaying();
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
        unregisterReceiver(newMsgInterruptBroadcastReceiver);
        if (musicReceiver != null)
            unregisterReceiver(musicReceiver);

        if (isFinishing())
            saveNavigationPositionToDB();
    }

    public void saveNavigationPositionToDB() {
        int navPosition = getMenuFragment().getCurrentIndex() * 10;
        ActionBar actionBar = getActionBar();
        int second = 0;
        if (actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
            second = actionBar.getSelectedNavigationIndex();
        }
        int result = navPosition + second;
        GlobalContext.getInstance().getAccountBean().setNavigationPosition(result);
        AccountDBTask.updateNavigationPosition(GlobalContext.getInstance().getAccountBean(), result);
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
        if (fragment == null) {
            fragment = new FriendsTimeLineFragment(getAccount(), getUser(), getToken());
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public MentionsTimeLine getMentionsTimeLineFragment() {
        MentionsTimeLine fragment = ((MentionsTimeLine) getSupportFragmentManager().findFragmentByTag(
                MentionsTimeLine.class.getName()));
        if (fragment == null) {
            fragment = new MentionsTimeLine();
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public CommentsTimeLine getCommentsTimeLineFragment() {
        CommentsTimeLine fragment = ((CommentsTimeLine) getSupportFragmentManager().findFragmentByTag(
                CommentsTimeLine.class.getName()));
        if (fragment == null) {
            fragment = new CommentsTimeLine();
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public SearchMainParentFragment getSearchFragment() {
        SearchMainParentFragment fragment = ((SearchMainParentFragment) getSupportFragmentManager().findFragmentByTag(
                SearchMainParentFragment.class.getName()));
        if (fragment == null) {
            fragment = new SearchMainParentFragment();
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public DMUserListFragment getDMFragment() {
        DMUserListFragment fragment = ((DMUserListFragment) getSupportFragmentManager().findFragmentByTag(
                DMUserListFragment.class.getName()));
        if (fragment == null) {
            fragment = new DMUserListFragment();
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public MyFavListFragment getFavFragment() {
        MyFavListFragment fragment = ((MyFavListFragment) getSupportFragmentManager().findFragmentByTag(
                MyFavListFragment.class.getName()));
        if (fragment == null) {
            fragment = new MyFavListFragment();
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public NewUserInfoFragment getMyProfileFragment() {
        NewUserInfoFragment fragment = ((NewUserInfoFragment) getSupportFragmentManager().findFragmentByTag(
                NewUserInfoFragment.class.getName()));
        if (fragment == null) {
            fragment = new NewUserInfoFragment(GlobalContext.getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getSpecialToken());
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    //todo
    private class NewMsgInterruptBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccountBean newMsgAccountBean = (AccountBean) intent.getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
            if (newMsgAccountBean.getUid().equals(MainTimeLineActivity.this.accountBean.getUid())) {
//                abortBroadcast();
            }
        }
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


    public void setMentionsWeiboCount(int count) {
        LeftMenuFragment fragment = getMenuFragment();
        fragment.setMentionWeiboUnreadCount(count);
    }

    public void setMentionsCommentCount(int count) {
        LeftMenuFragment fragment = getMenuFragment();
        fragment.setMentionCommentUnreadCount(count);
    }

    public void setCommentsToMeCount(int count) {
        LeftMenuFragment fragment = getMenuFragment();
        fragment.setCommentUnreadCount(count);
    }
}