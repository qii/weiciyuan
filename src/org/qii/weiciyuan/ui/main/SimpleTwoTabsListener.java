package org.qii.weiciyuan.ui.main;

import android.app.ActionBar;
import android.support.v4.view.ViewPager;

/**
 * User: qii
 * Date: 13-4-5
 */
public class SimpleTwoTabsListener implements ActionBar.TabListener {

    private ViewPager viewPager;

    public SimpleTwoTabsListener(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        if (viewPager != null && viewPager.getCurrentItem() != tab.getPosition())
            viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }
}
