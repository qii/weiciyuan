package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: qii
 * Date: 13-3-31
 */
public class MentionsTimeLine extends AbstractAppFragment {

    private ViewPager viewPager;
    private List<Fragment> mentionFragments = new ArrayList<Fragment>();
    private Map<Integer, ActionBar.Tab> tabMap = new HashMap<Integer, ActionBar.Tab>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);

        ActionBar.Tab mentionsWeiboTab = getWeiboTab();
        if (mentionsWeiboTab == null) {
            View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
            ((TextView) customView.findViewById(R.id.title)).setText(R.string.mentions_weibo);
            mentionsWeiboTab = getActivity().getActionBar().newTab().setCustomView(customView)
                    .setTag(MentionsWeiboTimeLineFragment.class.getName()).setTabListener(tabListener);
            tabMap.put(0, mentionsWeiboTab);
        }

        ActionBar.Tab mentionsCommentTab = getCommentTab();
        if (mentionsCommentTab == null) {
            View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
            ((TextView) customView.findViewById(R.id.title)).setText(R.string.mentions_to_me);
            mentionsCommentTab = getActivity().getActionBar().newTab().setCustomView(customView)
                    .setTag(MentionsCommentTimeLineFragment.class.getName()).setTabListener(tabListener);
            tabMap.put(1, mentionsCommentTab);
        }

        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex() == 1) {
            buildActionBarAndViewPagerTitles(getActivity().getActionBar(), R.string.mentions_weibo, R.string.mentions_to_me, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_layout, container, false);
        viewPager = (ViewPager) view;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        MentionsTimeLinePagerAdapter adapter = new MentionsTimeLinePagerAdapter(this, viewPager, getChildFragmentManager(), (MainTimeLineActivity) getActivity(), mentionFragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    public void buildActionBarAndViewPagerTitles(ActionBar actionBar, int firstTab, int secondTab, int nav) {
        actionBar.setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);

        ActionBar.Tab mentionsWeiboTab = getWeiboTab();
        if (mentionsWeiboTab == null) {
            View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
            ((TextView) customView.findViewById(R.id.title)).setText(firstTab);
            mentionsWeiboTab = actionBar.newTab().setCustomView(customView)
                    .setTag(MentionsWeiboTimeLineFragment.class.getName()).setTabListener(tabListener);
            tabMap.put(0, mentionsWeiboTab);
        }
        actionBar.addTab(mentionsWeiboTab);

        ActionBar.Tab mentionsCommentTab = getCommentTab();
        if (mentionsCommentTab == null) {
            View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
            ((TextView) customView.findViewById(R.id.title)).setText(secondTab);
            mentionsCommentTab = actionBar.newTab().setCustomView(customView)
                    .setTag(MentionsCommentTimeLineFragment.class.getName()).setTabListener(tabListener);
            tabMap.put(1, mentionsCommentTab);
        }

        actionBar.addTab(mentionsCommentTab);

        if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS && nav > -1) {
            actionBar.setSelectedNavigationItem(nav);
            viewPager.setCurrentItem(nav, false);
        }
    }

    public ActionBar.Tab getWeiboTab() {
        return tabMap.get(0);
    }

    public ActionBar.Tab getCommentTab() {
        return tabMap.get(1);
    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (getActivity().getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS)
                getActivity().getActionBar().setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            switch (state) {
                case ViewPager.SCROLL_STATE_SETTLING:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LongClickableLinkMovementMethod.getInstance().setLongClickable(true);

                        }
                    }, ViewConfiguration.getLongPressTimeout());
                    break;
                default:
                    LongClickableLinkMovementMethod.getInstance().setLongClickable(false);
                    break;
            }
        }
    };


    public MentionsCommentTimeLineFragment getMentionsCommentTimeLineFragment() {
        MentionsCommentTimeLineFragment fragment = ((MentionsCommentTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                MentionsCommentTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = new MentionsCommentTimeLineFragment(GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(), GlobalContext.getInstance().getSpecialToken());
        }

        return fragment;
    }

    public MentionsWeiboTimeLineFragment getMentionsWeiboTimeLineFragment() {
        MentionsWeiboTimeLineFragment fragment = ((MentionsWeiboTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                MentionsWeiboTimeLineFragment.class.getName()));
        if (fragment == null)
            fragment = new MentionsWeiboTimeLineFragment(GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(), GlobalContext.getInstance().getSpecialToken());

        return fragment;
    }
}
