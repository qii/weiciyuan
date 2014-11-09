package org.qii.weiciyuan.ui.main;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;
import com.slidingmenu.lib.SlidingMenu;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.othercomponent.ConnectionChangeReceiver;
import org.qii.weiciyuan.othercomponent.MusicReceiver;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.lib.RecordOperationAppBroadcastReceiver;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppEventAction;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.dm.DMUserListFragment;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.search.SearchMainParentFragment;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.MyFavListFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoFragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public class MainTimeLineActivity extends MainTimeLineParentActivity {

    public static final int REQUEST_CODE_UPDATE_FRIENDS_TIMELINE_COMMENT_REPOST_COUNT = 0;
    public static final int REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT = 1;
    public static final int REQUEST_CODE_UPDATE_MY_FAV_TIMELINE_COMMENT_REPOST_COUNT = 2;

    private AccountBean accountBean;

    private NewMsgInterruptBroadcastReceiver newMsgInterruptBroadcastReceiver;
    private MusicReceiver musicReceiver;

    private ScrollableListFragment currentFragment;
    private TextView titleText;
    private View clickToTop;

    public static interface ScrollableListFragment {
        public void scrollToTop();
    }

    public static Intent newIntent() {
        return new Intent(GlobalContext.getInstance(), MainTimeLineActivity.class);
    }

    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = newIntent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        return intent;
    }

    /*
      notification bar
     */
    public static Intent newIntent(AccountBean accountBean, MessageListBean mentionsWeiboData,
            CommentListBean mentionsCommentData, CommentListBean commentsToMeData,
            UnreadBean unreadBean) {
        Intent intent = newIntent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        intent.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, mentionsWeiboData);
        intent.putExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA, mentionsCommentData);
        intent.putExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA, commentsToMeData);
        intent.putExtra(BundleArgsConstants.UNREAD_EXTRA, unreadBean);
        return intent;
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
        outState.putParcelable(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            accountBean = savedInstanceState.getParcelable(BundleArgsConstants.ACCOUNT_EXTRA);
        } else {
            Intent intent = getIntent();
            accountBean = intent
                    .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        }

        if (accountBean == null) {
            accountBean = GlobalContext.getInstance().getAccountBean();
        }

        GlobalContext.getInstance().setGroup(null);
        GlobalContext.getInstance().setAccountBean(accountBean);
        SettingUtility.setDefaultAccountId(accountBean.getUid());

        buildInterface(savedInstanceState);
    }

    //build phone ui or table ui
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
            FragmentTransaction secondFragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();
            secondFragmentTransaction
                    .replace(R.id.menu_frame, getMenuFragment(), LeftMenuFragment.class.getName());
            getSlidingMenu().showContent();
            secondFragmentTransaction.commit();
        }
        configSlidingMenu(phone);
    }

    //init fragments
    private void initFragments() {
        Fragment friend = getFriendsTimeLineFragment();
        Fragment mentions = getMentionsTimeLineFragment();
        Fragment comments = getCommentsTimeLineFragment();

        Fragment fav = getFavFragment();
        Fragment myself = getMyProfileFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!friend.isAdded()) {
            fragmentTransaction
                    .add(R.id.menu_right_fl, friend, FriendsTimeLineFragment.class.getName());
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

        if (!fav.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, fav, MyFavListFragment.class.getName());
            fragmentTransaction.hide(fav);
        }

        if (!myself.isAdded()) {
            fragmentTransaction
                    .add(R.id.menu_right_fl, myself, UserInfoFragment.class.getName());
            fragmentTransaction.hide(myself);
        }

        if (GlobalContext.getInstance().getAccountBean().isBlack_magic()) {
            Fragment search = getSearchFragment();
            Fragment dm = getDMFragment();

            if (!search.isAdded()) {
                fragmentTransaction
                        .add(R.id.menu_right_fl, search, SearchMainParentFragment.class.getName());
                fragmentTransaction.hide(search);
            }

            if (!dm.isAdded()) {
                fragmentTransaction.add(R.id.menu_right_fl, dm, DMUserListFragment.class.getName());
                fragmentTransaction.hide(dm);
            }
        }

        if (!fragmentTransaction.isEmpty()) {
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    //configure left menu
    private void configSlidingMenu(boolean phone) {
        SlidingMenu slidingMenu = getSlidingMenu();
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow_slidingmenu);
        if (phone) {
            slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        } else {
            slidingMenu.setBehindOffset(Utility.getScreenWidth());
        }

        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setOnPageScrollListener(new SlidingMenu.OnPageScrollListener() {
            @Override
            public void onPageScroll() {
                LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                (getFriendsTimeLineFragment()).clearActionMode();
                (getFavFragment()).clearActionMode();
                (getCommentsTimeLineFragment()).clearActionMode();
                (getMentionsTimeLineFragment()).clearActionMode();
                (getMyProfileFragment()).clearActionMode();

                if (GlobalContext.getInstance().getAccountBean().isBlack_magic()) {
                    (getSearchFragment()).clearActionMode();
                    (getDMFragment()).clearActionMode();
                }
            }
        });

        slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
                LongClickableLinkMovementMethod.getInstance().setLongClickable(true);
                LocalBroadcastManager.getInstance(MainTimeLineActivity.this)
                        .sendBroadcast(new Intent(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
            }
        });
    }

    private void buildCustomActionBarTitle(Bundle savedInstanceState) {
        View title = getLayoutInflater().inflate(R.layout.maintimelineactivity_title_layout, null);
        titleText = (TextView) title.findViewById(R.id.tv_title);
        clickToTop = title.findViewById(R.id.tv_click_to_top);
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
                Intent intent = WriteWeiboActivity
                        .newIntent(GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
            }
        });
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.RIGHT);
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
        if (this.currentFragment != null) {
            this.currentFragment.scrollToTop();
        }
    }

    public View getClickToTopView() {
        return clickToTop;
    }

    public void setCurrentFragment(ScrollableListFragment fragment) {
        this.currentFragment = fragment;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (SettingUtility.isClickToTopTipFirstShow()) {
            ViewTarget target = new ViewTarget(getClickToTopView());
            ShowcaseView.insertShowcaseView(target, this, R.string.tip,
                    R.string.click_to_top_tip);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseManager.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        AccountBean intentAccountBean = intent
                .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        if (intentAccountBean == null) {
            return;
        }

        if (accountBean.equals(intentAccountBean)) {
            accountBean = intentAccountBean;
            GlobalContext.getInstance().setAccountBean(accountBean);
            setIntent(intent);
        } else {
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GlobalContext.getInstance().getBitmapCache().evictAll();
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

    public UserBean getUser() {
        return accountBean.getInfo();
    }

    public AccountBean getAccount() {
        return accountBean;
    }

    //read clipboard to know whether there are any string link can be opened
    private void readClipboard() {
        ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData cmContent = cm.getPrimaryClip();
        if (cmContent == null) {
            return;
        }
        ClipData.Item item = cmContent.getItemAt(0);
        if (item != null) {
            String url = item.coerceToText(this).toString();
            boolean a = !TextUtils.isEmpty(url) && !url
                    .equals(SettingUtility.getLastFoundWeiboAccountLink());
            boolean b = Utility.isWeiboAccountIdLink(url) || Utility.isWeiboAccountDomainLink(url);
            if (a && b) {
                OpenWeiboAccountLinkDialog dialog = OpenWeiboAccountLinkDialog.newInstance(url);
                dialog.show(getSupportFragmentManager(), "");
                SettingUtility.setLastFoundWeiboAccountLink(url);
            }
        }
    }

    public static class OpenWeiboAccountLinkDialog extends DialogFragment {

        public static OpenWeiboAccountLinkDialog newInstance(String url) {
            OpenWeiboAccountLinkDialog dialog = new OpenWeiboAccountLinkDialog();
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            dialog.setArguments(bundle);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String url = getArguments().getString("url");
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
                                intent.putExtra("domain",
                                        Utility.getDomainFromWeiboAccountLink(url));
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
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(AppEventAction.NEW_MSG_PRIORITY_BROADCAST);
        filter.setPriority(1);
        newMsgInterruptBroadcastReceiver = new NewMsgInterruptBroadcastReceiver();
        Utility.registerReceiverIgnoredReceiverHasRegisteredHereException(this,
                newMsgInterruptBroadcastReceiver, filter);
        musicReceiver = new MusicReceiver();
        Utility.registerReceiverIgnoredReceiverHasRegisteredHereException(this,
                musicReceiver,
                AppEventAction.getSystemMusicBroadcastFilterAction());
        readClipboard();
        //ensure timeline picture type is correct
        ConnectionChangeReceiver.judgeNetworkStatus(this, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(this,
                newMsgInterruptBroadcastReceiver);
        Utility.unregisterReceiverIgnoredReceiverNotRegisteredException(this, musicReceiver);

        if (isFinishing()) {
            saveNavigationPositionToDB();
        }
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
        AccountDBTask
                .updateNavigationPosition(GlobalContext.getInstance().getAccountBean(), result);
    }

    public LeftMenuFragment getMenuFragment() {
        LeftMenuFragment fragment = ((LeftMenuFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        LeftMenuFragment.class.getName()));
        if (fragment == null) {
            fragment = LeftMenuFragment.newInstance();
        }
        return fragment;
    }

    public FriendsTimeLineFragment getFriendsTimeLineFragment() {
        FriendsTimeLineFragment fragment = ((FriendsTimeLineFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        FriendsTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = FriendsTimeLineFragment.newInstance(getAccount(), getUser(), getToken());
        }
        return fragment;
    }

    public MentionsTimeLine getMentionsTimeLineFragment() {
        MentionsTimeLine fragment = ((MentionsTimeLine) getSupportFragmentManager()
                .findFragmentByTag(
                        MentionsTimeLine.class.getName()));
        if (fragment == null) {
            fragment = MentionsTimeLine.newInstance();
        }
        return fragment;
    }

    public CommentsTimeLine getCommentsTimeLineFragment() {
        CommentsTimeLine fragment = ((CommentsTimeLine) getSupportFragmentManager()
                .findFragmentByTag(
                        CommentsTimeLine.class.getName()));
        if (fragment == null) {
            fragment = CommentsTimeLine.newInstance();
        }
        return fragment;
    }

    public SearchMainParentFragment getSearchFragment() {
        SearchMainParentFragment fragment = ((SearchMainParentFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        SearchMainParentFragment.class.getName()));
        if (fragment == null) {
            fragment = SearchMainParentFragment.newInstance();
        }
        return fragment;
    }

    public DMUserListFragment getDMFragment() {
        DMUserListFragment fragment = ((DMUserListFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        DMUserListFragment.class.getName()));
        if (fragment == null) {
            fragment = DMUserListFragment.newInstance();
        }
        return fragment;
    }

    public MyFavListFragment getFavFragment() {
        MyFavListFragment fragment = ((MyFavListFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        MyFavListFragment.class.getName()));
        if (fragment == null) {
            fragment = MyFavListFragment.newInstance();
        }
        return fragment;
    }

    public UserInfoFragment getMyProfileFragment() {
        UserInfoFragment fragment = ((UserInfoFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        UserInfoFragment.class.getName()));
        if (fragment == null) {
            fragment = UserInfoFragment.newInstance(
                    GlobalContext.getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getSpecialToken());
        }
        return fragment;
    }

    //todo
    private class NewMsgInterruptBroadcastReceiver extends RecordOperationAppBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AccountBean intentAccount = intent
                    .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
//            if (accountBean.equals(intentAccount)) {
//                MessageListBean mentionsWeibo = intent
//                        .getParcelableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
//                CommentListBean mentionsComment = intent
//                        .getParcelableExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA);
//                CommentListBean commentsToMe = intent
//                        .getParcelableExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA);
//                int unreadCount = (mentionsWeibo != null ? mentionsWeibo.getSize() : 0) + (
//                        mentionsComment != null ? mentionsComment.getSize() : 0) + (
//                        commentsToMe != null ? commentsToMe
//                                .getSize() : 0);
//                String tip = String.format(context.getString(R.string.you_have_new_unread_count),
//                        String.valueOf(unreadCount));
//                Toast.makeText(MainTimeLineActivity.this, tip,
//                        Toast.LENGTH_LONG).show();
//                abortBroadcast();
//            }
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