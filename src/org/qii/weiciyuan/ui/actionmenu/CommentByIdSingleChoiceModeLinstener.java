package org.qii.weiciyuan.ui.actionmenu;

import org.qii.weiciyuan.bean.CommentBean;

import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * User: qii
 * Date: 12-9-11
 */
public class CommentByIdSingleChoiceModeLinstener extends CommentSingleChoiceModeListener {
    private LinearLayout quick_repost;
    private int initState;

    public CommentByIdSingleChoiceModeLinstener(ListView listView, BaseAdapter adapter,
            Fragment activity, LinearLayout quick_repost, CommentBean bean) {
        super(listView, adapter, activity, bean);
        this.quick_repost = quick_repost;
        initState = this.quick_repost.getVisibility();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (initState == View.VISIBLE) {
            quick_repost.setVisibility(View.GONE);
        }
        return super.onCreateActionMode(mode, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        buildMenu(mode, menu);
//        menu.findItem(R.id.menu_view).setVisible(true);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (initState == View.VISIBLE) {
            quick_repost.setVisibility(View.VISIBLE);
        }
        super.onDestroyActionMode(mode);
    }
}
