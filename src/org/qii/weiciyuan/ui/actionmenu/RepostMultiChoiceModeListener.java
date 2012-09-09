package org.qii.weiciyuan.ui.actionmenu;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * User: qii
 * Date: 12-9-9
 */
public class RepostMultiChoiceModeListener extends StatusMultiChoiceModeListener {
    LinearLayout quick_repost;

    public RepostMultiChoiceModeListener(ListView listView, BaseAdapter adapter, Activity activity, LinearLayout quick_repost) {
        super(listView, adapter, activity);
        this.quick_repost = quick_repost;

    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        quick_repost.setVisibility(View.GONE);
        return super.onCreateActionMode(mode, menu);

    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        quick_repost.setVisibility(View.VISIBLE);
        super.onDestroyActionMode(mode);
    }
}