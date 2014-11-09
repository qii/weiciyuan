package org.qii.weiciyuan.ui.main;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.android.UnreadTabIndex;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsByMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsToMeTimeLineFragment;

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
 * Date: 13-4-5
 */
public class CommentsTimeLine extends AbstractAppFragment
        implements MainTimeLineActivity.ScrollableListFragment {

    private ViewPager viewPager;

    private SparseArray<Fragment> childrenFragments = new SparseArray<Fragment>();
    private SparseArray<ActionBar.Tab> tabMap = new SparseArray<ActionBar.Tab>();

    static final int COMMENTS_TO_ME_CHILD_POSITION = 0;
    static final int COMMENTS_BY_ME_CHILD_POSITION = 1;

    public static CommentsTimeLine newInstance() {
        CommentsTimeLine fragment = new CommentsTimeLine();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);
        ActionBar.Tab commentsToMeTab = getCommentsToMeTab();
        if (commentsToMeTab == null) {
            buildCommentsToMeTab(tabListener);
        }
        ActionBar.Tab commentsByMeTab = getCommentsByMeTab();
        if (commentsByMeTab == null) {
            buildCommentsByMeTab(tabListener);
        }

        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.COMMENTS_INDEX) {
            buildActionBarAndViewPagerTitles(
                    ((MainTimeLineActivity) getActivity()).getMenuFragment().commentsTabIndex);
        }
    }

    private ActionBar.Tab buildCommentsByMeTab(SimpleTwoTabsListener tabListener) {
        View customView = getActivity().getLayoutInflater()
                .inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.my_comment);
        ActionBar.Tab commentsByMeTab = getActivity().getActionBar().newTab()
                .setCustomView(customView)
                .setTag(CommentsByMeTimeLineFragment.class.getName()).setTabListener(tabListener);
        tabMap.append(COMMENTS_BY_ME_CHILD_POSITION, commentsByMeTab);
        return commentsByMeTab;
    }

    private ActionBar.Tab buildCommentsToMeTab(SimpleTwoTabsListener tabListener) {
        View customView = getActivity().getLayoutInflater()
                .inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.all_people_send_to_me);
        ActionBar.Tab commentsToMeTab = getActivity().getActionBar().newTab()
                .setCustomView(customView)
                .setTag(CommentsToMeTimeLineFragment.class.getName()).setTabListener(tabListener);
        tabMap.append(COMMENTS_TO_ME_CHILD_POSITION, commentsToMeTab);
        return commentsToMeTab;
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
        CommentsTimeLinePagerAdapter adapter = new CommentsTimeLinePagerAdapter(this, viewPager,
                getChildFragmentManager(), childrenFragments);
        viewPager.setAdapter(adapter);
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
            case COMMENT_TO_ME:
                ((MainTimeLineActivity) getActivity()).getMenuFragment()
                        .switchCategory(LeftMenuFragment.COMMENTS_INDEX);
                viewPager.setCurrentItem(0);
                intent.putExtra(BundleArgsConstants.OPEN_NAVIGATION_INDEX_EXTRA,
                        UnreadTabIndex.NONE);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            int commentsTabIndex = getArguments().getInt("commentsTabIndex");
            buildActionBarAndViewPagerTitles(commentsTabIndex);
        }
    }

    public void buildActionBarAndViewPagerTitles(int nav) {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(R.string.comments);
            getActivity().getActionBar().setIcon(R.drawable.comment_light);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle("");
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);

        ActionBar.Tab commentsToMeTab = getCommentsToMeTab();
        if (commentsToMeTab == null) {
            commentsToMeTab = buildCommentsToMeTab(tabListener);
        }
        ActionBar.Tab commentsByMeTab = getCommentsByMeTab();
        if (commentsByMeTab == null) {
            commentsByMeTab = buildCommentsByMeTab(tabListener);
        }
        actionBar.addTab(commentsToMeTab);
        actionBar.addTab(commentsByMeTab);

        if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS && nav > -1) {
            viewPager.setCurrentItem(nav, false);
        }
    }

    public ActionBar.Tab getCommentsToMeTab() {
        return tabMap.get(COMMENTS_TO_ME_CHILD_POSITION);
    }

    public ActionBar.Tab getCommentsByMeTab() {
        return tabMap.get(COMMENTS_BY_ME_CHILD_POSITION);
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
                    .getMenuFragment()).commentsTabIndex = position;
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

    public CommentsToMeTimeLineFragment getCommentsToMeTimeLineFragment() {
        CommentsToMeTimeLineFragment fragment
                = ((CommentsToMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsToMeTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = CommentsToMeTimeLineFragment.newInstance(
                    GlobalContext.getInstance().getAccountBean()
                    , GlobalContext.getInstance().getAccountBean().getInfo(),
                    GlobalContext.getInstance().getSpecialToken());
        }

        return fragment;
    }

    public CommentsByMeTimeLineFragment getCommentsByMeTimeLineFragment() {
        CommentsByMeTimeLineFragment fragment
                = ((CommentsByMeTimeLineFragment) getChildFragmentManager().findFragmentByTag(
                CommentsByMeTimeLineFragment.class.getName()));
        if (fragment == null) {
            fragment = CommentsByMeTimeLineFragment.newInstance(
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
        getCommentsByMeTimeLineFragment().clearActionMode();
        getCommentsToMeTimeLineFragment().clearActionMode();
    }
}
