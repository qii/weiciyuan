package org.qii.weiciyuan.ui.search;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.LongClickableLinkMovementMethod;
import org.qii.weiciyuan.support.utils.SmileyPickerUtility;
import org.qii.weiciyuan.support.utils.ThemeUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.basefragment.AbstractUserListFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.main.LeftMenuFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.SimpleTwoTabsListener;

/**
 * User: qii
 * Date: 13-5-11
 */
public class SearchMainParentFragment extends AbstractAppFragment implements MainTimeLineActivity.ScrollableListFragment {

    private ViewPager viewPager;
    private SparseArray<Fragment> searchFragments = new SparseArray<Fragment>();
    private SparseArray<ActionBar.Tab> tabMap = new SparseArray<ActionBar.Tab>();

    private static final int SEARCH_WEIBO_CHILD_POSITION = 0;
    private static final int SEARCH_USER_CHILD_POSITION = 1;

    private SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("q", q);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.q = savedInstanceState.getString("q");
        }
        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.SEARCH_INDEX) {
            buildActionBarAndViewPagerTitles(((MainTimeLineActivity) getActivity()).getMenuFragment().searchTabIndex);
        }
    }

    private ActionBar.Tab buildSearchWeiboTab(SimpleTwoTabsListener tabListener) {
        ActionBar.Tab tab;
        View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.weibo);
        tab = getActivity().getActionBar().newTab().setCustomView(customView)
                .setTag(SearchStatusFragment.class.getName()).setTabListener(tabListener);
        tabMap.append(SEARCH_WEIBO_CHILD_POSITION, tab);
        return tab;
    }

    private ActionBar.Tab buildSearchUserTab(SimpleTwoTabsListener tabListener) {
        ActionBar.Tab tab;
        View customView = getActivity().getLayoutInflater().inflate(R.layout.ab_tab_custom_view_layout, null);
        ((TextView) customView.findViewById(R.id.title)).setText(R.string.user);
        tab = getActivity().getActionBar().newTab().setCustomView(customView)
                .setTag(SearchUserFragment.class.getName()).setTabListener(tabListener);
        tabMap.append(SEARCH_USER_CHILD_POSITION, tab);
        return tab;
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
        viewPager.setOffscreenPageLimit(2);
        viewPager.setOnPageChangeListener(onPageChangeListener);
        SearchTimeLinePagerAdapter adapter = new SearchTimeLinePagerAdapter(this, viewPager, getChildFragmentManager(), (MainTimeLineActivity) getActivity(), searchFragments);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            int searchTabIndex = getArguments().getInt("searchTabIndex");
            buildActionBarAndViewPagerTitles(searchTabIndex);

            if (searchView != null)
                SmileyPickerUtility.showKeyBoard(searchView);
        } else {
            if (searchView != null)
                SmileyPickerUtility.hideSoftInput(searchView);
        }
    }


    public void buildActionBarAndViewPagerTitles(int nav) {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(R.string.search);
            getActivity().getActionBar().setIcon(R.drawable.search_light);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle("");
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }


        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(Utility.isDevicePort());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();
        SimpleTwoTabsListener tabListener = new SimpleTwoTabsListener(viewPager);

        ActionBar.Tab weiboTab = getWeiboTab();
        if (weiboTab == null) {
            weiboTab = buildSearchWeiboTab(tabListener);
        }
        actionBar.addTab(weiboTab);

        ActionBar.Tab userTab = getUserTab();
        if (userTab == null) {
            userTab = buildSearchUserTab(tabListener);
        }

        actionBar.addTab(userTab);

        if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS && nav > -1) {
            viewPager.setCurrentItem(nav, false);
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_searchmainactivity, menu);
        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setMaxWidth(Utility.dip2px(250));
        ThemeUtility.customActionBarSearchViewTextColor(searchView);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(q);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchMainParentFragment.this.q = newText;
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(q);
            }
        });
        if (!TextUtils.isEmpty(this.q)) {
            searchView.setQuery(this.q, false);
        }
    }

    public String getSearchWord() {
        return this.q;
    }

    private String q;

    private void search(final String q) {
        if (!TextUtils.isEmpty(q)) {
            this.q = q;
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(this.q, null);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    ((SearchStatusFragment) getSearchWeiboFragment()).search();
                    break;
                case 1:
                    ((SearchUserFragment) getSearchUserFragment()).search();
                    break;
            }
        }
    }

    public ActionBar.Tab getWeiboTab() {
        return tabMap.get(SEARCH_WEIBO_CHILD_POSITION);
    }

    public ActionBar.Tab getUserTab() {
        return tabMap.get(SEARCH_USER_CHILD_POSITION);
    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            ActionBar ab = getActivity().getActionBar();
            if (getActivity().getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS
                    && ab.getTabAt(position) == tabMap.get(position)) {
                ab.setSelectedNavigationItem(position);
            }

            ((LeftMenuFragment) ((MainTimeLineActivity) getActivity()).getMenuFragment()).searchTabIndex = position;

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


    public SearchUserFragment getSearchUserFragment() {
        SearchUserFragment fragment = ((SearchUserFragment) getChildFragmentManager().findFragmentByTag(
                SearchUserFragment.class.getName()));
        if (fragment == null) {
            fragment = new SearchUserFragment();
        }

        return fragment;
    }

    public SearchStatusFragment getSearchWeiboFragment() {
        SearchStatusFragment fragment = ((SearchStatusFragment) getChildFragmentManager().findFragmentByTag(
                SearchStatusFragment.class.getName()));
        if (fragment == null)
            fragment = new SearchStatusFragment();

        return fragment;
    }

    @Override
    public void scrollToTop() {
        Fragment fragment = searchFragments.get(viewPager.getCurrentItem());
        if (fragment instanceof AbstractTimeLineFragment) {
            Utility.stopListViewScrollingAndScrollToTop(((AbstractTimeLineFragment) fragment).getListView());
        } else if (fragment instanceof AbstractUserListFragment) {
            Utility.stopListViewScrollingAndScrollToTop(((AbstractUserListFragment) fragment).getListView());

        }
    }
}

