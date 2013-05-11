package org.qii.weiciyuan.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.support.database.CommentsTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionCommentsTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionsTimeLineDBTask;
import org.qii.weiciyuan.support.utils.AppEventAction;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.dm.DMUserListActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.nearby.NearbyTimeLineActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.search.SearchMainActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

import java.util.HashSet;

/**
 * User: qii
 * Date: 13-1-22
 */
public class LeftMenuFragment extends AbstractAppFragment {

    private Layout layout;

    private int currentIndex = -1;

    private int mentionsWeiboUnreadCount = 0;
    private int mentionsCommentUnreadCount = 0;
    private int commentsToMeUnreadCount = 0;

    public int commentsTabIndex = -1;
    public int mentionsTabIndex = -1;
    public int searchTabIndex = -1;


    private boolean firstStart = true;

    private SparseArray<Fragment> rightFragments = new SparseArray<Fragment>();

    private static final int HOME_INDEX = 0;
    private static final int MENTIONS_INDEX = 1;
    private static final int COMMENTS_INDEX = 2;
    private static final int SEARCH_INDEX = 3;
    private static final int DM_INDEX = 4;


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentIndex", currentIndex);
        outState.putInt("mentionsWeiboUnreadCount", mentionsWeiboUnreadCount);
        outState.putInt("mentionsCommentUnreadCount", mentionsCommentUnreadCount);
        outState.putInt("commentsToMeUnreadCount", commentsToMeUnreadCount);
        outState.putInt("commentsTabIndex", commentsTabIndex);
        outState.putInt("mentionsTabIndex", mentionsTabIndex);
        outState.putInt("searchTabIndex", searchTabIndex);
        outState.putBoolean("firstStart", firstStart);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("currentIndex");
            mentionsWeiboUnreadCount = savedInstanceState.getInt("mentionsWeiboUnreadCount");
            mentionsCommentUnreadCount = savedInstanceState.getInt("mentionsCommentUnreadCount");
            commentsToMeUnreadCount = savedInstanceState.getInt("commentsToMeUnreadCount");
            commentsTabIndex = savedInstanceState.getInt("commentsTabIndex");
            mentionsTabIndex = savedInstanceState.getInt("mentionsTabIndex");
            searchTabIndex = savedInstanceState.getInt("searchTabIndex");
            firstStart = savedInstanceState.getBoolean("firstStart");
        } else {
            readUnreadCountFromDB();
        }
        if (currentIndex == -1) {
            currentIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
        }


        rightFragments.append(HOME_INDEX, ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment());
        rightFragments.append(MENTIONS_INDEX, ((MainTimeLineActivity) getActivity()).getMentionsTimeLineFragment());
        rightFragments.append(COMMENTS_INDEX, ((MainTimeLineActivity) getActivity()).getCommentsTimeLineFragment());
        rightFragments.append(SEARCH_INDEX, ((MainTimeLineActivity) getActivity()).getSearchFragment());
        rightFragments.append(DM_INDEX, ((MainTimeLineActivity) getActivity()).getDMFragment());

        switchCategory(currentIndex);

    }

    public void switchCategory(int position) {

        switch (position) {
            case 0:
                showHomePage(true);
                break;
            case 1:
                showMentionPage(true);
                break;
            case 2:
                showCommentPage(true);
                break;
            case 3:
                showSearchPage(true);
                break;
            case 4:
                showDMPage(true);
                break;
        }
        drawButtonsBackground(position);

        buildUnreadCount();

        firstStart = false;
    }

    private void readUnreadCountFromDB() {
        TimeLinePosition position = MentionsTimeLineDBTask.getPosition(GlobalContext.getInstance().getCurrentAccountId());
        HashSet<String> hashSet = position.newMsgIds;
        if (hashSet != null) {
            mentionsWeiboUnreadCount = hashSet.size();
        }

        position = MentionCommentsTimeLineDBTask.getPosition(GlobalContext.getInstance().getCurrentAccountId());
        hashSet = position.newMsgIds;
        if (hashSet != null) {
            mentionsCommentUnreadCount = hashSet.size();
        }
        position = CommentsTimeLineDBTask.getPosition(GlobalContext.getInstance().getCurrentAccountId());
        hashSet = position.newMsgIds;
        if (hashSet != null) {
            commentsToMeUnreadCount = hashSet.size();
        }
    }

    private void buildUnreadCount() {
        setMentionWeiboUnreadCount(mentionsWeiboUnreadCount);
        setMentionCommentUnreadCount(mentionsCommentUnreadCount);
        setCommentUnreadCount(commentsToMeUnreadCount);
    }

    private void openMyProfile() {
        Intent intent = new Intent(getActivity(), MyInfoActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("user", GlobalContext.getInstance().getAccountBean().getInfo());
        intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
        startActivity(intent);
    }


    private void showAccountSwitchPage() {
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        intent.putExtra("launcher", false);
        startActivity(intent);
        getActivity().finish();
    }

    private void showSettingPage() {
        startActivity(new Intent(getActivity(), SettingActivity.class));
    }

    private void showSearchPage() {
        startActivity(new Intent(getActivity(), SearchMainActivity.class));
    }

    private void showDMPage() {
        startActivity(new Intent(getActivity(), DMUserListActivity.class));
    }


    private boolean showHomePage(boolean reset) {
        if (currentIndex == 0 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        currentIndex = 0;

        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == 0)
                        showHomePageImp();

                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showHomePageImp();

        }


        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showHomePageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.hide(rightFragments.get(MENTIONS_INDEX));
        ft.hide(rightFragments.get(COMMENTS_INDEX));
        ft.hide(rightFragments.get(SEARCH_INDEX));
        ft.hide(rightFragments.get(DM_INDEX));

        FriendsTimeLineFragment fragment = (FriendsTimeLineFragment) rightFragments.get(HOME_INDEX);
        ft.show(fragment);
        ft.commit();
        setTitle("");
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(fragment);
    }

    private boolean showMentionPage(boolean reset) {
        if (currentIndex == 1 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        currentIndex = 1;

        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == 1)
                        showMentionPageImp();
                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showMentionPageImp();
        }
        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showMentionPageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(rightFragments.get(HOME_INDEX));
        ft.hide(rightFragments.get(COMMENTS_INDEX));
        ft.hide(rightFragments.get(SEARCH_INDEX));
        ft.hide(rightFragments.get(DM_INDEX));


        Fragment m = rightFragments.get(MENTIONS_INDEX);

        if (firstStart) {
            int navPosition = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
            if (navPosition == 1) {
                mentionsTabIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() % 10;
            }
        }
        m.getArguments().putInt("mentionsTabIndex", mentionsTabIndex);

        ft.show(m);
        ft.commit();


    }

    public int getCurrentIndex() {
        return currentIndex;
    }


    private boolean showCommentPage(boolean reset) {
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        if (currentIndex == 2 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }
        currentIndex = 2;
        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == 2)
                        showCommentPageImp();

                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showCommentPageImp();

        }


        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showCommentPageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.hide(rightFragments.get(HOME_INDEX));
        ft.hide(rightFragments.get(MENTIONS_INDEX));
        ft.hide(rightFragments.get(SEARCH_INDEX));
        ft.hide(rightFragments.get(DM_INDEX));

        Fragment fragment = rightFragments.get(COMMENTS_INDEX);
        if (firstStart) {
            int navPosition = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
            if (navPosition == 2) {
                commentsTabIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() % 10;
            }
        }
        fragment.getArguments().putInt("commentsTabIndex", commentsTabIndex);

        ft.show(fragment);
        ft.commit();


    }


    private boolean showSearchPage(boolean reset) {
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        if (currentIndex == 3 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }
        currentIndex = 3;
        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == 3)
                        showSearchPageImp();

                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showSearchPageImp();

        }


        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showSearchPageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.hide(rightFragments.get(HOME_INDEX));
        ft.hide(rightFragments.get(MENTIONS_INDEX));
        ft.hide(rightFragments.get(COMMENTS_INDEX));
        ft.hide(rightFragments.get(DM_INDEX));

        Fragment fragment = rightFragments.get(SEARCH_INDEX);

        if (firstStart) {
            int navPosition = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
            if (navPosition == 3) {
                searchTabIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() % 10;
            }
        }
        fragment.getArguments().putInt("searchTabIndex", searchTabIndex);

        ft.show(fragment);
        ft.commit();


    }


    private boolean showDMPage(boolean reset) {
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        if (currentIndex == 4 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }
        currentIndex = 4;
        if (Utility.isDevicePort() && !reset) {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);
                    if (currentIndex == 4)
                        showDMPageImp();

                }
            };
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(AppEventAction.SLIDING_MENU_CLOSED_BROADCAST));
        } else {
            showDMPageImp();

        }


        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();

        return false;
    }

    private void showDMPageImp() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.hide(rightFragments.get(HOME_INDEX));
        ft.hide(rightFragments.get(MENTIONS_INDEX));
        ft.hide(rightFragments.get(COMMENTS_INDEX));
        ft.hide(rightFragments.get(SEARCH_INDEX));

        Fragment fragment = rightFragments.get(DM_INDEX);


        ft.show(fragment);
        ft.commit();


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slidingdrawer_contents, container, false);
        layout = new Layout();
        layout.home = (LinearLayout) view.findViewById(R.id.btn_home);
        layout.mention = (LinearLayout) view.findViewById(R.id.btn_mention);
        layout.comment = (LinearLayout) view.findViewById(R.id.btn_comment);
        layout.search = (Button) view.findViewById(R.id.btn_search);
        layout.profile = (Button) view.findViewById(R.id.btn_profile);
        layout.location = (Button) view.findViewById(R.id.btn_location);
        layout.setting = (Button) view.findViewById(R.id.btn_setting);
        layout.dm = (Button) view.findViewById(R.id.btn_dm);
        layout.logout = (Button) view.findViewById(R.id.btn_logout);
        layout.homeCount = (TextView) view.findViewById(R.id.tv_home_count);
        layout.mentionCount = (TextView) view.findViewById(R.id.tv_mention_count);
        layout.commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layout.home.setOnClickListener(onClickListener);
        layout.mention.setOnClickListener(onClickListener);
        layout.comment.setOnClickListener(onClickListener);
        layout.search.setOnClickListener(onClickListener);
        layout.profile.setOnClickListener(onClickListener);
        layout.location.setOnClickListener(onClickListener);
        layout.setting.setOnClickListener(onClickListener);
        layout.dm.setOnClickListener(onClickListener);
        layout.logout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_home:
                    showHomePage(false);
                    drawButtonsBackground(0);
                    break;
                case R.id.btn_mention:
                    showMentionPage(false);
                    drawButtonsBackground(1);
                    break;
                case R.id.btn_comment:
                    showCommentPage(false);
                    drawButtonsBackground(2);
                    break;
                case R.id.btn_search:
//                    showSearchPage();
                    showSearchPage(false);
                    drawButtonsBackground(3);
                    break;
                case R.id.btn_profile:
                    openMyProfile();
//                    drawButtonsBackground(0);
                    break;
                case R.id.btn_location:
                    startActivity(new Intent(getActivity(), NearbyTimeLineActivity.class));
//                    drawButtonsBackground(5);
                    break;
                case R.id.btn_dm:
                    showDMPage(false);
                    drawButtonsBackground(4);
                    break;
                case R.id.btn_setting:
                    showSettingPage();
                    break;
                case R.id.btn_logout:
                    showAccountSwitchPage();
                    break;
            }
        }
    };

    private void drawButtonsBackground(int position) {
        layout.home.setBackgroundResource(R.drawable.btn_drawer_menu);
        layout.mention.setBackgroundResource(R.drawable.btn_drawer_menu);
        layout.comment.setBackgroundResource(R.drawable.btn_drawer_menu);
        layout.search.setBackgroundResource(R.drawable.btn_drawer_menu);
//        layout.profile.setBackgroundResource(R.color.transparent);
//        layout.location.setBackgroundResource(R.color.transparent);
//        layout.setting.setBackgroundResource(R.color.transparent);
        layout.dm.setBackgroundResource(R.drawable.btn_drawer_menu);
//        layout.logout.setBackgroundResource(R.color.transparent);
        switch (position) {
            case 0:
                layout.home.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 1:
                layout.mention.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 2:
                layout.comment.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 3:
                layout.search.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 4:
                layout.dm.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 5:
                layout.location.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 6:
                layout.profile.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 7:
                layout.logout.setBackgroundResource(R.color.ics_blue_semi);
                break;
            case 8:
                layout.setting.setBackgroundResource(R.color.ics_blue_semi);
                break;
        }
    }

    private SlidingMenu getSlidingMenu() {
        return ((MainTimeLineActivity) getActivity()).getSlidingMenu();
    }

    private void setTitle(int res) {
        ((MainTimeLineActivity) getActivity()).setTitle(res);
    }

    private void setTitle(String title) {
        ((MainTimeLineActivity) getActivity()).setTitle(title);
    }

    public void setHomeUnreadCount(int count) {
        if (count > 0) {
            layout.homeCount.setVisibility(View.VISIBLE);
            layout.homeCount.setText(String.valueOf(count));
        } else {
            layout.homeCount.setVisibility(View.GONE);
        }
    }

    public void setMentionWeiboUnreadCount(int count) {
        this.mentionsWeiboUnreadCount = count;
        int totalCount = this.mentionsWeiboUnreadCount + this.mentionsCommentUnreadCount;
        if (totalCount > 0) {
            layout.mentionCount.setVisibility(View.VISIBLE);
            layout.mentionCount.setText(String.valueOf(totalCount));
        } else {
            layout.mentionCount.setVisibility(View.GONE);
        }
    }

    public void setMentionCommentUnreadCount(int count) {
        this.mentionsCommentUnreadCount = count;
        int totalCount = this.mentionsWeiboUnreadCount + this.mentionsCommentUnreadCount;
        if (totalCount > 0) {
            layout.mentionCount.setVisibility(View.VISIBLE);
            layout.mentionCount.setText(String.valueOf(totalCount));
        } else {
            layout.mentionCount.setVisibility(View.GONE);
        }
    }

    public void setCommentUnreadCount(int count) {
        this.commentsToMeUnreadCount = count;
        if (this.commentsToMeUnreadCount > 0) {
            layout.commentCount.setVisibility(View.VISIBLE);
            layout.commentCount.setText(String.valueOf(this.commentsToMeUnreadCount));
        } else {
            layout.commentCount.setVisibility(View.GONE);
        }
    }

    private class Layout {
        LinearLayout home;
        LinearLayout mention;
        LinearLayout comment;
        TextView homeCount;
        TextView mentionCount;
        TextView commentCount;
        Button search;
        Button location;
        Button dm;
        Button logout;
        Button profile;
        Button setting;
    }


}