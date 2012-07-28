package org.qii.weiciyuan.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 上午1:01
 * To change this template use File | Settings | File Templates.
 */
public class TimeLinePagerAdapter extends
        FragmentStatePagerAdapter {

    List<Fragment> list = new ArrayList<Fragment>();

    public TimeLinePagerAdapter(FragmentManager fm) {
        super(fm);
        list.add(new MentionsFragment());
        list.add(new MentionsFragment());
        list.add(new MentionsFragment());
    }

    @Override
    public Fragment getItem(int i) {

        return list.get(i);
    }

    @Override
    public int getCount() {
        return list.size();
    }

}