package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.dm.DMUserListActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.FriendsTimeLineFragment;
import org.qii.weiciyuan.ui.nearby.NearbyTimeLineActivity;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.search.SearchMainActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.MyInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-22
 */
public class LeftMenuFragment extends AbstractAppFragment {

    private Layout layout;

    private List<Fragment> commentFragments = new ArrayList<Fragment>();
    private List<Fragment> mentionFragments = new ArrayList<Fragment>();
    private int currentIndex = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ViewPager mentionVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_mention);
        final ViewPager commentVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_comment);

        final View fl = getActivity().findViewById(R.id.menu_right_fl);


        layout.home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomePage(fl, mentionVP, commentVP);
            }
        });

        layout.mention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMentionPage(fl, mentionVP, commentVP);
            }
        });

        layout.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentPage(commentVP, fl, mentionVP);
            }
        });

        layout.dm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDMPage();
            }
        });

        layout.search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchPage();
            }
        });

        layout.setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingPage();
            }
        });

        layout.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountSwitchPage();
            }
        });


        layout.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyProfile();
            }
        });

        layout.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NearbyTimeLineActivity.class));

            }
        });

    }

    private void openMyProfile() {
        Intent intent = new Intent(getActivity(), MyInfoActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("user", GlobalContext.getInstance().getAccountBean().getInfo());
        intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
        startActivity(intent);
    }

    private void openWriteWeibo() {
        Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
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

    private boolean showCommentPage(final ViewPager commentVP, View fl, ViewPager mentionVP) {
        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        if (currentIndex == 2) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }
        currentIndex = 2;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment fragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        ft.hide(fragment);

        for (Fragment f : commentFragments) {
            ft.show(f);
        }
        for (Fragment f : mentionFragments) {
            ft.hide(f);
        }

        ft.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (commentVP.getAdapter() == null) {
                    commentVP.setAdapter(new CommentsTimeLinePagerAdapter(getFragmentManager(), (MainTimeLineActivity) getActivity(), commentFragments));
                }

                int index = commentVP.getCurrentItem();
                AbstractTimeLineFragment currentFragment;
                if (index == 0) {
                    currentFragment = ((MainTimeLineActivity) getActivity()).getCommentsTimeLineFragment();
                } else {
                    currentFragment = ((MainTimeLineActivity) getActivity()).getCommentsByMeTimeLineFragment();
                }
                ((MainTimeLineActivity) getActivity()).setCurrentFragment(currentFragment);

            }
        }, 500);


        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                if (commentVP.getCurrentItem() != tab.getPosition())
                    commentVP.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        ActionBar actionBar = getActivity().getActionBar();
        setTitle(getString(R.string.comments));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.all_people_send_to_me)
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(R.string.my_comment)
                .setTabListener(tabListener));
        commentVP.setOnPageChangeListener(onPageChangeListener);


        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        fl.setVisibility(View.GONE);
        mentionVP.setVisibility(View.GONE);
        commentVP.setVisibility(View.VISIBLE);
        return false;
    }

    private boolean showHomePage(View fl, ViewPager mentionVP, ViewPager commentVP) {
        if (currentIndex == 0) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        currentIndex = 0;

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().setTitle(GlobalContext.getInstance().getCurrentAccountName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        for (Fragment f : commentFragments) {
            ft.hide(f);
        }

        for (Fragment f : mentionFragments) {
            ft.hide(f);
        }

        Fragment fragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        if (fragment.isAdded() && fragment.isHidden()) {
            ft.show(fragment);
        } else if (!fragment.isAdded()) {
            ft.add(R.id.menu_right_fl, fragment, FriendsTimeLineFragment.class.getName());
        }

        ft.commit();

        fragment.setUserVisibleHint(true);
        fl.setVisibility(View.VISIBLE);
        mentionVP.setVisibility(View.GONE);
        commentVP.setVisibility(View.GONE);
        setTitle("");

        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        return false;
    }

    private boolean showMentionPage(View fl, final ViewPager mentionVP, ViewPager commentVP) {
        if (currentIndex == 1) {
            ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
            return true;
        }

        getActivity().getActionBar().setDisplayShowTitleEnabled(true);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);


        FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment fragment = ((MainTimeLineActivity) getActivity()).getFriendsTimeLineFragment();

        ft.hide(fragment);

        for (Fragment f : commentFragments) {
            ft.hide(f);
        }
        for (Fragment f : mentionFragments) {
            ft.show(f);
        }


        ft.commit();

        ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
        fl.setVisibility(View.GONE);
        mentionVP.setVisibility(View.VISIBLE);
        commentVP.setVisibility(View.GONE);
        currentIndex = 1;

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                if (mentionVP.getCurrentItem() != tab.getPosition())
                    mentionVP.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        ActionBar actionBar = getActivity().getActionBar();
        setTitle(getString(R.string.mentions));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.mentions_weibo)
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(R.string.mentions_to_me)
                .setTabListener(tabListener));
        mentionVP.setOnPageChangeListener(onPageChangeListener);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mentionVP.getAdapter() == null) {
                    mentionVP.setAdapter(new MentionsTimeLinePagerAdapter(getFragmentManager(), (MainTimeLineActivity) getActivity(), mentionFragments));
                }

                int index = mentionVP.getCurrentItem();
                AbstractTimeLineFragment currentFragment;
                if (index == 0) {
                    currentFragment = ((MainTimeLineActivity) getActivity()).getMentionsTimeLineFragment();
                } else {
                    currentFragment = ((MainTimeLineActivity) getActivity()).getMentionsCommentTimeLineFragment();
                }
                ((MainTimeLineActivity) getActivity()).setCurrentFragment(currentFragment);

            }
        }, 500);
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
        layout.home = (Button) view.findViewById(R.id.btn_home);
        layout.mention = (Button) view.findViewById(R.id.btn_mention);
        layout.comment = (Button) view.findViewById(R.id.btn_comment);
        layout.search = (Button) view.findViewById(R.id.btn_search);
        layout.profile = (Button) view.findViewById(R.id.btn_profile);
        layout.location = (Button) view.findViewById(R.id.btn_location);
        layout.setting = (Button) view.findViewById(R.id.btn_setting);
        layout.dm = (Button) view.findViewById(R.id.btn_dm);
        layout.logout = (Button) view.findViewById(R.id.btn_logout);
        return view;
    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActivity().getActionBar().setSelectedNavigationItem(position);
            switch (position) {
                case 0:
                    getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                    break;
                default:
                    getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
                    break;
            }
        }

    };

    private SlidingMenu getSlidingMenu() {
        return ((MainTimeLineActivity) getActivity()).getSlidingMenu();
    }

    private void setTitle(String title) {
        ((MainTimeLineActivity) getActivity()).setTitle(title);
    }

    private class Layout {
        Button home;
        Button mention;
        Button comment;
        Button search;
        Button location;
        Button dm;
        Button logout;
        Button profile;
        Button setting;
    }
}
