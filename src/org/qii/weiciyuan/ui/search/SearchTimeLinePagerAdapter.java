package org.qii.weiciyuan.ui.search;

import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

/**
 * User: qii
 * Date: 13-5-11
 */
public class SearchTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private SparseArray<Fragment> fragmentList;

    public SearchTimeLinePagerAdapter(SearchMainParentFragment fragment, ViewPager viewPager,
            FragmentManager fm, MainTimeLineActivity activity, SparseArray<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.append(0, fragment.getSearchWeiboFragment());
        fragmentList.append(1, fragment.getSearchUserFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(0).isAdded()) {
            transaction.add(viewPager.getId(), fragmentList.get(0),
                    SearchStatusFragment.class.getName());
        }
        if (!fragmentList.get(1).isAdded()) {
            transaction.add(viewPager.getId(), fragmentList.get(1),
                    SearchUserFragment.class.getName());
        }
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
