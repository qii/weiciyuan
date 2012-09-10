package org.qii.weiciyuan.ui.actionmenu;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.send.ReplyToCommentNewActivity;

/**
 * User: qii
 * Date: 12-9-10
 */
public class CommentChoiceModeListener implements ActionMode.Callback {

    ListView listView;
    BaseAdapter adapter;
    Fragment activity;
    ActionMode mode;
    CommentBean bean;

    public void finish() {
        if (mode != null)
            mode.finish();
    }

    public CommentChoiceModeListener(ListView listView, BaseAdapter adapter, Fragment activity, CommentBean bean) {
        this.listView = listView;
        this.activity = activity;
        this.adapter = adapter;
        this.bean = bean;
    }

    private Activity getActivity() {
        return activity.getActivity();
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mode == null)
            this.mode = mode;

        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        menu.clear();

        inflater.inflate(R.menu.fragment_comment_listview_item_contextual_menu, menu);

        mode.setTitle(bean.getUser().getScreen_name());


        return true;


    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_comment:
                Intent intent = new Intent(getActivity(), ReplyToCommentNewActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("msg", bean);
                getActivity().startActivity(intent);

                break;

        }

        listView.clearChoices();
        mode.finish();

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
        listView.clearChoices();
        adapter.notifyDataSetChanged();
        ((AbstractTimeLineFragment) activity).setmActionMode(null);

    }
}