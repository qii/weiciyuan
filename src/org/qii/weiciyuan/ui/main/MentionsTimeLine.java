package org.qii.weiciyuan.ui.main;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * User: qii
 * Date: 13-3-31
 */
public class MentionsTimeLine extends AbstractAppFragment
        implements MainTimeLineActivity.ScrollableListFragment {

    private ViewPager viewPager;

    private SparseArray<Fragment> childrenFragments = new SparseArray<Fragment>();
    private SparseArray<ActionBar.Tab> tabMap = new SparseArray<ActionBar.Tab>();

    static final int MENTIONS_WEIBO_CHILD_POSITION = 0;
    static final int MENTIONS_COMMENT_CHILD_POSITION = 1;

    public static MentionsTimeLine newInstance() {
        MentionsTimeLine fragment = new MentionsTimeLine();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);
        ActionBar.Tab mentionsWeiboTab = getWeiboTab();
        if (mentionsWeiboTab == null) {
            buildMentionsWeiboTab(tabListener);
        }

        ActionBar.Tab mentionsCommentTab = getCommentTab();
        if (mentionsCommentTab == null) {
            buildMentionsCommentTab(tabListener);
        }

        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.MENTIONS_INDEX) {
            buildActionBarAndViewPagerTitles(
                    ((MainTimeLineActivity) getActivity()).getMenuFragment().mentionsTabIndex);
        }
    }

    private ActionBar.Tab buildMentionsCommentTab(SimpleTwoTabsListener tabListener) {
        ActionBar.Tab mentionsCommentTab;
        View customView = getActivity().getLayoutInflater()
                .inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.mentions_to_me);
        mentionsCommentTab = getActivity().getActionBar().newTab().setCustomView(customView)
                .setTag(MentionsCommentTimeLineFragment.class.getName())
                .setTabListener(tabListener);
        tabMap.append(MENTIONS_COMMENT_CHILD_POSITION, mentionsCommentTab);
        return mentionsCommentTab;
    }

    private ActionBar.Tab buildMentionsWeiboTab(SimpleTwoTabsListener tabListener) {
        ActionBar.Tab mentionsWeiboTab;
        View customView = getActivity().getLayoutInflater()
                .inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.mentions_weibo);
        mentionsWeiboTab = getActivity().getActionBar().newTab().setCustomView(customView)
                .setTag(MentionsWeiboTimeLineFragment.class.getName()).setTabListener(tabListener);
        tabMap.append(MENTIONS_WEIBO_CHILD_POSITION, mentionsWeiboTab);
        return mentionsWeiboTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_layout, container, false);
        viewPager = (ViewPager) view;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        MentionsTimeLinePagerAdapter adapter = new MentionsTimeLinePagerAdapter(this, viewPager,
                getChildFragmentManager(), childrenFragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            int mentionsTabIndex = getArguments().getInt("mentionsTabIndex");
            buildActionBarAndViewPagerTitles(mentionsTabIndex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return;
        }
        UnreadTabIndex unreadTabIndex = (UnreadTabIndex) intent
                .getSerializableExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA);
        if (unreadTabIndex == null) {
            return;
        }
        switch (unreadTabIndex) {
            case MENTION_WEIBO:
                ((MainTimeLineActivity) getActivity()).getMenuFragment()
                        .switchCategory(LeftMenuFragment.MENTIONS_INDEX);
                viewPager.setCurrentItem(0);
                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                        UnreadTabIndex.NONE);
                break;
            case MENTION_COMMENT:
                ((MainTimeLineActivity) getActivity()).getMenuFragment()
                        .switchCategory(LeftMenuFragment.MENTIONS_INDEX);
                viewPager.setCurrentItem(1);
                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                        UnreadTabIndex.NONE);
                break;
        }
    }

    public void buildActionBarAndViewPagerTitles(int nav) {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(R.string.mentions);
            getActivity().getActionBar().setIcon(R.drawable.repost_light);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle("");
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);

        ActionBar.Tab mentionsWeiboTab = getWeiboTab();
        if (mentionsWeiboTab == null) {
            mentionsWeiboTab = buildMentionsWeiboTab(tabListener);
        }
        actionBar.addTab(mentionsWeiboTab);

        ActionBar.Tab mentionsCommentTab = getCommentTab();
        if (mentionsCommentTab == null) {
            mentionsCommentTab = buildMentionsCommentTab(tabListener);
        }

        actionBar.addTab(mentionsCommentTab);

        if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS && nav > -1) {
            viewPager.setCurrentItem(nav, false);
        }
    }

    public ActionBar.Tab getWeiboTab() {
        return tabMap.get(MENTIONS_WEIBO_CHILD_POSITION);
    }

    public ActionBar.Tab getCommentTab() {
        return tabMap.get(MENTIONS_COMMENT_CHILD_POSITION);
    }

    private ViewPager.SimpleOnPageChangeListener onPageChangeListener
            = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            ActionBar ab = getActivity().getActionBar();
            if (getActivity().getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS
                    && ab.getTabAt(position) == tabMap.get(position)) {
                ab.setSelectedNavigationItem(position);
            }
            ((LeftMenuFragment) ((MainTimeLineActivity) getActivity())
                    .getMenuFragment()).mentionsTabIndex = position;
            clearActionMode();
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
        MentionsCommentTimeLineFragment fragment
                = ((MentionsCommentTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                MentionsCommentTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = MentionsCommentTimeLineFragment.newInstance(
                    GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getSpecialToken());
        }

        return fragment;
    }

    public MentionsWeiboTimeLineFragment getMentionsWeiboTimeLineFragment() {
        MentionsWeiboTimeLineFragment fragment
                = ((MentionsWeiboTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                MentionsWeiboTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = MentionsWeiboTimeLineFragment.newInstance(
                    GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getSpecialToken());
        }

        return fragment;
    }

    @Override
    public void scrollToTop() {
        AbstractTimeLineFragment fragment = (AbstractTimeLineFragment) (childrenFragments
                .get(viewPager.getCurrentItem()));
        Utility.stopListViewScrollingAndScrollToTop(fragment.getListView());
    }

    public void clearActionMode() {
        getMentionsCommentTimeLineFragment().clearActionMode();
        getMentionsWeiboTimeLineFragment().clearActionMode();
    }
}
