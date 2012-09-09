package org.qii.weiciyuan.ui.actionmenu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.send.CommentNewActivity;
import org.qii.weiciyuan.ui.send.RepostNewActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-9
 */
public class StatusMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

    String currentUserId = GlobalContext.getInstance().getCurrentAccountId();
    ListView listView;
    BaseAdapter adapter;
    Activity activity;
    ActionMode mode;

    public void finish() {
        if (mode != null)
            mode.finish();
    }

    public StatusMultiChoiceModeListener(ListView listView, BaseAdapter adapter, Activity activity) {
        this.listView = listView;
        this.activity = activity;
        this.adapter = adapter;
    }

    private Activity getActivity() {
        return activity;
    }

    private boolean isAllMyMsg() {
        SparseBooleanArray size = listView.getCheckedItemPositions();
        for (int i = 0; i < size.size(); i++) {
            int position = size.keyAt(i);
            if (size.valueAt(i)) {
                MessageBean msg = (MessageBean) adapter.getItem(position - 1);
                if (!msg.getUser().getId().equals(currentUserId))
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        int size = listView.getCheckedItemCount();
        if (size > 0) {
            SparseBooleanArray s = listView.getCheckedItemPositions();
            int newCheckedPosition = s.keyAt(size - 1);
            if (newCheckedPosition > adapter.getCount() || newCheckedPosition == 0) {
                mode.finish();
            } else {
                mode.setTitle(String.valueOf(size) + getActivity().getString(R.string.weibos));
                mode.invalidate();
            }
        }

        adapter.notifyDataSetChanged();

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
        int size = listView.getCheckedItemCount();
        if (size == 0) {
            return false;
        }

        menu.clear();

        if (size == 1) {

            if (isAllMyMsg()) {
                inflater.inflate(R.menu.fragment_listview_item_contexual_menu_myself, menu);
            } else {
                inflater.inflate(R.menu.fragment_listview_item_contexual_menu, menu);
            }

            MenuItem item = menu.findItem(R.id.menu_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            MessageBean shareMsg = (MessageBean) adapter.getItem(listView.getCheckedItemPositions().keyAt(listView.getCheckedItemCount() - 1) - 1);

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareMsg.getText());
            PackageManager packageManager = getActivity().getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(sharingIntent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe && mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(sharingIntent);
            }

            return true;
        } else {
            if (isAllMyMsg()) {
                inflater.inflate(R.menu.fragment_listview_item_contexual_menu_only_delete_and_fav, menu);
            } else {
                inflater.inflate(R.menu.fragment_listview_item_contexual_menu_only_fav, menu);
            }

            return true;
        }

    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Intent intent;
        long[] ids = listView.getCheckedItemIds();
        switch (item.getItemId()) {
            case R.id.menu_repost:
                MessageBean msg = (MessageBean) adapter.getItem(listView.getCheckedItemPositions().keyAt(listView.getCheckedItemCount() - 1) - 1);
                intent = new Intent(getActivity(), RepostNewActivity.class);
                intent.putExtra("token", ((IToken) activity).getToken());
                intent.putExtra("id", String.valueOf(ids[0]));
                intent.putExtra("msg", msg);
                getActivity().startActivity(intent);
                mode.finish();
                break;
            case R.id.menu_comment:
                intent = new Intent(getActivity(), CommentNewActivity.class);
                intent.putExtra("token", ((IToken) activity).getToken());
                intent.putExtra("id", String.valueOf(ids[0]));
                getActivity().startActivity(intent);
                mode.finish();
                break;
            case R.id.menu_fav:
                Toast.makeText(getActivity(), "fav", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_remove:
                Toast.makeText(getActivity(), "remove", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;

    }
}