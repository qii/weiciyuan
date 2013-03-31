package org.qii.weiciyuan.ui.search;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-8-29
 */
public class SearchMainActivity extends AbstractAppActivity {

    private ViewPager mViewPager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager_layout);
        buildViewPager();
        buildActionBarAndViewPagerTitles();

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            search(query);
        }
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        SearchTabPagerAdapter adapter = new SearchTabPagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private void buildActionBarAndViewPagerTitles() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

        actionBar.setTitle(getString(R.string.search));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.status))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.user))
                .setTabListener(tabListener));

    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {

            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());


        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {

        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_searchmainactivity, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.requestFocus();
        return super.onCreateOptionsMenu(menu);

    }

    public String getSearchWord() {
        return this.q;
    }

    private String q;

    private void search(final String q) {
        if (!TextUtils.isEmpty(q)) {
            this.q = q;
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    ((SearchStatusFragment) getSearchStatusFragment()).search();
                    break;
                case 1:
                    ((SearchUserFragment) getSearchUserFragment()).search();
                    break;
            }
        }
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return true;
    }

    private Fragment getSearchUserFragment() {
        return getSupportFragmentManager().findFragmentByTag(
                SearchUserFragment.class.getName());
    }

    private AbstractMessageTimeLineFragment getSearchStatusFragment() {
        return (AbstractMessageTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                SearchStatusFragment.class.getName());
    }

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };


    private class SearchTabPagerAdapter extends AppFragmentPagerAdapter {


        List<Fragment> list = new ArrayList<Fragment>();


        public SearchTabPagerAdapter(FragmentManager fm) {
            super(fm);
            if (getSearchStatusFragment() == null) {
                list.add(new SearchStatusFragment());
            } else {
                list.add(getSearchStatusFragment());
            }

            if (getSearchUserFragment() == null) {
                list.add(new SearchUserFragment());
            } else {
                list.add(getSearchUserFragment());
            }


        }


        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(SearchStatusFragment.class.getName());
            tagList.add(SearchUserFragment.class.getName());
            return tagList.get(position);
        }


        @Override
        public int getCount() {
            return list.size();
        }


    }
}
