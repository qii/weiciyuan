package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.dm.DMUserListActivity;
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
public class LeftMenuFragment extends PreferenceFragment {

    private List<Fragment> commentFragments = new ArrayList<Fragment>();
    private List<Fragment> mentionFragments = new ArrayList<Fragment>();
    private int currentIndex = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ViewPager mentionVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_mention);
        final ViewPager commentVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_comment);

        final View fl = getActivity().findViewById(R.id.menu_right_fl);


        findPreference("a").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (showHomePage(fl, mentionVP, commentVP)) return true;
                return true;
            }
        });

        findPreference("b").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (showMentionPage(fl, mentionVP, commentVP)) return true;

                return true;
            }
        });

        findPreference("c").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (showCommentPage(commentVP, fl, mentionVP)) return true;
                return true;
            }
        });

        findPreference("d").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDMPage();
                return true;
            }
        });

        findPreference("e").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSearchPage();
                return true;
            }
        });

        findPreference("f").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showSettingPage();
                return true;
            }
        });

        findPreference("g").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAccountSwitchPage();
                return true;
            }
        });
        findPreference("h").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openWriteWeibo();
                return true;
            }
        });

        findPreference("i").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openMyProfile();
                return true;
            }
        });

        findPreference("j").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), NearbyTimeLineActivity.class));
                return true;
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
        Fragment fragment = ((MainTimeLineActivity) getActivity()).getOrNewFriendsTimeLineFragment();

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
                if (commentVP.getAdapter() == null)
                    commentVP.setAdapter(new CommentsTimeLinePagerAdapter(getFragmentManager(), (MainTimeLineActivity) getActivity(), commentFragments));


            }
        }, 500);


        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                if (commentVP.getCurrentItem() != tab.getPosition())
                    commentVP.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        ActionBar actionBar = getActivity().getActionBar();
        getActivity().getActionBar().setTitle(getString(R.string.comments));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab()
                .setText("收到的评论")
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText("发出的评论")
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

        Fragment fragment = ((MainTimeLineActivity) getActivity()).getOrNewFriendsTimeLineFragment();

        if (fragment.isAdded() && fragment.isHidden()) {
            ft.show(fragment);
        } else if (!fragment.isAdded()) {
            ft.add(R.id.menu_right_fl, fragment, FriendsTimeLineFragment.class.getName());
        }
        fragment.setUserVisibleHint(true);
        fl.setVisibility(View.VISIBLE);
        mentionVP.setVisibility(View.GONE);
        commentVP.setVisibility(View.GONE);

        ft.commit();

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

        Fragment fragment = ((MainTimeLineActivity) getActivity()).getOrNewFriendsTimeLineFragment();

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
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                if (mentionVP.getCurrentItem() != tab.getPosition())
                    mentionVP.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        ActionBar actionBar = getActivity().getActionBar();
        getActivity().getActionBar().setTitle(getString(R.string.mentions));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab()
                .setText("@的微博")
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText("@的评论")
                .setTabListener(tabListener));
        mentionVP.setOnPageChangeListener(onPageChangeListener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mentionVP.getAdapter() == null)
                    mentionVP.setAdapter(new MentionsTimeLinePagerAdapter(getFragmentManager(), (MainTimeLineActivity) getActivity(), mentionFragments));


            }
        }, 500);
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        addPreferencesFromResource(R.xml.slidingmenu_layout);


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
}
