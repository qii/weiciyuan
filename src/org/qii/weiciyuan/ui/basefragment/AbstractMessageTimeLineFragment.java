package org.qii.weiciyuan.ui.basefragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.adapter.StatusesListAdapter;
import org.qii.weiciyuan.ui.send.CommentNewActivity;
import org.qii.weiciyuan.ui.send.RepostNewActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public abstract class AbstractMessageTimeLineFragment extends AbstractTimeLineFragment<MessageListBean> {

    protected void showNewMsgToastMessage(MessageListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getStatuses().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getStatuses().size() > 0) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void clearAndReplaceValue(MessageListBean value) {
        bean.getStatuses().clear();
        bean.getStatuses().addAll(value.getStatuses());
        bean.setTotal_number(value.getTotal_number());
    }

    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getStatuses().size() == 0) {
//                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getStatuses().size() > 0) {
//                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    //for speed, add old data after new data
                    newValue.getStatuses().addAll(getList().getStatuses());
                }
                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();

            }
        }
        afterGetNewMsg();
    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            int index = newValue.getStatuses().size() - 1;

            if (index > 1) {

                getList().getStatuses().addAll(newValue.getStatuses().subList(1, index));
            }

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new MessageListBean();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MyMultiChoiceModeListener());
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new StatusesListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList().getStatuses(), listView);
        listView.setAdapter(timeLineAdapter);
    }

    private class MyMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        String currentUserId = GlobalContext.getInstance().getCurrentAccountId();

        private boolean isAllMyMsg() {
            SparseBooleanArray size = listView.getCheckedItemPositions();
            for (int i = 0; i < size.size(); i++) {
                int position = size.keyAt(i);
                MessageBean msg = (MessageBean) timeLineAdapter.getItem(position - 1);
                if (!msg.getUser().getId().equals(currentUserId))
                    return false;
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int size = listView.getCheckedItemCount();
            if (size > 0) {
                SparseBooleanArray s = listView.getCheckedItemPositions();
                int newCheckedPosition = s.keyAt(size - 1);
                if (newCheckedPosition > timeLineAdapter.getCount() || newCheckedPosition == 0) {
                    mode.finish();
                } else {
                    mode.setTitle(String.valueOf(size) + getString(R.string.weibos));
                    mode.invalidate();
                }
            }


        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
                    MessageBean msg = (MessageBean) timeLineAdapter.getItem(listView.getCheckedItemPositions().keyAt(listView.getCheckedItemCount() - 1) - 1);
                    intent = new Intent(getActivity(), RepostNewActivity.class);
                    intent.putExtra("token", ((IToken) getActivity()).getToken());
                    intent.putExtra("id", ids[0]);
                    intent.putExtra("msg", msg);
                    startActivity(intent);
                    mode.finish();
                    break;
                case R.id.menu_comment:
                    intent = new Intent(getActivity(), CommentNewActivity.class);
                    intent.putExtra("token", ((IToken) getActivity()).getToken());
                    intent.putExtra("id", ids[0]);
                    startActivity(intent);
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


        }
    }
}
