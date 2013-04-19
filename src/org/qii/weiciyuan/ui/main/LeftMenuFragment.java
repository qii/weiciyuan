package org.qii.weiciyuan.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
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

/**
 * User: qii
 * Date: 13-1-22
 */
public class LeftMenuFragment extends AbstractAppFragment {

    private Layout layout;

    private int currentIndex = -1;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentIndex", currentIndex);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("currentIndex");
        }
        if (currentIndex == -1) {
            currentIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
        }

        switch (currentIndex) {
            case 0:
                showHomePage(true);
                break;
            case 1:
                showMentionPage(true);
                break;
            case 2:
                showCommentPage(true);
                break;
        }
        drawButtonsBackground(currentIndex);
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

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(((MainTimeLineActivity) getActivity()).getMentionsTimeLineFragment());
        ft.hide(((MainTimeLineActivity) getActivity()).getCommentsTimeLineFragment());
        FriendsTimeLineFragment fragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        if (fragment.isAdded() && fragment.isHidden()) {
            ft.show(fragment);
        } else if (!fragment.isAdded()) {
            ft.add(R.id.menu_right_fl, fragment, FriendsTimeLineFragment.class.getName());
        }

        ft.commit();
        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        setTitle("");
        return false;
    }

    private boolean showMentionPage(boolean reset) {
        if (currentIndex == 1 && !reset) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment fragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        ft.hide(fragment);
        ft.hide(((MainTimeLineActivity) getActivity()).getCommentsTimeLineFragment());

        currentIndex = 1;

        MentionsTimeLine m = ((MainTimeLineActivity) getActivity()).getMentionsTimeLineFragment();

        if (m.isAdded() && m.isHidden()) {
            ft.show(m);
        } else if (!m.isAdded()) {
            ft.attach(m);
            ft.add(R.id.menu_right_fl, m, MentionsTimeLine.class.getName());
        }

        ft.commit();
        int tabIndex = 0;
        int navPosition = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
        if (navPosition == 1) {
            tabIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() % 10;
        }
        m.buildActionBarAndViewPagerTitles(getActivity().getActionBar(), R.string.mentions_weibo, R.string.mentions_to_me, tabIndex);
        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        if (Utility.isDevicePort()) {
            setTitle(R.string.mentions);
        }

        return false;
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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment friendsTimeLineFragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        ft.hide(friendsTimeLineFragment);
        ft.hide(((MainTimeLineActivity) getActivity()).getMentionsTimeLineFragment());


        CommentsTimeLine fragment = ((MainTimeLineActivity) getActivity()).getCommentsTimeLineFragment();

        if (fragment.isAdded() && fragment.isHidden()) {
            ft.show(fragment);
        } else if (!fragment.isAdded()) {
            ft.add(R.id.menu_right_fl, fragment, CommentsTimeLine.class.getName());
        }

        ft.commit();
        int tabIndex = 0;
        int navPosition = GlobalContext.getInstance().getAccountBean().getNavigationPosition() / 10;
        if (navPosition == 2) {
            tabIndex = GlobalContext.getInstance().getAccountBean().getNavigationPosition() % 10;
        }
        fragment.buildActionBarAndViewPagerTitles(getActivity().getActionBar(), R.string.all_people_send_to_me, R.string.my_comment, tabIndex);
        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        if (Utility.isDevicePort()) {
            setTitle(R.string.comments);
        }
        return false;
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
                    showSearchPage();
//                    drawButtonsBackground(3);
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
                    showDMPage();
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
        layout.home.setBackgroundResource(R.color.transparent);
        layout.mention.setBackgroundResource(R.color.transparent);
        layout.comment.setBackgroundResource(R.color.transparent);
//        layout.search.setBackgroundResource(R.color.transparent);
//        layout.profile.setBackgroundResource(R.color.transparent);
//        layout.location.setBackgroundResource(R.color.transparent);
//        layout.setting.setBackgroundResource(R.color.transparent);
//        layout.dm.setBackgroundResource(R.color.transparent);
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

    public void setMentionUnreadCount(int count) {
        if (count > 0) {
            layout.mentionCount.setVisibility(View.VISIBLE);
            layout.mentionCount.setText(String.valueOf(count));
        } else {
            layout.mentionCount.setVisibility(View.GONE);
        }
    }

    public void setCommentUnreadCount(int count) {
        if (count > 0) {
            layout.commentCount.setVisibility(View.VISIBLE);
            layout.commentCount.setText(String.valueOf(count));
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
