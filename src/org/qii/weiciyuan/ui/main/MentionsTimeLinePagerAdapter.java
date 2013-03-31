package org.qii.weiciyuan.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.ui.maintimeline.MentionsCommentTimeLineFragment;
import org.qii.weiciyuan.ui.maintimeline.MentionsWeiboTimeLineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-3-8
 */
public class MentionsTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private List<Fragment> fragmentList;

    public MentionsTimeLinePagerAdapter(FragmentManager fm, MainTimeLineActivity activity, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.add(0, activity.getMentionsTimeLineFragment());
        fragmentList.add(1, activity.getMentionsCommentTimeLineFragment());
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