package org.qii.weiciyuan.ui.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-5-11
 */
public class SearchTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public SearchTimeLinePagerAdapter(SearchMainParentFragment fragment, ViewPager viewPager, FragmentManager fm, MainTimeLineActivity activity, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.add(0, fragment.getSearchWeiboFragment());
        fragmentList.add(1, fragment.getSearchUserFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(0).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(0), SearchStatusFragment.class.getName());
        if (!fragmentList.get(1).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(1), SearchUserFragment.class.getName());
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
        List<String> tagList = new ArrayList<String>();
        tagList.add(MentionsWeiboTimeLineFragment.class.getName());
        tagList.add(MentionsCommentTimeLineFragment.class.getName());

        return tagList.get(position);
    }


    @Override
    public int getCount() {
        return 2;
    }


}
