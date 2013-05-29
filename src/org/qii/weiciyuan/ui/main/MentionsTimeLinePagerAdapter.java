package org.qii.weiciyuan.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

/**
 * User: qii
 * Date: 13-3-8
 */
public class MentionsTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private SparseArray<Fragment> fragmentList;
    private MentionsTimeLine fragment;

    public MentionsTimeLinePagerAdapter(MentionsTimeLine fragment, ViewPager viewPager, FragmentManager fm, SparseArray<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.append(0, fragment.getMentionsWeiboTimeLineFragment());
        fragmentList.append(1, fragment.getMentionsCommentTimeLineFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(0).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(0), MentionsWeiboTimeLineFragment.class.getName());
        if (!fragmentList.get(1).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(1), MentionsCommentTimeLineFragment.class.getName());
        if (!transaction.isEmpty()) {
            transaction.commit();
            fragment.getChildFragmentManager().executePendingTransactions();
        }
    }


    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    protected String getTag(int position) {
        SparseArray<String> tagList = new SparseArray<String>();
        tagList.append(0, MentionsWeiboTimeLineFragment.class.getName());
        tagList.append(0, MentionsCommentTimeLineFragment.class.getName());

        return tagList.get(position);
    }


    @Override
    public int getCount() {
        return 2;
    }


}