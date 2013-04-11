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
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsByMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsToMeTimeLineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-4-5
 */
public class CommentsTimeLine extends AbstractAppFragment {

    private ViewPager viewPager;
    private List<Fragment> mentionFragments = new ArrayList<Fragment>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex() == 2) {
            buildActionBarAndViewPagerTitles(getActivity().getActionBar(), R.string.all_people_send_to_me, R.string.my_comment, 0);
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
        CommentsTimeLinePagerAdapter adapter = new CommentsTimeLinePagerAdapter(this, viewPager, getChildFragmentManager(), (MainTimeLineActivity) getActivity(), mentionFragments);
        viewPager.setAdapter(adapter);
    }

    public void buildActionBarAndViewPagerTitles(ActionBar actionBar, int firstTab, int secondTab, int nav) {
        actionBar.setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);
        actionBar.addTab(actionBar.newTab()
                .setText(firstTab)
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(secondTab)
                .setTabListener(tabListener));
        actionBar.setSelectedNavigationItem(nav);

    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
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

    public CommentsToMeTimeLineFragment getCommentsToMeTimeLineFragment() {
        CommentsToMeTimeLineFragment fragment = ((CommentsToMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsToMeTimeLineFragment.class.getName()));
        if (fragment == null)
            fragment = new CommentsToMeTimeLineFragment(GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(), GlobalContext.getInstance().getSpecialToken());

        return fragment;
    }

    public CommentsByMeTimeLineFragment getCommentsByMeTimeLineFragment() {
        CommentsByMeTimeLineFragment fragment = ((CommentsByMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsByMeTimeLineFragment.class.getName()));
        if (fragment == null)
            fragment = new CommentsByMeTimeLineFragment(GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(), GlobalContext.getInstance().getSpecialToken());

        return fragment;
    }
}
