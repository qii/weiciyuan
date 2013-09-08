package org.qii.weiciyuan.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.maintimeline.CommentsByMeTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.CommentsToMeTimeLineFragment;

/**
 * User: qii
 * Date: 13-3-8
 */
public class CommentsTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private SparseArray<Fragment> fragmentList;

    public CommentsTimeLinePagerAdapter(CommentsTimeLine fragment, ViewPager viewPager, FragmentManager fm, SparseArray<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.append(CommentsTimeLine.COMMENTS_TO_ME_CHILD_POSITION, fragment.getCommentsToMeTimeLineFragment());
        fragmentList.append(CommentsTimeLine.COMMENTS_BY_ME_CHILD_POSITION, fragment.getCommentsByMeTimeLineFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(CommentsTimeLine.COMMENTS_TO_ME_CHILD_POSITION).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(CommentsTimeLine.COMMENTS_TO_ME_CHILD_POSITION), CommentsToMeTimeLineFragment.class.getName());
        if (!fragmentList.get(CommentsTimeLine.COMMENTS_BY_ME_CHILD_POSITION).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(CommentsTimeLine.COMMENTS_BY_ME_CHILD_POSITION), CommentsByMeTimeLineFragment.class.getName());
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
        tagList.append(CommentsTimeLine.COMMENTS_TO_ME_CHILD_POSITION, CommentsToMeTimeLineFragment.class.getName());
        tagList.append(CommentsTimeLine.COMMENTS_BY_ME_CHILD_POSITION, CommentsByMeTimeLineFragment.class.getName());

        return tagList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

}
