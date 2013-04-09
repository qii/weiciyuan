package org.qii.weiciyuan.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.maintimeline.CommentsByMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsToMeTimeLineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-3-8
 */
public class CommentsTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public CommentsTimeLinePagerAdapter(CommentsTimeLine fragment, ViewPager viewPager, FragmentManager fm, MainTimeLineActivity activity, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.add(0, fragment.getCommentsToMeTimeLineFragment());
        fragmentList.add(1, fragment.getCommentsByMeTimeLineFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(0).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(0), CommentsToMeTimeLineFragment.class.getName());
        if (!fragmentList.get(1).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(1), CommentsByMeTimeLineFragment.class.getName());
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
        tagList.add(CommentsToMeTimeLineFragment.class.getName());
        tagList.add(CommentsByMeTimeLineFragment.class.getName());

        return tagList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

}
