package org.qii.weiciyuan.ui.preference;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.preference.filter.FilterKeywordFragment;
import org.qii.weiciyuan.ui.preference.filter.FilterSourceFragment;
import org.qii.weiciyuan.ui.preference.filter.FilterTopicFragment;
import org.qii.weiciyuan.ui.preference.filter.FilterUserFragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-9-21
 */
public class FilterActivity extends AbstractAppActivity {

    private ViewPager viewPager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
    }

    private void initLayout() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setTitle(getString(R.string.filter));
        setContentView(R.layout.viewpager_layout);

        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(getString(R.string.filter));
        actionBar.setIcon(R.drawable.ic_filter);

        View title = getLayoutInflater().inflate(R.layout.filteractivity_title_layout, null);
        Switch switchBtn = (Switch) title.findViewById(R.id.switchBtn);
        actionBar.setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        actionBar.setDisplayShowCustomEnabled(true);

        switchBtn.setChecked(SettingUtility.isEnableFilter());
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingUtility.setEnableFilter(isChecked);
            }
        });

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.filter_keyword))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.filter_user))
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.filter_topic))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.filter_source))
                .setTabListener(tabListener));

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(onPageChangeListener);
    }

    private ActionBar.TabListener tabListener = new ActionBar.TabListener() {

        public void onTabSelected(ActionBar.Tab tab,
                FragmentTransaction ft) {
            if (viewPager != null && viewPager.getCurrentItem() != tab.getPosition()) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        }

        public void onTabUnselected(ActionBar.Tab tab,
                FragmentTransaction ft) {

        }

        public void onTabReselected(ActionBar.Tab tab,
                FragmentTransaction ft) {

        }
    };

    private ViewPager.SimpleOnPageChangeListener onPageChangeListener
            = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };

    private class TimeLinePagerAdapter extends AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();

        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            if (getFilterFragment() == null) {
                list.add(new FilterKeywordFragment());
            } else {
                list.add(getFilterFragment());
            }
            if (getFilterUserFragment() == null) {
                list.add(new FilterUserFragment());
            } else {
                list.add(getFilterUserFragment());
            }

            if (getFilterTopicFragment() == null) {
                list.add(new FilterTopicFragment());
            } else {
                list.add(getFilterTopicFragment());
            }

            if (getFilterSourceFragment() == null) {
                list.add(new FilterSourceFragment());
            } else {
                list.add(getFilterSourceFragment());
            }
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(FilterKeywordFragment.class.getName());
            tagList.add(FilterUserFragment.class.getName());
            tagList.add(FilterTopicFragment.class.getName());
            tagList.add(FilterSourceFragment.class.getName());
            return tagList.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }

    private FilterKeywordFragment getFilterFragment() {
        return ((FilterKeywordFragment) getSupportFragmentManager().findFragmentByTag(
                FilterKeywordFragment.class.getName()));
    }

    private FilterUserFragment getFilterUserFragment() {
        return ((FilterUserFragment) getSupportFragmentManager().findFragmentByTag(
                FilterUserFragment.class.getName()));
    }

    private FilterTopicFragment getFilterTopicFragment() {
        return ((FilterTopicFragment) getSupportFragmentManager().findFragmentByTag(
                FilterTopicFragment.class.getName()));
    }

    private FilterSourceFragment getFilterSourceFragment() {
        return ((FilterSourceFragment) getSupportFragmentManager().findFragmentByTag(
                FilterSourceFragment.class.getName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_filteractivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.filter_rule:
                new FilterRuleDialog().show(getSupportFragmentManager(), "");
                break;
        }
        return false;
    }

    public static class FilterRuleDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.filter_rule_content))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
        }
    }
}
