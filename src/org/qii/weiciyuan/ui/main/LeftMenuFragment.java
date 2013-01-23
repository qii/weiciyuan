package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.slidingmenu.lib.SlidingMenu;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.maintimeline.*;
import org.qii.weiciyuan.ui.preference.SettingActivity;
import org.qii.weiciyuan.ui.search.SearchMainActivity;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-22
 */
public class LeftMenuFragment extends PreferenceFragment {

    List<Fragment> commentFragments = new ArrayList<Fragment>();
    List<Fragment> mentionFragments = new ArrayList<Fragment>();

    int index = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        addPreferencesFromResource(R.xml.slidingmenu_layout);

        final ViewPager mentionVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_mention);
        final ViewPager commentVP = (ViewPager) getActivity().findViewById(R.id.menu_right_vp_comment);

        final View fl = getActivity().findViewById(R.id.menu_right_fl);


        findPreference("a").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (index == 0) {
                    ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
                    return true;
                }

                index = 0;


                getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                getActivity().getActionBar().show();
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

                fl.setVisibility(View.VISIBLE);
                mentionVP.setVisibility(View.GONE);
                commentVP.setVisibility(View.GONE);

                ft.commit();

                ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
                return true;
            }
        });

        findPreference("b").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (index == 1) {
                    ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
                    return true;
                }

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

                if (mentionVP.getAdapter() == null)
                    mentionVP.setAdapter(new MentionsTimeLinePagerAdapter(getFragmentManager()));

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


                ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
                fl.setVisibility(View.GONE);
                mentionVP.setVisibility(View.VISIBLE);
                commentVP.setVisibility(View.GONE);
                index = 1;
                return true;
            }
        });

        findPreference("c").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (index == 2) {
                    ((MainTimeLineActivity) getActivity()).getSlidingMenu().showContent();
                    return true;
                }
                index = 2;
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

                if (commentVP.getAdapter() == null)
                    commentVP.setAdapter(new CommentsTimeLinePagerAdapter(getFragmentManager()));

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
                return true;
            }
        });

        findPreference("d").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return true;
            }
        });

        findPreference("e").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SearchMainActivity.class));
                return true;
            }
        });

        findPreference("f").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
                return true;
            }
        });

        findPreference("g").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AccountActivity.class);
                intent.putExtra("launcher", false);
                startActivity(intent);
                getActivity().finish();
                return true;
            }
        });
        findPreference("h").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                startActivity(intent);
                return true;
            }
        });

    }

    private class CommentsTimeLinePagerAdapter extends AppFragmentPagerAdapter {


        public CommentsTimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            commentFragments.add(0, ((MainTimeLineActivity) getActivity()).newCommentsTimeLineFragment());
            commentFragments.add(1, ((MainTimeLineActivity) getActivity()).newCommentsByMeTimeLineFragment());


        }


        public Fragment getItem(int position) {
            return commentFragments.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(CommentsTimeLineFragment.class.getName());
            tagList.add(CommentsByMeTimeLineFragment.class.getName());

            return tagList.get(position);
        }


        @Override
        public int getCount() {
            return 2;
        }


    }

    private class MentionsTimeLinePagerAdapter extends AppFragmentPagerAdapter {


        public MentionsTimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            mentionFragments.add(0, ((MainTimeLineActivity) getActivity()).newMentionsTimeLineFragment());
            mentionFragments.add(1, ((MainTimeLineActivity) getActivity()).newMentionsCommentTimeLineFragment());


        }


        public Fragment getItem(int position) {
            return mentionFragments.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(MentionsTimeLineFragment.class.getName());
            tagList.add(MentionsCommentTimeLineFragment.class.getName());

            return tagList.get(position);
        }


        @Override
        public int getCount() {
            return 2;
        }


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
